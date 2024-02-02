package org.eclipse.jetty.io;

import java.text.DateFormatSymbols;
import java.util.Locale;
import org.eclipse.jetty.util.DateCache;
/* loaded from: classes.dex */
public class BufferDateCache extends DateCache {
    Buffer _buffer;
    String _last;

    public BufferDateCache() {
    }

    public BufferDateCache(String format, DateFormatSymbols s) {
        super(format, s);
    }

    public BufferDateCache(String format, Locale l) {
        super(format, l);
    }

    public BufferDateCache(String format) {
        super(format);
    }

    public synchronized Buffer formatBuffer(long date) {
        String d = super.format(date);
        if (d == this._last) {
            return this._buffer;
        }
        this._last = d;
        this._buffer = new ByteArrayBuffer(d);
        return this._buffer;
    }
}
