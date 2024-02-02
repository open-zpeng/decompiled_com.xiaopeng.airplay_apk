package org.eclipse.jetty.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.ChannelEndPoint;
import org.eclipse.jetty.server.BlockingHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class BlockingChannelConnector extends AbstractNIOConnector {
    private static final Logger LOG = Log.getLogger(BlockingChannelConnector.class);
    private transient ServerSocketChannel _acceptChannel;
    private final Set<BlockingChannelEndPoint> _endpoints = new ConcurrentHashSet();

    @Override // org.eclipse.jetty.server.Connector
    public Object getConnection() {
        return this._acceptChannel;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        getThreadPool().dispatch(new Runnable() { // from class: org.eclipse.jetty.server.nio.BlockingChannelConnector.1
            @Override // java.lang.Runnable
            public void run() {
                while (BlockingChannelConnector.this.isRunning()) {
                    try {
                        Thread.sleep(400L);
                        long now = System.currentTimeMillis();
                        for (BlockingChannelEndPoint endp : BlockingChannelConnector.this._endpoints) {
                            endp.checkIdleTimestamp(now);
                        }
                    } catch (InterruptedException e) {
                        BlockingChannelConnector.LOG.ignore(e);
                    } catch (Exception e2) {
                        BlockingChannelConnector.LOG.warn(e2);
                    }
                }
            }
        });
    }

    @Override // org.eclipse.jetty.server.Connector
    public void open() throws IOException {
        this._acceptChannel = ServerSocketChannel.open();
        this._acceptChannel.configureBlocking(true);
        InetSocketAddress addr = getHost() == null ? new InetSocketAddress(getPort()) : new InetSocketAddress(getHost(), getPort());
        this._acceptChannel.socket().bind(addr, getAcceptQueueSize());
    }

    @Override // org.eclipse.jetty.server.Connector
    public void close() throws IOException {
        if (this._acceptChannel != null) {
            this._acceptChannel.close();
        }
        this._acceptChannel = null;
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    public void accept(int acceptorID) throws IOException, InterruptedException {
        SocketChannel channel = this._acceptChannel.accept();
        channel.configureBlocking(true);
        Socket socket = channel.socket();
        configure(socket);
        BlockingChannelEndPoint connection = new BlockingChannelEndPoint(channel);
        connection.dispatch();
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void customize(EndPoint endpoint, Request request) throws IOException {
        super.customize(endpoint, request);
        endpoint.setMaxIdleTime(this._maxIdleTime);
        configure(((SocketChannel) endpoint.getTransport()).socket());
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getLocalPort() {
        if (this._acceptChannel == null || !this._acceptChannel.isOpen()) {
            return -1;
        }
        return this._acceptChannel.socket().getLocalPort();
    }

    /* loaded from: classes.dex */
    private class BlockingChannelEndPoint extends ChannelEndPoint implements Runnable, ConnectedEndPoint {
        private Connection _connection;
        private volatile long _idleTimestamp;
        private int _timeout;

        BlockingChannelEndPoint(ByteChannel channel) throws IOException {
            super(channel, BlockingChannelConnector.this._maxIdleTime);
            this._connection = new BlockingHttpConnection(BlockingChannelConnector.this, this, BlockingChannelConnector.this.getServer());
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public Connection getConnection() {
            return this._connection;
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public void setConnection(Connection connection) {
            this._connection = connection;
        }

        public void checkIdleTimestamp(long now) {
            if (this._idleTimestamp != 0 && this._timeout > 0 && now > this._idleTimestamp + this._timeout) {
                idleExpired();
            }
        }

        protected void idleExpired() {
            try {
                super.close();
            } catch (IOException e) {
                BlockingChannelConnector.LOG.ignore(e);
            }
        }

        void dispatch() throws IOException {
            if (!BlockingChannelConnector.this.getThreadPool().dispatch(this)) {
                BlockingChannelConnector.LOG.warn("dispatch failed for  {}", this._connection);
                super.close();
            }
        }

        @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
        public int fill(Buffer buffer) throws IOException {
            this._idleTimestamp = System.currentTimeMillis();
            return super.fill(buffer);
        }

        @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
        public int flush(Buffer buffer) throws IOException {
            this._idleTimestamp = System.currentTimeMillis();
            return super.flush(buffer);
        }

        @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
        public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
            this._idleTimestamp = System.currentTimeMillis();
            return super.flush(header, buffer, trailer);
        }

        /* JADX WARN: Incorrect condition in loop: B:26:0x0096 */
        /* JADX WARN: Incorrect condition in loop: B:46:0x00ff */
        /* JADX WARN: Incorrect condition in loop: B:64:0x0165 */
        /* JADX WARN: Incorrect condition in loop: B:82:0x01ca */
        /* JADX WARN: Incorrect condition in loop: B:97:0x0223 */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void run() {
            /*
                Method dump skipped, instructions count: 582
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.nio.BlockingChannelConnector.BlockingChannelEndPoint.run():void");
        }

        public String toString() {
            return String.format("BCEP@%x{l(%s)<->r(%s),open=%b,ishut=%b,oshut=%b}-{%s}", Integer.valueOf(hashCode()), this._socket.getRemoteSocketAddress(), this._socket.getLocalSocketAddress(), Boolean.valueOf(isOpen()), Boolean.valueOf(isInputShutdown()), Boolean.valueOf(isOutputShutdown()), this._connection);
        }
    }
}
