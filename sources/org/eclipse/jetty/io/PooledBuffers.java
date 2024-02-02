package org.eclipse.jetty.io;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.io.Buffers;
/* loaded from: classes.dex */
public class PooledBuffers extends AbstractBuffers {
    private final Queue<Buffer> _buffers;
    private final Queue<Buffer> _headers;
    private final int _maxSize;
    private final boolean _otherBuffers;
    private final boolean _otherHeaders;
    private final Queue<Buffer> _others;
    private final AtomicInteger _size;

    public PooledBuffers(Buffers.Type headerType, int headerSize, Buffers.Type bufferType, int bufferSize, Buffers.Type otherType, int maxSize) {
        super(headerType, headerSize, bufferType, bufferSize, otherType);
        this._size = new AtomicInteger();
        this._headers = new ConcurrentLinkedQueue();
        this._buffers = new ConcurrentLinkedQueue();
        this._others = new ConcurrentLinkedQueue();
        this._otherHeaders = headerType == otherType;
        this._otherBuffers = bufferType == otherType;
        this._maxSize = maxSize;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getHeader() {
        Buffer buffer = this._headers.poll();
        if (buffer == null) {
            return newHeader();
        }
        this._size.decrementAndGet();
        return buffer;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getBuffer() {
        Buffer buffer = this._buffers.poll();
        if (buffer == null) {
            return newBuffer();
        }
        this._size.decrementAndGet();
        return buffer;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public Buffer getBuffer(int size) {
        if (this._otherHeaders && size == getHeaderSize()) {
            return getHeader();
        }
        if (this._otherBuffers && size == getBufferSize()) {
            return getBuffer();
        }
        Buffer buffer = this._others.poll();
        while (buffer != null && buffer.capacity() != size) {
            this._size.decrementAndGet();
            Buffer buffer2 = this._others.poll();
            buffer = buffer2;
        }
        if (buffer == null) {
            Buffer buffer3 = newBuffer(size);
            return buffer3;
        }
        this._size.decrementAndGet();
        return buffer;
    }

    @Override // org.eclipse.jetty.io.Buffers
    public void returnBuffer(Buffer buffer) {
        buffer.clear();
        if (buffer.isVolatile() || buffer.isImmutable()) {
            return;
        }
        if (this._size.incrementAndGet() > this._maxSize) {
            this._size.decrementAndGet();
        } else if (isHeader(buffer)) {
            this._headers.add(buffer);
        } else if (isBuffer(buffer)) {
            this._buffers.add(buffer);
        } else {
            this._others.add(buffer);
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffers
    public String toString() {
        return String.format("%s [%d/%d@%d,%d/%d@%d,%d/%d@-]", getClass().getSimpleName(), Integer.valueOf(this._headers.size()), Integer.valueOf(this._maxSize), Integer.valueOf(this._headerSize), Integer.valueOf(this._buffers.size()), Integer.valueOf(this._maxSize), Integer.valueOf(this._bufferSize), Integer.valueOf(this._others.size()), Integer.valueOf(this._maxSize));
    }
}
