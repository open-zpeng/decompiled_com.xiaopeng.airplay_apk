package org.eclipse.jetty.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.AsyncConnection;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SelectorManager;
import org.eclipse.jetty.server.AsyncHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.thread.ThreadPool;
/* loaded from: classes.dex */
public class SelectChannelConnector extends AbstractNIOConnector {
    protected ServerSocketChannel _acceptChannel;
    private int _lowResourcesConnections;
    private int _lowResourcesMaxIdleTime;
    private int _localPort = -1;
    private final SelectorManager _manager = new ConnectorSelectorManager();

    public SelectChannelConnector() {
        this._manager.setMaxIdleTime(getMaxIdleTime());
        addBean(this._manager, true);
        setAcceptors(Math.max(1, (Runtime.getRuntime().availableProcessors() + 3) / 4));
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    public void setThreadPool(ThreadPool pool) {
        super.setThreadPool(pool);
        removeBean(this._manager);
        addBean(this._manager, true);
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    public void accept(int acceptorID) throws IOException {
        synchronized (this) {
            try {
                try {
                    ServerSocketChannel server = this._acceptChannel;
                    if (server != null && server.isOpen() && this._manager.isStarted()) {
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        Socket socket = channel.socket();
                        configure(socket);
                        this._manager.register(channel);
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

    @Override // org.eclipse.jetty.server.Connector
    public void close() throws IOException {
        synchronized (this) {
            if (this._acceptChannel != null) {
                removeBean(this._acceptChannel);
                if (this._acceptChannel.isOpen()) {
                    this._acceptChannel.close();
                }
            }
            this._acceptChannel = null;
            this._localPort = -2;
        }
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void customize(EndPoint endpoint, Request request) throws IOException {
        request.setTimeStamp(System.currentTimeMillis());
        endpoint.setMaxIdleTime(this._maxIdleTime);
        super.customize(endpoint, request);
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void persist(EndPoint endpoint) throws IOException {
        AsyncEndPoint aEndp = (AsyncEndPoint) endpoint;
        aEndp.setCheckForIdle(true);
        super.persist(endpoint);
    }

    public SelectorManager getSelectorManager() {
        return this._manager;
    }

    @Override // org.eclipse.jetty.server.Connector
    public synchronized Object getConnection() {
        return this._acceptChannel;
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getLocalPort() {
        int i;
        synchronized (this) {
            i = this._localPort;
        }
        return i;
    }

    public void open() throws IOException {
        synchronized (this) {
            if (this._acceptChannel == null) {
                this._acceptChannel = ServerSocketChannel.open();
                this._acceptChannel.configureBlocking(true);
                this._acceptChannel.socket().setReuseAddress(getReuseAddress());
                InetSocketAddress addr = getHost() == null ? new InetSocketAddress(getPort()) : new InetSocketAddress(getHost(), getPort());
                this._acceptChannel.socket().bind(addr, getAcceptQueueSize());
                this._localPort = this._acceptChannel.socket().getLocalPort();
                if (this._localPort <= 0) {
                    throw new IOException("Server channel not bound");
                }
                addBean(this._acceptChannel);
            }
        }
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void setMaxIdleTime(int maxIdleTime) {
        this._manager.setMaxIdleTime(maxIdleTime);
        super.setMaxIdleTime(maxIdleTime);
    }

    public int getLowResourcesConnections() {
        return this._lowResourcesConnections;
    }

    public void setLowResourcesConnections(int lowResourcesConnections) {
        this._lowResourcesConnections = lowResourcesConnections;
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    public int getLowResourcesMaxIdleTime() {
        return this._lowResourcesMaxIdleTime;
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    public void setLowResourcesMaxIdleTime(int lowResourcesMaxIdleTime) {
        this._lowResourcesMaxIdleTime = lowResourcesMaxIdleTime;
        super.setLowResourcesMaxIdleTime(lowResourcesMaxIdleTime);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._manager.setSelectSets(getAcceptors());
        this._manager.setMaxIdleTime(getMaxIdleTime());
        this._manager.setLowResourcesConnections(getLowResourcesConnections());
        this._manager.setLowResourcesMaxIdleTime(getLowResourcesMaxIdleTime());
        super.doStart();
    }

    protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key) throws IOException {
        SelectChannelEndPoint endp = new SelectChannelEndPoint(channel, selectSet, key, this._maxIdleTime);
        endp.setConnection(selectSet.getManager().newConnection(channel, endp, key.attachment()));
        return endp;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void endPointClosed(SelectChannelEndPoint endpoint) {
        connectionClosed(endpoint.getConnection());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AsyncConnection newConnection(SocketChannel channel, AsyncEndPoint endpoint) {
        return new AsyncHttpConnection(this, endpoint, getServer());
    }

    /* loaded from: classes.dex */
    private final class ConnectorSelectorManager extends SelectorManager {
        private ConnectorSelectorManager() {
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public boolean dispatch(Runnable task) {
            ThreadPool pool = SelectChannelConnector.this.getThreadPool();
            if (pool == null) {
                pool = SelectChannelConnector.this.getServer().getThreadPool();
            }
            return pool.dispatch(task);
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointClosed(SelectChannelEndPoint endpoint) {
            SelectChannelConnector.this.endPointClosed(endpoint);
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointOpened(SelectChannelEndPoint endpoint) {
            SelectChannelConnector.this.connectionOpened(endpoint.getConnection());
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public void endPointUpgraded(ConnectedEndPoint endpoint, Connection oldConnection) {
            SelectChannelConnector.this.connectionUpgraded(oldConnection, endpoint.getConnection());
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public AsyncConnection newConnection(SocketChannel channel, AsyncEndPoint endpoint, Object attachment) {
            return SelectChannelConnector.this.newConnection(channel, endpoint);
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey sKey) throws IOException {
            return SelectChannelConnector.this.newEndPoint(channel, selectSet, sKey);
        }
    }
}
