package org.eclipse.jetty.http;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public interface HttpContent {
    long getContentLength();

    Buffer getContentType();

    Buffer getDirectBuffer();

    Buffer getETag();

    Buffer getIndirectBuffer();

    InputStream getInputStream() throws IOException;

    Buffer getLastModified();

    Resource getResource();

    void release();

    /* loaded from: classes.dex */
    public static class ResourceAsHttpContent implements HttpContent {
        private static final Logger LOG = Log.getLogger(ResourceAsHttpContent.class);
        final Buffer _etag;
        final int _maxBuffer;
        final Buffer _mimeType;
        final Resource _resource;

        public ResourceAsHttpContent(Resource resource, Buffer mimeType) {
            this(resource, mimeType, -1, false);
        }

        public ResourceAsHttpContent(Resource resource, Buffer mimeType, int maxBuffer) {
            this(resource, mimeType, maxBuffer, false);
        }

        public ResourceAsHttpContent(Resource resource, Buffer mimeType, boolean etag) {
            this(resource, mimeType, -1, etag);
        }

        public ResourceAsHttpContent(Resource resource, Buffer mimeType, int maxBuffer, boolean etag) {
            this._resource = resource;
            this._mimeType = mimeType;
            this._maxBuffer = maxBuffer;
            this._etag = etag ? new ByteArrayBuffer(resource.getWeakETag()) : null;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getContentType() {
            return this._mimeType;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getLastModified() {
            return null;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getDirectBuffer() {
            return null;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Buffer getETag() {
            return this._etag;
        }

        /* JADX WARN: Code restructure failed: missing block: B:17:0x004c, code lost:
            r1.close();
         */
        /* JADX WARN: Code restructure failed: missing block: B:19:0x0050, code lost:
            r2 = move-exception;
         */
        /* JADX WARN: Code restructure failed: missing block: B:20:0x0051, code lost:
            org.eclipse.jetty.http.HttpContent.ResourceAsHttpContent.LOG.warn("Couldn't close inputStream. Possible file handle leak", r2);
         */
        @Override // org.eclipse.jetty.http.HttpContent
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public org.eclipse.jetty.io.Buffer getIndirectBuffer() {
            /*
                r6 = this;
                r0 = 0
                r1 = r0
                org.eclipse.jetty.util.resource.Resource r2 = r6._resource     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                long r2 = r2.length()     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                r4 = 0
                int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                if (r2 <= 0) goto L49
                int r2 = r6._maxBuffer     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                long r2 = (long) r2     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                org.eclipse.jetty.util.resource.Resource r4 = r6._resource     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                long r4 = r4.length()     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
                if (r2 >= 0) goto L1c
                goto L49
            L1c:
                org.eclipse.jetty.io.ByteArrayBuffer r0 = new org.eclipse.jetty.io.ByteArrayBuffer     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                org.eclipse.jetty.util.resource.Resource r2 = r6._resource     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                long r2 = r2.length()     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                int r2 = (int) r2     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                r0.<init>(r2)     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                org.eclipse.jetty.util.resource.Resource r2 = r6._resource     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                java.io.InputStream r2 = r2.getInputStream()     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                r1 = r2
                org.eclipse.jetty.util.resource.Resource r2 = r6._resource     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                long r2 = r2.length()     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                int r2 = (int) r2     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                r0.readFrom(r1, r2)     // Catch: java.lang.Throwable -> L59 java.io.IOException -> L5b
                if (r1 == 0) goto L48
                r1.close()     // Catch: java.io.IOException -> L40
                goto L48
            L40:
                r2 = move-exception
                org.eclipse.jetty.util.log.Logger r3 = org.eclipse.jetty.http.HttpContent.ResourceAsHttpContent.LOG
                java.lang.String r4 = "Couldn't close inputStream. Possible file handle leak"
                r3.warn(r4, r2)
            L48:
                return r0
            L49:
                if (r1 == 0) goto L58
                r1.close()     // Catch: java.io.IOException -> L50
                goto L58
            L50:
                r2 = move-exception
                org.eclipse.jetty.util.log.Logger r3 = org.eclipse.jetty.http.HttpContent.ResourceAsHttpContent.LOG
                java.lang.String r4 = "Couldn't close inputStream. Possible file handle leak"
                r3.warn(r4, r2)
            L58:
                return r0
            L59:
                r0 = move-exception
                goto L62
            L5b:
                r0 = move-exception
                java.lang.RuntimeException r2 = new java.lang.RuntimeException     // Catch: java.lang.Throwable -> L59
                r2.<init>(r0)     // Catch: java.lang.Throwable -> L59
                throw r2     // Catch: java.lang.Throwable -> L59
            L62:
                if (r1 == 0) goto L70
                r1.close()     // Catch: java.io.IOException -> L68
                goto L70
            L68:
                r2 = move-exception
                org.eclipse.jetty.util.log.Logger r3 = org.eclipse.jetty.http.HttpContent.ResourceAsHttpContent.LOG
                java.lang.String r4 = "Couldn't close inputStream. Possible file handle leak"
                r3.warn(r4, r2)
            L70:
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.http.HttpContent.ResourceAsHttpContent.getIndirectBuffer():org.eclipse.jetty.io.Buffer");
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public long getContentLength() {
            return this._resource.length();
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public InputStream getInputStream() throws IOException {
            return this._resource.getInputStream();
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public Resource getResource() {
            return this._resource;
        }

        @Override // org.eclipse.jetty.http.HttpContent
        public void release() {
            this._resource.release();
        }
    }
}
