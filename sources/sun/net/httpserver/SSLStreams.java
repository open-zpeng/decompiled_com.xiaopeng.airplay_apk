package sun.net.httpserver;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SSLStreams {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static long readTimeout = ServerConfig.getReadTimeout();
    static long writeTimeout = ServerConfig.getWriteTimeout();
    int app_buf_size;
    SocketChannel chan;
    SSLEngine engine;
    Lock handshaking = new ReentrantLock();
    InputStream is;
    OutputStream os;
    int packet_buf_size;
    ServerImpl server;
    SSLContext sslctx;
    TimeSource time;
    EngineWrapper wrapper;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SSLStreams(ServerImpl serverImpl, SSLContext sSLContext, SocketChannel socketChannel) throws IOException {
        this.server = serverImpl;
        this.time = serverImpl;
        this.sslctx = sSLContext;
        this.chan = socketChannel;
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketChannel.socket().getRemoteSocketAddress();
        this.engine = sSLContext.createSSLEngine(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        this.engine.setUseClientMode(false);
        configureEngine(serverImpl.getHttpsConfigurator(), inetSocketAddress);
        this.wrapper = new EngineWrapper(socketChannel, this.engine);
    }

    private void configureEngine(HttpsConfigurator httpsConfigurator, InetSocketAddress inetSocketAddress) {
        if (httpsConfigurator != null) {
            Parameters parameters = new Parameters(httpsConfigurator, inetSocketAddress);
            httpsConfigurator.configure(parameters);
            if (parameters.getCipherSuites() != null) {
                try {
                    this.engine.setEnabledCipherSuites(parameters.getCipherSuites());
                } catch (IllegalArgumentException e) {
                }
            }
            this.engine.setNeedClientAuth(parameters.getNeedClientAuth());
            this.engine.setWantClientAuth(parameters.getWantClientAuth());
            if (parameters.getProtocols() != null) {
                try {
                    this.engine.setEnabledProtocols(parameters.getProtocols());
                } catch (IllegalArgumentException e2) {
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class Parameters extends HttpsParameters {
        InetSocketAddress addr;
        HttpsConfigurator cfg;

        Parameters(HttpsConfigurator httpsConfigurator, InetSocketAddress inetSocketAddress) {
            this.addr = inetSocketAddress;
            this.cfg = httpsConfigurator;
        }

        @Override // com.sun.net.httpserver.HttpsParameters
        public InetSocketAddress getClientAddress() {
            return this.addr;
        }

        @Override // com.sun.net.httpserver.HttpsParameters
        public HttpsConfigurator getHttpsConfigurator() {
            return this.cfg;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void close() throws IOException {
        this.wrapper.close();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public InputStream getInputStream() throws IOException {
        if (this.is == null) {
            this.is = new InputStream();
        }
        return this.is;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public OutputStream getOutputStream() throws IOException {
        if (this.os == null) {
            this.os = new OutputStream();
        }
        return this.os;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SSLEngine getSSLEngine() {
        return this.engine;
    }

    void beginHandshake() throws SSLException {
        this.engine.beginHandshake();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class WrapperResult {
        ByteBuffer buf;
        SSLEngineResult result;

        WrapperResult() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public enum BufType {
        PACKET,
        APPLICATION;

        public static BufType valueOf(String str) {
            BufType[] values;
            for (BufType bufType : values()) {
                if (bufType.name().equals(str)) {
                    return bufType;
                }
            }
            throw new IllegalArgumentException(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ByteBuffer allocate(BufType bufType) {
        return allocate(bufType, -1);
    }

    private ByteBuffer allocate(BufType bufType, int i) {
        int i2;
        ByteBuffer allocate;
        synchronized (this) {
            if (bufType == BufType.PACKET) {
                if (this.packet_buf_size == 0) {
                    this.packet_buf_size = this.engine.getSession().getPacketBufferSize();
                }
                if (i > this.packet_buf_size) {
                    this.packet_buf_size = i;
                }
                i2 = this.packet_buf_size;
            } else {
                if (this.app_buf_size == 0) {
                    this.app_buf_size = this.engine.getSession().getApplicationBufferSize();
                }
                if (i > this.app_buf_size) {
                    this.app_buf_size = i;
                }
                i2 = this.app_buf_size;
            }
            allocate = ByteBuffer.allocate(i2);
        }
        return allocate;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ByteBuffer realloc(ByteBuffer byteBuffer, boolean z, BufType bufType) {
        ByteBuffer allocate;
        synchronized (this) {
            allocate = allocate(bufType, 2 * byteBuffer.capacity());
            if (z) {
                byteBuffer.flip();
            }
            allocate.put(byteBuffer);
        }
        return allocate;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class EngineWrapper {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        SocketChannel chan;
        SSLEngine engine;
        SelectionKey rkey;
        int u_remaining;
        ByteBuffer unwrap_src;
        SelectionKey wkey;
        ByteBuffer wrap_dst;
        boolean closed = false;
        Object wrapLock = new Object();
        Object unwrapLock = new Object();
        SelectorCache sc = SelectorCache.getSelectorCache();
        Selector write_selector = this.sc.getSelector();
        Selector read_selector = this.sc.getSelector();

        EngineWrapper(SocketChannel socketChannel, SSLEngine sSLEngine) throws IOException {
            this.chan = socketChannel;
            this.engine = sSLEngine;
            this.unwrap_src = SSLStreams.this.allocate(BufType.PACKET);
            this.wrap_dst = SSLStreams.this.allocate(BufType.PACKET);
            this.wkey = socketChannel.register(this.write_selector, 4);
            this.wkey = socketChannel.register(this.read_selector, 1);
        }

        void close() throws IOException {
            this.sc.freeSelector(this.write_selector);
            this.sc.freeSelector(this.read_selector);
        }

        WrapperResult wrapAndSend(ByteBuffer byteBuffer) throws IOException {
            return wrapAndSendX(byteBuffer, false);
        }

        WrapperResult wrapAndSendX(ByteBuffer byteBuffer, boolean z) throws IOException {
            SSLEngineResult.Status status;
            if (this.closed && !z) {
                throw new IOException("Engine is closed");
            }
            WrapperResult wrapperResult = new WrapperResult();
            synchronized (this.wrapLock) {
                this.wrap_dst.clear();
                do {
                    wrapperResult.result = this.engine.wrap(byteBuffer, this.wrap_dst);
                    status = wrapperResult.result.getStatus();
                    if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                        this.wrap_dst = SSLStreams.this.realloc(this.wrap_dst, true, BufType.PACKET);
                    }
                } while (status == SSLEngineResult.Status.BUFFER_OVERFLOW);
                if (status == SSLEngineResult.Status.CLOSED && !z) {
                    this.closed = true;
                    return wrapperResult;
                }
                if (wrapperResult.result.bytesProduced() > 0) {
                    this.wrap_dst.flip();
                    int remaining = this.wrap_dst.remaining();
                    long time = SSLStreams.this.time.getTime() + SSLStreams.writeTimeout;
                    while (remaining > 0) {
                        this.write_selector.select(SSLStreams.writeTimeout);
                        if (SSLStreams.this.time.getTime() > time) {
                            throw new SocketTimeoutException("write timed out");
                        }
                        this.write_selector.selectedKeys().clear();
                        remaining -= this.chan.write(this.wrap_dst);
                    }
                }
                return wrapperResult;
            }
        }

        WrapperResult recvAndUnwrap(ByteBuffer byteBuffer) throws IOException {
            boolean z;
            SSLEngineResult.Status status;
            SSLEngineResult.Status status2 = SSLEngineResult.Status.OK;
            WrapperResult wrapperResult = new WrapperResult();
            wrapperResult.buf = byteBuffer;
            if (this.closed) {
                throw new IOException("Engine is closed");
            }
            if (this.u_remaining > 0) {
                this.unwrap_src.compact();
                this.unwrap_src.flip();
                z = false;
            } else {
                this.unwrap_src.clear();
                z = true;
            }
            synchronized (this.unwrapLock) {
                do {
                    if (z) {
                        try {
                            long time = SSLStreams.this.time.getTime();
                            long j = SSLStreams.readTimeout + time;
                            while (time <= j) {
                                int select = this.read_selector.select(SSLStreams.readTimeout);
                                long time2 = SSLStreams.this.time.getTime();
                                if (select != 1) {
                                    time = time2;
                                } else {
                                    this.read_selector.selectedKeys().clear();
                                    if (this.chan.read(this.unwrap_src) == -1) {
                                        throw new IOException("connection closed for reading");
                                    }
                                    this.unwrap_src.flip();
                                }
                            }
                            throw new SocketTimeoutException("read timedout");
                        } finally {
                        }
                    }
                    wrapperResult.result = this.engine.unwrap(this.unwrap_src, wrapperResult.buf);
                    status = wrapperResult.result.getStatus();
                    if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                        if (this.unwrap_src.limit() == this.unwrap_src.capacity()) {
                            this.unwrap_src = SSLStreams.this.realloc(this.unwrap_src, false, BufType.PACKET);
                        } else {
                            this.unwrap_src.position(this.unwrap_src.limit());
                            this.unwrap_src.limit(this.unwrap_src.capacity());
                        }
                        z = true;
                    } else if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                        wrapperResult.buf = SSLStreams.this.realloc(wrapperResult.buf, true, BufType.APPLICATION);
                        z = false;
                    } else if (status == SSLEngineResult.Status.CLOSED) {
                        this.closed = true;
                        wrapperResult.buf.flip();
                        return wrapperResult;
                    }
                } while (status != SSLEngineResult.Status.OK);
                this.u_remaining = this.unwrap_src.remaining();
                return wrapperResult;
            }
        }
    }

    public WrapperResult sendData(ByteBuffer byteBuffer) throws IOException {
        WrapperResult wrapperResult = null;
        while (byteBuffer.remaining() > 0) {
            wrapperResult = this.wrapper.wrapAndSend(byteBuffer);
            if (wrapperResult.result.getStatus() == SSLEngineResult.Status.CLOSED) {
                doClosure();
                return wrapperResult;
            }
            SSLEngineResult.HandshakeStatus handshakeStatus = wrapperResult.result.getHandshakeStatus();
            if (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                doHandshake(handshakeStatus);
            }
        }
        return wrapperResult;
    }

    public WrapperResult recvData(ByteBuffer byteBuffer) throws IOException {
        WrapperResult wrapperResult = null;
        while (byteBuffer.position() == 0) {
            wrapperResult = this.wrapper.recvAndUnwrap(byteBuffer);
            if (wrapperResult.buf != byteBuffer) {
                byteBuffer = wrapperResult.buf;
            }
            if (wrapperResult.result.getStatus() == SSLEngineResult.Status.CLOSED) {
                doClosure();
                return wrapperResult;
            }
            SSLEngineResult.HandshakeStatus handshakeStatus = wrapperResult.result.getHandshakeStatus();
            if (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                doHandshake(handshakeStatus);
            }
        }
        byteBuffer.flip();
        return wrapperResult;
    }

    void doClosure() throws IOException {
        try {
            this.handshaking.lock();
            ByteBuffer allocate = allocate(BufType.APPLICATION);
            do {
                allocate.clear();
                allocate.flip();
            } while (this.wrapper.wrapAndSendX(allocate, true).result.getStatus() != SSLEngineResult.Status.CLOSED);
        } finally {
            this.handshaking.unlock();
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    void doHandshake(SSLEngineResult.HandshakeStatus handshakeStatus) throws IOException {
        try {
            this.handshaking.lock();
            ByteBuffer allocate = allocate(BufType.APPLICATION);
            while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                WrapperResult wrapperResult = null;
                switch (AnonymousClass1.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[handshakeStatus.ordinal()]) {
                    case 1:
                        while (true) {
                            Runnable delegatedTask = this.engine.getDelegatedTask();
                            if (delegatedTask == null) {
                                break;
                            } else {
                                delegatedTask.run();
                            }
                        }
                    case 2:
                        break;
                    case 3:
                        allocate.clear();
                        wrapperResult = this.wrapper.recvAndUnwrap(allocate);
                        if (wrapperResult.buf != allocate) {
                            allocate = wrapperResult.buf;
                        }
                        continue;
                        handshakeStatus = wrapperResult.result.getHandshakeStatus();
                    default:
                        continue;
                        handshakeStatus = wrapperResult.result.getHandshakeStatus();
                }
                allocate.clear();
                allocate.flip();
                wrapperResult = this.wrapper.wrapAndSend(allocate);
                handshakeStatus = wrapperResult.result.getHandshakeStatus();
            }
        } finally {
            this.handshaking.unlock();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: sun.net.httpserver.SSLStreams$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus = new int[SSLEngineResult.HandshakeStatus.values().length];

        static {
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_TASK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_WRAP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_UNWRAP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class InputStream extends java.io.InputStream {
        ByteBuffer bbuf;
        boolean closed = false;
        boolean eof = false;
        boolean needData = true;
        byte[] single = new byte[1];

        InputStream() {
            this.bbuf = SSLStreams.this.allocate(BufType.APPLICATION);
        }

        @Override // java.io.InputStream
        public int read(byte[] bArr, int i, int i2) throws IOException {
            int i3;
            if (this.closed) {
                throw new IOException("SSL stream is closed");
            }
            if (this.eof) {
                return 0;
            }
            if (!this.needData) {
                i3 = this.bbuf.remaining();
                this.needData = i3 == 0;
            } else {
                i3 = 0;
            }
            if (this.needData) {
                this.bbuf.clear();
                WrapperResult recvData = SSLStreams.this.recvData(this.bbuf);
                this.bbuf = recvData.buf == this.bbuf ? this.bbuf : recvData.buf;
                i3 = this.bbuf.remaining();
                if (i3 == 0) {
                    this.eof = true;
                    return 0;
                }
                this.needData = false;
            }
            if (i2 > i3) {
                i2 = i3;
            }
            this.bbuf.get(bArr, i, i2);
            return i2;
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            return this.bbuf.remaining();
        }

        @Override // java.io.InputStream
        public boolean markSupported() {
            return false;
        }

        @Override // java.io.InputStream
        public void reset() throws IOException {
            throw new IOException("mark/reset not supported");
        }

        @Override // java.io.InputStream
        public long skip(long j) throws IOException {
            int i = (int) j;
            if (this.closed) {
                throw new IOException("SSL stream is closed");
            }
            if (this.eof) {
                return 0L;
            }
            int i2 = i;
            while (i2 > 0) {
                if (this.bbuf.remaining() >= i2) {
                    this.bbuf.position(this.bbuf.position() + i2);
                    return i;
                }
                i2 -= this.bbuf.remaining();
                this.bbuf.clear();
                WrapperResult recvData = SSLStreams.this.recvData(this.bbuf);
                this.bbuf = recvData.buf == this.bbuf ? this.bbuf : recvData.buf;
            }
            return i;
        }

        @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            this.eof = true;
            SSLStreams.this.engine.closeInbound();
        }

        @Override // java.io.InputStream
        public int read(byte[] bArr) throws IOException {
            return read(bArr, 0, bArr.length);
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            if (read(this.single, 0, 1) == 0) {
                return -1;
            }
            return this.single[0] & 255;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class OutputStream extends java.io.OutputStream {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        ByteBuffer buf;
        boolean closed = false;
        byte[] single = new byte[1];

        OutputStream() {
            this.buf = SSLStreams.this.allocate(BufType.APPLICATION);
        }

        @Override // java.io.OutputStream
        public void write(int i) throws IOException {
            this.single[0] = (byte) i;
            write(this.single, 0, 1);
        }

        @Override // java.io.OutputStream
        public void write(byte[] bArr) throws IOException {
            write(bArr, 0, bArr.length);
        }

        @Override // java.io.OutputStream
        public void write(byte[] bArr, int i, int i2) throws IOException {
            if (this.closed) {
                throw new IOException("output stream is closed");
            }
            while (i2 > 0) {
                int capacity = i2 > this.buf.capacity() ? this.buf.capacity() : i2;
                this.buf.clear();
                this.buf.put(bArr, i, capacity);
                i2 -= capacity;
                i += capacity;
                this.buf.flip();
                if (SSLStreams.this.sendData(this.buf).result.getStatus() == SSLEngineResult.Status.CLOSED) {
                    this.closed = true;
                    if (i2 > 0) {
                        throw new IOException("output stream is closed");
                    }
                }
            }
        }

        @Override // java.io.OutputStream, java.io.Flushable
        public void flush() throws IOException {
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            SSLStreams.this.engine.closeOutbound();
            this.closed = true;
            SSLEngineResult.HandshakeStatus handshakeStatus = SSLEngineResult.HandshakeStatus.NEED_WRAP;
            this.buf.clear();
            while (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                handshakeStatus = SSLStreams.this.wrapper.wrapAndSend(this.buf).result.getHandshakeStatus();
            }
        }
    }
}
