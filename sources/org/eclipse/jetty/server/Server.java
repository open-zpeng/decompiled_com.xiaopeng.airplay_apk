package org.eclipse.jetty.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ShutdownThread;
import org.eclipse.jetty.util.thread.ThreadPool;
/* loaded from: classes.dex */
public class Server extends HandlerWrapper implements Attributes {
    private static final Logger LOG = Log.getLogger(Server.class);
    private static final String __version;
    private Connector[] _connectors;
    private SessionIdManager _sessionIdManager;
    private boolean _stopAtShutdown;
    private ThreadPool _threadPool;
    private final Container _container = new Container();
    private final AttributesMap _attributes = new AttributesMap();
    private boolean _sendServerVersion = true;
    private boolean _sendDateHeader = false;
    private int _graceful = 0;
    private boolean _dumpAfterStart = false;
    private boolean _dumpBeforeStop = false;
    private boolean _uncheckedPrintWriter = false;

    /* loaded from: classes.dex */
    public interface Graceful extends Handler {
        void setShutdown(boolean z);
    }

    static {
        if (Server.class.getPackage() != null && "Eclipse.org - Jetty".equals(Server.class.getPackage().getImplementationVendor()) && Server.class.getPackage().getImplementationVersion() != null) {
            __version = Server.class.getPackage().getImplementationVersion();
        } else {
            __version = System.getProperty("jetty.version", "8.y.z-SNAPSHOT");
        }
    }

    public Server() {
        setServer(this);
    }

    public Server(int port) {
        setServer(this);
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        setConnectors(new Connector[]{connector});
    }

    public Server(InetSocketAddress addr) {
        setServer(this);
        Connector connector = new SelectChannelConnector();
        connector.setHost(addr.getHostName());
        connector.setPort(addr.getPort());
        setConnectors(new Connector[]{connector});
    }

    public static String getVersion() {
        return __version;
    }

    public Container getContainer() {
        return this._container;
    }

    public boolean getStopAtShutdown() {
        return this._stopAtShutdown;
    }

    public void setStopAtShutdown(boolean stop) {
        if (stop) {
            if (!this._stopAtShutdown && isStarted()) {
                ShutdownThread.register(this);
            }
        } else {
            ShutdownThread.deregister(this);
        }
        this._stopAtShutdown = stop;
    }

    public Connector[] getConnectors() {
        return this._connectors;
    }

    public void addConnector(Connector connector) {
        setConnectors((Connector[]) LazyList.addToArray(getConnectors(), connector, Connector.class));
    }

    public void removeConnector(Connector connector) {
        setConnectors((Connector[]) LazyList.removeFromArray(getConnectors(), connector));
    }

    public void setConnectors(Connector[] connectors) {
        if (connectors != null) {
            for (Connector connector : connectors) {
                connector.setServer(this);
            }
        }
        this._container.update((Object) this, (Object[]) this._connectors, (Object[]) connectors, "connector");
        this._connectors = connectors;
    }

    public ThreadPool getThreadPool() {
        return this._threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        if (this._threadPool != null) {
            removeBean(this._threadPool);
        }
        this._container.update((Object) this, (Object) this._threadPool, (Object) threadPool, "threadpool", false);
        this._threadPool = threadPool;
        if (this._threadPool != null) {
            addBean(this._threadPool);
        }
    }

    public boolean isDumpAfterStart() {
        return this._dumpAfterStart;
    }

    public void setDumpAfterStart(boolean dumpAfterStart) {
        this._dumpAfterStart = dumpAfterStart;
    }

    public boolean isDumpBeforeStop() {
        return this._dumpBeforeStop;
    }

    public void setDumpBeforeStop(boolean dumpBeforeStop) {
        this._dumpBeforeStop = dumpBeforeStop;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    protected void doStart() throws Exception {
        if (getStopAtShutdown()) {
            ShutdownThread.register(this);
        }
        ShutdownMonitor.getInstance().start();
        Logger logger = LOG;
        logger.info("jetty-" + __version, new Object[0]);
        HttpGenerator.setServerVersion(__version);
        MultiException mex = new MultiException();
        if (this._threadPool == null) {
            setThreadPool(new QueuedThreadPool());
        }
        try {
            super.doStart();
        } catch (Throwable e) {
            mex.add(e);
        }
        if (this._connectors != null && mex.size() == 0) {
            for (int i = 0; i < this._connectors.length; i++) {
                try {
                    this._connectors[i].start();
                } catch (Throwable e2) {
                    mex.add(e2);
                }
            }
        }
        if (isDumpAfterStart()) {
            dumpStdErr();
        }
        mex.ifExceptionThrow();
    }

    /* JADX WARN: Can't wrap try/catch for region: R(10:1|(1:3)|4|(6:6|(2:8|(2:9|(5:11|12|13|15|16)(1:20)))(0)|21|(2:24|22)|25|26)|27|(5:29|(2:30|(4:32|33|35|36)(0))|41|42|(2:44|45)(1:47))(0)|40|41|42|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x007e, code lost:
        r1 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x007f, code lost:
        r0.add(r1);
     */
    /* JADX WARN: Removed duplicated region for block: B:39:0x008b  */
    /* JADX WARN: Removed duplicated region for block: B:53:? A[RETURN, SYNTHETIC] */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void doStop() throws java.lang.Exception {
        /*
            r9 = this;
            boolean r0 = r9.isDumpBeforeStop()
            if (r0 == 0) goto L9
            r9.dumpStdErr()
        L9:
            org.eclipse.jetty.util.MultiException r0 = new org.eclipse.jetty.util.MultiException
            r0.<init>()
            int r1 = r9._graceful
            if (r1 <= 0) goto L61
            org.eclipse.jetty.server.Connector[] r1 = r9._connectors
            r2 = 0
            r3 = 1
            if (r1 == 0) goto L3c
            org.eclipse.jetty.server.Connector[] r1 = r9._connectors
            int r1 = r1.length
        L1b:
            int r4 = r1 + (-1)
            if (r1 <= 0) goto L3c
            org.eclipse.jetty.util.log.Logger r1 = org.eclipse.jetty.server.Server.LOG
            java.lang.String r5 = "Graceful shutdown {}"
            java.lang.Object[] r6 = new java.lang.Object[r3]
            org.eclipse.jetty.server.Connector[] r7 = r9._connectors
            r7 = r7[r4]
            r6[r2] = r7
            r1.info(r5, r6)
            org.eclipse.jetty.server.Connector[] r1 = r9._connectors     // Catch: java.lang.Throwable -> L36
            r1 = r1[r4]     // Catch: java.lang.Throwable -> L36
            r1.close()     // Catch: java.lang.Throwable -> L36
            goto L3a
        L36:
            r1 = move-exception
            r0.add(r1)
        L3a:
            r1 = r4
            goto L1b
        L3c:
            java.lang.Class<org.eclipse.jetty.server.Server$Graceful> r1 = org.eclipse.jetty.server.Server.Graceful.class
            org.eclipse.jetty.server.Handler[] r1 = r9.getChildHandlersByClass(r1)
            r4 = r2
        L43:
            int r5 = r1.length
            if (r4 >= r5) goto L5b
            r5 = r1[r4]
            org.eclipse.jetty.server.Server$Graceful r5 = (org.eclipse.jetty.server.Server.Graceful) r5
            org.eclipse.jetty.util.log.Logger r6 = org.eclipse.jetty.server.Server.LOG
            java.lang.String r7 = "Graceful shutdown {}"
            java.lang.Object[] r8 = new java.lang.Object[r3]
            r8[r2] = r5
            r6.info(r7, r8)
            r5.setShutdown(r3)
            int r4 = r4 + 1
            goto L43
        L5b:
            int r2 = r9._graceful
            long r2 = (long) r2
            java.lang.Thread.sleep(r2)
        L61:
            org.eclipse.jetty.server.Connector[] r1 = r9._connectors
            if (r1 == 0) goto L7a
            org.eclipse.jetty.server.Connector[] r1 = r9._connectors
            int r1 = r1.length
        L68:
            int r2 = r1 + (-1)
            if (r1 <= 0) goto L7a
            org.eclipse.jetty.server.Connector[] r1 = r9._connectors     // Catch: java.lang.Throwable -> L74
            r1 = r1[r2]     // Catch: java.lang.Throwable -> L74
            r1.stop()     // Catch: java.lang.Throwable -> L74
            goto L78
        L74:
            r1 = move-exception
            r0.add(r1)
        L78:
            r1 = r2
            goto L68
        L7a:
            super.doStop()     // Catch: java.lang.Throwable -> L7e
            goto L82
        L7e:
            r1 = move-exception
            r0.add(r1)
        L82:
            r0.ifExceptionThrow()
            boolean r1 = r9.getStopAtShutdown()
            if (r1 == 0) goto L8e
            org.eclipse.jetty.util.thread.ShutdownThread.deregister(r9)
        L8e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.Server.doStop():void");
    }

    public void handle(AbstractHttpConnection connection) throws IOException, ServletException {
        String target = connection.getRequest().getPathInfo();
        Request request = connection.getRequest();
        Response response = connection.getResponse();
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("REQUEST " + target + " on " + connection, new Object[0]);
            handle(target, request, request, response);
            Logger logger2 = LOG;
            logger2.debug("RESPONSE " + target + "  " + connection.getResponse().getStatus() + " handled=" + request.isHandled(), new Object[0]);
            return;
        }
        handle(target, request, request, response);
    }

    public void handleAsync(AbstractHttpConnection connection) throws IOException, ServletException {
        AsyncContinuation async = connection.getRequest().getAsyncContinuation();
        AsyncContinuation.AsyncEventState state = async.getAsyncEventState();
        Request baseRequest = connection.getRequest();
        String path = state.getPath();
        if (path != null) {
            String contextPath = state.getServletContext().getContextPath();
            HttpURI uri = new HttpURI(URIUtil.addPaths(contextPath, path));
            baseRequest.setUri(uri);
            baseRequest.setRequestURI(null);
            baseRequest.setPathInfo(baseRequest.getRequestURI());
            if (uri.getQuery() != null) {
                baseRequest.mergeQueryString(uri.getQuery());
            }
        }
        String target = baseRequest.getPathInfo();
        HttpServletRequest request = (HttpServletRequest) async.getRequest();
        HttpServletResponse response = (HttpServletResponse) async.getResponse();
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("REQUEST " + target + " on " + connection, new Object[0]);
            handle(target, baseRequest, request, response);
            Logger logger2 = LOG;
            logger2.debug("RESPONSE " + target + "  " + connection.getResponse().getStatus(), new Object[0]);
            return;
        }
        handle(target, baseRequest, request, response);
    }

    public void join() throws InterruptedException {
        getThreadPool().join();
    }

    public SessionIdManager getSessionIdManager() {
        return this._sessionIdManager;
    }

    public void setSessionIdManager(SessionIdManager sessionIdManager) {
        if (this._sessionIdManager != null) {
            removeBean(this._sessionIdManager);
        }
        this._container.update((Object) this, (Object) this._sessionIdManager, (Object) sessionIdManager, "sessionIdManager", false);
        this._sessionIdManager = sessionIdManager;
        if (this._sessionIdManager != null) {
            addBean(this._sessionIdManager);
        }
    }

    public void setSendServerVersion(boolean sendServerVersion) {
        this._sendServerVersion = sendServerVersion;
    }

    public boolean getSendServerVersion() {
        return this._sendServerVersion;
    }

    public void setSendDateHeader(boolean sendDateHeader) {
        this._sendDateHeader = sendDateHeader;
    }

    public boolean getSendDateHeader() {
        return this._sendDateHeader;
    }

    @Deprecated
    public int getMaxCookieVersion() {
        return 1;
    }

    @Deprecated
    public void setMaxCookieVersion(int maxCookieVersion) {
    }

    @Deprecated
    public void addLifeCycle(LifeCycle c) {
        addBean(c);
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle
    public boolean addBean(Object o) {
        if (super.addBean(o)) {
            this._container.addBean(o);
            return true;
        }
        return false;
    }

    @Deprecated
    public void removeLifeCycle(LifeCycle c) {
        removeBean(c);
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle
    public boolean removeBean(Object o) {
        if (super.removeBean(o)) {
            this._container.removeBean(o);
            return true;
        }
        return false;
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void clearAttributes() {
        this._attributes.clearAttributes();
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Object getAttribute(String name) {
        return this._attributes.getAttribute(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Enumeration getAttributeNames() {
        return AttributesMap.getAttributeNamesCopy(this._attributes);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void removeAttribute(String name) {
        this._attributes.removeAttribute(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void setAttribute(String name, Object attribute) {
        this._attributes.setAttribute(name, attribute);
    }

    public int getGracefulShutdown() {
        return this._graceful;
    }

    public void setGracefulShutdown(int timeoutMS) {
        this._graceful = timeoutMS;
    }

    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        dump(out, indent, TypeUtil.asList(getHandlers()), getBeans(), TypeUtil.asList(this._connectors));
    }

    public boolean isUncheckedPrintWriter() {
        return this._uncheckedPrintWriter;
    }

    public void setUncheckedPrintWriter(boolean unchecked) {
        this._uncheckedPrintWriter = unchecked;
    }

    public static void main(String... args) throws Exception {
        System.err.println(getVersion());
    }
}
