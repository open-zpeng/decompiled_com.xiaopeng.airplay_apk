package org.eclipse.jetty.io;

import org.eclipse.jetty.io.Buffer;
/* loaded from: classes.dex */
public class View extends AbstractBuffer {
    Buffer _buffer;

    public View(Buffer buffer, int mark, int get, int put, int access) {
        super(2, !buffer.isImmutable());
        this._buffer = buffer.buffer();
        setPutIndex(put);
        setGetIndex(get);
        setMarkIndex(mark);
        this._access = access;
    }

    public View(Buffer buffer) {
        super(2, !buffer.isImmutable());
        this._buffer = buffer.buffer();
        setPutIndex(buffer.putIndex());
        setGetIndex(buffer.getIndex());
        setMarkIndex(buffer.markIndex());
        this._access = buffer.isReadOnly() ? 1 : 2;
    }

    public View() {
        super(2, true);
    }

    public void update(Buffer buffer) {
        this._access = 2;
        this._buffer = buffer.buffer();
        setGetIndex(0);
        setPutIndex(buffer.putIndex());
        setGetIndex(buffer.getIndex());
        setMarkIndex(buffer.markIndex());
        this._access = buffer.isReadOnly() ? 1 : 2;
    }

    public void update(int get, int put) {
        int a = this._access;
        this._access = 2;
        setGetIndex(0);
        setPutIndex(put);
        setGetIndex(get);
        setMarkIndex(-1);
        this._access = a;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte[] array() {
        return this._buffer.array();
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public Buffer buffer() {
        return this._buffer.buffer();
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int capacity() {
        return this._buffer.capacity();
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public void clear() {
        setMarkIndex(-1);
        setGetIndex(0);
        setPutIndex(this._buffer.getIndex());
        setGetIndex(this._buffer.getIndex());
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public void compact() {
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer
    public boolean equals(Object obj) {
        return this == obj || ((obj instanceof Buffer) && obj.equals(this)) || super.equals(obj);
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public boolean isReadOnly() {
        return this._buffer.isReadOnly();
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public boolean isVolatile() {
        return true;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte peek(int index) {
        return this._buffer.peek(index);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int peek(int index, byte[] b, int offset, int length) {
        return this._buffer.peek(index, b, offset, length);
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public Buffer peek(int index, int length) {
        return this._buffer.peek(index, length);
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, Buffer src) {
        return this._buffer.poke(index, src);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void poke(int index, byte b) {
        this._buffer.poke(index, b);
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, byte[] b, int offset, int length) {
        return this._buffer.poke(index, b, offset, length);
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer
    public String toString() {
        if (this._buffer == null) {
            return "INVALID";
        }
        return super.toString();
    }

    /* loaded from: classes.dex */
    public static class CaseInsensitive extends View implements Buffer.CaseInsensitve {
        public CaseInsensitive() {
        }

        public CaseInsensitive(Buffer buffer, int mark, int get, int put, int access) {
            super(buffer, mark, get, put, access);
        }

        public CaseInsensitive(Buffer buffer) {
            super(buffer);
        }

        @Override // org.eclipse.jetty.io.View, org.eclipse.jetty.io.AbstractBuffer
        public boolean equals(Object obj) {
            return this == obj || ((obj instanceof Buffer) && ((Buffer) obj).equalsIgnoreCase(this)) || super.equals(obj);
        }
    }
}
