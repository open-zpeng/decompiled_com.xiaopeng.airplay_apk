package org.eclipse.jetty.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.SelectConnector;
import org.eclipse.jetty.client.security.Authentication;
import org.eclipse.jetty.client.security.SecurityListener;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.PathMap;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
/* loaded from: classes.dex */
public class HttpDestination implements Dumpable {
    private static final Logger LOG = Log.getLogger(HttpDestination.class);
    private final Address _address;
    private PathMap _authorizations;
    private final HttpClient _client;
    private List<HttpCookie> _cookies;
    private final ByteArrayBuffer _hostHeader;
    private volatile int _maxConnections;
    private volatile int _maxQueueSize;
    private volatile Address _proxy;
    private Authentication _proxyAuthentication;
    private final boolean _ssl;
    private final SslContextFactory _sslContextFactory;
    private final List<HttpExchange> _exchanges = new LinkedList();
    private final List<AbstractHttpConnection> _connections = new LinkedList();
    private final BlockingQueue<Object> _reservedConnections = new ArrayBlockingQueue(10, true);
    private final List<AbstractHttpConnection> _idleConnections = new ArrayList();
    private int _pendingConnections = 0;
    private int _pendingReservedConnections = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public HttpDestination(HttpClient client, Address address, boolean ssl, SslContextFactory sslContextFactory) {
        this._client = client;
        this._address = address;
        this._ssl = ssl;
        this._sslContextFactory = sslContextFactory;
        this._maxConnections = this._client.getMaxConnectionsPerAddress();
        this._maxQueueSize = this._client.getMaxQueueSizePerAddress();
        String addressString = address.getHost();
        if (address.getPort() != (this._ssl ? 443 : 80)) {
            addressString = addressString + ":" + address.getPort();
        }
        this._hostHeader = new ByteArrayBuffer(addressString);
    }

    public HttpClient getHttpClient() {
        return this._client;
    }

    public Address getAddress() {
        return this._address;
    }

    public boolean isSecure() {
        return this._ssl;
    }

    public SslContextFactory getSslContextFactory() {
        return this._sslContextFactory;
    }

    public Buffer getHostHeader() {
        return this._hostHeader;
    }

    public int getMaxConnections() {
        return this._maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this._maxConnections = maxConnections;
    }

    public int getMaxQueueSize() {
        return this._maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this._maxQueueSize = maxQueueSize;
    }

    public int getConnections() {
        int size;
        synchronized (this) {
            size = this._connections.size();
        }
        return size;
    }

    public int getIdleConnections() {
        int size;
        synchronized (this) {
            size = this._idleConnections.size();
        }
        return size;
    }

    public void addAuthorization(String pathSpec, Authentication authorization) {
        synchronized (this) {
            if (this._authorizations == null) {
                this._authorizations = new PathMap();
            }
            this._authorizations.put(pathSpec, authorization);
        }
    }

    public void addCookie(HttpCookie cookie) {
        synchronized (this) {
            if (this._cookies == null) {
                this._cookies = new ArrayList();
            }
            this._cookies.add(cookie);
        }
    }

    public void clearCookies() {
        synchronized (this) {
            this._cookies.clear();
        }
    }

    private AbstractHttpConnection getConnection(long timeout) throws IOException {
        Object o;
        AbstractHttpConnection connection = null;
        while (connection == null) {
            AbstractHttpConnection idleConnection = getIdleConnection();
            connection = idleConnection;
            if (idleConnection != null || timeout <= 0) {
                break;
            }
            boolean startConnection = false;
            synchronized (this) {
                int totalConnections = this._connections.size() + this._pendingConnections;
                if (totalConnections < this._maxConnections) {
                    this._pendingReservedConnections++;
                    startConnection = true;
                }
            }
            if (startConnection) {
                startNewConnection();
                try {
                    o = this._reservedConnections.take();
                } catch (InterruptedException e) {
                    LOG.ignore(e);
                }
                if (o instanceof AbstractHttpConnection) {
                    connection = (AbstractHttpConnection) o;
                } else {
                    throw ((IOException) o);
                    break;
                }
            } else {
                try {
                    Thread.currentThread();
                    Thread.sleep(200L);
                    timeout -= 200;
                } catch (InterruptedException e2) {
                    LOG.ignore(e2);
                }
            }
        }
        return connection;
    }

    public AbstractHttpConnection reserveConnection(long timeout) throws IOException {
        AbstractHttpConnection connection = getConnection(timeout);
        if (connection != null) {
            connection.setReserved(true);
        }
        return connection;
    }

    public AbstractHttpConnection getIdleConnection() throws IOException {
        AbstractHttpConnection connection = null;
        do {
            synchronized (this) {
                if (connection != null) {
                    try {
                        this._connections.remove(connection);
                        connection.close();
                        connection = null;
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (this._idleConnections.size() > 0) {
                    connection = this._idleConnections.remove(this._idleConnections.size() - 1);
                }
            }
            if (connection == null) {
                return null;
            }
        } while (!connection.cancelIdleTimeout());
        return connection;
    }

    protected void startNewConnection() {
        try {
            synchronized (this) {
                this._pendingConnections++;
            }
            HttpClient.Connector connector = this._client._connector;
            if (connector != null) {
                connector.startConnection(this);
            }
        } catch (Exception e) {
            LOG.debug(e);
            onConnectionFailed(e);
        }
    }

    public void onConnectionFailed(Throwable throwable) {
        Throwable connect_failure = null;
        boolean startConnection = false;
        synchronized (this) {
            this._pendingConnections--;
            if (this._pendingReservedConnections > 0) {
                connect_failure = throwable;
                this._pendingReservedConnections--;
            } else if (this._exchanges.size() > 0) {
                HttpExchange ex = this._exchanges.remove(0);
                if (ex.setStatus(9)) {
                    ex.getEventListener().onConnectionFailed(throwable);
                }
                if (!this._exchanges.isEmpty() && this._client.isStarted()) {
                    startConnection = true;
                }
            }
        }
        if (startConnection) {
            startNewConnection();
        }
        if (connect_failure != null) {
            try {
                this._reservedConnections.put(connect_failure);
            } catch (InterruptedException e) {
                LOG.ignore(e);
            }
        }
    }

    public void onException(Throwable throwable) {
        synchronized (this) {
            this._pendingConnections--;
            if (this._exchanges.size() > 0) {
                HttpExchange ex = this._exchanges.remove(0);
                if (ex.setStatus(9)) {
                    ex.getEventListener().onException(throwable);
                }
            }
        }
    }

    public void onNewConnection(AbstractHttpConnection connection) throws IOException {
        Connection reservedConnection = null;
        synchronized (this) {
            this._pendingConnections--;
            this._connections.add(connection);
            if (this._pendingReservedConnections > 0) {
                reservedConnection = connection;
                this._pendingReservedConnections--;
            } else {
                EndPoint endPoint = connection.getEndPoint();
                if (isProxied() && (endPoint instanceof SelectConnector.UpgradableEndPoint)) {
                    SelectConnector.UpgradableEndPoint proxyEndPoint = (SelectConnector.UpgradableEndPoint) endPoint;
                    ConnectExchange connect = new ConnectExchange(getAddress(), proxyEndPoint);
                    connect.setAddress(getProxy());
                    LOG.debug("Establishing tunnel to {} via {}", getAddress(), getProxy());
                    send(connection, connect);
                } else if (this._exchanges.size() == 0) {
                    LOG.debug("No exchanges for new connection {}", connection);
                    connection.setIdleTimeout();
                    this._idleConnections.add(connection);
                } else {
                    HttpExchange exchange = this._exchanges.remove(0);
                    send(connection, exchange);
                }
            }
        }
        if (reservedConnection != null) {
            try {
                this._reservedConnections.put(reservedConnection);
            } catch (InterruptedException e) {
                LOG.ignore(e);
            }
        }
    }

    public void returnConnection(AbstractHttpConnection connection, boolean close) throws IOException {
        boolean z = false;
        if (connection.isReserved()) {
            connection.setReserved(false);
        }
        if (close) {
            try {
                connection.close();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
        if (!this._client.isStarted()) {
            return;
        }
        if (!close && connection.getEndPoint().isOpen()) {
            synchronized (this) {
                if (this._exchanges.size() == 0) {
                    connection.setIdleTimeout();
                    this._idleConnections.add(connection);
                } else {
                    HttpExchange ex = this._exchanges.remove(0);
                    send(connection, ex);
                }
                notifyAll();
            }
            return;
        }
        boolean startConnection = false;
        boolean remove = false;
        synchronized (this) {
            this._connections.remove(connection);
            if (this._exchanges.isEmpty()) {
                if (this._client.isRemoveIdleDestinations() && ((this._cookies == null || this._cookies.isEmpty()) && this._connections.isEmpty() && this._idleConnections.isEmpty())) {
                    z = true;
                }
                remove = z;
            } else if (this._client.isStarted()) {
                startConnection = true;
            }
        }
        if (startConnection) {
            startNewConnection();
        }
        if (remove) {
            this._client.removeDestination(this);
        }
    }

    public void returnIdleConnection(AbstractHttpConnection connection) {
        long idleForMs = connection.getEndPoint() != null ? connection.getEndPoint().getMaxIdleTime() : -1L;
        connection.onIdleExpired(idleForMs);
        boolean startConnection = false;
        boolean remove = false;
        synchronized (this) {
            this._idleConnections.remove(connection);
            this._connections.remove(connection);
            if (this._exchanges.isEmpty()) {
                remove = this._client.isRemoveIdleDestinations() && (this._cookies == null || this._cookies.isEmpty()) && this._connections.isEmpty() && this._idleConnections.isEmpty();
            } else if (this._client.isStarted()) {
                startConnection = true;
            }
        }
        if (startConnection) {
            startNewConnection();
        }
        if (remove) {
            this._client.removeDestination(this);
        }
    }

    public void send(HttpExchange ex) throws IOException {
        ex.setStatus(1);
        LinkedList<String> listeners = this._client.getRegisteredListeners();
        if (listeners != null) {
            for (int i = listeners.size(); i > 0; i--) {
                String listenerClass = listeners.get(i - 1);
                try {
                    Class<?> listener = Class.forName(listenerClass);
                    Constructor constructor = listener.getDeclaredConstructor(HttpDestination.class, HttpExchange.class);
                    HttpEventListener elistener = (HttpEventListener) constructor.newInstance(this, ex);
                    ex.setEventListener(elistener);
                } catch (Exception e) {
                    throw new IOException("Unable to instantiate registered listener for destination: " + listenerClass) { // from class: org.eclipse.jetty.client.HttpDestination.1
                        {
                            initCause(e);
                        }
                    };
                }
            }
        }
        if (this._client.hasRealms()) {
            ex.setEventListener(new SecurityListener(this, ex));
        }
        doSend(ex);
    }

    public void resend(HttpExchange ex) throws IOException {
        ex.getEventListener().onRetry();
        ex.reset();
        doSend(ex);
    }

    protected void doSend(HttpExchange ex) throws IOException {
        Authentication auth;
        synchronized (this) {
            if (this._cookies != null) {
                StringBuilder buf = null;
                for (HttpCookie cookie : this._cookies) {
                    if (buf == null) {
                        buf = new StringBuilder();
                    } else {
                        buf.append("; ");
                    }
                    buf.append(cookie.getName());
                    buf.append("=");
                    buf.append(cookie.getValue());
                }
                if (buf != null) {
                    ex.addRequestHeader(HttpHeaders.COOKIE, buf.toString());
                }
            }
        }
        if (this._authorizations != null && (auth = (Authentication) this._authorizations.match(ex.getRequestURI())) != null) {
            auth.setCredentials(ex);
        }
        ex.scheduleTimeout(this);
        AbstractHttpConnection connection = getIdleConnection();
        if (connection != null) {
            send(connection, ex);
            return;
        }
        boolean startConnection = false;
        synchronized (this) {
            if (this._exchanges.size() == this._maxQueueSize) {
                throw new RejectedExecutionException("Queue full for address " + this._address);
            }
            this._exchanges.add(ex);
            if (this._connections.size() + this._pendingConnections < this._maxConnections) {
                startConnection = true;
            }
        }
        if (startConnection) {
            startNewConnection();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void exchangeExpired(HttpExchange exchange) {
        synchronized (this) {
            this._exchanges.remove(exchange);
        }
    }

    protected void send(AbstractHttpConnection connection, HttpExchange exchange) throws IOException {
        synchronized (this) {
            if (!connection.send(exchange)) {
                if (exchange.getStatus() <= 1) {
                    this._exchanges.add(0, exchange);
                }
                returnIdleConnection(connection);
            }
        }
    }

    public synchronized String toString() {
        return String.format("HttpDestination@%x//%s:%d(%d/%d,%d,%d/%d)%n", Integer.valueOf(hashCode()), this._address.getHost(), Integer.valueOf(this._address.getPort()), Integer.valueOf(this._connections.size()), Integer.valueOf(this._maxConnections), Integer.valueOf(this._idleConnections.size()), Integer.valueOf(this._exchanges.size()), Integer.valueOf(this._maxQueueSize));
    }

    public synchronized String toDetailString() {
        StringBuilder b;
        b = new StringBuilder();
        b.append(toString());
        b.append('\n');
        synchronized (this) {
            for (AbstractHttpConnection connection : this._connections) {
                b.append(connection.toDetailString());
                if (this._idleConnections.contains(connection)) {
                    b.append(" IDLE");
                }
                b.append('\n');
            }
        }
        return b.toString();
        b.append("--");
        b.append('\n');
        return b.toString();
    }

    public void setProxy(Address proxy) {
        this._proxy = proxy;
    }

    public Address getProxy() {
        return this._proxy;
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

    public void close() throws IOException {
        synchronized (this) {
            for (AbstractHttpConnection connection : this._connections) {
                connection.close();
            }
        }
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public String dump() {
        return AggregateLifeCycle.dump(this);
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        synchronized (this) {
            out.append(String.valueOf(this));
            out.append("idle=");
            out.append(String.valueOf(this._idleConnections.size()));
            out.append(" pending=");
            out.append(String.valueOf(this._pendingConnections));
            out.append("\n");
            AggregateLifeCycle.dump(out, indent, this._connections);
        }
    }

    /* loaded from: classes.dex */
    private class ConnectExchange extends ContentExchange {
        private final SelectConnector.UpgradableEndPoint proxyEndPoint;

        public ConnectExchange(Address serverAddress, SelectConnector.UpgradableEndPoint proxyEndPoint) {
            this.proxyEndPoint = proxyEndPoint;
            setMethod(HttpMethods.CONNECT);
            String serverHostAndPort = serverAddress.toString();
            setRequestURI(serverHostAndPort);
            addRequestHeader(HttpHeaders.HOST, serverHostAndPort);
            addRequestHeader(HttpHeaders.PROXY_CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            addRequestHeader(HttpHeaders.USER_AGENT, "Jetty-Client");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.client.HttpExchange
        public void onResponseComplete() throws IOException {
            int responseStatus = getResponseStatus();
            if (responseStatus == 200) {
                this.proxyEndPoint.upgrade();
            } else if (responseStatus == 504) {
                onExpire();
            } else {
                onException(new ProtocolException("Proxy: " + this.proxyEndPoint.getRemoteAddr() + ":" + this.proxyEndPoint.getRemotePort() + " didn't return http return code 200, but " + responseStatus));
            }
        }

        @Override // org.eclipse.jetty.client.HttpExchange
        protected void onConnectionFailed(Throwable x) {
            HttpDestination.this.onConnectionFailed(x);
        }

        @Override // org.eclipse.jetty.client.HttpExchange
        protected void onException(Throwable x) {
            HttpExchange exchange = null;
            synchronized (HttpDestination.this) {
                if (!HttpDestination.this._exchanges.isEmpty()) {
                    exchange = (HttpExchange) HttpDestination.this._exchanges.remove(0);
                }
            }
            if (exchange != null && exchange.setStatus(9)) {
                exchange.getEventListener().onException(x);
            }
        }

        @Override // org.eclipse.jetty.client.HttpExchange
        protected void onExpire() {
            HttpExchange exchange = null;
            synchronized (HttpDestination.this) {
                if (!HttpDestination.this._exchanges.isEmpty()) {
                    exchange = (HttpExchange) HttpDestination.this._exchanges.remove(0);
                }
            }
            if (exchange != null && exchange.setStatus(8)) {
                exchange.getEventListener().onExpire();
            }
        }
    }
}
