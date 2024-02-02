package sun.net.httpserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
/* loaded from: classes.dex */
class FixedLengthOutputStream extends FilterOutputStream {
    private boolean closed;
    private boolean eof;
    private long remaining;
    ExchangeImpl t;

    /* JADX INFO: Access modifiers changed from: package-private */
    public FixedLengthOutputStream(ExchangeImpl exchangeImpl, OutputStream outputStream, long j) {
        super(outputStream);
        this.eof = false;
        this.closed = false;
        this.t = exchangeImpl;
        this.remaining = j;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(int i) throws IOException {
        if (this.closed) {
            throw new IOException("stream closed");
        }
        this.eof = this.remaining == 0;
        if (this.eof) {
            throw new StreamClosedException();
        }
        this.out.write(i);
        this.remaining--;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (this.closed) {
            throw new IOException("stream closed");
        }
        this.eof = this.remaining == 0;
        if (this.eof) {
            throw new StreamClosedException();
        }
        long j = i2;
        if (j > this.remaining) {
            throw new IOException("too many bytes to write to stream");
        }
        this.out.write(bArr, i, i2);
        this.remaining -= j;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.remaining > 0) {
            this.t.close();
            throw new IOException("insufficient bytes written to stream");
        }
        flush();
        this.eof = true;
        LeftOverInputStream originalInputStream = this.t.getOriginalInputStream();
        if (!originalInputStream.isClosed()) {
            try {
                originalInputStream.close();
            } catch (IOException e) {
            }
        }
        this.t.getHttpContext().getServerImpl().addEvent(new WriteFinishedEvent(this.t));
    }
}
