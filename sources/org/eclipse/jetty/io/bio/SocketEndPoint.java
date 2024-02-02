package org.eclipse.jetty.io.bio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class SocketEndPoint extends StreamEndPoint {
    private static final Logger LOG = Log.getLogger(SocketEndPoint.class);
    final InetSocketAddress _local;
    final InetSocketAddress _remote;
    final Socket _socket;

    public SocketEndPoint(Socket socket) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream());
        this._socket = socket;
        this._local = (InetSocketAddress) this._socket.getLocalSocketAddress();
        this._remote = (InetSocketAddress) this._socket.getRemoteSocketAddress();
        super.setMaxIdleTime(this._socket.getSoTimeout());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public SocketEndPoint(Socket socket, int maxIdleTime) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream());
        this._socket = socket;
        this._local = (InetSocketAddress) this._socket.getLocalSocketAddress();
        this._remote = (InetSocketAddress) this._socket.getRemoteSocketAddress();
        this._socket.setSoTimeout(maxIdleTime > 0 ? maxIdleTime : 0);
        super.setMaxIdleTime(maxIdleTime);
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public boolean isOpen() {
        return (!super.isOpen() || this._socket == null || this._socket.isClosed()) ? false : true;
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public boolean isInputShutdown() {
        if (this._socket instanceof SSLSocket) {
            return super.isInputShutdown();
        }
        return this._socket.isClosed() || this._socket.isInputShutdown();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public boolean isOutputShutdown() {
        if (this._socket instanceof SSLSocket) {
            return super.isOutputShutdown();
        }
        return this._socket.isClosed() || this._socket.isOutputShutdown();
    }

    protected final void shutdownSocketOutput() throws IOException {
        if (!this._socket.isClosed()) {
            if (!this._socket.isOutputShutdown()) {
                this._socket.shutdownOutput();
            }
            if (this._socket.isInputShutdown()) {
                this._socket.close();
            }
        }
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public void shutdownOutput() throws IOException {
        if (this._socket instanceof SSLSocket) {
            super.shutdownOutput();
        } else {
            shutdownSocketOutput();
        }
    }

    public void shutdownSocketInput() throws IOException {
        if (!this._socket.isClosed()) {
            if (!this._socket.isInputShutdown()) {
                this._socket.shutdownInput();
            }
            if (this._socket.isOutputShutdown()) {
                this._socket.close();
            }
        }
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public void shutdownInput() throws IOException {
        if (this._socket instanceof SSLSocket) {
            super.shutdownInput();
        } else {
            shutdownSocketInput();
        }
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public void close() throws IOException {
        this._socket.close();
        this._in = null;
        this._out = null;
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public String getLocalAddr() {
        if (this._local == null || this._local.getAddress() == null || this._local.getAddress().isAnyLocalAddress()) {
            return StringUtil.ALL_INTERFACES;
        }
        return this._local.getAddress().getHostAddress();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public String getLocalHost() {
        if (this._local == null || this._local.getAddress() == null || this._local.getAddress().isAnyLocalAddress()) {
            return StringUtil.ALL_INTERFACES;
        }
        return this._local.getAddress().getCanonicalHostName();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public int getLocalPort() {
        if (this._local == null) {
            return -1;
        }
        return this._local.getPort();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public String getRemoteAddr() {
        InetAddress addr;
        if (this._remote == null || (addr = this._remote.getAddress()) == null) {
            return null;
        }
        return addr.getHostAddress();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public String getRemoteHost() {
        if (this._remote == null) {
            return null;
        }
        return this._remote.getAddress().getCanonicalHostName();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public int getRemotePort() {
        if (this._remote == null) {
            return -1;
        }
        return this._remote.getPort();
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public Object getTransport() {
        return this._socket;
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
    public void setMaxIdleTime(int timeMs) throws IOException {
        if (timeMs != getMaxIdleTime()) {
            this._socket.setSoTimeout(timeMs > 0 ? timeMs : 0);
        }
        super.setMaxIdleTime(timeMs);
    }

    @Override // org.eclipse.jetty.io.bio.StreamEndPoint
    protected void idleExpired() throws IOException {
        try {
            if (!isInputShutdown()) {
                shutdownInput();
            }
        } catch (IOException e) {
            LOG.ignore(e);
            this._socket.close();
        }
    }

    public String toString() {
        return this._local + " <--> " + this._remote;
    }
}
