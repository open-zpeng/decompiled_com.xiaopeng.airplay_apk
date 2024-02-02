package com.sun.net.httpserver;

import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
/* loaded from: classes.dex */
public abstract class HttpServer {
    public abstract void bind(InetSocketAddress inetSocketAddress, int i) throws IOException;

    public abstract HttpContext createContext(String str);

    public abstract HttpContext createContext(String str, HttpHandler httpHandler);

    public abstract InetSocketAddress getAddress();

    public abstract Executor getExecutor();

    public abstract void removeContext(HttpContext httpContext);

    public abstract void removeContext(String str) throws IllegalArgumentException;

    public abstract void setExecutor(Executor executor);

    public abstract void start();

    public abstract void stop(int i);

    public static HttpServer create() throws IOException {
        return create(null, 0);
    }

    public static HttpServer create(InetSocketAddress inetSocketAddress, int i) throws IOException {
        return HttpServerProvider.provider().createHttpServer(inetSocketAddress, i);
    }
}
