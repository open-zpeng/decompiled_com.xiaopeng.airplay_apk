package org.eclipse.jetty.io;
/* loaded from: classes.dex */
public class SimpleBuffers implements Buffers {
    final Buffer _buffer;
    boolean _bufferOut;
    final Buffer _header;
    boolean _headerOut;

    public SimpleBuffers(Buffer header, Buffer buffer) {
        this._header = header;
        this._buffer = buffer;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getBuffer() {
        synchronized (this) {
            if (this._buffer != null && !this._bufferOut) {
                this._bufferOut = true;
                return this._buffer;
            } else if (this._buffer != null && this._header != null && this._header.capacity() == this._buffer.capacity() && !this._headerOut) {
                this._headerOut = true;
                return this._header;
            } else if (this._buffer != null) {
                return new ByteArrayBuffer(this._buffer.capacity());
            } else {
                return new ByteArrayBuffer(4096);
            }
        }
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getHeader() {
        synchronized (this) {
            if (this._header != null && !this._headerOut) {
                this._headerOut = true;
                return this._header;
            } else if (this._buffer != null && this._header != null && this._header.capacity() == this._buffer.capacity() && !this._bufferOut) {
                this._bufferOut = true;
                return this._buffer;
            } else if (this._header != null) {
                return new ByteArrayBuffer(this._header.capacity());
            } else {
                return new ByteArrayBuffer(4096);
            }
        }
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getBuffer(int size) {
        synchronized (this) {
            if (this._header != null && this._header.capacity() == size) {
                return getHeader();
            } else if (this._buffer != null && this._buffer.capacity() == size) {
                return getBuffer();
            } else {
                return null;
            }
        }
    }

    @Override // org.eclipse.jetty.io.Buffers
    public void returnBuffer(Buffer buffer) {
        synchronized (this) {
            buffer.clear();
            if (buffer == this._header) {
                this._headerOut = false;
            }
            if (buffer == this._buffer) {
                this._bufferOut = false;
            }
        }
    }
}
