package org.eclipse.jetty.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.net.ssl.SSLContext;
import org.eclipse.jetty.client.security.Authentication;
import org.eclipse.jetty.client.security.RealmResolver;
import org.eclipse.jetty.http.HttpBuffers;
import org.eclipse.jetty.http.HttpBuffersImpl;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.AttributesMap;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public class HttpClient extends AggregateLifeCycle implements HttpBuffers, Attributes, Dumpable {
    public static final int CONNECTOR_SELECT_CHANNEL = 2;
    public static final int CONNECTOR_SOCKET = 0;
    private AttributesMap _attributes;
    private final HttpBuffersImpl _buffers;
    private boolean _connectBlocking;
    private int _connectTimeout;
    Connector _connector;
    private int _connectorType;
    private ConcurrentMap<Address, HttpDestination> _destinations;
    private long _idleTimeout;
    private Timeout _idleTimeoutQ;
    private int _maxConnectionsPerAddress;
    private int _maxQueueSizePerAddress;
    private int _maxRedirects;
    private int _maxRetries;
    private Set<String> _noProxy;
    private Address _proxy;
    private Authentication _proxyAuthentication;
    private RealmResolver _realmResolver;
    private LinkedList<String> _registeredListeners;
    private boolean _removeIdleDestinations;
    private final SslContextFactory _sslContextFactory;
    ThreadPool _threadPool;
    private long _timeout;
    private Timeout _timeoutQ;
    private boolean _useDirectBuffers;

    /* loaded from: classes.dex */
    interface Connector extends LifeCycle {
        void startConnection(HttpDestination httpDestination) throws IOException;
    }

    private void setBufferTypes() {
        if (this._connectorType == 0) {
            this._buffers.setRequestBufferType(Buffers.Type.BYTE_ARRAY);
            this._buffers.setRequestHeaderType(Buffers.Type.BYTE_ARRAY);
            this._buffers.setResponseBufferType(Buffers.Type.BYTE_ARRAY);
            this._buffers.setResponseHeaderType(Buffers.Type.BYTE_ARRAY);
            return;
        }
        this._buffers.setRequestBufferType(Buffers.Type.DIRECT);
        this._buffers.setRequestHeaderType(this._useDirectBuffers ? Buffers.Type.DIRECT : Buffers.Type.INDIRECT);
        this._buffers.setResponseBufferType(Buffers.Type.DIRECT);
        this._buffers.setResponseHeaderType(this._useDirectBuffers ? Buffers.Type.DIRECT : Buffers.Type.INDIRECT);
    }

    public HttpClient() {
        this(new SslContextFactory());
    }

    public HttpClient(SslContextFactory sslContextFactory) {
        this._connectorType = 2;
        this._useDirectBuffers = true;
        this._connectBlocking = true;
        this._removeIdleDestinations = false;
        this._maxConnectionsPerAddress = Integer.MAX_VALUE;
        this._maxQueueSizePerAddress = Integer.MAX_VALUE;
        this._destinations = new ConcurrentHashMap();
        this._idleTimeout = 20000L;
        this._timeout = 320000L;
        this._connectTimeout = 75000;
        this._timeoutQ = new Timeout();
        this._idleTimeoutQ = new Timeout();
        this._maxRetries = 3;
        this._maxRedirects = 20;
        this._attributes = new AttributesMap();
        this._buffers = new HttpBuffersImpl();
        this._sslContextFactory = sslContextFactory;
        addBean(this._sslContextFactory);
        addBean(this._buffers);
    }

    public boolean isConnectBlocking() {
        return this._connectBlocking;
    }

    public void setConnectBlocking(boolean connectBlocking) {
        this._connectBlocking = connectBlocking;
    }

    public boolean isRemoveIdleDestinations() {
        return this._removeIdleDestinations;
    }

    public void setRemoveIdleDestinations(boolean removeIdleDestinations) {
        this._removeIdleDestinations = removeIdleDestinations;
    }

    public void send(HttpExchange exchange) throws IOException {
        boolean ssl = HttpSchemes.HTTPS_BUFFER.equalsIgnoreCase(exchange.getScheme());
        HttpDestination destination = getDestination(exchange.getAddress(), ssl);
        destination.send(exchange);
    }

    public ThreadPool getThreadPool() {
        return this._threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        removeBean(this._threadPool);
        this._threadPool = threadPool;
        addBean(this._threadPool);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Object getAttribute(String name) {
        return this._attributes.getAttribute(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Enumeration getAttributeNames() {
        return this._attributes.getAttributeNames();
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void removeAttribute(String name) {
        this._attributes.removeAttribute(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void setAttribute(String name, Object attribute) {
        this._attributes.setAttribute(name, attribute);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void clearAttributes() {
        this._attributes.clearAttributes();
    }

    public HttpDestination getDestination(Address remote, boolean ssl) throws IOException {
        return getDestination(remote, ssl, getSslContextFactory());
    }

    public HttpDestination getDestination(Address remote, boolean ssl, SslContextFactory sslContextFactory) throws IOException {
        if (remote == null) {
            throw new UnknownHostException("Remote socket address cannot be null.");
        }
        HttpDestination destination = this._destinations.get(remote);
        if (destination == null) {
            HttpDestination destination2 = new HttpDestination(this, remote, ssl, sslContextFactory);
            if (this._proxy != null && (this._noProxy == null || !this._noProxy.contains(remote.getHost()))) {
                destination2.setProxy(this._proxy);
                if (this._proxyAuthentication != null) {
                    destination2.setProxyAuthentication(this._proxyAuthentication);
                }
            }
            HttpDestination other = this._destinations.putIfAbsent(remote, destination2);
            return other != null ? other : destination2;
        }
        return destination;
    }

    public Collection<Address> getDestinations() {
        return Collections.unmodifiableCollection(this._destinations.keySet());
    }

    public void removeDestination(HttpDestination destination) {
        this._destinations.remove(destination.getAddress(), destination);
    }

    public void schedule(Timeout.Task task) {
        this._timeoutQ.schedule(task);
    }

    public void schedule(Timeout.Task task, long timeout) {
        this._timeoutQ.schedule(task, timeout - this._timeoutQ.getDuration());
    }

    public void scheduleIdle(Timeout.Task task) {
        this._idleTimeoutQ.schedule(task);
    }

    public void cancel(Timeout.Task task) {
        task.cancel();
    }

    public boolean getUseDirectBuffers() {
        return this._useDirectBuffers;
    }

    public void setRealmResolver(RealmResolver resolver) {
        this._realmResolver = resolver;
    }

    public RealmResolver getRealmResolver() {
        return this._realmResolver;
    }

    public boolean hasRealms() {
        return this._realmResolver != null;
    }

    public void registerListener(String listenerClass) {
        if (this._registeredListeners == null) {
            this._registeredListeners = new LinkedList<>();
        }
        this._registeredListeners.add(listenerClass);
    }

    public LinkedList<String> getRegisteredListeners() {
        return this._registeredListeners;
    }

    public void setUseDirectBuffers(boolean direct) {
        this._useDirectBuffers = direct;
        setBufferTypes();
    }

    public int getConnectorType() {
        return this._connectorType;
    }

    public void setConnectorType(int connectorType) {
        this._connectorType = connectorType;
        setBufferTypes();
    }

    public int getMaxConnectionsPerAddress() {
        return this._maxConnectionsPerAddress;
    }

    public void setMaxConnectionsPerAddress(int maxConnectionsPerAddress) {
        this._maxConnectionsPerAddress = maxConnectionsPerAddress;
    }

    public int getMaxQueueSizePerAddress() {
        return this._maxQueueSizePerAddress;
    }

    public void setMaxQueueSizePerAddress(int maxQueueSizePerAddress) {
        this._maxQueueSizePerAddress = maxQueueSizePerAddress;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        setBufferTypes();
        this._timeoutQ.setDuration(this._timeout);
        this._timeoutQ.setNow();
        this._idleTimeoutQ.setDuration(this._idleTimeout);
        this._idleTimeoutQ.setNow();
        if (this._threadPool == null) {
            QueuedThreadPool pool = new LocalQueuedThreadPool();
            pool.setMaxThreads(16);
            pool.setDaemon(true);
            pool.setName("HttpClient");
            this._threadPool = pool;
            addBean(this._threadPool, true);
        }
        this._connector = this._connectorType == 2 ? new SelectConnector(this) : new SocketConnector(this);
        addBean(this._connector, true);
        super.doStart();
        this._threadPool.dispatch(new Runnable() { // from class: org.eclipse.jetty.client.HttpClient.1
            @Override // java.lang.Runnable
            public void run() {
                while (HttpClient.this.isRunning()) {
                    HttpClient.this._timeoutQ.tick(System.currentTimeMillis());
                    HttpClient.this._idleTimeoutQ.tick(HttpClient.this._timeoutQ.getNow());
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        for (HttpDestination destination : this._destinations.values()) {
            destination.close();
        }
        this._timeoutQ.cancelAll();
        this._idleTimeoutQ.cancelAll();
        super.doStop();
        if (this._threadPool instanceof LocalQueuedThreadPool) {
            removeBean(this._threadPool);
            this._threadPool = null;
        }
        removeBean(this._connector);
    }

    protected SSLContext getSSLContext() {
        return this._sslContextFactory.getSslContext();
    }

    public SslContextFactory getSslContextFactory() {
        return this._sslContextFactory;
    }

    public long getIdleTimeout() {
        return this._idleTimeout;
    }

    public void setIdleTimeout(long ms) {
        this._idleTimeout = ms;
    }

    @Deprecated
    public int getSoTimeout() {
        return Long.valueOf(getTimeout()).intValue();
    }

    @Deprecated
    public void setSoTimeout(int timeout) {
        setTimeout(timeout);
    }

    public long getTimeout() {
        return this._timeout;
    }

    public void setTimeout(long timeout) {
        this._timeout = timeout;
    }

    public int getConnectTimeout() {
        return this._connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this._connectTimeout = connectTimeout;
    }

    public Address getProxy() {
        return this._proxy;
    }

    public void setProxy(Address proxy) {
        this._proxy = proxy;
    }

    public Authentication getProxyAuthentication() {
        return this._proxyAuthentication;
    }

    public void setProxyAuthentication(Authentication authentication) {
        this._proxyAuthentication = authentication;
    }

    public boolean isProxied() {
        return this._proxy != null;
    }

    public Set<String> getNoProxy() {
        return this._noProxy;
    }

    public void setNoProxy(Set<String> noProxyAddresses) {
        this._noProxy = noProxyAddresses;
    }

    public int maxRetries() {
        return this._maxRetries;
    }

    public void setMaxRetries(int retries) {
        this._maxRetries = retries;
    }

    public int maxRedirects() {
        return this._maxRedirects;
    }

    public void setMaxRedirects(int redirects) {
        this._maxRedirects = redirects;
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

    @Deprecated
    public String getTrustStoreLocation() {
        return this._sslContextFactory.getTrustStore();
    }

    @Deprecated
    public void setTrustStoreLocation(String trustStoreLocation) {
        this._sslContextFactory.setTrustStore(trustStoreLocation);
    }

    @Deprecated
    public InputStream getTrustStoreInputStream() {
        return this._sslContextFactory.getTrustStoreInputStream();
    }

    @Deprecated
    public void setTrustStoreInputStream(InputStream trustStoreInputStream) {
        this._sslContextFactory.setTrustStoreInputStream(trustStoreInputStream);
    }

    @Deprecated
    public String getKeyStoreLocation() {
        return this._sslContextFactory.getKeyStorePath();
    }

    @Deprecated
    public void setKeyStoreLocation(String keyStoreLocation) {
        this._sslContextFactory.setKeyStorePath(keyStoreLocation);
    }

    @Deprecated
    public InputStream getKeyStoreInputStream() {
        return this._sslContextFactory.getKeyStoreInputStream();
    }

    @Deprecated
    public void setKeyStoreInputStream(InputStream keyStoreInputStream) {
        this._sslContextFactory.setKeyStoreInputStream(keyStoreInputStream);
    }

    @Deprecated
    public void setKeyStorePassword(String keyStorePassword) {
        this._sslContextFactory.setKeyStorePassword(keyStorePassword);
    }

    @Deprecated
    public void setKeyManagerPassword(String keyManagerPassword) {
        this._sslContextFactory.setKeyManagerPassword(keyManagerPassword);
    }

    @Deprecated
    public void setTrustStorePassword(String trustStorePassword) {
        this._sslContextFactory.setTrustStorePassword(trustStorePassword);
    }

    @Deprecated
    public String getKeyStoreType() {
        return this._sslContextFactory.getKeyStoreType();
    }

    @Deprecated
    public void setKeyStoreType(String keyStoreType) {
        this._sslContextFactory.setKeyStoreType(keyStoreType);
    }

    @Deprecated
    public String getTrustStoreType() {
        return this._sslContextFactory.getTrustStoreType();
    }

    @Deprecated
    public void setTrustStoreType(String trustStoreType) {
        this._sslContextFactory.setTrustStoreType(trustStoreType);
    }

    @Deprecated
    public String getKeyManagerAlgorithm() {
        return this._sslContextFactory.getSslKeyManagerFactoryAlgorithm();
    }

    @Deprecated
    public void setKeyManagerAlgorithm(String keyManagerAlgorithm) {
        this._sslContextFactory.setSslKeyManagerFactoryAlgorithm(keyManagerAlgorithm);
    }

    @Deprecated
    public String getTrustManagerAlgorithm() {
        return this._sslContextFactory.getTrustManagerFactoryAlgorithm();
    }

    @Deprecated
    public void setTrustManagerAlgorithm(String trustManagerAlgorithm) {
        this._sslContextFactory.setTrustManagerFactoryAlgorithm(trustManagerAlgorithm);
    }

    @Deprecated
    public String getProtocol() {
        return this._sslContextFactory.getProtocol();
    }

    @Deprecated
    public void setProtocol(String protocol) {
        this._sslContextFactory.setProtocol(protocol);
    }

    @Deprecated
    public String getProvider() {
        return this._sslContextFactory.getProvider();
    }

    @Deprecated
    public void setProvider(String provider) {
        this._sslContextFactory.setProvider(provider);
    }

    @Deprecated
    public String getSecureRandomAlgorithm() {
        return this._sslContextFactory.getSecureRandomAlgorithm();
    }

    @Deprecated
    public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        this._sslContextFactory.setSecureRandomAlgorithm(secureRandomAlgorithm);
    }

    /* loaded from: classes.dex */
    private static class LocalQueuedThreadPool extends QueuedThreadPool {
        private LocalQueuedThreadPool() {
        }
    }
}
