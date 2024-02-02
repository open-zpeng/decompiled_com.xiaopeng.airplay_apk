package org.eclipse.jetty.io;

import java.io.IOException;
/* loaded from: classes.dex */
public class ByteArrayEndPoint implements ConnectedEndPoint {
    protected boolean _closed;
    protected Connection _connection;
    protected boolean _growOutput;
    protected ByteArrayBuffer _in;
    protected byte[] _inBytes;
    protected int _maxIdleTime;
    protected boolean _nonBlocking;
    protected ByteArrayBuffer _out;

    public ByteArrayEndPoint() {
    }

    @Override // org.eclipse.jetty.io.ConnectedEndPoint
    public Connection getConnection() {
        return this._connection;
    }

    @Override // org.eclipse.jetty.io.ConnectedEndPoint
    public void setConnection(Connection connection) {
        this._connection = connection;
    }

    public boolean isNonBlocking() {
        return this._nonBlocking;
    }

    public void setNonBlocking(boolean nonBlocking) {
        this._nonBlocking = nonBlocking;
    }

    public ByteArrayEndPoint(byte[] input, int outputSize) {
        this._inBytes = input;
        this._in = new ByteArrayBuffer(input);
        this._out = new ByteArrayBuffer(outputSize);
    }

    public ByteArrayBuffer getIn() {
        return this._in;
    }

    public void setIn(ByteArrayBuffer in) {
        this._in = in;
    }

    public ByteArrayBuffer getOut() {
        return this._out;
    }

    public void setOut(ByteArrayBuffer out) {
        this._out = out;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isOpen() {
        return !this._closed;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isInputShutdown() {
        return this._closed;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isOutputShutdown() {
        return this._closed;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean isBlocking() {
        return !this._nonBlocking;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean blockReadable(long millisecs) {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public boolean blockWritable(long millisecs) {
        return true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void shutdownOutput() throws IOException {
        close();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void shutdownInput() throws IOException {
        close();
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void close() throws IOException {
        this._closed = true;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int fill(Buffer buffer) throws IOException {
        if (this._closed) {
            throw new IOException("CLOSED");
        }
        if (this._in != null && this._in.length() > 0) {
            int len = buffer.put(this._in);
            this._in.skip(len);
            return len;
        } else if (this._in != null && this._in.length() == 0 && this._nonBlocking) {
            return 0;
        } else {
            close();
            return -1;
        }
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int flush(Buffer buffer) throws IOException {
        if (this._closed) {
            throw new IOException("CLOSED");
        }
        if (this._growOutput && buffer.length() > this._out.space()) {
            this._out.compact();
            if (buffer.length() > this._out.space()) {
                ByteArrayBuffer n = new ByteArrayBuffer(this._out.putIndex() + buffer.length());
                n.put(this._out.peek(0, this._out.putIndex()));
                if (this._out.getIndex() > 0) {
                    n.mark();
                    n.setGetIndex(this._out.getIndex());
                }
                this._out = n;
            }
        }
        int len = this._out.put(buffer);
        if (!buffer.isImmutable()) {
            buffer.skip(len);
        }
        return len;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
        if (this._closed) {
            throw new IOException("CLOSED");
        }
        int flushed = 0;
        if (header != null && header.length() > 0) {
            flushed = flush(header);
        }
        if (header == null || header.length() == 0) {
            if (buffer != null && buffer.length() > 0) {
                flushed += flush(buffer);
            }
            if ((buffer == null || buffer.length() == 0) && trailer != null && trailer.length() > 0) {
                return flushed + flush(trailer);
            }
            return flushed;
        }
        return flushed;
    }

    public void reset() {
        this._closed = false;
        this._in.clear();
        this._out.clear();
        if (this._inBytes != null) {
            this._in.setPutIndex(this._inBytes.length);
        }
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
        return this._inBytes;
    }

    @Override // org.eclipse.jetty.io.EndPoint
    public void flush() throws IOException {
    }

    public boolean isGrowOutput() {
        return this._growOutput;
    }

    public void setGrowOutput(boolean growOutput) {
        this._growOutput = growOutput;
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
