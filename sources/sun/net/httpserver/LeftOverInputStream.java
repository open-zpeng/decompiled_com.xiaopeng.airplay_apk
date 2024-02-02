package sun.net.httpserver;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class LeftOverInputStream extends FilterInputStream {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected boolean closed;
    protected boolean eof;
    byte[] one;
    ServerImpl server;
    ExchangeImpl t;

    protected abstract int readImpl(byte[] bArr, int i, int i2) throws IOException;

    public LeftOverInputStream(ExchangeImpl exchangeImpl, InputStream inputStream) {
        super(inputStream);
        this.closed = false;
        this.eof = false;
        this.one = new byte[1];
        this.t = exchangeImpl;
        this.server = exchangeImpl.getServerImpl();
    }

    public boolean isDataBuffered() throws IOException {
        return super.available() > 0;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (!this.eof) {
            this.eof = drain(ServerConfig.getDrainAmount());
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean isEOF() {
        return this.eof;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public synchronized int read() throws IOException {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
        int readImpl = readImpl(this.one, 0, 1);
        if (readImpl != -1 && readImpl != 0) {
            return this.one[0] & 255;
        }
        return readImpl;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public synchronized int read(byte[] bArr, int i, int i2) throws IOException {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
        return readImpl(bArr, i, i2);
    }

    public boolean drain(long j) throws IOException {
        byte[] bArr = new byte[2048];
        while (j > 0) {
            long readImpl = readImpl(bArr, 0, 2048);
            if (readImpl == -1) {
                this.eof = true;
                return true;
            }
            j -= readImpl;
        }
        return false;
    }
}
