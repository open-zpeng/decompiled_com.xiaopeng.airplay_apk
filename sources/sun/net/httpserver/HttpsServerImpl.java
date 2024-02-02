package sun.net.httpserver;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
/* loaded from: classes.dex */
public class HttpsServerImpl extends HttpsServer {
    ServerImpl server;

    HttpsServerImpl() throws IOException {
        this(new InetSocketAddress(443), 0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HttpsServerImpl(InetSocketAddress inetSocketAddress, int i) throws IOException {
        this.server = new ServerImpl(this, "https", inetSocketAddress, i);
    }

    @Override // com.sun.net.httpserver.HttpsServer
    public void setHttpsConfigurator(HttpsConfigurator httpsConfigurator) {
        this.server.setHttpsConfigurator(httpsConfigurator);
    }

    @Override // com.sun.net.httpserver.HttpsServer
    public HttpsConfigurator getHttpsConfigurator() {
        return this.server.getHttpsConfigurator();
    }

    @Override // com.sun.net.httpserver.HttpServer
    public void bind(InetSocketAddress inetSocketAddress, int i) throws IOException {
        this.server.bind(inetSocketAddress, i);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public void start() {
        this.server.start();
    }

    @Override // com.sun.net.httpserver.HttpServer
    public void setExecutor(Executor executor) {
        this.server.setExecutor(executor);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public Executor getExecutor() {
        return this.server.getExecutor();
    }

    @Override // com.sun.net.httpserver.HttpServer
    public void stop(int i) {
        this.server.stop(i);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public HttpContextImpl createContext(String str, HttpHandler httpHandler) {
        return this.server.createContext(str, httpHandler);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public HttpContextImpl createContext(String str) {
        return this.server.createContext(str);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public void removeContext(String str) throws IllegalArgumentException {
        this.server.removeContext(str);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public void removeContext(HttpContext httpContext) throws IllegalArgumentException {
        this.server.removeContext(httpContext);
    }

    @Override // com.sun.net.httpserver.HttpServer
    public InetSocketAddress getAddress() {
        return this.server.getAddress();
    }
}
