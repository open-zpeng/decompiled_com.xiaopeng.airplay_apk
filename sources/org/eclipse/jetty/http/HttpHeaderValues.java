package org.eclipse.jetty.http;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
/* loaded from: classes.dex */
public class HttpHeaderValues extends BufferCache {
    public static final int BYTES_ORDINAL = 9;
    public static final int CHUNKED_ORDINAL = 2;
    public static final int CLOSE_ORDINAL = 1;
    public static final int CONTINUE_ORDINAL = 6;
    public static final int GZIP_ORDINAL = 3;
    public static final String IDENTITY = "identity";
    public static final int IDENTITY_ORDINAL = 4;
    public static final int KEEP_ALIVE_ORDINAL = 5;
    public static final int NO_CACHE_ORDINAL = 10;
    public static final int PROCESSING_ORDINAL = 7;
    public static final String TE = "TE";
    public static final int TE_ORDINAL = 8;
    public static final String UPGRADE = "Upgrade";
    public static final int UPGRADE_ORDINAL = 11;
    public static final HttpHeaderValues CACHE = new HttpHeaderValues();
    public static final String CLOSE = "close";
    public static final Buffer CLOSE_BUFFER = CACHE.add(CLOSE, 1);
    public static final String CHUNKED = "chunked";
    public static final Buffer CHUNKED_BUFFER = CACHE.add(CHUNKED, 2);
    public static final String GZIP = "gzip";
    public static final Buffer GZIP_BUFFER = CACHE.add(GZIP, 3);
    public static final Buffer IDENTITY_BUFFER = CACHE.add("identity", 4);
    public static final String KEEP_ALIVE = "keep-alive";
    public static final Buffer KEEP_ALIVE_BUFFER = CACHE.add(KEEP_ALIVE, 5);
    public static final String CONTINUE = "100-continue";
    public static final Buffer CONTINUE_BUFFER = CACHE.add(CONTINUE, 6);
    public static final String PROCESSING = "102-processing";
    public static final Buffer PROCESSING_BUFFER = CACHE.add(PROCESSING, 7);
    public static final Buffer TE_BUFFER = CACHE.add("TE", 8);
    public static final String BYTES = "bytes";
    public static final Buffer BYTES_BUFFER = CACHE.add(BYTES, 9);
    public static final String NO_CACHE = "no-cache";
    public static final Buffer NO_CACHE_BUFFER = CACHE.add(NO_CACHE, 10);
    public static final Buffer UPGRADE_BUFFER = CACHE.add("Upgrade", 11);

    public static boolean hasKnownValues(int httpHeaderOrdinal) {
        if (httpHeaderOrdinal == 1 || httpHeaderOrdinal == 5 || httpHeaderOrdinal == 10) {
            return true;
        }
        return false;
    }
}
