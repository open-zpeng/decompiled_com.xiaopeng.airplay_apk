package org.fourthline.cling.transport.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamServer;
/* loaded from: classes.dex */
public class StreamServerImpl implements StreamServer<StreamServerConfigurationImpl> {
    private static Logger log = Logger.getLogger(StreamServer.class.getName());
    protected final StreamServerConfigurationImpl configuration;
    protected HttpServer server;

    public StreamServerImpl(StreamServerConfigurationImpl configuration) {
        this.configuration = configuration;
    }

    @Override // org.fourthline.cling.transport.spi.StreamServer
    public synchronized void init(InetAddress bindAddress, Router router) throws InitializationException {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(bindAddress, this.configuration.getListenPort());
            this.server = HttpServer.create(socketAddress, this.configuration.getTcpConnectionBacklog());
            this.server.createContext("/", new RequestHttpHandler(router));
            Logger logger = log;
            logger.info("Created server (for receiving TCP streams) on: " + this.server.getAddress());
        } catch (Exception ex) {
            throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex.toString(), ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.StreamServer
    public synchronized int getPort() {
        return this.server.getAddress().getPort();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.transport.spi.StreamServer
    public StreamServerConfigurationImpl getConfiguration() {
        return this.configuration;
    }

    @Override // java.lang.Runnable
    public synchronized void run() {
        log.fine("Starting StreamServer...");
        this.server.start();
    }

    @Override // org.fourthline.cling.transport.spi.StreamServer
    public synchronized void stop() {
        log.fine("Stopping StreamServer...");
        if (this.server != null) {
            this.server.stop(1);
        }
    }

    /* loaded from: classes.dex */
    protected class RequestHttpHandler implements HttpHandler {
        private final Router router;

        public RequestHttpHandler(Router router) {
            this.router = router;
        }

        @Override // com.sun.net.httpserver.HttpHandler
        public void handle(final HttpExchange httpExchange) throws IOException {
            Logger logger = StreamServerImpl.log;
            logger.fine("Received HTTP exchange: " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI());
            this.router.received(new HttpExchangeUpnpStream(this.router.getProtocolFactory(), httpExchange) { // from class: org.fourthline.cling.transport.impl.StreamServerImpl.RequestHttpHandler.1
                @Override // org.fourthline.cling.transport.impl.HttpExchangeUpnpStream
                protected Connection createConnection() {
                    return new HttpServerConnection(httpExchange);
                }
            });
        }
    }

    protected boolean isConnectionOpen(HttpExchange exchange) {
        log.warning("Can't check client connection, socket access impossible on JDK webserver!");
        return true;
    }

    /* loaded from: classes.dex */
    protected class HttpServerConnection implements Connection {
        protected HttpExchange exchange;

        public HttpServerConnection(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override // org.fourthline.cling.model.message.Connection
        public boolean isOpen() {
            return StreamServerImpl.this.isConnectionOpen(this.exchange);
        }

        @Override // org.fourthline.cling.model.message.Connection
        public InetAddress getRemoteAddress() {
            if (this.exchange.getRemoteAddress() != null) {
                return this.exchange.getRemoteAddress().getAddress();
            }
            return null;
        }

        @Override // org.fourthline.cling.model.message.Connection
        public InetAddress getLocalAddress() {
            if (this.exchange.getLocalAddress() != null) {
                return this.exchange.getLocalAddress().getAddress();
            }
            return null;
        }
    }
}
