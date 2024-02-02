package sun.net.httpserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.jetty.http.HttpTokens;
/* loaded from: classes.dex */
class ChunkedOutputStream extends FilterOutputStream {
    static final int CHUNK_SIZE = 4096;
    static final int OFFSET = 6;
    private byte[] buf;
    private boolean closed;
    private int count;
    private int pos;
    ExchangeImpl t;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ChunkedOutputStream(ExchangeImpl exchangeImpl, OutputStream outputStream) {
        super(outputStream);
        this.closed = false;
        this.pos = 6;
        this.count = 0;
        this.buf = new byte[4104];
        this.t = exchangeImpl;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(int i) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        byte[] bArr = this.buf;
        int i2 = this.pos;
        this.pos = i2 + 1;
        bArr[i2] = (byte) i;
        this.count++;
        if (this.count == CHUNK_SIZE) {
            writeChunk();
        }
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        int i3 = 4096 - this.count;
        if (i2 > i3) {
            System.arraycopy(bArr, i, this.buf, this.pos, i3);
            this.count = CHUNK_SIZE;
            writeChunk();
            i2 -= i3;
            i += i3;
            while (i2 > CHUNK_SIZE) {
                System.arraycopy(bArr, i, this.buf, 6, CHUNK_SIZE);
                i2 -= 4096;
                i += CHUNK_SIZE;
                this.count = CHUNK_SIZE;
                writeChunk();
            }
            this.pos = 6;
        }
        if (i2 > 0) {
            System.arraycopy(bArr, i, this.buf, this.pos, i2);
            this.count += i2;
            this.pos += i2;
        }
    }

    private void writeChunk() throws IOException {
        char[] charArray = Integer.toHexString(this.count).toCharArray();
        int length = charArray.length;
        int i = 4 - length;
        int i2 = 0;
        while (i2 < length) {
            this.buf[i + i2] = (byte) charArray[i2];
            i2++;
        }
        int i3 = i2 + 1;
        this.buf[i2 + i] = HttpTokens.CARRIAGE_RETURN;
        int i4 = i3 + 1;
        this.buf[i3 + i] = 10;
        int i5 = i4 + 1;
        this.buf[i4 + i + this.count] = HttpTokens.CARRIAGE_RETURN;
        this.buf[i5 + i + this.count] = 10;
        this.out.write(this.buf, i, i5 + 1 + this.count);
        this.count = 0;
        this.pos = 6;
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        flush();
        try {
            writeChunk();
            this.out.flush();
            LeftOverInputStream originalInputStream = this.t.getOriginalInputStream();
            if (!originalInputStream.isClosed()) {
                originalInputStream.close();
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            this.closed = true;
            throw th;
        }
        this.closed = true;
        this.t.getHttpContext().getServerImpl().addEvent(new WriteFinishedEvent(this.t));
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.count > 0) {
            writeChunk();
        }
        this.out.flush();
    }
}
