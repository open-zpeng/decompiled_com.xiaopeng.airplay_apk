package org.eclipse.jetty.util;
/* loaded from: classes.dex */
public class Utf8StringBuffer extends Utf8Appendable {
    final StringBuffer _buffer;

    public Utf8StringBuffer() {
        super(new StringBuffer());
        this._buffer = (StringBuffer) this._appendable;
    }

    public Utf8StringBuffer(int capacity) {
        super(new StringBuffer(capacity));
        this._buffer = (StringBuffer) this._appendable;
    }

    @Override // org.eclipse.jetty.util.Utf8Appendable
    public int length() {
        return this._buffer.length();
    }

    @Override // org.eclipse.jetty.util.Utf8Appendable
    public void reset() {
        super.reset();
        this._buffer.setLength(0);
    }

    public StringBuffer getStringBuffer() {
        checkState();
        return this._buffer;
    }

    public String toString() {
        checkState();
        return this._buffer.toString();
    }
}
