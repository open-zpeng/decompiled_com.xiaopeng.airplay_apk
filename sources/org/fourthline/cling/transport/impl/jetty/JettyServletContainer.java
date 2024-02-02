package org.fourthline.cling.transport.impl.jetty;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.fourthline.cling.transport.spi.ServletContainerAdapter;
/* loaded from: classes.dex */
public class JettyServletContainer implements ServletContainerAdapter {
    protected Server server;
    private static final Logger log = Logger.getLogger(JettyServletContainer.class.getName());
    public static final JettyServletContainer INSTANCE = new JettyServletContainer();

    private JettyServletContainer() {
        resetServer();
    }

    @Override // org.fourthline.cling.transport.spi.ServletContainerAdapter
    public synchronized void setExecutorService(ExecutorService executorService) {
        if (INSTANCE.server.getThreadPool() == null) {
            INSTANCE.server.setThreadPool(new ExecutorThreadPool(executorService) { // from class: org.fourthline.cling.transport.impl.jetty.JettyServletContainer.1
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // org.eclipse.jetty.util.thread.ExecutorThreadPool, org.eclipse.jetty.util.component.AbstractLifeCycle
                public void doStop() throws Exception {
                }
            });
        }
    }

    @Override // org.fourthline.cling.transport.spi.ServletContainerAdapter
    public synchronized int addConnector(String host, int port) throws IOException {
        SocketConnector connector;
        connector = new SocketConnector();
        connector.setHost(host);
        connector.setPort(port);
        connector.open();
        this.server.addConnector(connector);
        if (this.server.isStarted()) {
            try {
                connector.start();
            } catch (Exception ex) {
                Logger logger = log;
                logger.severe("Couldn't start connector: " + connector + " " + ex);
                throw new RuntimeException(ex);
            }
        }
        return connector.getLocalPort();
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x0021, code lost:
        if (r3.isStarted() != false) goto L20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x0027, code lost:
        if (r3.isStarting() == false) goto L14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0029, code lost:
        r3.stop();
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x002d, code lost:
        r6.server.removeConnector(r3);
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0034, code lost:
        if (r0.length != 1) goto L17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0036, code lost:
        org.fourthline.cling.transport.impl.jetty.JettyServletContainer.log.info("No more connectors, stopping Jetty server");
        stopIfRunning();
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0041, code lost:
        r1 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x0042, code lost:
        r2 = org.fourthline.cling.transport.impl.jetty.JettyServletContainer.log;
        r2.severe("Couldn't stop connector: " + r3 + " " + r1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x0065, code lost:
        throw new java.lang.RuntimeException(r1);
     */
    @Override // org.fourthline.cling.transport.spi.ServletContainerAdapter
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public synchronized void removeConnector(java.lang.String r7, int r8) {
        /*
            r6 = this;
            monitor-enter(r6)
            org.eclipse.jetty.server.Server r0 = r6.server     // Catch: java.lang.Throwable -> L6b
            org.eclipse.jetty.server.Connector[] r0 = r0.getConnectors()     // Catch: java.lang.Throwable -> L6b
            int r1 = r0.length     // Catch: java.lang.Throwable -> L6b
            r2 = 0
        L9:
            if (r2 >= r1) goto L69
            r3 = r0[r2]     // Catch: java.lang.Throwable -> L6b
            java.lang.String r4 = r3.getHost()     // Catch: java.lang.Throwable -> L6b
            boolean r4 = r4.equals(r7)     // Catch: java.lang.Throwable -> L6b
            if (r4 == 0) goto L66
            int r4 = r3.getLocalPort()     // Catch: java.lang.Throwable -> L6b
            if (r4 != r8) goto L66
            boolean r1 = r3.isStarted()     // Catch: java.lang.Throwable -> L6b
            if (r1 != 0) goto L29
            boolean r1 = r3.isStarting()     // Catch: java.lang.Throwable -> L6b
            if (r1 == 0) goto L2d
        L29:
            r3.stop()     // Catch: java.lang.Exception -> L41 java.lang.Throwable -> L6b
        L2d:
            org.eclipse.jetty.server.Server r1 = r6.server     // Catch: java.lang.Throwable -> L6b
            r1.removeConnector(r3)     // Catch: java.lang.Throwable -> L6b
            int r1 = r0.length     // Catch: java.lang.Throwable -> L6b
            r2 = 1
            if (r1 != r2) goto L69
            java.util.logging.Logger r1 = org.fourthline.cling.transport.impl.jetty.JettyServletContainer.log     // Catch: java.lang.Throwable -> L6b
            java.lang.String r2 = "No more connectors, stopping Jetty server"
            r1.info(r2)     // Catch: java.lang.Throwable -> L6b
            r6.stopIfRunning()     // Catch: java.lang.Throwable -> L6b
            goto L69
        L41:
            r1 = move-exception
            java.util.logging.Logger r2 = org.fourthline.cling.transport.impl.jetty.JettyServletContainer.log     // Catch: java.lang.Throwable -> L6b
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L6b
            r4.<init>()     // Catch: java.lang.Throwable -> L6b
            java.lang.String r5 = "Couldn't stop connector: "
            r4.append(r5)     // Catch: java.lang.Throwable -> L6b
            r4.append(r3)     // Catch: java.lang.Throwable -> L6b
            java.lang.String r5 = " "
            r4.append(r5)     // Catch: java.lang.Throwable -> L6b
            r4.append(r1)     // Catch: java.lang.Throwable -> L6b
            java.lang.String r4 = r4.toString()     // Catch: java.lang.Throwable -> L6b
            r2.severe(r4)     // Catch: java.lang.Throwable -> L6b
            java.lang.RuntimeException r2 = new java.lang.RuntimeException     // Catch: java.lang.Throwable -> L6b
            r2.<init>(r1)     // Catch: java.lang.Throwable -> L6b
            throw r2     // Catch: java.lang.Throwable -> L6b
        L66:
            int r2 = r2 + 1
            goto L9
        L69:
            monitor-exit(r6)
            return
        L6b:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.transport.impl.jetty.JettyServletContainer.removeConnector(java.lang.String, int):void");
    }

    @Override // org.fourthline.cling.transport.spi.ServletContainerAdapter
    public synchronized void registerServlet(String contextPath, Servlet servlet) {
        if (this.server.getHandler() != null) {
            return;
        }
        Logger logger = log;
        logger.info("Registering UPnP servlet under context path: " + contextPath);
        ServletContextHandler servletHandler = new ServletContextHandler(0);
        if (contextPath != null && contextPath.length() > 0) {
            servletHandler.setContextPath(contextPath);
        }
        ServletHolder s = new ServletHolder(servlet);
        servletHandler.addServlet(s, "/*");
        this.server.setHandler(servletHandler);
    }

    @Override // org.fourthline.cling.transport.spi.ServletContainerAdapter
    public synchronized void startIfNotRunning() {
        if (!this.server.isStarted() && !this.server.isStarting()) {
            log.info("Starting Jetty server... ");
            try {
                this.server.start();
            } catch (Exception ex) {
                Logger logger = log;
                logger.severe("Couldn't start Jetty server: " + ex);
                throw new RuntimeException(ex);
            }
        }
    }

    @Override // org.fourthline.cling.transport.spi.ServletContainerAdapter
    public synchronized void stopIfRunning() {
        if (!this.server.isStopped() && !this.server.isStopping()) {
            log.info("Stopping Jetty server...");
            try {
                this.server.stop();
                resetServer();
            } catch (Exception ex) {
                Logger logger = log;
                logger.severe("Couldn't stop Jetty server: " + ex);
                throw new RuntimeException(ex);
            }
        }
    }

    protected void resetServer() {
        this.server = new Server();
        this.server.setGracefulShutdown(1000);
    }

    public static boolean isConnectionOpen(HttpServletRequest request) {
        return isConnectionOpen(request, " ".getBytes());
    }

    public static boolean isConnectionOpen(HttpServletRequest request, byte[] heartbeat) {
        Request jettyRequest = (Request) request;
        AbstractHttpConnection connection = jettyRequest.getConnection();
        Socket socket = (Socket) connection.getEndPoint().getTransport();
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Checking if client connection is still open: " + socket.getRemoteSocketAddress());
        }
        try {
            socket.getOutputStream().write(heartbeat);
            socket.getOutputStream().flush();
            return true;
        } catch (IOException e) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger2 = log;
                logger2.fine("Client connection has been closed: " + socket.getRemoteSocketAddress());
                return false;
            }
            return false;
        }
    }
}
