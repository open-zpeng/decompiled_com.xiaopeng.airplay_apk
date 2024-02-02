package org.eclipse.jetty.util;
/* loaded from: classes.dex */
public class Utf8StringBuilder extends Utf8Appendable {
    final StringBuilder _buffer;

    public Utf8StringBuilder() {
        super(new StringBuilder());
        this._buffer = (StringBuilder) this._appendable;
    }

    public Utf8StringBuilder(int capacity) {
        super(new StringBuilder(capacity));
        this._buffer = (StringBuilder) this._appendable;
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

    public StringBuilder getStringBuilder() {
        checkState();
        return this._buffer;
    }

    public String toString() {
        checkState();
        return this._buffer.toString();
    }
}
