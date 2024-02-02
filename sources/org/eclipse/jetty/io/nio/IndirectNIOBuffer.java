package org.eclipse.jetty.io.nio;

import java.nio.ByteBuffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
/* loaded from: classes.dex */
public class IndirectNIOBuffer extends ByteArrayBuffer implements NIOBuffer {
    protected final ByteBuffer _buf;

    public IndirectNIOBuffer(int size) {
        super(size, 2, false);
        this._buf = ByteBuffer.wrap(this._bytes);
        this._buf.position(0);
        this._buf.limit(this._buf.capacity());
    }

    public IndirectNIOBuffer(ByteBuffer buffer, boolean immutable) {
        super(buffer.array(), 0, 0, immutable ? 0 : 2, false);
        if (buffer.isDirect()) {
            throw new IllegalArgumentException();
        }
        this._buf = buffer;
        this._get = buffer.position();
        this._put = buffer.limit();
        buffer.position(0);
        buffer.limit(buffer.capacity());
    }

    @Override // org.eclipse.jetty.io.nio.NIOBuffer
    public ByteBuffer getByteBuffer() {
        return this._buf;
    }

    @Override // org.eclipse.jetty.io.nio.NIOBuffer
    public boolean isDirect() {
        return false;
    }
}
