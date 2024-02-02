package com.sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
/* loaded from: classes.dex */
public abstract class HttpExchange {
    public abstract void close();

    public abstract Object getAttribute(String str);

    public abstract HttpContext getHttpContext();

    public abstract InetSocketAddress getLocalAddress();

    public abstract HttpPrincipal getPrincipal();

    public abstract String getProtocol();

    public abstract InetSocketAddress getRemoteAddress();

    public abstract InputStream getRequestBody();

    public abstract Headers getRequestHeaders();

    public abstract String getRequestMethod();

    public abstract URI getRequestURI();

    public abstract OutputStream getResponseBody();

    public abstract int getResponseCode();

    public abstract Headers getResponseHeaders();

    public abstract void sendResponseHeaders(int i, long j) throws IOException;

    public abstract void setAttribute(String str, Object obj);

    public abstract void setStreams(InputStream inputStream, OutputStream outputStream);
}
