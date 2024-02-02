package sun.net.httpserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class UndefLengthOutputStream extends FilterOutputStream {
    private boolean closed;
    ExchangeImpl t;

    /* JADX INFO: Access modifiers changed from: package-private */
    public UndefLengthOutputStream(ExchangeImpl exchangeImpl, OutputStream outputStream) {
        super(outputStream);
        this.closed = false;
        this.t = exchangeImpl;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(int i) throws IOException {
        if (this.closed) {
            throw new IOException("stream closed");
        }
        this.out.write(i);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (this.closed) {
            throw new IOException("stream closed");
        }
        this.out.write(bArr, i, i2);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        flush();
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
