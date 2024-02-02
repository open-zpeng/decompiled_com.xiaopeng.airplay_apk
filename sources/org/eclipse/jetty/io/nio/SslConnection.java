package org.eclipse.jetty.io.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public class SslConnection extends AbstractConnection implements AsyncConnection {
    private static final NIOBuffer __ZERO_BUFFER = new IndirectNIOBuffer(0);
    private static final ThreadLocal<SslBuffers> __buffers = new ThreadLocal<>();
    private AsyncEndPoint _aEndp;
    private int _allocations;
    private boolean _allowRenegotiate;
    private SslBuffers _buffers;
    private AsyncConnection _connection;
    private final SSLEngine _engine;
    private boolean _handshook;
    private NIOBuffer _inbound;
    private boolean _ishut;
    private final Logger _logger;
    private boolean _oshut;
    private NIOBuffer _outbound;
    private final AtomicBoolean _progressed;
    private final SSLSession _session;
    private final SslEndPoint _sslEndPoint;
    private NIOBuffer _unwrapBuf;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SslBuffers {
        final NIOBuffer _in;
        final NIOBuffer _out;
        final NIOBuffer _unwrap;

        SslBuffers(int packetSize, int appSize) {
            this._in = new IndirectNIOBuffer(packetSize);
            this._out = new IndirectNIOBuffer(packetSize);
            this._unwrap = new IndirectNIOBuffer(appSize);
        }
    }

    public SslConnection(SSLEngine engine, EndPoint endp) {
        this(engine, endp, System.currentTimeMillis());
    }

    public SslConnection(SSLEngine engine, EndPoint endp, long timeStamp) {
        super(endp, timeStamp);
        this._logger = Log.getLogger("org.eclipse.jetty.io.nio.ssl");
        this._allowRenegotiate = true;
        this._progressed = new AtomicBoolean();
        this._engine = engine;
        this._session = this._engine.getSession();
        this._aEndp = (AsyncEndPoint) endp;
        this._sslEndPoint = newSslEndPoint();
    }

    protected SslEndPoint newSslEndPoint() {
        return new SslEndPoint();
    }

    public boolean isAllowRenegotiate() {
        return this._allowRenegotiate;
    }

    public void setAllowRenegotiate(boolean allowRenegotiate) {
        this._allowRenegotiate = allowRenegotiate;
    }

    private void allocateBuffers() {
        synchronized (this) {
            int i = this._allocations;
            this._allocations = i + 1;
            if (i == 0 && this._buffers == null) {
                this._buffers = __buffers.get();
                if (this._buffers == null) {
                    this._buffers = new SslBuffers(this._session.getPacketBufferSize() * 2, this._session.getApplicationBufferSize() * 2);
                }
                this._inbound = this._buffers._in;
                this._outbound = this._buffers._out;
                this._unwrapBuf = this._buffers._unwrap;
                __buffers.set(null);
            }
        }
    }

    private void releaseBuffers() {
        synchronized (this) {
            int i = this._allocations - 1;
            this._allocations = i;
            if (i == 0 && this._buffers != null && this._inbound.length() == 0 && this._outbound.length() == 0 && this._unwrapBuf.length() == 0) {
                this._inbound = null;
                this._outbound = null;
                this._unwrapBuf = null;
                __buffers.set(this._buffers);
                this._buffers = null;
            }
        }
    }

    @Override // org.eclipse.jetty.io.Connection
    public Connection handle() throws IOException {
        try {
            allocateBuffers();
            boolean progress = true;
            while (progress) {
                progress = this._engine.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ? process(null, null) : false;
                AsyncConnection next = (AsyncConnection) this._connection.handle();
                if (next != this._connection && next != null) {
                    this._connection = next;
                    progress = true;
                }
                this._logger.debug("{} handle {} progress={}", this._session, this, Boolean.valueOf(progress));
            }
            return this;
        } finally {
            releaseBuffers();
            if (!this._ishut && this._sslEndPoint.isInputShutdown() && this._sslEndPoint.isOpen()) {
                this._ishut = true;
                try {
                    this._connection.onInputShutdown();
                } catch (Throwable x) {
                    this._logger.warn("onInputShutdown failed", x);
                    try {
                        this._sslEndPoint.close();
                    } catch (IOException e2) {
                        this._logger.ignore(e2);
                    }
                }
            }
        }
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
        Connection connection = this._sslEndPoint.getConnection();
        if (connection != null && connection != this) {
            connection.onClose();
        }
    }

    @Override // org.eclipse.jetty.io.AbstractConnection, org.eclipse.jetty.io.Connection
    public void onIdleExpired(long idleForMs) {
        try {
            this._logger.debug("onIdleExpired {}ms on {}", Long.valueOf(idleForMs), this);
            if (this._endp.isOutputShutdown()) {
                this._sslEndPoint.close();
            } else {
                this._sslEndPoint.shutdownOutput();
            }
        } catch (IOException e) {
            this._logger.warn(e);
            super.onIdleExpired(idleForMs);
        }
    }

    @Override // org.eclipse.jetty.io.nio.AsyncConnection
    public void onInputShutdown() throws IOException {
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:100:0x018c A[Catch: all -> 0x022c, TryCatch #4 {, blocks: (B:127:0x0220, B:129:0x0225, B:15:0x0041, B:17:0x0046, B:21:0x004e, B:23:0x0053, B:31:0x0072, B:33:0x0077, B:5:0x000a, B:7:0x000f, B:36:0x007f, B:52:0x00b4, B:53:0x0100, B:98:0x0184, B:100:0x018c, B:102:0x0194, B:104:0x019c, B:105:0x019f, B:107:0x01a7, B:109:0x01af, B:111:0x01b7, B:55:0x0105, B:57:0x0109, B:59:0x010d, B:60:0x0114, B:64:0x011f, B:65:0x0125, B:68:0x012d, B:70:0x0131, B:72:0x0135, B:73:0x013b, B:77:0x0143, B:79:0x014c, B:81:0x0152, B:83:0x0158, B:85:0x0160, B:88:0x0167, B:90:0x016d, B:92:0x0175, B:95:0x017d, B:96:0x0182, B:118:0x01c8, B:119:0x0206, B:120:0x0207, B:122:0x020b, B:124:0x0213, B:126:0x021b, B:8:0x0018, B:10:0x0024, B:12:0x002d, B:14:0x0035, B:26:0x005a, B:28:0x005e, B:30:0x0066), top: B:143:0x000a }] */
    /* JADX WARN: Removed duplicated region for block: B:107:0x01a7 A[Catch: all -> 0x022c, TryCatch #4 {, blocks: (B:127:0x0220, B:129:0x0225, B:15:0x0041, B:17:0x0046, B:21:0x004e, B:23:0x0053, B:31:0x0072, B:33:0x0077, B:5:0x000a, B:7:0x000f, B:36:0x007f, B:52:0x00b4, B:53:0x0100, B:98:0x0184, B:100:0x018c, B:102:0x0194, B:104:0x019c, B:105:0x019f, B:107:0x01a7, B:109:0x01af, B:111:0x01b7, B:55:0x0105, B:57:0x0109, B:59:0x010d, B:60:0x0114, B:64:0x011f, B:65:0x0125, B:68:0x012d, B:70:0x0131, B:72:0x0135, B:73:0x013b, B:77:0x0143, B:79:0x014c, B:81:0x0152, B:83:0x0158, B:85:0x0160, B:88:0x0167, B:90:0x016d, B:92:0x0175, B:95:0x017d, B:96:0x0182, B:118:0x01c8, B:119:0x0206, B:120:0x0207, B:122:0x020b, B:124:0x0213, B:126:0x021b, B:8:0x0018, B:10:0x0024, B:12:0x002d, B:14:0x0035, B:26:0x005a, B:28:0x005e, B:30:0x0066), top: B:143:0x000a }] */
    /* JADX WARN: Removed duplicated region for block: B:147:0x01bc A[ADDED_TO_REGION, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public synchronized boolean process(org.eclipse.jetty.io.Buffer r18, org.eclipse.jetty.io.Buffer r19) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 586
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.io.nio.SslConnection.process(org.eclipse.jetty.io.Buffer, org.eclipse.jetty.io.Buffer):boolean");
    }

    private void closeInbound() {
        try {
            this._engine.closeInbound();
        } catch (SSLException x) {
            this._logger.debug(x);
        }
    }

    private synchronized boolean wrap(Buffer buffer) throws IOException {
        boolean z;
        SSLEngineResult result;
        int decrypted_consumed;
        int encrypted_produced;
        ByteBuffer bbuf = extractByteBuffer(buffer);
        synchronized (bbuf) {
            try {
                try {
                    this._outbound.compact();
                    ByteBuffer out_buffer = this._outbound.getByteBuffer();
                    synchronized (out_buffer) {
                        z = false;
                        try {
                            try {
                                bbuf.position(buffer.getIndex());
                                bbuf.limit(buffer.putIndex());
                                int decrypted_position = bbuf.position();
                                out_buffer.position(this._outbound.putIndex());
                                out_buffer.limit(out_buffer.capacity());
                                int encrypted_position = out_buffer.position();
                                result = this._engine.wrap(bbuf, out_buffer);
                                if (this._logger.isDebugEnabled()) {
                                    this._logger.debug("{} wrap {} {} consumed={} produced={}", this._session, result.getStatus(), result.getHandshakeStatus(), Integer.valueOf(result.bytesConsumed()), Integer.valueOf(result.bytesProduced()));
                                }
                                decrypted_consumed = bbuf.position() - decrypted_position;
                                try {
                                    buffer.skip(decrypted_consumed);
                                    encrypted_produced = out_buffer.position() - encrypted_position;
                                    this._outbound.setPutIndex(this._outbound.putIndex() + encrypted_produced);
                                    out_buffer.position(0);
                                    out_buffer.limit(out_buffer.capacity());
                                    bbuf.position(0);
                                    bbuf.limit(bbuf.capacity());
                                } catch (SSLException e) {
                                    e = e;
                                    this._logger.debug(String.valueOf(this._endp), e);
                                    this._endp.close();
                                    throw e;
                                } catch (IOException x) {
                                    throw x;
                                } catch (Exception e2) {
                                    x = e2;
                                    throw new IOException(x);
                                }
                            } catch (Throwable th) {
                                e = th;
                                out_buffer.position(0);
                                out_buffer.limit(out_buffer.capacity());
                                bbuf.position(0);
                                bbuf.limit(bbuf.capacity());
                                throw e;
                            }
                        } catch (SSLException e3) {
                            e = e3;
                        } catch (IOException x2) {
                            throw x2;
                        } catch (Exception e4) {
                            x = e4;
                        } catch (Throwable th2) {
                            e = th2;
                            out_buffer.position(0);
                            out_buffer.limit(out_buffer.capacity());
                            bbuf.position(0);
                            bbuf.limit(bbuf.capacity());
                            throw e;
                        }
                    }
                    switch (AnonymousClass1.$SwitchMap$javax$net$ssl$SSLEngineResult$Status[result.getStatus().ordinal()]) {
                        case 1:
                            throw new IllegalStateException();
                        case 2:
                            break;
                        case 3:
                            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                                this._handshook = true;
                                break;
                            }
                            break;
                        case 4:
                            this._logger.debug("wrap CLOSE {} {}", this, result);
                            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                                this._endp.close();
                                break;
                            }
                            break;
                        default:
                            this._logger.debug("{} wrap default {}", this._session, result);
                            throw new IOException(result.toString());
                    }
                    if (decrypted_consumed > 0 || encrypted_produced > 0) {
                        z = true;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: org.eclipse.jetty.io.nio.SslConnection$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus;
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$Status = new int[SSLEngineResult.Status.values().length];

        static {
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.BUFFER_UNDERFLOW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.BUFFER_OVERFLOW.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.OK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.CLOSED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus = new int[SSLEngineResult.HandshakeStatus.values().length];
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.FINISHED.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_TASK.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_WRAP.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_UNWRAP.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    private synchronized boolean unwrap(Buffer buffer) throws IOException {
        SSLEngineResult result;
        boolean z;
        int encrypted_consumed;
        int decrypted_produced;
        if (!this._inbound.hasContent()) {
            return false;
        }
        ByteBuffer bbuf = extractByteBuffer(buffer);
        synchronized (bbuf) {
            try {
                try {
                    ByteBuffer in_buffer = this._inbound.getByteBuffer();
                    try {
                        synchronized (in_buffer) {
                            try {
                                bbuf.position(buffer.putIndex());
                                bbuf.limit(buffer.capacity());
                                int decrypted_position = bbuf.position();
                                in_buffer.position(this._inbound.getIndex());
                                in_buffer.limit(this._inbound.putIndex());
                                int encrypted_position = in_buffer.position();
                                result = this._engine.unwrap(in_buffer, bbuf);
                                z = true;
                                if (this._logger.isDebugEnabled()) {
                                    this._logger.debug("{} unwrap {} {} consumed={} produced={}", this._session, result.getStatus(), result.getHandshakeStatus(), Integer.valueOf(result.bytesConsumed()), Integer.valueOf(result.bytesProduced()));
                                }
                                encrypted_consumed = in_buffer.position() - encrypted_position;
                                this._inbound.skip(encrypted_consumed);
                                this._inbound.compact();
                                decrypted_produced = bbuf.position() - decrypted_position;
                                try {
                                    buffer.setPutIndex(buffer.putIndex() + decrypted_produced);
                                    in_buffer.position(0);
                                    in_buffer.limit(in_buffer.capacity());
                                    bbuf.position(0);
                                    bbuf.limit(bbuf.capacity());
                                } catch (SSLException e) {
                                    e = e;
                                    this._logger.debug(String.valueOf(this._endp), e);
                                    this._endp.close();
                                    throw e;
                                } catch (IOException x) {
                                    throw x;
                                } catch (Exception e2) {
                                    x = e2;
                                    throw new IOException(x);
                                }
                            } catch (SSLException e3) {
                                e = e3;
                            } catch (IOException x2) {
                                throw x2;
                            } catch (Exception e4) {
                                x = e4;
                            } catch (Throwable th) {
                                e = th;
                                in_buffer.position(0);
                                in_buffer.limit(in_buffer.capacity());
                                bbuf.position(0);
                                bbuf.limit(bbuf.capacity());
                                throw e;
                            }
                        }
                        switch (AnonymousClass1.$SwitchMap$javax$net$ssl$SSLEngineResult$Status[result.getStatus().ordinal()]) {
                            case 1:
                                if (this._endp.isInputShutdown()) {
                                    this._inbound.clear();
                                    break;
                                }
                                break;
                            case 2:
                                if (this._logger.isDebugEnabled()) {
                                    this._logger.debug("{} unwrap {} {}->{}", this._session, result.getStatus(), this._inbound.toDetailString(), buffer.toDetailString());
                                    break;
                                }
                                break;
                            case 3:
                                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                                    this._handshook = true;
                                    break;
                                }
                                break;
                            case 4:
                                this._logger.debug("unwrap CLOSE {} {}", this, result);
                                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                                    this._endp.close();
                                    break;
                                }
                                break;
                            default:
                                this._logger.debug("{} wrap default {}", this._session, result);
                                throw new IOException(result.toString());
                        }
                        if (encrypted_consumed <= 0 && decrypted_produced <= 0) {
                            z = false;
                        }
                        return z;
                    } catch (Throwable th2) {
                        e = th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    private ByteBuffer extractByteBuffer(Buffer buffer) {
        if (buffer.buffer() instanceof NIOBuffer) {
            return ((NIOBuffer) buffer.buffer()).getByteBuffer();
        }
        return ByteBuffer.wrap(buffer.array());
    }

    public AsyncEndPoint getSslEndPoint() {
        return this._sslEndPoint;
    }

    @Override // org.eclipse.jetty.io.AbstractConnection
    public String toString() {
        return String.format("%s %s", super.toString(), this._sslEndPoint);
    }

    /* loaded from: classes.dex */
    public class SslEndPoint implements AsyncEndPoint {
        public SslEndPoint() {
        }

        public SSLEngine getSslEngine() {
            return SslConnection.this._engine;
        }

        public AsyncEndPoint getEndpoint() {
            return SslConnection.this._aEndp;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void shutdownOutput() throws IOException {
            synchronized (SslConnection.this) {
                try {
                    SslConnection.this._logger.debug("{} ssl endp.oshut {}", SslConnection.this._session, this);
                    SslConnection.this._oshut = true;
                    SslConnection.this._engine.closeOutbound();
                } catch (Exception x) {
                    throw new IOException(x);
                }
            }
            flush();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isOutputShutdown() {
            boolean z;
            synchronized (SslConnection.this) {
                z = SslConnection.this._oshut || !isOpen() || SslConnection.this._engine.isOutboundDone();
            }
            return z;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void shutdownInput() throws IOException {
            SslConnection.this._logger.debug("{} ssl endp.ishut!", SslConnection.this._session);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isInputShutdown() {
            boolean z;
            synchronized (SslConnection.this) {
                z = SslConnection.this._endp.isInputShutdown() && (SslConnection.this._unwrapBuf == null || !SslConnection.this._unwrapBuf.hasContent()) && (SslConnection.this._inbound == null || !SslConnection.this._inbound.hasContent());
            }
            return z;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void close() throws IOException {
            SslConnection.this._logger.debug("{} ssl endp.close", SslConnection.this._session);
            SslConnection.this._endp.close();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int fill(Buffer buffer) throws IOException {
            int size = buffer.length();
            SslConnection.this.process(buffer, null);
            int filled = buffer.length() - size;
            if (filled == 0 && isInputShutdown()) {
                return -1;
            }
            return filled;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int flush(Buffer buffer) throws IOException {
            int size = buffer.length();
            SslConnection.this.process(null, buffer);
            return size - buffer.length();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
            if (header != null && header.hasContent()) {
                return flush(header);
            }
            if (buffer != null && buffer.hasContent()) {
                return flush(buffer);
            }
            if (trailer != null && trailer.hasContent()) {
                return flush(trailer);
            }
            return 0;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean blockReadable(long millisecs) throws IOException {
            long now = System.currentTimeMillis();
            long end = millisecs > 0 ? now + millisecs : Long.MAX_VALUE;
            while (now < end && !SslConnection.this.process(null, null)) {
                SslConnection.this._endp.blockReadable(end - now);
                now = System.currentTimeMillis();
            }
            return now < end;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean blockWritable(long millisecs) throws IOException {
            return SslConnection.this._endp.blockWritable(millisecs);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isOpen() {
            return SslConnection.this._endp.isOpen();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public Object getTransport() {
            return SslConnection.this._endp;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void flush() throws IOException {
            SslConnection.this.process(null, null);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void dispatch() {
            SslConnection.this._aEndp.dispatch();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void asyncDispatch() {
            SslConnection.this._aEndp.asyncDispatch();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void scheduleWrite() {
            SslConnection.this._aEndp.scheduleWrite();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void onIdleExpired(long idleForMs) {
            SslConnection.this._aEndp.onIdleExpired(idleForMs);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void setCheckForIdle(boolean check) {
            SslConnection.this._aEndp.setCheckForIdle(check);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public boolean isCheckForIdle() {
            return SslConnection.this._aEndp.isCheckForIdle();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void scheduleTimeout(Timeout.Task task, long timeoutMs) {
            SslConnection.this._aEndp.scheduleTimeout(task, timeoutMs);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public void cancelTimeout(Timeout.Task task) {
            SslConnection.this._aEndp.cancelTimeout(task);
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public boolean isWritable() {
            return SslConnection.this._aEndp.isWritable();
        }

        @Override // org.eclipse.jetty.io.AsyncEndPoint
        public boolean hasProgressed() {
            return SslConnection.this._progressed.getAndSet(false);
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getLocalAddr() {
            return SslConnection.this._aEndp.getLocalAddr();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getLocalHost() {
            return SslConnection.this._aEndp.getLocalHost();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int getLocalPort() {
            return SslConnection.this._aEndp.getLocalPort();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getRemoteAddr() {
            return SslConnection.this._aEndp.getRemoteAddr();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public String getRemoteHost() {
            return SslConnection.this._aEndp.getRemoteHost();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int getRemotePort() {
            return SslConnection.this._aEndp.getRemotePort();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public boolean isBlocking() {
            return false;
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public int getMaxIdleTime() {
            return SslConnection.this._aEndp.getMaxIdleTime();
        }

        @Override // org.eclipse.jetty.io.EndPoint
        public void setMaxIdleTime(int timeMs) throws IOException {
            SslConnection.this._aEndp.setMaxIdleTime(timeMs);
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public Connection getConnection() {
            return SslConnection.this._connection;
        }

        @Override // org.eclipse.jetty.io.ConnectedEndPoint
        public void setConnection(Connection connection) {
            SslConnection.this._connection = (AsyncConnection) connection;
        }

        public String toString() {
            Buffer inbound = SslConnection.this._inbound;
            Buffer outbound = SslConnection.this._outbound;
            Buffer unwrap = SslConnection.this._unwrapBuf;
            int i = inbound == null ? -1 : inbound.length();
            int o = outbound == null ? -1 : outbound.length();
            int u = unwrap != null ? unwrap.length() : -1;
            return String.format("SSL %s i/o/u=%d/%d/%d ishut=%b oshut=%b {%s}", SslConnection.this._engine.getHandshakeStatus(), Integer.valueOf(i), Integer.valueOf(o), Integer.valueOf(u), Boolean.valueOf(SslConnection.this._ishut), Boolean.valueOf(SslConnection.this._oshut), SslConnection.this._connection);
        }
    }
}
