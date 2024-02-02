package sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ChunkedInputStream extends LeftOverInputStream {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static char CR = '\r';
    static char LF = '\n';
    private boolean needToReadHeader;
    private int remaining;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ChunkedInputStream(ExchangeImpl exchangeImpl, InputStream inputStream) {
        super(exchangeImpl, inputStream);
        this.needToReadHeader = true;
    }

    private int numeric(char[] cArr, int i) throws IOException {
        int i2;
        int i3 = 0;
        for (int i4 = 0; i4 < i; i4++) {
            char c = cArr[i4];
            if (c >= '0' && c <= '9') {
                i2 = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                i2 = (c - 'a') + 10;
            } else if (c >= 'A' && c <= 'F') {
                i2 = (c - 'A') + 10;
            } else {
                throw new IOException("invalid chunk length");
            }
            i3 = (i3 * 16) + i2;
        }
        return i3;
    }

    private int readChunkHeader() throws IOException {
        char[] cArr = new char[16];
        int i = 0;
        boolean z = false;
        boolean z2 = false;
        while (true) {
            char read = (char) this.in.read();
            if (read == 65535) {
                throw new IOException("end of stream reading chunk header");
            }
            if (i == cArr.length - 1) {
                throw new IOException("invalid chunk header");
            }
            if (z) {
                if (read == LF) {
                    return numeric(cArr, i);
                }
                if (!z2) {
                    cArr[i] = read;
                    i++;
                }
                z = false;
            } else if (read == CR) {
                z = true;
            } else if (read == ';') {
                z2 = true;
            } else if (!z2) {
                cArr[i] = read;
                i++;
            }
        }
    }

    @Override // sun.net.httpserver.LeftOverInputStream
    protected int readImpl(byte[] bArr, int i, int i2) throws IOException {
        if (this.eof) {
            return -1;
        }
        if (this.needToReadHeader) {
            this.remaining = readChunkHeader();
            if (this.remaining == 0) {
                this.eof = true;
                consumeCRLF();
                return -1;
            }
            this.needToReadHeader = false;
        }
        if (i2 > this.remaining) {
            i2 = this.remaining;
        }
        int read = this.in.read(bArr, i, i2);
        if (read > -1) {
            this.remaining -= read;
        }
        if (this.remaining == 0) {
            this.needToReadHeader = true;
            consumeCRLF();
        }
        return read;
    }

    private void consumeCRLF() throws IOException {
        if (((char) this.in.read()) != CR) {
            throw new IOException("invalid chunk end");
        }
        if (((char) this.in.read()) != LF) {
            throw new IOException("invalid chunk end");
        }
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int available() throws IOException {
        if (this.eof || this.closed) {
            return 0;
        }
        int available = this.in.available();
        return available > this.remaining ? this.remaining : available;
    }

    @Override // sun.net.httpserver.LeftOverInputStream
    public boolean isDataBuffered() throws IOException {
        return this.in.available() > 0;
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
