package sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class FixedLengthInputStream extends LeftOverInputStream {
    private int remaining;

    /* JADX INFO: Access modifiers changed from: package-private */
    public FixedLengthInputStream(ExchangeImpl exchangeImpl, InputStream inputStream, int i) {
        super(exchangeImpl, inputStream);
        this.remaining = i;
    }

    @Override // sun.net.httpserver.LeftOverInputStream
    protected int readImpl(byte[] bArr, int i, int i2) throws IOException {
        this.eof = this.remaining == 0;
        if (this.eof) {
            return -1;
        }
        if (i2 > this.remaining) {
            i2 = this.remaining;
        }
        int read = this.in.read(bArr, i, i2);
        if (read > -1) {
            this.remaining -= read;
        }
        return read;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int available() throws IOException {
        if (this.eof) {
            return 0;
        }
        int available = this.in.available();
        return available < this.remaining ? available : this.remaining;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public boolean markSupported() {
        return false;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public void mark(int i) {
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
