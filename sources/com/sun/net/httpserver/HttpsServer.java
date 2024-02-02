package com.sun.net.httpserver;

import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.IOException;
import java.net.InetSocketAddress;
/* loaded from: classes.dex */
public abstract class HttpsServer extends HttpServer {
    public abstract HttpsConfigurator getHttpsConfigurator();

    public abstract void setHttpsConfigurator(HttpsConfigurator httpsConfigurator);

    public static HttpsServer create() throws IOException {
        return create((InetSocketAddress) null, 0);
    }

    public static HttpsServer create(InetSocketAddress inetSocketAddress, int i) throws IOException {
        return HttpServerProvider.provider().createHttpsServer(inetSocketAddress, i);
    }
}
