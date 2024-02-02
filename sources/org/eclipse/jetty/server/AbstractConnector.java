package org.eclipse.jetty.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.http.HttpBuffers;
import org.eclipse.jetty.http.HttpBuffersImpl;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.util.statistic.SampleStatistic;
import org.eclipse.jetty.util.thread.ThreadPool;
/* loaded from: classes.dex */
public abstract class AbstractConnector extends AggregateLifeCycle implements HttpBuffers, Connector, Dumpable {
    private static final Logger LOG = Log.getLogger(AbstractConnector.class);
    private transient Thread[] _acceptorThreads;
    private boolean _forwarded;
    private String _forwardedCipherSuiteHeader;
    private String _forwardedSslSessionIdHeader;
    private String _host;
    private String _hostHeader;
    private String _name;
    private Server _server;
    private ThreadPool _threadPool;
    private boolean _useDNS;
    private int _port = 0;
    private String _integralScheme = "https";
    private int _integralPort = 0;
    private String _confidentialScheme = "https";
    private int _confidentialPort = 0;
    private int _acceptQueueSize = 0;
    private int _acceptors = 1;
    private int _acceptorPriorityOffset = 0;
    private String _forwardedHostHeader = HttpHeaders.X_FORWARDED_HOST;
    private String _forwardedServerHeader = HttpHeaders.X_FORWARDED_SERVER;
    private String _forwardedForHeader = HttpHeaders.X_FORWARDED_FOR;
    private String _forwardedProtoHeader = HttpHeaders.X_FORWARDED_PROTO;
    private boolean _reuseAddress = true;
    protected int _maxIdleTime = 200000;
    protected int _lowResourceMaxIdleTime = -1;
    protected int _soLingerTime = -1;
    private final AtomicLong _statsStartedAt = new AtomicLong(-1);
    private final CounterStatistic _connectionStats = new CounterStatistic();
    private final SampleStatistic _requestStats = new SampleStatistic();
    private final SampleStatistic _connectionDurationStats = new SampleStatistic();
    protected final HttpBuffersImpl _buffers = new HttpBuffersImpl();

    protected abstract void accept(int i) throws IOException, InterruptedException;

    public AbstractConnector() {
        addBean(this._buffers);
    }

    @Override // org.eclipse.jetty.server.Connector
    public Server getServer() {
        return this._server;
    }

    @Override // org.eclipse.jetty.server.Connector
    public void setServer(Server server) {
        this._server = server;
    }

    public ThreadPool getThreadPool() {
        return this._threadPool;
    }

    public void setThreadPool(ThreadPool pool) {
        removeBean(this._threadPool);
        this._threadPool = pool;
        addBean(this._threadPool);
    }

    @Override // org.eclipse.jetty.server.Connector
    public void setHost(String host) {
        this._host = host;
    }

    @Override // org.eclipse.jetty.server.Connector
    public String getHost() {
        return this._host;
    }

    @Override // org.eclipse.jetty.server.Connector
    public void setPort(int port) {
        this._port = port;
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getPort() {
        return this._port;
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getMaxIdleTime() {
        return this._maxIdleTime;
    }

    @Override // org.eclipse.jetty.server.Connector
    public void setMaxIdleTime(int maxIdleTime) {
        this._maxIdleTime = maxIdleTime;
    }

    public int getLowResourcesMaxIdleTime() {
        return this._lowResourceMaxIdleTime;
    }

    public void setLowResourcesMaxIdleTime(int maxIdleTime) {
        this._lowResourceMaxIdleTime = maxIdleTime;
    }

    @Override // org.eclipse.jetty.server.Connector
    @Deprecated
    public final int getLowResourceMaxIdleTime() {
        return getLowResourcesMaxIdleTime();
    }

    @Override // org.eclipse.jetty.server.Connector
    @Deprecated
    public final void setLowResourceMaxIdleTime(int maxIdleTime) {
        setLowResourcesMaxIdleTime(maxIdleTime);
    }

    public int getSoLingerTime() {
        return this._soLingerTime;
    }

    public int getAcceptQueueSize() {
        return this._acceptQueueSize;
    }

    public void setAcceptQueueSize(int acceptQueueSize) {
        this._acceptQueueSize = acceptQueueSize;
    }

    public int getAcceptors() {
        return this._acceptors;
    }

    public void setAcceptors(int acceptors) {
        if (acceptors > 2 * Runtime.getRuntime().availableProcessors()) {
            Logger logger = LOG;
            logger.warn("Acceptors should be <=2*availableProcessors: " + this, new Object[0]);
        }
        this._acceptors = acceptors;
    }

    public void setSoLingerTime(int soLingerTime) {
        this._soLingerTime = soLingerTime;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        if (this._server == null) {
            throw new IllegalStateException("No server");
        }
        open();
        if (this._threadPool == null) {
            this._threadPool = this._server.getThreadPool();
            addBean(this._threadPool, false);
        }
        super.doStart();
        synchronized (this) {
            this._acceptorThreads = new Thread[getAcceptors()];
            for (int i = 0; i < this._acceptorThreads.length; i++) {
                if (!this._threadPool.dispatch(new Acceptor(i))) {
                    throw new IllegalStateException("!accepting");
                }
            }
            if (this._threadPool.isLowOnThreads()) {
                LOG.warn("insufficient threads configured for {}", this);
            }
        }
        LOG.info("Started {}", this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        Thread[] acceptors;
        try {
            close();
        } catch (IOException e) {
            LOG.warn(e);
        }
        super.doStop();
        synchronized (this) {
            acceptors = this._acceptorThreads;
            this._acceptorThreads = null;
        }
        if (acceptors != null) {
            for (Thread thread : acceptors) {
                if (thread != null) {
                    thread.interrupt();
                }
            }
        }
    }

    public void join() throws InterruptedException {
        synchronized (this) {
            try {
                try {
                    Thread[] threads = this._acceptorThreads;
                    if (threads != null) {
                        for (Thread thread : threads) {
                            if (thread != null) {
                                thread.join();
                            }
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void configure(Socket socket) throws IOException {
        try {
            socket.setTcpNoDelay(true);
            if (this._soLingerTime >= 0) {
                socket.setSoLinger(true, this._soLingerTime / 1000);
            } else {
                socket.setSoLinger(false, 0);
            }
        } catch (Exception e) {
            LOG.ignore(e);
        }
    }

    @Override // org.eclipse.jetty.server.Connector
    public void customize(EndPoint endpoint, Request request) throws IOException {
        if (isForwarded()) {
            checkForwardedHeaders(endpoint, request);
        }
    }

    protected void checkForwardedHeaders(EndPoint endpoint, Request request) throws IOException {
        String ssl_session_id;
        String cipher_suite;
        HttpFields httpFields = request.getConnection().getRequestFields();
        if (getForwardedCipherSuiteHeader() != null && (cipher_suite = httpFields.getStringField(getForwardedCipherSuiteHeader())) != null) {
            request.setAttribute("javax.servlet.request.cipher_suite", cipher_suite);
        }
        if (getForwardedSslSessionIdHeader() != null && (ssl_session_id = httpFields.getStringField(getForwardedSslSessionIdHeader())) != null) {
            request.setAttribute("javax.servlet.request.ssl_session_id", ssl_session_id);
            request.setScheme("https");
        }
        String forwardedHost = getLeftMostFieldValue(httpFields, getForwardedHostHeader());
        String forwardedServer = getLeftMostFieldValue(httpFields, getForwardedServerHeader());
        String forwardedFor = getLeftMostFieldValue(httpFields, getForwardedForHeader());
        String forwardedProto = getLeftMostFieldValue(httpFields, getForwardedProtoHeader());
        if (this._hostHeader != null) {
            httpFields.put(HttpHeaders.HOST_BUFFER, this._hostHeader);
            request.setServerName(null);
            request.setServerPort(-1);
            request.getServerName();
        } else if (forwardedHost != null) {
            httpFields.put(HttpHeaders.HOST_BUFFER, forwardedHost);
            request.setServerName(null);
            request.setServerPort(-1);
            request.getServerName();
        } else if (forwardedServer != null) {
            request.setServerName(forwardedServer);
        }
        if (forwardedFor != null) {
            request.setRemoteAddr(forwardedFor);
            InetAddress inetAddress = null;
            if (this._useDNS) {
                try {
                    inetAddress = InetAddress.getByName(forwardedFor);
                } catch (UnknownHostException e) {
                    LOG.ignore(e);
                }
            }
            request.setRemoteHost(inetAddress == null ? forwardedFor : inetAddress.getHostName());
        }
        if (forwardedProto != null) {
            request.setScheme(forwardedProto);
        }
    }

    protected String getLeftMostFieldValue(HttpFields fields, String header) {
        String headerValue;
        if (header == null || (headerValue = fields.getStringField(header)) == null) {
            return null;
        }
        int commaIndex = headerValue.indexOf(44);
        if (commaIndex == -1) {
            return headerValue;
        }
        return headerValue.substring(0, commaIndex);
    }

    @Override // org.eclipse.jetty.server.Connector
    public void persist(EndPoint endpoint) throws IOException {
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getConfidentialPort() {
        return this._confidentialPort;
    }

    @Override // org.eclipse.jetty.server.Connector
    public String getConfidentialScheme() {
        return this._confidentialScheme;
    }

    @Override // org.eclipse.jetty.server.Connector
    public boolean isIntegral(Request request) {
        return false;
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getIntegralPort() {
        return this._integralPort;
    }

    @Override // org.eclipse.jetty.server.Connector
    public String getIntegralScheme() {
        return this._integralScheme;
    }

    @Override // org.eclipse.jetty.server.Connector
    public boolean isConfidential(Request request) {
        return this._forwarded && request.getScheme().equalsIgnoreCase("https");
    }

    public void setConfidentialPort(int confidentialPort) {
        this._confidentialPort = confidentialPort;
    }

    public void setConfidentialScheme(String confidentialScheme) {
        this._confidentialScheme = confidentialScheme;
    }

    public void setIntegralPort(int integralPort) {
        this._integralPort = integralPort;
    }

    public void setIntegralScheme(String integralScheme) {
        this._integralScheme = integralScheme;
    }

    public void stopAccept(int acceptorID) throws Exception {
    }

    @Override // org.eclipse.jetty.server.Connector
    public boolean getResolveNames() {
        return this._useDNS;
    }

    public void setResolveNames(boolean resolve) {
        this._useDNS = resolve;
    }

    public boolean isForwarded() {
        return this._forwarded;
    }

    public void setForwarded(boolean check) {
        if (check) {
            LOG.debug("{} is forwarded", this);
        }
        this._forwarded = check;
    }

    public String getHostHeader() {
        return this._hostHeader;
    }

    public void setHostHeader(String hostHeader) {
        this._hostHeader = hostHeader;
    }

    public String getForwardedHostHeader() {
        return this._forwardedHostHeader;
    }

    public void setForwardedHostHeader(String forwardedHostHeader) {
        this._forwardedHostHeader = forwardedHostHeader;
    }

    public String getForwardedServerHeader() {
        return this._forwardedServerHeader;
    }

    public void setForwardedServerHeader(String forwardedServerHeader) {
        this._forwardedServerHeader = forwardedServerHeader;
    }

    public String getForwardedForHeader() {
        return this._forwardedForHeader;
    }

    public void setForwardedForHeader(String forwardedRemoteAddressHeader) {
        this._forwardedForHeader = forwardedRemoteAddressHeader;
    }

    public String getForwardedProtoHeader() {
        return this._forwardedProtoHeader;
    }

    public void setForwardedProtoHeader(String forwardedProtoHeader) {
        this._forwardedProtoHeader = forwardedProtoHeader;
    }

    public String getForwardedCipherSuiteHeader() {
        return this._forwardedCipherSuiteHeader;
    }

    public void setForwardedCipherSuiteHeader(String forwardedCipherSuite) {
        this._forwardedCipherSuiteHeader = forwardedCipherSuite;
    }

    public String getForwardedSslSessionIdHeader() {
        return this._forwardedSslSessionIdHeader;
    }

    public void setForwardedSslSessionIdHeader(String forwardedSslSessionId) {
        this._forwardedSslSessionIdHeader = forwardedSslSessionId;
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getRequestBufferSize() {
        return this._buffers.getRequestBufferSize();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setRequestBufferSize(int requestBufferSize) {
        this._buffers.setRequestBufferSize(requestBufferSize);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getRequestHeaderSize() {
        return this._buffers.getRequestHeaderSize();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setRequestHeaderSize(int requestHeaderSize) {
        this._buffers.setRequestHeaderSize(requestHeaderSize);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getResponseBufferSize() {
        return this._buffers.getResponseBufferSize();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setResponseBufferSize(int responseBufferSize) {
        this._buffers.setResponseBufferSize(responseBufferSize);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getResponseHeaderSize() {
        return this._buffers.getResponseHeaderSize();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setResponseHeaderSize(int responseHeaderSize) {
        this._buffers.setResponseHeaderSize(responseHeaderSize);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getRequestBufferType() {
        return this._buffers.getRequestBufferType();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getRequestHeaderType() {
        return this._buffers.getRequestHeaderType();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getResponseBufferType() {
        return this._buffers.getResponseBufferType();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers.Type getResponseHeaderType() {
        return this._buffers.getResponseHeaderType();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setRequestBuffers(Buffers requestBuffers) {
        this._buffers.setRequestBuffers(requestBuffers);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setResponseBuffers(Buffers responseBuffers) {
        this._buffers.setResponseBuffers(responseBuffers);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers getRequestBuffers() {
        return this._buffers.getRequestBuffers();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public Buffers getResponseBuffers() {
        return this._buffers.getResponseBuffers();
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public void setMaxBuffers(int maxBuffers) {
        this._buffers.setMaxBuffers(maxBuffers);
    }

    @Override // org.eclipse.jetty.http.HttpBuffers
    public int getMaxBuffers() {
        return this._buffers.getMaxBuffers();
    }

    public String toString() {
        Object[] objArr = new Object[3];
        objArr[0] = getClass().getSimpleName();
        objArr[1] = getHost() == null ? StringUtil.ALL_INTERFACES : getHost();
        objArr[2] = Integer.valueOf(getLocalPort() <= 0 ? getPort() : getLocalPort());
        return String.format("%s@%s:%d", objArr);
    }

    /* loaded from: classes.dex */
    private class Acceptor implements Runnable {
        int _acceptor;

        Acceptor(int id) {
            this._acceptor = 0;
            this._acceptor = id;
        }

        @Override // java.lang.Runnable
        public void run() {
            Thread current = Thread.currentThread();
            synchronized (AbstractConnector.this) {
                try {
                    try {
                        if (AbstractConnector.this._acceptorThreads == null) {
                            return;
                        }
                        AbstractConnector.this._acceptorThreads[this._acceptor] = current;
                        String name = AbstractConnector.this._acceptorThreads[this._acceptor].getName();
                        current.setName(name + " Acceptor" + this._acceptor + " " + AbstractConnector.this);
                        int old_priority = current.getPriority();
                        try {
                            current.setPriority(old_priority - AbstractConnector.this._acceptorPriorityOffset);
                            while (AbstractConnector.this.isRunning() && AbstractConnector.this.getConnection() != null) {
                                try {
                                    try {
                                        AbstractConnector.this.accept(this._acceptor);
                                    } catch (EofException e) {
                                        AbstractConnector.LOG.ignore(e);
                                    }
                                } catch (IOException e2) {
                                    AbstractConnector.LOG.ignore(e2);
                                } catch (InterruptedException x) {
                                    AbstractConnector.LOG.ignore(x);
                                }
                            }
                            current.setPriority(old_priority);
                            current.setName(name);
                            synchronized (AbstractConnector.this) {
                                if (AbstractConnector.this._acceptorThreads != null) {
                                    AbstractConnector.this._acceptorThreads[this._acceptor] = null;
                                }
                            }
                        } catch (Throwable th) {
                            current.setPriority(old_priority);
                            current.setName(name);
                            synchronized (AbstractConnector.this) {
                                if (AbstractConnector.this._acceptorThreads != null) {
                                    AbstractConnector.this._acceptorThreads[this._acceptor] = null;
                                }
                                throw th;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        }
    }

    @Override // org.eclipse.jetty.server.Connector
    public String getName() {
        if (this._name == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getHost() == null ? StringUtil.ALL_INTERFACES : getHost());
            sb.append(":");
            sb.append(getLocalPort() <= 0 ? getPort() : getLocalPort());
            this._name = sb.toString();
        }
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getRequests() {
        return (int) this._requestStats.getTotal();
    }

    @Override // org.eclipse.jetty.server.Connector
    public long getConnectionsDurationTotal() {
        return this._connectionDurationStats.getTotal();
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getConnections() {
        return (int) this._connectionStats.getTotal();
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getConnectionsOpen() {
        return (int) this._connectionStats.getCurrent();
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getConnectionsOpenMax() {
        return (int) this._connectionStats.getMax();
    }

    @Override // org.eclipse.jetty.server.Connector
    public double getConnectionsDurationMean() {
        return this._connectionDurationStats.getMean();
    }

    @Override // org.eclipse.jetty.server.Connector
    public long getConnectionsDurationMax() {
        return this._connectionDurationStats.getMax();
    }

    @Override // org.eclipse.jetty.server.Connector
    public double getConnectionsDurationStdDev() {
        return this._connectionDurationStats.getStdDev();
    }

    @Override // org.eclipse.jetty.server.Connector
    public double getConnectionsRequestsMean() {
        return this._requestStats.getMean();
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getConnectionsRequestsMax() {
        return (int) this._requestStats.getMax();
    }

    @Override // org.eclipse.jetty.server.Connector
    public double getConnectionsRequestsStdDev() {
        return this._requestStats.getStdDev();
    }

    @Override // org.eclipse.jetty.server.Connector
    public void statsReset() {
        updateNotEqual(this._statsStartedAt, -1L, System.currentTimeMillis());
        this._requestStats.reset();
        this._connectionStats.reset();
        this._connectionDurationStats.reset();
    }

    @Override // org.eclipse.jetty.server.Connector
    public void setStatsOn(boolean on) {
        if (on && this._statsStartedAt.get() != -1) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Statistics on = " + on + " for " + this, new Object[0]);
        }
        statsReset();
        this._statsStartedAt.set(on ? System.currentTimeMillis() : -1L);
    }

    @Override // org.eclipse.jetty.server.Connector
    public boolean getStatsOn() {
        return this._statsStartedAt.get() != -1;
    }

    @Override // org.eclipse.jetty.server.Connector
    public long getStatsOnMs() {
        long start = this._statsStartedAt.get();
        if (start != -1) {
            return System.currentTimeMillis() - start;
        }
        return 0L;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void connectionOpened(Connection connection) {
        if (this._statsStartedAt.get() == -1) {
            return;
        }
        this._connectionStats.increment();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void connectionUpgraded(Connection oldConnection, Connection newConnection) {
        this._requestStats.set(oldConnection instanceof AbstractHttpConnection ? ((AbstractHttpConnection) oldConnection).getRequests() : 0L);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void connectionClosed(Connection connection) {
        connection.onClose();
        if (this._statsStartedAt.get() == -1) {
            return;
        }
        long duration = System.currentTimeMillis() - connection.getTimeStamp();
        int requests = connection instanceof AbstractHttpConnection ? ((AbstractHttpConnection) connection).getRequests() : 0;
        this._requestStats.set(requests);
        this._connectionStats.decrement();
        this._connectionDurationStats.set(duration);
    }

    public int getAcceptorPriorityOffset() {
        return this._acceptorPriorityOffset;
    }

    public void setAcceptorPriorityOffset(int offset) {
        this._acceptorPriorityOffset = offset;
    }

    public boolean getReuseAddress() {
        return this._reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this._reuseAddress = reuseAddress;
    }

    @Override // org.eclipse.jetty.server.Connector
    public boolean isLowResources() {
        if (this._threadPool != null) {
            return this._threadPool.isLowOnThreads();
        }
        return this._server.getThreadPool().isLowOnThreads();
    }

    private void updateNotEqual(AtomicLong valueHolder, long compare, long value) {
        long oldValue = valueHolder.get();
        while (compare != oldValue && !valueHolder.compareAndSet(oldValue, value)) {
            oldValue = valueHolder.get();
        }
    }
}
