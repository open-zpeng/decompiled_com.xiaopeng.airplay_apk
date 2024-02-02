package org.eclipse.jetty.server.bio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.bio.SocketEndPoint;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.BlockingHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class SocketConnector extends AbstractConnector {
    private static final Logger LOG = Log.getLogger(SocketConnector.class);
    protected ServerSocket _serverSocket;
    protected volatile int _localPort = -1;
    protected final Set<EndPoint> _connections = new HashSet();

    @Override // org.eclipse.jetty.server.Connector
    public Object getConnection() {
        return this._serverSocket;
    }

    @Override // org.eclipse.jetty.server.Connector
    public void open() throws IOException {
        if (this._serverSocket == null || this._serverSocket.isClosed()) {
            this._serverSocket = newServerSocket(getHost(), getPort(), getAcceptQueueSize());
        }
        this._serverSocket.setReuseAddress(getReuseAddress());
        this._localPort = this._serverSocket.getLocalPort();
        if (this._localPort <= 0) {
            throw new IllegalStateException("port not allocated for " + this);
        }
    }

    protected ServerSocket newServerSocket(String host, int port, int backlog) throws IOException {
        if (host == null) {
            ServerSocket ss = new ServerSocket(port, backlog);
            return ss;
        }
        ServerSocket ss2 = new ServerSocket(port, backlog, InetAddress.getByName(host));
        return ss2;
    }

    @Override // org.eclipse.jetty.server.Connector
    public void close() throws IOException {
        if (this._serverSocket != null) {
            this._serverSocket.close();
        }
        this._serverSocket = null;
        this._localPort = -2;
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    public void accept(int acceptorID) throws IOException, InterruptedException {
        Socket socket = this._serverSocket.accept();
        configure(socket);
        ConnectorEndPoint connection = new ConnectorEndPoint(socket);
        connection.dispatch();
    }

    protected Connection newConnection(EndPoint endpoint) {
        return new BlockingHttpConnection(this, endpoint, getServer());
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void customize(EndPoint endpoint, Request request) throws IOException {
        ConnectorEndPoint connection = (ConnectorEndPoint) endpoint;
        int lrmit = isLowResources() ? this._lowResourceMaxIdleTime : this._maxIdleTime;
        connection.setMaxIdleTime(lrmit);
        super.customize(endpoint, request);
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getLocalPort() {
        return this._localPort;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._connections.clear();
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        Set<EndPoint> set = new HashSet<>();
        synchronized (this._connections) {
            set.addAll(this._connections);
        }
        for (EndPoint endPoint : set) {
            ConnectorEndPoint connection = (ConnectorEndPoint) endPoint;
            connection.close();
        }
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        super.dump(out, indent);
        Set<EndPoint> connections = new HashSet<>();
        synchronized (this._connections) {
            connections.addAll(this._connections);
        }
        AggregateLifeCycle.dump(out, indent, connections);
    }

    /* loaded from: classes.dex */
    protected class ConnectorEndPoint extends SocketEndPoint implements Runnable, ConnectedEndPoint {
        volatile Connection _connection;
        protected final Socket _socket;

        public ConnectorEndPoint(Socket socket) throws IOException {
            super(socket, SocketConnector.this._maxIdleTime);
            this._connection = SocketConnector.this.newConnection(this);
            this._socket = socket;
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public Connection getConnection() {
            return this._connection;
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public void setConnection(Connection connection) {
            if (this._connection != connection && this._connection != null) {
                SocketConnector.this.connectionUpgraded(this._connection, connection);
            }
            this._connection = connection;
        }

        public void dispatch() throws IOException {
            if (SocketConnector.this.getThreadPool() == null || !SocketConnector.this.getThreadPool().dispatch(this)) {
                SocketConnector.LOG.warn("dispatch failed for {}", this._connection);
                close();
            }
        }

        @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
        public int fill(Buffer buffer) throws IOException {
            int l = super.fill(buffer);
            if (l < 0) {
                if (!isInputShutdown()) {
                    shutdownInput();
                }
                if (isOutputShutdown()) {
                    close();
                }
            }
            return l;
        }

        @Override // org.eclipse.jetty.io.bio.SocketEndPoint, org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
        public void close() throws IOException {
            if (this._connection instanceof AbstractHttpConnection) {
                ((AbstractHttpConnection) this._connection).getRequest().getAsyncContinuation().cancel();
            }
            super.close();
        }

        /* JADX WARN: Incorrect condition in loop: B:103:0x01cb */
        /* JADX WARN: Incorrect condition in loop: B:127:0x0237 */
        /* JADX WARN: Incorrect condition in loop: B:148:0x0297 */
        /* JADX WARN: Incorrect condition in loop: B:26:0x007e */
        /* JADX WARN: Incorrect condition in loop: B:55:0x00f1 */
        /* JADX WARN: Incorrect condition in loop: B:79:0x015e */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void run() {
            /*
                Method dump skipped, instructions count: 701
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint.run():void");
        }
    }
}
