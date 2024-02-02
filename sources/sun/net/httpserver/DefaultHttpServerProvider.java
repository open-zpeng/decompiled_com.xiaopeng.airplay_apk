package sun.net.httpserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.IOException;
import java.net.InetSocketAddress;
/* loaded from: classes.dex */
public class DefaultHttpServerProvider extends HttpServerProvider {
    @Override // com.sun.net.httpserver.spi.HttpServerProvider
    public HttpServer createHttpServer(InetSocketAddress inetSocketAddress, int i) throws IOException {
        return new HttpServerImpl(inetSocketAddress, i);
    }

    @Override // com.sun.net.httpserver.spi.HttpServerProvider
    public HttpsServer createHttpsServer(InetSocketAddress inetSocketAddress, int i) throws IOException {
        return new HttpsServerImpl(inetSocketAddress, i);
    }
}
