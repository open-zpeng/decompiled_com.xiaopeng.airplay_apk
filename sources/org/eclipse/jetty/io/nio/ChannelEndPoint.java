package org.eclipse.jetty.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ChannelEndPoint implements EndPoint {
    private static final Logger LOG = Log.getLogger(ChannelEndPoint.class);
    protected final ByteChannel _channel;
    protected final ByteBuffer[] _gather2;
    private volatile boolean _ishut;
    protected final InetSocketAddress _local;
    protected volatile int _maxIdleTime;
    private volatile boolean _oshut;
    protected final InetSocketAddress _remote;
    protected final Socket _socket;

    public ChannelEndPoint(ByteChannel channel) throws IOException {
        this._gather2 = new ByteBuffer[2];
        this._channel = channel;
        this._socket = channel instanceof SocketChannel ? ((SocketChannel) channel).socket() : null;
        if (this._socket != null) {
            this._local = (InetSocketAddress) this._socket.getLocalSocketAddress();
            this._remote = (InetSocketAddress) this._socket.getRemoteSocketAddress();
            this._maxIdleTime = this._socket.getSoTimeout();
            return;
        }
        this._remote = null;
        this._local = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ChannelEndPoint(ByteChannel channel, int maxIdleTime) throws IOException {
        this._gather2 = new ByteBuffer[2];
        this._channel = channel;
        this._maxIdleTime = maxIdleTime;
        this._socket = channel instanceof SocketChannel ? ((SocketChannel) channel).socket() : null;
        if (this._socket != null) {
            this._local = (InetSocketAddress) this._socket.getLocalSocketAddress();
            this._remote = (InetSocketAddress) this._socket.getRemoteSocketAddress();
            this._socket.setSoTimeout(this._maxIdleTime);
            return;
        }
        this._remote = null;
        this._local = null;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isBlocking() {
        return !(this._channel instanceof SelectableChannel) || ((SelectableChannel) this._channel).isBlocking();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean blockReadable(long millisecs) throws IOException {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean blockWritable(long millisecs) throws IOException {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isOpen() {
        return this._channel.isOpen();
    }

    protected final void shutdownChannelInput() throws IOException {
        LOG.debug("ishut {}", this);
        this._ishut = true;
        if (this._channel.isOpen()) {
            try {
                if (this._socket != null) {
                    try {
                        if (!this._socket.isInputShutdown()) {
                            this._socket.shutdownInput();
                        }
                        if (!this._oshut) {
                            return;
                        }
                    } catch (SocketException e) {
                        LOG.debug(e.toString(), new Object[0]);
                        LOG.ignore(e);
                        if (!this._oshut) {
                            return;
                        }
                    }
                    close();
                }
            } catch (Throwable th) {
                if (this._oshut) {
                    close();
                }
                throw th;
            }
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void shutdownInput() throws IOException {
        shutdownChannelInput();
    }

    protected final void shutdownChannelOutput() throws IOException {
        LOG.debug("oshut {}", this);
        this._oshut = true;
        if (this._channel.isOpen()) {
            try {
                if (this._socket != null) {
                    try {
                        if (!this._socket.isOutputShutdown()) {
                            this._socket.shutdownOutput();
                        }
                        if (!this._ishut) {
                            return;
                        }
                    } catch (SocketException e) {
                        LOG.debug(e.toString(), new Object[0]);
                        LOG.ignore(e);
                        if (!this._ishut) {
                            return;
                        }
                    }
                    close();
                }
            } catch (Throwable th) {
                if (this._ishut) {
                    close();
                }
                throw th;
            }
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void shutdownOutput() throws IOException {
        shutdownChannelOutput();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isOutputShutdown() {
        return this._oshut || !this._channel.isOpen() || (this._socket != null && this._socket.isOutputShutdown());
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isInputShutdown() {
        return this._ishut || !this._channel.isOpen() || (this._socket != null && this._socket.isInputShutdown());
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void close() throws IOException {
        LOG.debug("close {}", this);
        this._channel.close();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int fill(Buffer buffer) throws IOException {
        if (this._ishut) {
            return -1;
        }
        Buffer buf = buffer.buffer();
        int len = 0;
        if (buf instanceof NIOBuffer) {
            NIOBuffer nbuf = (NIOBuffer) buf;
            ByteBuffer bbuf = nbuf.getByteBuffer();
            try {
                synchronized (bbuf) {
                    bbuf.position(buffer.putIndex());
                    len = this._channel.read(bbuf);
                    buffer.setPutIndex(bbuf.position());
                    bbuf.position(0);
                }
                if (len < 0 && isOpen()) {
                    if (!isInputShutdown()) {
                        shutdownInput();
                    }
                    if (isOutputShutdown()) {
                        this._channel.close();
                        return len;
                    }
                    return len;
                }
                return len;
            } catch (IOException x) {
                LOG.debug("Exception while filling", x);
                try {
                    if (this._channel.isOpen()) {
                        this._channel.close();
                    }
                } catch (Exception xx) {
                    LOG.ignore(xx);
                }
                if (len > 0) {
                    throw x;
                }
                return -1;
            }
        }
        throw new IOException("Not Implemented");
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int flush(Buffer buffer) throws IOException {
        int len;
        Buffer buf = buffer.buffer();
        int len2 = 0;
        if (buf instanceof NIOBuffer) {
            NIOBuffer nbuf = (NIOBuffer) buf;
            ByteBuffer bbuf = nbuf.getByteBuffer().asReadOnlyBuffer();
            try {
                bbuf.position(buffer.getIndex());
                bbuf.limit(buffer.putIndex());
                len = this._channel.write(bbuf);
                if (len > 0) {
                    buffer.skip(len);
                }
            } finally {
                if (len2 > 0) {
                    buffer.skip(len2);
                }
            }
        } else if (buf instanceof RandomAccessFileBuffer) {
            len2 = ((RandomAccessFileBuffer) buf).writeTo(this._channel, buffer.getIndex(), buffer.length());
        } else if (buffer.array() != null) {
            ByteBuffer b = ByteBuffer.wrap(buffer.array(), buffer.getIndex(), buffer.length());
            len = this._channel.write(b);
            if (len > 0) {
                buffer.skip(len);
            }
        } else {
            throw new IOException("Not Implemented");
        }
        return len;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
        int length = 0;
        Buffer buf0 = header == null ? null : header.buffer();
        Buffer buf1 = buffer != null ? buffer.buffer() : null;
        if ((this._channel instanceof GatheringByteChannel) && header != null && header.length() != 0 && (buf0 instanceof NIOBuffer) && buffer != null && buffer.length() != 0 && (buf1 instanceof NIOBuffer)) {
            return gatheringFlush(header, ((NIOBuffer) buf0).getByteBuffer(), buffer, ((NIOBuffer) buf1).getByteBuffer());
        }
        if (header != null && header.length() > 0) {
            length = flush(header);
        }
        if ((header == null || header.length() == 0) && buffer != null && buffer.length() > 0) {
            length += flush(buffer);
        }
        if (header == null || header.length() == 0) {
            if ((buffer == null || buffer.length() == 0) && trailer != null && trailer.length() > 0) {
                return length + flush(trailer);
            }
            return length;
        }
        return length;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int gatheringFlush(Buffer header, ByteBuffer bbuf0, Buffer buffer, ByteBuffer bbuf1) throws IOException {
        int length;
        synchronized (this) {
            ByteBuffer bbuf02 = bbuf0.asReadOnlyBuffer();
            bbuf02.position(header.getIndex());
            bbuf02.limit(header.putIndex());
            ByteBuffer bbuf12 = bbuf1.asReadOnlyBuffer();
            bbuf12.position(buffer.getIndex());
            bbuf12.limit(buffer.putIndex());
            this._gather2[0] = bbuf02;
            this._gather2[1] = bbuf12;
            length = (int) ((GatheringByteChannel) this._channel).write(this._gather2);
            int hl = header.length();
            if (length > hl) {
                header.clear();
                buffer.skip(length - hl);
            } else if (length > 0) {
                header.skip(length);
            }
        }
        return length;
    }

    public ByteChannel getChannel() {
        return this._channel;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getLocalAddr() {
        if (this._socket == null) {
            return null;
        }
        if (this._local == null || this._local.getAddress() == null || this._local.getAddress().isAnyLocalAddress()) {
            return StringUtil.ALL_INTERFACES;
        }
        return this._local.getAddress().getHostAddress();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getLocalHost() {
        if (this._socket == null) {
            return null;
        }
        if (this._local == null || this._local.getAddress() == null || this._local.getAddress().isAnyLocalAddress()) {
            return StringUtil.ALL_INTERFACES;
        }
        return this._local.getAddress().getCanonicalHostName();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int getLocalPort() {
        if (this._socket == null) {
            return 0;
        }
        if (this._local == null) {
            return -1;
        }
        return this._local.getPort();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getRemoteAddr() {
        if (this._socket == null || this._remote == null) {
            return null;
        }
        return this._remote.getAddress().getHostAddress();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getRemoteHost() {
        if (this._socket == null || this._remote == null) {
            return null;
        }
        return this._remote.getAddress().getCanonicalHostName();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int getRemotePort() {
        if (this._socket == null) {
            return 0;
        }
        if (this._remote == null) {
            return -1;
        }
        return this._remote.getPort();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public Object getTransport() {
        return this._channel;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void flush() throws IOException {
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int getMaxIdleTime() {
        return this._maxIdleTime;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void setMaxIdleTime(int timeMs) throws IOException {
        if (this._socket != null && timeMs != this._maxIdleTime) {
            this._socket.setSoTimeout(timeMs > 0 ? timeMs : 0);
        }
        this._maxIdleTime = timeMs;
    }
}
