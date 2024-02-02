package org.eclipse.jetty.io;

import org.eclipse.jetty.io.Buffers;
/* loaded from: classes.dex */
public class ThreadLocalBuffers extends AbstractBuffers {
    private final ThreadLocal<ThreadBuffers> _buffers;

    public ThreadLocalBuffers(Buffers.Type headerType, int headerSize, Buffers.Type bufferType, int bufferSize, Buffers.Type otherType) {
        super(headerType, headerSize, bufferType, bufferSize, otherType);
        this._buffers = new ThreadLocal<ThreadBuffers>() { // from class: org.eclipse.jetty.io.ThreadLocalBuffers.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // java.lang.ThreadLocal
            public ThreadBuffers initialValue() {
                return new ThreadBuffers();
            }
        };
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getBuffer() {
        ThreadBuffers buffers = this._buffers.get();
        if (buffers._buffer != null) {
            Buffer b = buffers._buffer;
            buffers._buffer = null;
            return b;
        }
        Buffer b2 = buffers._other;
        if (b2 != null && isBuffer(buffers._other)) {
            Buffer b3 = buffers._other;
            buffers._other = null;
            return b3;
        }
        Buffer b4 = newBuffer();
        return b4;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getHeader() {
        ThreadBuffers buffers = this._buffers.get();
        if (buffers._header != null) {
            Buffer b = buffers._header;
            buffers._header = null;
            return b;
        }
        Buffer b2 = buffers._other;
        if (b2 != null && isHeader(buffers._other)) {
            Buffer b3 = buffers._other;
            buffers._other = null;
            return b3;
        }
        Buffer b4 = newHeader();
        return b4;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getBuffer(int size) {
        ThreadBuffers buffers = this._buffers.get();
        if (buffers._other != null && buffers._other.capacity() == size) {
            Buffer b = buffers._other;
            buffers._other = null;
            return b;
        }
        Buffer b2 = newBuffer(size);
        return b2;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public void returnBuffer(Buffer buffer) {
        buffer.clear();
        if (buffer.isVolatile() || buffer.isImmutable()) {
            return;
        }
        ThreadBuffers buffers = this._buffers.get();
        if (buffers._header == null && isHeader(buffer)) {
            buffers._header = buffer;
        } else if (buffers._buffer == null && isBuffer(buffer)) {
            buffers._buffer = buffer;
        } else {
            buffers._other = buffer;
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffers
    public String toString() {
        return "{{" + getHeaderSize() + "," + getBufferSize() + "}}";
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public static class ThreadBuffers {
        Buffer _buffer;
        Buffer _header;
        Buffer _other;

        protected ThreadBuffers() {
        }
    }
}
