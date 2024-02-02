package sun.net.httpserver;

import java.io.IOException;
import java.io.OutputStream;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: ExchangeImpl.java */
/* loaded from: classes.dex */
public class PlaceholderOutputStream extends OutputStream {
    OutputStream wrapped;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PlaceholderOutputStream(OutputStream outputStream) {
        this.wrapped = outputStream;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setWrappedStream(OutputStream outputStream) {
        this.wrapped = outputStream;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isWrapped() {
        return this.wrapped != null;
    }

    private void checkWrap() throws IOException {
        if (this.wrapped == null) {
            throw new IOException("response headers not sent yet");
        }
    }

    @Override // java.io.OutputStream
    public void write(int i) throws IOException {
        checkWrap();
        this.wrapped.write(i);
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr) throws IOException {
        checkWrap();
        this.wrapped.write(bArr);
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        checkWrap();
        this.wrapped.write(bArr, i, i2);
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        checkWrap();
        this.wrapped.flush();
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        checkWrap();
        this.wrapped.close();
    }
}
