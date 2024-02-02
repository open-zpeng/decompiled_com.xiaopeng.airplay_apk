package org.eclipse.jetty.client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLEngine;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.nio.AsyncConnection;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SelectorManager;
import org.eclipse.jetty.io.nio.SslConnection;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.Timeout;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SelectConnector extends AggregateLifeCycle implements HttpClient.Connector, Dumpable {
    private static final Logger LOG = Log.getLogger(SelectConnector.class);
    private final HttpClient _httpClient;
    private final Manager _selectorManager = new Manager();
    private final Map<SocketChannel, Timeout.Task> _connectingChannels = new ConcurrentHashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    public SelectConnector(HttpClient httpClient) {
        this._httpClient = httpClient;
        addBean(this._httpClient, false);
        addBean(this._selectorManager, true);
    }

    @Override // org.eclipse.jetty.client.HttpClient.Connector
    public void startConnection(HttpDestination destination) throws IOException {
        SocketChannel channel = null;
        try {
            SocketChannel channel2 = SocketChannel.open();
            Address address = destination.isProxied() ? destination.getProxy() : destination.getAddress();
            channel2.socket().setTcpNoDelay(true);
            if (this._httpClient.isConnectBlocking()) {
                channel2.socket().connect(address.toSocketAddress(), this._httpClient.getConnectTimeout());
                channel2.configureBlocking(false);
                this._selectorManager.register(channel2, destination);
                return;
            }
            channel2.configureBlocking(false);
            channel2.connect(address.toSocketAddress());
            this._selectorManager.register(channel2, destination);
            ConnectTimeout connectTimeout = new ConnectTimeout(channel2, destination);
            this._httpClient.schedule(connectTimeout, this._httpClient.getConnectTimeout());
            this._connectingChannels.put(channel2, connectTimeout);
        } catch (IOException ex) {
            if (0 != 0) {
                channel.close();
            }
            destination.onConnectionFailed(ex);
        } catch (UnresolvedAddressException ex2) {
            if (0 != 0) {
                channel.close();
            }
            destination.onConnectionFailed(ex2);
        }
    }

    /* loaded from: classes.dex */
    class Manager extends SelectorManager {
        Logger LOG = SelectConnector.LOG;

        Manager() {
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public boolean dispatch(Runnable task) {
            return SelectConnector.this._httpClient._threadPool.dispatch(task);
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointOpened(SelectChannelEndPoint endpoint) {
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointClosed(SelectChannelEndPoint endpoint) {
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void endPointUpgraded(ConnectedEndPoint endpoint, Connection oldConnection) {
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        public AsyncConnection newConnection(SocketChannel channel, AsyncEndPoint endpoint, Object attachment) {
            return new AsyncHttpConnection(SelectConnector.this._httpClient.getRequestBuffers(), SelectConnector.this._httpClient.getResponseBuffers(), endpoint);
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key) throws IOException {
            Timeout.Task connectTimeout = (Timeout.Task) SelectConnector.this._connectingChannels.remove(channel);
            if (connectTimeout != null) {
                connectTimeout.cancel();
            }
            if (this.LOG.isDebugEnabled()) {
                this.LOG.debug("Channels with connection pending: {}", Integer.valueOf(SelectConnector.this._connectingChannels.size()));
            }
            HttpDestination dest = (HttpDestination) key.attachment();
            SelectChannelEndPoint scep = new SelectChannelEndPoint(channel, selectSet, key, (int) SelectConnector.this._httpClient.getIdleTimeout());
            AsyncEndPoint ep = scep;
            if (dest.isSecure()) {
                this.LOG.debug("secure to {}, proxied={}", channel, Boolean.valueOf(dest.isProxied()));
                ep = new UpgradableEndPoint(ep, newSslEngine(dest.getSslContextFactory(), channel));
            }
            AsyncConnection connection = selectSet.getManager().newConnection(channel, ep, key.attachment());
            ep.setConnection(connection);
            AbstractHttpConnection httpConnection = (AbstractHttpConnection) connection;
            httpConnection.setDestination(dest);
            if (dest.isSecure() && !dest.isProxied()) {
                ((UpgradableEndPoint) ep).upgrade();
            }
            dest.onNewConnection(httpConnection);
            return scep;
        }

        private synchronized SSLEngine newSslEngine(SslContextFactory sslContextFactory, SocketChannel channel) throws IOException {
            SSLEngine sslEngine;
            try {
                if (channel != null) {
                    String peerHost = channel.socket().getInetAddress().getHostAddress();
                    int peerPort = channel.socket().getPort();
                    sslEngine = sslContextFactory.newSslEngine(peerHost, peerPort);
                } else {
                    sslEngine = sslContextFactory.newSslEngine();
                }
                sslEngine.setUseClientMode(true);
                sslEngine.beginHandshake();
            } catch (Throwable th) {
                throw th;
            }
            return sslEngine;
        }

        @Override // org.eclipse.jetty.io.nio.SelectorManager
        protected void connectionFailed(SocketChannel channel, Throwable ex, Object attachment) {
            Timeout.Task connectTimeout = (Timeout.Task) SelectConnector.this._connectingChannels.remove(channel);
            if (connectTimeout != null) {
                connectTimeout.cancel();
            }
            if (attachment instanceof HttpDestination) {
                ((HttpDestination) attachment).onConnectionFailed(ex);
            } else {
                super.connectionFailed(channel, ex, attachment);
            }
        }
    }

    /* loaded from: classes.dex */
    private class ConnectTimeout extends Timeout.Task {
        private final SocketChannel channel;
        private final HttpDestination destination;

        public ConnectTimeout(SocketChannel channel, HttpDestination destination) {
            this.channel = channel;
            this.destination = destination;
        }

        @Override // org.eclipse.jetty.util.thread.Timeout.Task
        public void expired() {
            if (this.channel.isConnectionPending()) {
                SelectConnector.LOG.debug("Channel {} timed out while connecting, closing it", this.channel);
                close();
                SelectConnector.this._connectingChannels.remove(this.channel);
                this.destination.onConnectionFailed(new SocketTimeoutException());
            }
        }

        private void close() {
            try {
                this.channel.close();
            } catch (IOException x) {
                SelectConnector.LOG.ignore(x);
            }
        }
    }

    /* loaded from: classes.dex */
    public static class UpgradableEndPoint implements AsyncEndPoint {
        AsyncEndPoint _endp;
        SSLEngine _engine;

        public UpgradableEndPoint(AsyncEndPoint endp, SSLEngine engine) throws IOException {
            this._engine = engine;
            this._endp = endp;
        }

        public void upgrade() {
            AsyncHttpConnection connection = (AsyncHttpConnection) this._endp.getConnection();
            SslConnection sslConnection = new SslConnection(this._engine, this._endp);
            this._endp.setConnection(sslConnection);
            this._endp = sslConnection.getSslEndPoint();
            sslConnection.getSslEndPoint().setConnection(connection);
            SelectConnector.LOG.debug("upgrade {} to {} for {}", this, sslConnection, connection);
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public Connection getConnection() {
            return this._endp.getConnection();
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public void setConnection(Connection connection) {
            this._endp.setConnection(connection);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void shutdownOutput() throws IOException {
            this._endp.shutdownOutput();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void dispatch() {
            this._endp.asyncDispatch();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void asyncDispatch() {
            this._endp.asyncDispatch();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isOutputShutdown() {
            return this._endp.isOutputShutdown();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void shutdownInput() throws IOException {
            this._endp.shutdownInput();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void scheduleWrite() {
            this._endp.scheduleWrite();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isInputShutdown() {
            return this._endp.isInputShutdown();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void close() throws IOException {
            this._endp.close();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int fill(Buffer buffer) throws IOException {
            return this._endp.fill(buffer);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public boolean isWritable() {
            return this._endp.isWritable();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public boolean hasProgressed() {
            return this._endp.hasProgressed();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int flush(Buffer buffer) throws IOException {
            return this._endp.flush(buffer);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void scheduleTimeout(Timeout.Task task, long timeoutMs) {
            this._endp.scheduleTimeout(task, timeoutMs);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void cancelTimeout(Timeout.Task task) {
            this._endp.cancelTimeout(task);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
            return this._endp.flush(header, buffer, trailer);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getLocalAddr() {
            return this._endp.getLocalAddr();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getLocalHost() {
            return this._endp.getLocalHost();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int getLocalPort() {
            return this._endp.getLocalPort();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getRemoteAddr() {
            return this._endp.getRemoteAddr();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getRemoteHost() {
            return this._endp.getRemoteHost();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int getRemotePort() {
            return this._endp.getRemotePort();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isBlocking() {
            return this._endp.isBlocking();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean blockReadable(long millisecs) throws IOException {
            return this._endp.blockReadable(millisecs);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean blockWritable(long millisecs) throws IOException {
            return this._endp.blockWritable(millisecs);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isOpen() {
            return this._endp.isOpen();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public Object getTransport() {
            return this._endp.getTransport();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void flush() throws IOException {
            this._endp.flush();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int getMaxIdleTime() {
            return this._endp.getMaxIdleTime();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void setMaxIdleTime(int timeMs) throws IOException {
            this._endp.setMaxIdleTime(timeMs);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void onIdleExpired(long idleForMs) {
            this._endp.onIdleExpired(idleForMs);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void setCheckForIdle(boolean check) {
            this._endp.setCheckForIdle(check);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public boolean isCheckForIdle() {
            return this._endp.isCheckForIdle();
        }

        public String toString() {
            return "Upgradable:" + this._endp.toString();
        }
    }
}
