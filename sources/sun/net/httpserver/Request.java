package sun.net.httpserver;

import com.sun.net.httpserver.Headers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class Request {
    static final int BUF_LEN = 2048;
    static final byte CR = 13;
    static final byte LF = 10;
    private InputStream is;
    StringBuffer lineBuf;
    private OutputStream os;
    int pos;
    char[] buf = new char[BUF_LEN];
    Headers hdrs = null;
    private SocketChannel chan = this.chan;
    private SocketChannel chan = this.chan;
    private String startLine = readLine();

    /* JADX INFO: Access modifiers changed from: package-private */
    public Request(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.is = inputStream;
        this.os = outputStream;
        do {
        } while (this.startLine.equals(""));
    }

    public InputStream inputStream() {
        return this.is;
    }

    public OutputStream outputStream() {
        return this.os;
    }

    public String readLine() throws IOException {
        this.pos = 0;
        this.lineBuf = new StringBuffer();
        boolean z = false;
        boolean z2 = false;
        while (!z) {
            int read = this.is.read();
            if (read == -1) {
                return null;
            }
            if (z2) {
                if (read == 10) {
                    z = true;
                } else {
                    consume(13);
                    consume(read);
                    z2 = false;
                }
            } else if (read == 13) {
                z2 = true;
            } else {
                consume(read);
            }
        }
        this.lineBuf.append(this.buf, 0, this.pos);
        return new String(this.lineBuf);
    }

    private void consume(int i) {
        if (this.pos == BUF_LEN) {
            this.lineBuf.append(this.buf);
            this.pos = 0;
        }
        char[] cArr = this.buf;
        int i2 = this.pos;
        this.pos = i2 + 1;
        cArr[i2] = (char) i;
    }

    public String requestLine() {
        return this.startLine;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:43:0x0077  */
    /* JADX WARN: Removed duplicated region for block: B:79:0x0081 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public com.sun.net.httpserver.Headers headers() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 210
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.httpserver.Request.headers():com.sun.net.httpserver.Headers");
    }

    /* loaded from: classes.dex */
    static class ReadStream extends InputStream {
        static long readTimeout = ServerConfig.getReadTimeout();
        SocketChannel channel;
        boolean closed;
        SelectionKey key;
        ByteBuffer markBuf;
        int readlimit;
        ServerImpl server;
        boolean eof = false;
        SelectorCache sc = SelectorCache.getSelectorCache();
        Selector selector = this.sc.getSelector();
        ByteBuffer chanbuf = ByteBuffer.allocate(CompressedResponseWrapper.DEFAULT_BUFFER_SIZE);
        int available = 0;
        byte[] one = new byte[1];
        boolean reset = false;
        boolean marked = false;

        public ReadStream(ServerImpl serverImpl, SocketChannel socketChannel) throws IOException {
            this.closed = false;
            this.channel = socketChannel;
            this.server = serverImpl;
            this.key = socketChannel.register(this.selector, 1);
            this.closed = false;
        }

        @Override // java.io.InputStream
        public synchronized int read(byte[] bArr) throws IOException {
            return read(bArr, 0, bArr.length);
        }

        @Override // java.io.InputStream
        public synchronized int read() throws IOException {
            if (read(this.one, 0, 1) == 1) {
                return this.one[0] & 255;
            }
            return -1;
        }

        @Override // java.io.InputStream
        public synchronized int read(byte[] bArr, int i, int i2) throws IOException {
            if (this.closed) {
                throw new IOException("Stream closed");
            }
            if (this.eof) {
                return -1;
            }
            if (this.reset) {
                int remaining = this.markBuf.remaining();
                if (remaining <= i2) {
                    i2 = remaining;
                }
                this.markBuf.get(bArr, i, i2);
                if (remaining == i2) {
                    this.reset = false;
                }
            } else {
                int available = available();
                while (available == 0 && !this.eof) {
                    block();
                    available = available();
                }
                if (this.eof) {
                    return -1;
                }
                if (available <= i2) {
                    i2 = available;
                }
                this.chanbuf.get(bArr, i, i2);
                this.available -= i2;
                if (this.marked) {
                    try {
                        this.markBuf.put(bArr, i, i2);
                    } catch (BufferOverflowException e) {
                        this.marked = false;
                    }
                }
            }
            return i2;
        }

        @Override // java.io.InputStream
        public synchronized int available() throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            }
            if (this.eof) {
                return -1;
            }
            if (this.reset) {
                return this.markBuf.remaining();
            } else if (this.available > 0) {
                return this.available;
            } else {
                this.chanbuf.clear();
                this.available = this.channel.read(this.chanbuf);
                if (this.available > 0) {
                    this.chanbuf.flip();
                } else if (this.available == -1) {
                    this.eof = true;
                    this.available = 0;
                }
                return this.available;
            }
        }

        private synchronized void block() throws IOException {
            long time = this.server.getTime();
            long j = readTimeout + time;
            while (time < j) {
                if (this.selector.select(readTimeout) == 1) {
                    this.selector.selectedKeys().clear();
                    available();
                } else {
                    time = this.server.getTime();
                }
            }
            throw new SocketTimeoutException("no data received");
        }

        @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            this.channel.close();
            this.selector.selectNow();
            this.sc.freeSelector(this.selector);
            this.closed = true;
        }

        @Override // java.io.InputStream
        public synchronized void mark(int i) {
            if (this.closed) {
                return;
            }
            this.readlimit = i;
            this.markBuf = ByteBuffer.allocate(i);
            this.marked = true;
            this.reset = false;
        }

        @Override // java.io.InputStream
        public synchronized void reset() throws IOException {
            if (this.closed) {
                return;
            }
            if (!this.marked) {
                throw new IOException("Stream not marked");
            }
            this.marked = false;
            this.reset = true;
            this.markBuf.flip();
        }
    }

    /* loaded from: classes.dex */
    static class WriteStream extends OutputStream {
        static long writeTimeout = ServerConfig.getWriteTimeout();
        SocketChannel channel;
        SelectionKey key;
        ServerImpl server;
        SelectorCache sc = SelectorCache.getSelectorCache();
        Selector selector = this.sc.getSelector();
        boolean closed = false;
        byte[] one = new byte[1];
        ByteBuffer buf = ByteBuffer.allocate(4096);

        public WriteStream(ServerImpl serverImpl, SocketChannel socketChannel) throws IOException {
            this.channel = socketChannel;
            this.server = serverImpl;
            this.key = socketChannel.register(this.selector, 4);
        }

        @Override // java.io.OutputStream
        public synchronized void write(int i) throws IOException {
            this.one[0] = (byte) i;
            write(this.one, 0, 1);
        }

        @Override // java.io.OutputStream
        public synchronized void write(byte[] bArr) throws IOException {
            write(bArr, 0, bArr.length);
        }

        @Override // java.io.OutputStream
        public synchronized void write(byte[] bArr, int i, int i2) throws IOException {
            if (this.closed) {
                throw new IOException("stream is closed");
            }
            int capacity = this.buf.capacity();
            if (capacity < i2) {
                this.buf = ByteBuffer.allocate(2 * (capacity + (i2 - capacity)));
            }
            this.buf.clear();
            this.buf.put(bArr, i, i2);
            this.buf.flip();
            while (true) {
                int write = this.channel.write(this.buf);
                if (write >= i2) {
                    return;
                }
                i2 -= write;
                if (i2 == 0) {
                    return;
                }
                block();
            }
        }

        void block() throws IOException {
            long time = this.server.getTime();
            long j = writeTimeout + time;
            while (time < j) {
                if (this.selector.select(writeTimeout) == 1) {
                    this.selector.selectedKeys().clear();
                    return;
                }
                time = this.server.getTime();
            }
            throw new SocketTimeoutException("write blocked too long");
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            this.channel.close();
            this.selector.selectNow();
            this.sc.freeSelector(this.selector);
            this.closed = true;
        }
    }
}
