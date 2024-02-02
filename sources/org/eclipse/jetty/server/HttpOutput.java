package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletOutputStream;
import org.eclipse.jetty.http.AbstractGenerator;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.util.ByteArrayOutputStream2;
/* loaded from: classes.dex */
public class HttpOutput extends ServletOutputStream {
    ByteArrayOutputStream2 _bytes;
    String _characterEncoding;
    char[] _chars;
    private boolean _closed;
    protected final AbstractHttpConnection _connection;
    Writer _converter;
    protected final AbstractGenerator _generator;
    private ByteArrayBuffer _onebyte;

    public HttpOutput(AbstractHttpConnection connection) {
        this._connection = connection;
        this._generator = (AbstractGenerator) connection.getGenerator();
    }

    public int getMaxIdleTime() {
        return this._connection.getMaxIdleTime();
    }

    public boolean isWritten() {
        return this._generator.getContentWritten() > 0;
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this._closed = true;
    }

    public boolean isClosed() {
        return this._closed;
    }

    public void reopen() {
        this._closed = false;
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        this._generator.flush(getMaxIdleTime());
    }

    @Override // java.io.OutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        write(new ByteArrayBuffer(b, off, len));
    }

    @Override // java.io.OutputStream
    public void write(byte[] b) throws IOException {
        write(new ByteArrayBuffer(b));
    }

    @Override // java.io.OutputStream
    public void write(int b) throws IOException {
        if (this._onebyte == null) {
            this._onebyte = new ByteArrayBuffer(1);
        } else {
            this._onebyte.clear();
        }
        this._onebyte.put((byte) b);
        write(this._onebyte);
    }

    private void write(Buffer buffer) throws IOException {
        if (this._closed) {
            throw new IOException("Closed");
        }
        if (!this._generator.isOpen()) {
            throw new EofException();
        }
        while (this._generator.isBufferFull()) {
            this._generator.blockForOutput(getMaxIdleTime());
            if (this._closed) {
                throw new IOException("Closed");
            }
            if (!this._generator.isOpen()) {
                throw new EofException();
            }
        }
        this._generator.addContent(buffer, false);
        if (this._generator.isAllContentWritten()) {
            flush();
            close();
        } else if (this._generator.isBufferFull()) {
            this._connection.commitResponse(false);
        }
        while (buffer.length() > 0 && this._generator.isOpen()) {
            this._generator.blockForOutput(getMaxIdleTime());
        }
    }

    @Override // javax.servlet.ServletOutputStream
    public void print(String s) throws IOException {
        write(s.getBytes());
    }
}
