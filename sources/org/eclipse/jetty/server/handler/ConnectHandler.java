package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.AsyncConnection;
import org.eclipse.jetty.io.nio.IndirectNIOBuffer;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SelectorManager;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.HostMap;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ThreadPool;
/* loaded from: classes.dex */
public class ConnectHandler extends HandlerWrapper {
    private static final Logger LOG = Log.getLogger(ConnectHandler.class);
    private HostMap<String> _black;
    private volatile int _connectTimeout;
    private volatile boolean _privateThreadPool;
    private final SelectorManager _selectorManager;
    private volatile ThreadPool _threadPool;
    private HostMap<String> _white;
    private volatile int _writeTimeout;

    public ConnectHandler() {
        this(null);
    }

    public ConnectHandler(String[] white, String[] black) {
        this(null, white, black);
    }

    public ConnectHandler(Handler handler) {
        this._selectorManager = new Manager();
        this._connectTimeout = 5000;
        this._writeTimeout = 30000;
        this._white = new HostMap<>();
        this._black = new HostMap<>();
        setHandler(handler);
    }

    public ConnectHandler(Handler handler, String[] white, String[] black) {
        this._selectorManager = new Manager();
        this._connectTimeout = 5000;
        this._writeTimeout = 30000;
        this._white = new HostMap<>();
        this._black = new HostMap<>();
        setHandler(handler);
        set(white, this._white);
        set(black, this._black);
    }

    public int getConnectTimeout() {
        return this._connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this._connectTimeout = connectTimeout;
    }

    public int getWriteTimeout() {
        return this._writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this._writeTimeout = writeTimeout;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        super.setServer(server);
        server.getContainer().update(this, (Object) null, this._selectorManager, "selectManager");
        if (this._privateThreadPool) {
            server.getContainer().update((Object) this, (Object) null, (Object) Boolean.valueOf(this._privateThreadPool), "threadpool", true);
        } else {
            this._threadPool = server.getThreadPool();
        }
    }

    public ThreadPool getThreadPool() {
        return this._threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object) (this._privateThreadPool ? this._threadPool : null), (Object) threadPool, "threadpool", true);
        }
        this._privateThreadPool = threadPool != null;
        this._threadPool = threadPool;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        if (this._threadPool == null) {
            this._threadPool = getServer().getThreadPool();
            this._privateThreadPool = false;
        }
        if ((this._threadPool instanceof LifeCycle) && !((LifeCycle) this._threadPool).isRunning()) {
            ((LifeCycle) this._threadPool).start();
        }
        this._selectorManager.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._selectorManager.stop();
        ThreadPool threadPool = this._threadPool;
        if (this._privateThreadPool && this._threadPool != null && (threadPool instanceof LifeCycle)) {
            ((LifeCycle) threadPool).stop();
        }
        super.doStop();
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (HttpMethods.CONNECT.equalsIgnoreCase(request.getMethod())) {
            LOG.debug("CONNECT request for {}", request.getRequestURI());
            try {
                handleConnect(baseRequest, request, response, request.getRequestURI());
                return;
            } catch (Exception e) {
                Logger logger = LOG;
                logger.warn("ConnectHandler " + baseRequest.getUri() + " " + e, new Object[0]);
                LOG.debug(e);
                return;
            }
        }
        super.handle(target, baseRequest, request, response);
    }

    protected void handleConnect(Request baseRequest, HttpServletRequest request, HttpServletResponse response, String serverAddress) throws ServletException, IOException {
        boolean proceed = handleAuthentication(request, response, serverAddress);
        if (!proceed) {
            return;
        }
        String host = serverAddress;
        int port = 80;
        int colon = serverAddress.indexOf(58);
        if (colon > 0) {
            host = serverAddress.substring(0, colon);
            port = Integer.parseInt(serverAddress.substring(colon + 1));
        }
        int port2 = port;
        String host2 = host;
        if (!validateDestination(host2)) {
            Logger logger = LOG;
            logger.info("ProxyHandler: Forbidden destination " + host2, new Object[0]);
            response.setStatus(403);
            baseRequest.setHandled(true);
            return;
        }
        try {
            SocketChannel channel = connectToServer(request, host2, port2);
            AbstractHttpConnection httpConnection = AbstractHttpConnection.getCurrentConnection();
            Buffer headerBuffer = ((HttpParser) httpConnection.getParser()).getHeaderBuffer();
            Buffer bodyBuffer = ((HttpParser) httpConnection.getParser()).getBodyBuffer();
            int length = (headerBuffer == null ? 0 : headerBuffer.length()) + (bodyBuffer != null ? bodyBuffer.length() : 0);
            IndirectNIOBuffer buffer = null;
            if (length > 0) {
                buffer = new IndirectNIOBuffer(length);
                if (headerBuffer != null) {
                    buffer.put(headerBuffer);
                    headerBuffer.clear();
                }
                if (bodyBuffer != null) {
                    buffer.put(bodyBuffer);
                    bodyBuffer.clear();
                }
            }
            ConcurrentMap<String, Object> context = new ConcurrentHashMap<>();
            prepareContext(request, context);
            ClientToProxyConnection clientToProxy = prepareConnections(context, channel, buffer);
            response.setStatus(200);
            baseRequest.getConnection().getGenerator().setPersistent(true);
            response.getOutputStream().close();
            upgradeConnection(request, response, clientToProxy);
        } catch (SocketException se) {
            Logger logger2 = LOG;
            logger2.info("ConnectHandler: SocketException " + se.getMessage(), new Object[0]);
            response.setStatus(500);
            baseRequest.setHandled(true);
        } catch (SocketTimeoutException ste) {
            Logger logger3 = LOG;
            logger3.info("ConnectHandler: SocketTimeoutException" + ste.getMessage(), new Object[0]);
            response.setStatus(504);
            baseRequest.setHandled(true);
        } catch (IOException ioe) {
            Logger logger4 = LOG;
            logger4.info("ConnectHandler: IOException" + ioe.getMessage(), new Object[0]);
            response.setStatus(500);
            baseRequest.setHandled(true);
        }
    }

    private ClientToProxyConnection prepareConnections(ConcurrentMap<String, Object> context, SocketChannel channel, Buffer buffer) {
        AbstractHttpConnection httpConnection = AbstractHttpConnection.getCurrentConnection();
        ProxyToServerConnection proxyToServer = newProxyToServerConnection(context, buffer);
        ClientToProxyConnection clientToProxy = newClientToProxyConnection(context, channel, httpConnection.getEndPoint(), httpConnection.getTimeStamp());
        clientToProxy.setConnection(proxyToServer);
        proxyToServer.setConnection(clientToProxy);
        return clientToProxy;
    }

    protected boolean handleAuthentication(HttpServletRequest request, HttpServletResponse response, String address) throws ServletException, IOException {
        return true;
    }

    protected ClientToProxyConnection newClientToProxyConnection(ConcurrentMap<String, Object> context, SocketChannel channel, EndPoint endPoint, long timeStamp) {
        return new ClientToProxyConnection(context, channel, endPoint, timeStamp);
    }

    protected ProxyToServerConnection newProxyToServerConnection(ConcurrentMap<String, Object> context, Buffer buffer) {
        return new ProxyToServerConnection(context, buffer);
    }

    private SocketChannel connectToServer(HttpServletRequest request, String host, int port) throws IOException {
        SocketChannel channel = connect(request, host, port);
        channel.configureBlocking(false);
        return channel;
    }

    protected SocketChannel connect(HttpServletRequest request, String host, int port) throws IOException {
        SocketChannel channel = SocketChannel.open();
        if (channel == null) {
            throw new IOException("unable to connect to " + host + ":" + port);
        }
        try {
            LOG.debug("Establishing connection to {}:{}", host, Integer.valueOf(port));
            channel.socket().setTcpNoDelay(true);
            channel.socket().connect(new InetSocketAddress(host, port), getConnectTimeout());
            LOG.debug("Established connection to {}:{}", host, Integer.valueOf(port));
            return channel;
        } catch (IOException x) {
            Logger logger = LOG;
            logger.debug("Failed to establish connection to " + host + ":" + port, x);
            try {
                channel.close();
            } catch (IOException xx) {
                LOG.ignore(xx);
            }
            throw x;
        }
    }

    protected void prepareContext(HttpServletRequest request, ConcurrentMap<String, Object> context) {
    }

    private void upgradeConnection(HttpServletRequest request, HttpServletResponse response, Connection connection) throws IOException {
        request.setAttribute("org.eclipse.jetty.io.Connection", connection);
        response.setStatus(101);
        LOG.debug("Upgraded connection to {}", connection);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void register(SocketChannel channel, ProxyToServerConnection proxyToServer) throws IOException {
        this._selectorManager.register(channel, proxyToServer);
        proxyToServer.waitReady(this._connectTimeout);
    }

    protected int read(EndPoint endPoint, Buffer buffer, ConcurrentMap<String, Object> context) throws IOException {
        return endPoint.fill(buffer);
    }

    protected int write(EndPoint endPoint, Buffer buffer, ConcurrentMap<String, Object> context) throws IOException {
        if (buffer == null) {
            return 0;
        }
        int length = buffer.length();
        StringBuilder debug = LOG.isDebugEnabled() ? new StringBuilder() : null;
        int flushed = endPoint.flush(buffer);
        if (debug != null) {
            debug.append(flushed);
        }
        while (buffer.length() > 0 && !endPoint.isOutputShutdown()) {
            if (!endPoint.isBlocking()) {
                boolean ready = endPoint.blockWritable(getWriteTimeout());
                if (!ready) {
                    throw new IOException("Write timeout");
                }
            }
            int flushed2 = endPoint.flush(buffer);
            if (debug != null) {
                debug.append("+");
                debug.append(flushed2);
            }
        }
        LOG.debug("Written {}/{} bytes {}", debug, Integer.valueOf(length), endPoint);
        buffer.compact();
        return length;
    }

    /* loaded from: classes.dex */
    private class Manager extends SelectorManager {
        private Manager() {
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key) throws IOException {
            SelectChannelEndPoint endp = new SelectChannelEndPoint(channel, selectSet, key, channel.socket().getSoTimeout());
            endp.setConnection(selectSet.getManager().newConnection(channel, endp, key.attachment()));
            endp.setMaxIdleTime(ConnectHandler.this._writeTimeout);
            return endp;
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public AsyncConnection newConnection(SocketChannel channel, AsyncEndPoint endpoint, Object attachment) {
            ProxyToServerConnection proxyToServer = (ProxyToServerConnection) attachment;
            proxyToServer.setTimeStamp(System.currentTimeMillis());
            proxyToServer.setEndPoint(endpoint);
            return proxyToServer;
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointOpened(SelectChannelEndPoint endpoint) {
            ProxyToServerConnection proxyToServer = (ProxyToServerConnection) endpoint.getSelectionKey().attachment();
            proxyToServer.ready();
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public boolean dispatch(Runnable task) {
            return ConnectHandler.this._threadPool.dispatch(task);
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointClosed(SelectChannelEndPoint endpoint) {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public void endPointUpgraded(ConnectedEndPoint endpoint, Connection oldConnection) {
        }
    }

    /* loaded from: classes.dex */
    public class ProxyToServerConnection implements AsyncConnection {
        private final ConcurrentMap<String, Object> _context;
        private volatile Buffer _data;
        private volatile AsyncEndPoint _endPoint;
        private volatile long _timestamp;
        private volatile ClientToProxyConnection _toClient;
        private final CountDownLatch _ready = new CountDownLatch(1);
        private final Buffer _buffer = new IndirectNIOBuffer(4096);

        public ProxyToServerConnection(ConcurrentMap<String, Object> context, Buffer data) {
            this._context = context;
            this._data = data;
        }

        public String toString() {
            return "ProxyToServer(:" + this._endPoint.getLocalPort() + "<=>:" + this._endPoint.getRemotePort() + ")";
        }

        @Override // org.eclipse.jetty.io.Connection
        public Connection handle() throws IOException {
            ConnectHandler.LOG.debug("{}: begin reading from server", this);
            try {
                try {
                    try {
                        writeData();
                        while (true) {
                            int read = ConnectHandler.this.read(this._endPoint, this._buffer, this._context);
                            if (read == -1) {
                                ConnectHandler.LOG.debug("{}: server closed connection {}", this, this._endPoint);
                                if (!this._endPoint.isOutputShutdown() && this._endPoint.isOpen()) {
                                    this._toClient.shutdownOutput();
                                }
                                closeClient();
                            } else if (read == 0) {
                                break;
                            } else {
                                ConnectHandler.LOG.debug("{}: read from server {} bytes {}", this, Integer.valueOf(read), this._endPoint);
                                int written = ConnectHandler.this.write(this._toClient._endPoint, this._buffer, this._context);
                                ConnectHandler.LOG.debug("{}: written to {} {} bytes", this, this._toClient, Integer.valueOf(written));
                            }
                        }
                        ConnectHandler.LOG.debug("{}: end reading from server", this);
                        return this;
                    } catch (IOException x) {
                        Logger logger = ConnectHandler.LOG;
                        logger.warn(this + ": unexpected exception", x);
                        close();
                        throw x;
                    }
                } catch (RuntimeException x2) {
                    Logger logger2 = ConnectHandler.LOG;
                    logger2.warn(this + ": unexpected exception", x2);
                    close();
                    throw x2;
                } catch (ClosedChannelException x3) {
                    ConnectHandler.LOG.debug(x3);
                    throw x3;
                }
            } catch (Throwable th) {
                ConnectHandler.LOG.debug("{}: end reading from server", this);
                throw th;
            }
        }

        @Override // org.eclipse.jetty.io.nio.AsyncConnection
        public void onInputShutdown() throws IOException {
        }

        private void writeData() throws IOException {
            synchronized (this) {
                if (this._data != null) {
                    int written = ConnectHandler.this.write(this._endPoint, this._data, this._context);
                    ConnectHandler.LOG.debug("{}: written to server {} bytes", this, Integer.valueOf(written));
                    this._data = null;
                }
            }
        }

        public void setConnection(ClientToProxyConnection connection) {
            this._toClient = connection;
        }

        @Override // org.eclipse.jetty.io.Connection
        public long getTimeStamp() {
            return this._timestamp;
        }

        public void setTimeStamp(long timestamp) {
            this._timestamp = timestamp;
        }

        public void setEndPoint(AsyncEndPoint endpoint) {
            this._endPoint = endpoint;
        }

        @Override // org.eclipse.jetty.io.Connection
        public boolean isIdle() {
            return false;
        }

        @Override // org.eclipse.jetty.io.Connection
        public boolean isSuspended() {
            return false;
        }

        @Override // org.eclipse.jetty.io.Connection
        public void onClose() {
        }

        public void ready() {
            this._ready.countDown();
        }

        public void waitReady(long timeout) throws IOException {
            try {
                this._ready.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException x) {
                throw new IOException() { // from class: org.eclipse.jetty.server.handler.ConnectHandler.ProxyToServerConnection.1
                    {
                        initCause(x);
                    }
                };
            }
        }

        public void closeClient() throws IOException {
            this._toClient.closeClient();
        }

        public void closeServer() throws IOException {
            this._endPoint.close();
        }

        public void close() {
            try {
                closeClient();
            } catch (IOException x) {
                Logger logger = ConnectHandler.LOG;
                logger.debug(this + ": unexpected exception closing the client", x);
            }
            try {
                closeServer();
            } catch (IOException x2) {
                Logger logger2 = ConnectHandler.LOG;
                logger2.debug(this + ": unexpected exception closing the server", x2);
            }
        }

        public void shutdownOutput() throws IOException {
            writeData();
            this._endPoint.shutdownOutput();
        }

        @Override // org.eclipse.jetty.io.Connection
        public void onIdleExpired(long idleForMs) {
            try {
                ConnectHandler.LOG.debug("{} idle expired", this);
                if (this._endPoint.isOutputShutdown()) {
                    close();
                } else {
                    shutdownOutput();
                }
            } catch (Exception e) {
                ConnectHandler.LOG.debug(e);
                close();
            }
        }
    }

    /* loaded from: classes.dex */
    public class ClientToProxyConnection implements AsyncConnection {
        private final SocketChannel _channel;
        private final ConcurrentMap<String, Object> _context;
        private final EndPoint _endPoint;
        private final long _timestamp;
        private volatile ProxyToServerConnection _toServer;
        private final Buffer _buffer = new IndirectNIOBuffer(4096);
        private boolean _firstTime = true;

        public ClientToProxyConnection(ConcurrentMap<String, Object> context, SocketChannel channel, EndPoint endPoint, long timestamp) {
            this._context = context;
            this._channel = channel;
            this._endPoint = endPoint;
            this._timestamp = timestamp;
        }

        public String toString() {
            return "ClientToProxy(:" + this._endPoint.getLocalPort() + "<=>:" + this._endPoint.getRemotePort() + ")";
        }

        @Override // org.eclipse.jetty.io.Connection
        public Connection handle() throws IOException {
            ConnectHandler.LOG.debug("{}: begin reading from client", this);
            try {
                try {
                    try {
                        try {
                            if (this._firstTime) {
                                this._firstTime = false;
                                ConnectHandler.this.register(this._channel, this._toServer);
                                ConnectHandler.LOG.debug("{}: registered channel {} with connection {}", this, this._channel, this._toServer);
                            }
                            while (true) {
                                int read = ConnectHandler.this.read(this._endPoint, this._buffer, this._context);
                                if (read == -1) {
                                    ConnectHandler.LOG.debug("{}: client closed connection {}", this, this._endPoint);
                                    if (!this._endPoint.isOutputShutdown() && this._endPoint.isOpen()) {
                                        this._toServer.shutdownOutput();
                                    }
                                    closeServer();
                                } else if (read == 0) {
                                    break;
                                } else {
                                    ConnectHandler.LOG.debug("{}: read from client {} bytes {}", this, Integer.valueOf(read), this._endPoint);
                                    int written = ConnectHandler.this.write(this._toServer._endPoint, this._buffer, this._context);
                                    ConnectHandler.LOG.debug("{}: written to {} {} bytes", this, this._toServer, Integer.valueOf(written));
                                }
                            }
                            ConnectHandler.LOG.debug("{}: end reading from client", this);
                            return this;
                        } catch (RuntimeException x) {
                            Logger logger = ConnectHandler.LOG;
                            logger.warn(this + ": unexpected exception", x);
                            close();
                            throw x;
                        }
                    } catch (IOException x2) {
                        Logger logger2 = ConnectHandler.LOG;
                        logger2.warn(this + ": unexpected exception", x2);
                        close();
                        throw x2;
                    }
                } catch (ClosedChannelException x3) {
                    ConnectHandler.LOG.debug(x3);
                    closeServer();
                    throw x3;
                }
            } catch (Throwable th) {
                ConnectHandler.LOG.debug("{}: end reading from client", this);
                throw th;
            }
        }

        @Override // org.eclipse.jetty.io.nio.AsyncConnection
        public void onInputShutdown() throws IOException {
        }

        @Override // org.eclipse.jetty.io.Connection
        public long getTimeStamp() {
            return this._timestamp;
        }

        @Override // org.eclipse.jetty.io.Connection
        public boolean isIdle() {
            return false;
        }

        @Override // org.eclipse.jetty.io.Connection
        public boolean isSuspended() {
            return false;
        }

        @Override // org.eclipse.jetty.io.Connection
        public void onClose() {
        }

        public void setConnection(ProxyToServerConnection connection) {
            this._toServer = connection;
        }

        public void closeClient() throws IOException {
            this._endPoint.close();
        }

        public void closeServer() throws IOException {
            this._toServer.closeServer();
        }

        public void close() {
            try {
                closeClient();
            } catch (IOException x) {
                Logger logger = ConnectHandler.LOG;
                logger.debug(this + ": unexpected exception closing the client", x);
            }
            try {
                closeServer();
            } catch (IOException x2) {
                Logger logger2 = ConnectHandler.LOG;
                logger2.debug(this + ": unexpected exception closing the server", x2);
            }
        }

        public void shutdownOutput() throws IOException {
            this._endPoint.shutdownOutput();
        }

        @Override // org.eclipse.jetty.io.Connection
        public void onIdleExpired(long idleForMs) {
            try {
                ConnectHandler.LOG.debug("{} idle expired", this);
                if (this._endPoint.isOutputShutdown()) {
                    close();
                } else {
                    shutdownOutput();
                }
            } catch (Exception e) {
                ConnectHandler.LOG.debug(e);
                close();
            }
        }
    }

    public void addWhite(String entry) {
        add(entry, this._white);
    }

    public void addBlack(String entry) {
        add(entry, this._black);
    }

    public void setWhite(String[] entries) {
        set(entries, this._white);
    }

    public void setBlack(String[] entries) {
        set(entries, this._black);
    }

    protected void set(String[] entries, HostMap<String> hostMap) {
        hostMap.clear();
        if (entries != null && entries.length > 0) {
            for (String addrPath : entries) {
                add(addrPath, hostMap);
            }
        }
    }

    private void add(String entry, HostMap<String> hostMap) {
        if (entry != null && entry.length() > 0) {
            String entry2 = entry.trim();
            if (hostMap.get(entry2) == null) {
                hostMap.put(entry2, entry2);
            }
        }
    }

    public boolean validateDestination(String host) {
        if (this._white.size() > 0) {
            Object whiteObj = this._white.getLazyMatches(host);
            if (whiteObj == null) {
                return false;
            }
        }
        if (this._black.size() > 0) {
            Object blackObj = this._black.getLazyMatches(host);
            return blackObj == null;
        }
        return true;
    }

    @Override // org.eclipse.jetty.server.handler.AbstractHandlerContainer, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        if (this._privateThreadPool) {
            dump(out, indent, Arrays.asList(this._threadPool, this._selectorManager), TypeUtil.asList(getHandlers()), getBeans());
        } else {
            dump(out, indent, Arrays.asList(this._selectorManager), TypeUtil.asList(getHandlers()), getBeans());
        }
    }
}
