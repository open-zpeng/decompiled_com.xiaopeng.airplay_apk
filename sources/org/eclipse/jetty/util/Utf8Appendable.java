package org.eclipse.jetty.util;

import java.io.IOException;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class Utf8Appendable {
    public static final char REPLACEMENT = 65533;
    private static final int UTF8_ACCEPT = 0;
    private static final int UTF8_REJECT = 12;
    protected final Appendable _appendable;
    private int _codep;
    protected int _state = 0;
    protected static final Logger LOG = Log.getLogger(Utf8Appendable.class);
    private static final byte[] BYTE_TABLE = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 11, 6, 6, 6, 5, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};
    private static final byte[] TRANS_TABLE = {0, 12, 24, 36, 60, 96, 84, 12, 12, 12, 48, 72, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 0, 12, 12, 12, 12, 12, 0, 12, 0, 12, 12, 12, 24, 12, 12, 12, 12, 12, 24, 12, 24, 12, 12, 12, 12, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 12, 12, 12, 12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12};

    public abstract int length();

    public Utf8Appendable(Appendable appendable) {
        this._appendable = appendable;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reset() {
        this._state = 0;
    }

    public void append(byte b) {
        try {
            appendByte(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void append(byte[] b, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            try {
                appendByte(b[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean append(byte[] b, int offset, int length, int maxChars) {
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            try {
                if (length() > maxChars) {
                    return false;
                }
                appendByte(b[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    protected void appendByte(byte b) throws IOException {
        if (b > 0 && this._state == 0) {
            this._appendable.append((char) (b & 255));
            return;
        }
        int i = b & 255;
        int type = BYTE_TABLE[i];
        this._codep = this._state == 0 ? (255 >> type) & i : (i & 63) | (this._codep << 6);
        int next = TRANS_TABLE[this._state + type];
        if (next == 0) {
            this._state = next;
            if (this._codep < 55296) {
                this._appendable.append((char) this._codep);
                return;
            }
            char[] arr$ = Character.toChars(this._codep);
            for (char c : arr$) {
                this._appendable.append(c);
            }
        } else if (next == 12) {
            String reason = "byte " + TypeUtil.toHexString(b) + " in state " + (this._state / 12);
            this._codep = 0;
            this._state = 0;
            this._appendable.append(REPLACEMENT);
            throw new NotUtf8Exception(reason);
        } else {
            this._state = next;
        }
    }

    public boolean isUtf8SequenceComplete() {
        return this._state == 0;
    }

    /* loaded from: classes.dex */
    public static class NotUtf8Exception extends IllegalArgumentException {
        public NotUtf8Exception(String reason) {
            super("Not valid UTF8! " + reason);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void checkState() {
        if (!isUtf8SequenceComplete()) {
            this._codep = 0;
            this._state = 0;
            try {
                this._appendable.append(REPLACEMENT);
                throw new NotUtf8Exception("incomplete UTF8 sequence");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String toReplacedString() {
        if (!isUtf8SequenceComplete()) {
            this._codep = 0;
            this._state = 0;
            try {
                this._appendable.append(REPLACEMENT);
                Throwable th = new NotUtf8Exception("incomplete UTF8 sequence");
                LOG.warn(th.toString(), new Object[0]);
                LOG.debug(th);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this._appendable.toString();
    }
}
