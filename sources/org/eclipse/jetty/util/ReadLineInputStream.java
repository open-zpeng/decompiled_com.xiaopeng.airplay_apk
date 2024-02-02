package org.eclipse.jetty.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class ReadLineInputStream extends BufferedInputStream {
    boolean _seenCRLF;
    boolean _skipLF;

    public ReadLineInputStream(InputStream in) {
        super(in);
    }

    public ReadLineInputStream(InputStream in, int size) {
        super(in, size);
    }

    public String readLine() throws IOException {
        mark(this.buf.length);
        while (true) {
            int b = super.read();
            if (this.markpos < 0) {
                throw new IOException("Buffer size exceeded: no line terminator");
            }
            if (b == -1) {
                int m = this.markpos;
                this.markpos = -1;
                if (this.pos > m) {
                    return new String(this.buf, m, this.pos - m, StringUtil.__UTF8_CHARSET);
                }
                return null;
            } else if (b == 13) {
                int p = this.pos;
                if (this._seenCRLF && this.pos < this.count) {
                    if (this.buf[this.pos] == 10) {
                        this.pos++;
                    }
                } else {
                    this._skipLF = true;
                }
                int m2 = this.markpos;
                this.markpos = -1;
                return new String(this.buf, m2, (p - m2) - 1, StringUtil.__UTF8_CHARSET);
            } else if (b == 10) {
                if (this._skipLF) {
                    this._skipLF = false;
                    this._seenCRLF = true;
                    this.markpos++;
                } else {
                    int m3 = this.markpos;
                    this.markpos = -1;
                    return new String(this.buf, m3, (this.pos - m3) - 1, StringUtil.__UTF8_CHARSET);
                }
            }
        }
    }

    @Override // java.io.BufferedInputStream, java.io.FilterInputStream, java.io.InputStream
    public synchronized int read() throws IOException {
        int b;
        b = super.read();
        if (this._skipLF) {
            this._skipLF = false;
            if (this._seenCRLF && b == 10) {
                b = super.read();
            }
        }
        return b;
    }

    @Override // java.io.BufferedInputStream, java.io.FilterInputStream, java.io.InputStream
    public synchronized int read(byte[] buf, int off, int len) throws IOException {
        if (this._skipLF && len > 0) {
            this._skipLF = false;
            if (this._seenCRLF) {
                int b = super.read();
                if (b == -1) {
                    return -1;
                }
                if (b != 10) {
                    buf[off] = (byte) (255 & b);
                    return 1 + super.read(buf, off + 1, len - 1);
                }
            }
        }
        return super.read(buf, off, len);
    }
}
