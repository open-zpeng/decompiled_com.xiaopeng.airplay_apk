package org.eclipse.jetty.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.EndPoint;
/* loaded from: classes.dex */
public class StreamEndPoint implements EndPoint {
    InputStream _in;
    boolean _ishut;
    int _maxIdleTime;
    boolean _oshut;
    OutputStream _out;

    public StreamEndPoint(InputStream in, OutputStream out) {
        this._in = in;
        this._out = out;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isBlocking() {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean blockReadable(long millisecs) throws IOException {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean blockWritable(long millisecs) throws IOException {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isOpen() {
        return this._in != null;
    }

    public final boolean isClosed() {
        return !isOpen();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void shutdownOutput() throws IOException {
        this._oshut = true;
        if (this._ishut && this._out != null) {
            this._out.close();
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isInputShutdown() {
        return this._ishut;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void shutdownInput() throws IOException {
        this._ishut = true;
        if (this._oshut && this._in != null) {
            this._in.close();
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isOutputShutdown() {
        return this._oshut;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void close() throws IOException {
        if (this._in != null) {
            this._in.close();
        }
        this._in = null;
        if (this._out != null) {
            this._out.close();
        }
        this._out = null;
    }

    protected void idleExpired() throws IOException {
        if (this._in != null) {
            this._in.close();
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int fill(Buffer buffer) throws IOException {
        if (this._ishut) {
            return -1;
        }
        if (this._in == null) {
            return 0;
        }
        int space = buffer.space();
        if (space <= 0) {
            if (buffer.hasContent()) {
                return 0;
            }
            throw new IOException("FULL");
        }
        try {
            int filled = buffer.readFrom(this._in, space);
            if (filled < 0) {
                shutdownInput();
            }
            return filled;
        } catch (SocketTimeoutException e) {
            idleExpired();
            return -1;
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int flush(Buffer buffer) throws IOException {
        if (this._oshut) {
            return -1;
        }
        if (this._out == null) {
            return 0;
        }
        int length = buffer.length();
        if (length > 0) {
            buffer.writeTo(this._out);
        }
        if (!buffer.isImmutable()) {
            buffer.clear();
        }
        return length;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
        int tw;
        int tw2;
        int len = 0;
        if (header != null && (tw2 = header.length()) > 0) {
            int f = flush(header);
            len = f;
            if (f < tw2) {
                return len;
            }
        }
        if (buffer != null && (tw = buffer.length()) > 0) {
            int f2 = flush(buffer);
            if (f2 < 0) {
                return len > 0 ? len : f2;
            }
            len += f2;
            if (f2 < tw) {
                return len;
            }
        }
        if (trailer != null && trailer.length() > 0) {
            int f3 = flush(trailer);
            if (f3 < 0) {
                return len > 0 ? len : f3;
            }
            return len + f3;
        }
        return len;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getLocalAddr() {
        return null;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getLocalHost() {
        return null;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int getLocalPort() {
        return 0;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getRemoteAddr() {
        return null;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public String getRemoteHost() {
        return null;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int getRemotePort() {
        return 0;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public Object getTransport() {
        return null;
    }

    public InputStream getInputStream() {
        return this._in;
    }

    public void setInputStream(InputStream in) {
        this._in = in;
    }

    public OutputStream getOutputStream() {
        return this._out;
    }

    public void setOutputStream(OutputStream out) {
        this._out = out;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void flush() throws IOException {
        if (this._out != null) {
            this._out.flush();
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int getMaxIdleTime() {
        return this._maxIdleTime;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void setMaxIdleTime(int timeMs) throws IOException {
        this._maxIdleTime = timeMs;
    }
}
