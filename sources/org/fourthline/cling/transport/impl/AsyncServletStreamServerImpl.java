package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamServer;
/* loaded from: classes.dex */
public class AsyncServletStreamServerImpl implements StreamServer<AsyncServletStreamServerConfigurationImpl> {
    private static final Logger log = Logger.getLogger(StreamServer.class.getName());
    protected final AsyncServletStreamServerConfigurationImpl configuration;
    protected String hostAddress;
    protected int localPort;
    private int mCounter = 0;
    protected ArrayBlockingQueue<Runnable> upnpStreamQueue;

    static /* synthetic */ int access$008(AsyncServletStreamServerImpl x0) {
        int i = x0.mCounter;
        x0.mCounter = i + 1;
        return i;
    }

    public AsyncServletStreamServerImpl(AsyncServletStreamServerConfigurationImpl configuration) {
        this.configuration = configuration;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.transport.spi.StreamServer
    public AsyncServletStreamServerConfigurationImpl getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.transport.spi.StreamServer
    public synchronized void init(InetAddress bindAddress, Router router) throws InitializationException {
        try {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Setting executor service on servlet container adapter");
            }
            getConfiguration().getServletContainerAdapter().setExecutorService(router.getConfiguration().getStreamServerExecutorService());
            if (log.isLoggable(Level.FINE)) {
                Logger logger = log;
                logger.fine("Adding connector: " + bindAddress + ":" + getConfiguration().getListenPort());
            }
            this.hostAddress = bindAddress.getHostAddress();
            this.localPort = getConfiguration().getServletContainerAdapter().addConnector(this.hostAddress, getConfiguration().getListenPort());
            String contextPath = router.getConfiguration().getNamespace().getBasePath().getPath();
            getConfiguration().getServletContainerAdapter().registerServlet(contextPath, createServlet(router));
            this.upnpStreamQueue = router.getConfiguration().getUpnpStreamQueue();
        } catch (Exception ex) {
            throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex.toString(), ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.StreamServer
    public synchronized int getPort() {
        return this.localPort;
    }

    @Override // org.fourthline.cling.transport.spi.StreamServer
    public synchronized void stop() {
        getConfiguration().getServletContainerAdapter().removeConnector(this.hostAddress, this.localPort);
    }

    @Override // java.lang.Runnable
    public void run() {
        getConfiguration().getServletContainerAdapter().startIfNotRunning();
    }

    protected Servlet createServlet(final Router router) {
        return new HttpServlet() { // from class: org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl.1
            @Override // javax.servlet.http.HttpServlet
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                final long startTime = System.currentTimeMillis();
                final int counter = AsyncServletStreamServerImpl.access$008(AsyncServletStreamServerImpl.this);
                if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
                    AsyncServletStreamServerImpl.log.fine(String.format("HttpServlet.service(): id: %3d, request URI: %s", Integer.valueOf(counter), req.getRequestURI()));
                }
                AsyncContext async = req.startAsync();
                async.setTimeout(AsyncServletStreamServerImpl.this.getConfiguration().getAsyncTimeoutSeconds() * 1000);
                async.addListener(new AsyncListener() { // from class: org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl.1.1
                    @Override // javax.servlet.AsyncListener
                    public void onTimeout(AsyncEvent arg0) throws IOException {
                        long duration = System.currentTimeMillis() - startTime;
                        if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
                            AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onTimeout(): id: %3d, duration: %,4d, request: %s", Integer.valueOf(counter), Long.valueOf(duration), arg0.getSuppliedRequest()));
                        }
                    }

                    @Override // javax.servlet.AsyncListener
                    public void onStartAsync(AsyncEvent arg0) throws IOException {
                        if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
                            AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onStartAsync(): id: %3d, request: %s", Integer.valueOf(counter), arg0.getSuppliedRequest()));
                        }
                    }

                    @Override // javax.servlet.AsyncListener
                    public void onError(AsyncEvent arg0) throws IOException {
                        long duration = System.currentTimeMillis() - startTime;
                        if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
                            AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onError(): id: %3d, duration: %,4d, response: %s", Integer.valueOf(counter), Long.valueOf(duration), arg0.getSuppliedResponse()));
                        }
                    }

                    @Override // javax.servlet.AsyncListener
                    public void onComplete(AsyncEvent arg0) throws IOException {
                        long duration = System.currentTimeMillis() - startTime;
                        if (AsyncServletStreamServerImpl.log.isLoggable(Level.FINE)) {
                            AsyncServletStreamServerImpl.log.fine(String.format("AsyncListener.onComplete(): id: %3d, duration: %,4d, response: %s", Integer.valueOf(counter), Long.valueOf(duration), arg0.getSuppliedResponse()));
                        }
                    }
                });
                AsyncServletUpnpStream stream = (AsyncServletUpnpStream) AsyncServletStreamServerImpl.this.upnpStreamQueue.poll();
                if (stream == null) {
                    stream = new AsyncServletUpnpStream(router.getProtocolFactory(), async, req) { // from class: org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl.1.2
                        @Override // org.fourthline.cling.transport.impl.AsyncServletUpnpStream
                        protected Connection createConnection() {
                            if (this.connection == null) {
                                this.connection = new AsyncServletConnection(getRequest());
                            }
                            return this.connection;
                        }
                    };
                } else {
                    stream.setAsyncContext(async);
                    stream.setRequest(req);
                    if (stream.connection != null) {
                        AsyncServletConnection connection = (AsyncServletConnection) stream.connection;
                        connection.setRequest(req);
                    }
                }
                router.received(stream);
            }
        };
    }

    protected boolean isConnectionOpen(HttpServletRequest request) {
        return true;
    }

    /* loaded from: classes.dex */
    protected class AsyncServletConnection implements Connection {
        protected HttpServletRequest request;

        public AsyncServletConnection(HttpServletRequest request) {
            this.request = request;
        }

        public void setRequest(HttpServletRequest req) {
            this.request = req;
        }

        public HttpServletRequest getRequest() {
            return this.request;
        }

        @Override // org.fourthline.cling.model.message.Connection
        public boolean isOpen() {
            return AsyncServletStreamServerImpl.this.isConnectionOpen(getRequest());
        }

        @Override // org.fourthline.cling.model.message.Connection
        public InetAddress getRemoteAddress() {
            try {
                return InetAddress.getByName(getRequest().getRemoteAddr());
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override // org.fourthline.cling.model.message.Connection
        public InetAddress getLocalAddress() {
            try {
                return InetAddress.getByName(getRequest().getLocalAddr());
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
