package org.eclipse.jetty.io;

import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.nio.DirectNIOBuffer;
import org.eclipse.jetty.io.nio.IndirectNIOBuffer;
/* loaded from: classes.dex */
public abstract class AbstractBuffers implements Buffers {
    protected final int _bufferSize;
    protected final Buffers.Type _bufferType;
    protected final int _headerSize;
    protected final Buffers.Type _headerType;
    protected final Buffers.Type _otherType;

    public AbstractBuffers(Buffers.Type headerType, int headerSize, Buffers.Type bufferType, int bufferSize, Buffers.Type otherType) {
        this._headerType = headerType;
        this._headerSize = headerSize;
        this._bufferType = bufferType;
        this._bufferSize = bufferSize;
        this._otherType = otherType;
    }

    public int getBufferSize() {
        return this._bufferSize;
    }

    public int getHeaderSize() {
        return this._headerSize;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final Buffer newHeader() {
        switch (this._headerType) {
            case BYTE_ARRAY:
                return new ByteArrayBuffer(this._headerSize);
            case DIRECT:
                return new DirectNIOBuffer(this._headerSize);
            case INDIRECT:
                return new IndirectNIOBuffer(this._headerSize);
            default:
                throw new IllegalStateException();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final Buffer newBuffer() {
        switch (this._bufferType) {
            case BYTE_ARRAY:
                return new ByteArrayBuffer(this._bufferSize);
            case DIRECT:
                return new DirectNIOBuffer(this._bufferSize);
            case INDIRECT:
                return new IndirectNIOBuffer(this._bufferSize);
            default:
                throw new IllegalStateException();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final Buffer newBuffer(int size) {
        switch (this._otherType) {
            case BYTE_ARRAY:
                return new ByteArrayBuffer(size);
            case DIRECT:
                return new DirectNIOBuffer(size);
            case INDIRECT:
                return new IndirectNIOBuffer(size);
            default:
                throw new IllegalStateException();
        }
    }

    public final boolean isHeader(Buffer buffer) {
        if (buffer.capacity() == this._headerSize) {
            switch (this._headerType) {
                case BYTE_ARRAY:
                    return (buffer instanceof ByteArrayBuffer) && !(buffer instanceof IndirectNIOBuffer);
                case DIRECT:
                    return buffer instanceof DirectNIOBuffer;
                case INDIRECT:
                    return buffer instanceof IndirectNIOBuffer;
            }
        }
        return false;
    }

    public final boolean isBuffer(Buffer buffer) {
        if (buffer.capacity() == this._bufferSize) {
            switch (this._bufferType) {
                case BYTE_ARRAY:
                    return (buffer instanceof ByteArrayBuffer) && !(buffer instanceof IndirectNIOBuffer);
                case DIRECT:
                    return buffer instanceof DirectNIOBuffer;
                case INDIRECT:
                    return buffer instanceof IndirectNIOBuffer;
            }
        }
        return false;
    }

    public String toString() {
        return String.format("%s [%d,%d]", getClass().getSimpleName(), Integer.valueOf(this._headerSize), Integer.valueOf(this._bufferSize));
    }
}
