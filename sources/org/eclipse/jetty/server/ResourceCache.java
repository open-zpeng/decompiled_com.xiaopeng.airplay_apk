package org.eclipse.jetty.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.View;
import org.eclipse.jetty.io.nio.DirectNIOBuffer;
import org.eclipse.jetty.io.nio.IndirectNIOBuffer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
/* loaded from: classes.dex */
public class ResourceCache {
    private static final Logger LOG = Log.getLogger(ResourceCache.class);
    private final boolean _etags;
    private final ResourceFactory _factory;
    private final MimeTypes _mimeTypes;
    private final ResourceCache _parent;
    private boolean _useFileMappedBuffer;
    private int _maxCachedFileSize = 4194304;
    private int _maxCachedFiles = 2048;
    private int _maxCacheSize = 33554432;
    private final ConcurrentMap<String, Content> _cache = new ConcurrentHashMap();
    private final AtomicInteger _cachedSize = new AtomicInteger();
    private final AtomicInteger _cachedFiles = new AtomicInteger();

    public ResourceCache(ResourceCache parent, ResourceFactory factory, MimeTypes mimeTypes, boolean useFileMappedBuffer, boolean etags) {
        this._useFileMappedBuffer = true;
        this._factory = factory;
        this._mimeTypes = mimeTypes;
        this._parent = parent;
        this._etags = etags;
        this._useFileMappedBuffer = useFileMappedBuffer;
    }

    public int getCachedSize() {
        return this._cachedSize.get();
    }

    public int getCachedFiles() {
        return this._cachedFiles.get();
    }

    public int getMaxCachedFileSize() {
        return this._maxCachedFileSize;
    }

    public void setMaxCachedFileSize(int maxCachedFileSize) {
        this._maxCachedFileSize = maxCachedFileSize;
        shrinkCache();
    }

    public int getMaxCacheSize() {
        return this._maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this._maxCacheSize = maxCacheSize;
        shrinkCache();
    }

    public int getMaxCachedFiles() {
        return this._maxCachedFiles;
    }

    public void setMaxCachedFiles(int maxCachedFiles) {
        this._maxCachedFiles = maxCachedFiles;
        shrinkCache();
    }

    public boolean isUseFileMappedBuffer() {
        return this._useFileMappedBuffer;
    }

    public void setUseFileMappedBuffer(boolean useFileMappedBuffer) {
        this._useFileMappedBuffer = useFileMappedBuffer;
    }

    public void flushCache() {
        if (this._cache == null) {
            return;
        }
        while (this._cache.size() > 0) {
            for (String path : this._cache.keySet()) {
                Content content = this._cache.remove(path);
                if (content != null) {
                    content.invalidate();
                }
            }
        }
    }

    public HttpContent lookup(String pathInContext) throws IOException {
        HttpContent httpContent;
        Content content = this._cache.get(pathInContext);
        if (content != null && content.isValid()) {
            return content;
        }
        Resource resource = this._factory.getResource(pathInContext);
        HttpContent loaded = load(pathInContext, resource);
        if (loaded != null) {
            return loaded;
        }
        if (this._parent != null && (httpContent = this._parent.lookup(pathInContext)) != null) {
            return httpContent;
        }
        return null;
    }

    protected boolean isCacheable(Resource resource) {
        long len = resource.length();
        return len > 0 && len < ((long) this._maxCachedFileSize) && len < ((long) this._maxCacheSize);
    }

    private HttpContent load(String pathInContext, Resource resource) throws IOException {
        if (resource == null || !resource.exists()) {
            return null;
        }
        if (!resource.isDirectory() && isCacheable(resource)) {
            Content content = new Content(pathInContext, resource);
            shrinkCache();
            Content added = this._cache.putIfAbsent(pathInContext, content);
            if (added != null) {
                content.invalidate();
                return added;
            }
            return content;
        }
        return new HttpContent.ResourceAsHttpContent(resource, this._mimeTypes.getMimeByExtension(resource.toString()), getMaxCachedFileSize(), this._etags);
    }

    private void shrinkCache() {
        while (this._cache.size() > 0) {
            if (this._cachedFiles.get() > this._maxCachedFiles || this._cachedSize.get() > this._maxCacheSize) {
                SortedSet<Content> sorted = new TreeSet<>(new Comparator<Content>() { // from class: org.eclipse.jetty.server.ResourceCache.1
                    @Override // java.util.Comparator
                    public int compare(Content c1, Content c2) {
                        if (c1._lastAccessed < c2._lastAccessed) {
                            return -1;
                        }
                        if (c1._lastAccessed > c2._lastAccessed) {
                            return 1;
                        }
                        if (c1._length < c2._length) {
                            return -1;
                        }
                        return c1._key.compareTo(c2._key);
                    }
                });
                for (Content content : this._cache.values()) {
                    sorted.add(content);
                }
                for (Content content2 : sorted) {
                    if (this._cachedFiles.get() > this._maxCachedFiles || this._cachedSize.get() > this._maxCacheSize) {
                        if (content2 == this._cache.remove(content2.getKey())) {
                            content2.invalidate();
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    protected Buffer getIndirectBuffer(Resource resource) {
        try {
            int len = (int) resource.length();
            if (len < 0) {
                Logger logger = LOG;
                logger.warn("invalid resource: " + String.valueOf(resource) + " " + len, new Object[0]);
                return null;
            }
            Buffer buffer = new IndirectNIOBuffer(len);
            InputStream is = resource.getInputStream();
            buffer.readFrom(is, len);
            is.close();
            return buffer;
        } catch (IOException e) {
            LOG.warn(e);
            return null;
        }
    }

    protected Buffer getDirectBuffer(Resource resource) {
        try {
            if (this._useFileMappedBuffer && resource.getFile() != null) {
                return new DirectNIOBuffer(resource.getFile());
            }
            int len = (int) resource.length();
            if (len < 0) {
                Logger logger = LOG;
                logger.warn("invalid resource: " + String.valueOf(resource) + " " + len, new Object[0]);
                return null;
            }
            Buffer buffer = new DirectNIOBuffer(len);
            InputStream is = resource.getInputStream();
            buffer.readFrom(is, len);
            is.close();
            return buffer;
        } catch (IOException e) {
            LOG.warn(e);
            return null;
        }
    }

    public String toString() {
        return "ResourceCache[" + this._parent + "," + this._factory + "]@" + hashCode();
    }

    /* loaded from: classes.dex */
    public class Content implements HttpContent {
        final Buffer _contentType;
        final Buffer _etagBuffer;
        final String _key;
        volatile long _lastAccessed;
        final long _lastModified;
        final Buffer _lastModifiedBytes;
        final int _length;
        final Resource _resource;
        AtomicReference<Buffer> _indirectBuffer = new AtomicReference<>();
        AtomicReference<Buffer> _directBuffer = new AtomicReference<>();

        Content(String pathInContext, Resource resource) {
            this._key = pathInContext;
            this._resource = resource;
            this._contentType = ResourceCache.this._mimeTypes.getMimeByExtension(this._resource.toString());
            boolean exists = resource.exists();
            this._lastModified = exists ? resource.lastModified() : -1L;
            this._lastModifiedBytes = this._lastModified < 0 ? null : new ByteArrayBuffer(HttpFields.formatDate(this._lastModified));
            this._length = exists ? (int) resource.length() : 0;
            ResourceCache.this._cachedSize.addAndGet(this._length);
            ResourceCache.this._cachedFiles.incrementAndGet();
            this._lastAccessed = System.currentTimeMillis();
            this._etagBuffer = ResourceCache.this._etags ? new ByteArrayBuffer(resource.getWeakETag()) : null;
        }

        public String getKey() {
            return this._key;
        }

        public boolean isCached() {
            return this._key != null;
        }

        public boolean isMiss() {
            return false;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Resource getResource() {
            return this._resource;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getETag() {
            return this._etagBuffer;
        }

        boolean isValid() {
            if (this._lastModified != this._resource.lastModified() || this._length != this._resource.length()) {
                if (this == ResourceCache.this._cache.remove(this._key)) {
                    invalidate();
                    return false;
                }
                return false;
            }
            this._lastAccessed = System.currentTimeMillis();
            return true;
        }

        protected void invalidate() {
            ResourceCache.this._cachedSize.addAndGet(-this._length);
            ResourceCache.this._cachedFiles.decrementAndGet();
            this._resource.release();
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getLastModified() {
            return this._lastModifiedBytes;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getContentType() {
            return this._contentType;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public void release() {
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getIndirectBuffer() {
            Buffer buffer = this._indirectBuffer.get();
            if (buffer == null) {
                Buffer buffer2 = ResourceCache.this.getIndirectBuffer(this._resource);
                if (buffer2 == null) {
                    Logger logger = ResourceCache.LOG;
                    logger.warn("Could not load " + this, new Object[0]);
                } else {
                    buffer = this._indirectBuffer.compareAndSet(null, buffer2) ? buffer2 : this._indirectBuffer.get();
                }
            }
            if (buffer == null) {
                return null;
            }
            return new View(buffer);
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getDirectBuffer() {
            Buffer buffer = this._directBuffer.get();
            if (buffer == null) {
                Buffer buffer2 = ResourceCache.this.getDirectBuffer(this._resource);
                if (buffer2 == null) {
                    Logger logger = ResourceCache.LOG;
                    logger.warn("Could not load " + this, new Object[0]);
                } else {
                    buffer = this._directBuffer.compareAndSet(null, buffer2) ? buffer2 : this._directBuffer.get();
                }
            }
            if (buffer == null) {
                return null;
            }
            return new View(buffer);
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public long getContentLength() {
            return this._length;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public InputStream getInputStream() throws IOException {
            Buffer indirect = getIndirectBuffer();
            if (indirect != null && indirect.array() != null) {
                return new ByteArrayInputStream(indirect.array(), indirect.getIndex(), indirect.length());
            }
            return this._resource.getInputStream();
        }

        public String toString() {
            return String.format("%s %s %d %s %s", this._resource, Boolean.valueOf(this._resource.exists()), Long.valueOf(this._resource.lastModified()), this._contentType, this._lastModifiedBytes);
        }
    }
}
