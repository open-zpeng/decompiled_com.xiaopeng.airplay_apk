package org.seamless.http;

import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import org.eclipse.jetty.http.HttpHeaders;
/* loaded from: classes.dex */
public class Representation<E> implements Serializable {
    private CacheControl cacheControl;
    private Integer contentLength;
    private String contentType;
    private E entity;
    private String entityTag;
    private Long lastModified;
    private URL url;

    public Representation(CacheControl cacheControl, Integer contentLength, String contentType, Long lastModified, String entityTag, E entity) {
        this(null, cacheControl, contentLength, contentType, lastModified, entityTag, entity);
    }

    public Representation(URL url, CacheControl cacheControl, Integer contentLength, String contentType, Long lastModified, String entityTag, E entity) {
        this.url = url;
        this.cacheControl = cacheControl;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.entityTag = entityTag;
        this.entity = entity;
    }

    public Representation(URLConnection urlConnection, E entity) {
        this(urlConnection.getURL(), CacheControl.valueOf(urlConnection.getHeaderField(HttpHeaders.CACHE_CONTROL)), Integer.valueOf(urlConnection.getContentLength()), urlConnection.getContentType(), Long.valueOf(urlConnection.getLastModified()), urlConnection.getHeaderField("Etag"), entity);
    }

    public URL getUrl() {
        return this.url;
    }

    public CacheControl getCacheControl() {
        return this.cacheControl;
    }

    public Integer getContentLength() {
        if (this.contentLength == null || this.contentLength.intValue() == -1) {
            return null;
        }
        return this.contentLength;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Long getLastModified() {
        if (this.lastModified.longValue() == 0) {
            return null;
        }
        return this.lastModified;
    }

    public String getEntityTag() {
        return this.entityTag;
    }

    public E getEntity() {
        return this.entity;
    }

    public Long getMaxAgeOrNull() {
        if (getCacheControl() == null || getCacheControl().getMaxAge() == -1 || getCacheControl().getMaxAge() == 0) {
            return null;
        }
        return Long.valueOf(getCacheControl().getMaxAge());
    }

    public boolean isExpired(long storedOn, long maxAge) {
        return (1000 * maxAge) + storedOn < new Date().getTime();
    }

    public boolean isExpired(long storedOn) {
        return getMaxAgeOrNull() == null || isExpired(storedOn, getMaxAgeOrNull().longValue());
    }

    public boolean isNoStore() {
        return getCacheControl() != null && getCacheControl().isNoStore();
    }

    public boolean isNoCache() {
        return getCacheControl() != null && getCacheControl().isNoCache();
    }

    public boolean mustRevalidate() {
        return getCacheControl() != null && getCacheControl().isProxyRevalidate();
    }

    public boolean hasEntityTagChanged(String currentEtag) {
        return (getEntityTag() == null || getEntityTag().equals(currentEtag)) ? false : true;
    }

    public boolean hasBeenModified(long currentModificationTime) {
        return getLastModified() == null || getLastModified().longValue() < currentModificationTime;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") CT: " + getContentType();
    }
}
