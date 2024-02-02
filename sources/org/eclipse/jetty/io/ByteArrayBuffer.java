package org.eclipse.jetty.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class ByteArrayBuffer extends AbstractBuffer {
    static final int MAX_WRITE = Integer.getInteger("org.eclipse.jetty.io.ByteArrayBuffer.MAX_WRITE", 131072).intValue();
    protected final byte[] _bytes;

    /* JADX INFO: Access modifiers changed from: protected */
    public ByteArrayBuffer(int size, int access, boolean isVolatile) {
        this(new byte[size], 0, 0, access, isVolatile);
    }

    public ByteArrayBuffer(byte[] bytes) {
        this(bytes, 0, bytes.length, 2);
    }

    public ByteArrayBuffer(byte[] bytes, int index, int length) {
        this(bytes, index, length, 2);
    }

    public ByteArrayBuffer(byte[] bytes, int index, int length, int access) {
        super(2, false);
        this._bytes = bytes;
        setPutIndex(index + length);
        setGetIndex(index);
        this._access = access;
    }

    public ByteArrayBuffer(byte[] bytes, int index, int length, int access, boolean isVolatile) {
        super(2, isVolatile);
        this._bytes = bytes;
        setPutIndex(index + length);
        setGetIndex(index);
        this._access = access;
    }

    public ByteArrayBuffer(int size) {
        this(new byte[size], 0, 0, 2);
        setPutIndex(0);
    }

    public ByteArrayBuffer(String value) {
        super(2, false);
        this._bytes = StringUtil.getBytes(value);
        setGetIndex(0);
        setPutIndex(this._bytes.length);
        this._access = 0;
        this._string = value;
    }

    public ByteArrayBuffer(String value, boolean immutable) {
        super(2, false);
        this._bytes = StringUtil.getBytes(value);
        setGetIndex(0);
        setPutIndex(this._bytes.length);
        if (immutable) {
            this._access = 0;
            this._string = value;
        }
    }

    public ByteArrayBuffer(String value, String encoding) throws UnsupportedEncodingException {
        super(2, false);
        this._bytes = value.getBytes(encoding);
        setGetIndex(0);
        setPutIndex(this._bytes.length);
        this._access = 0;
        this._string = value;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte[] array() {
        return this._bytes;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int capacity() {
        return this._bytes.length;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public void compact() {
        if (isReadOnly()) {
            throw new IllegalStateException("READONLY");
        }
        int s = markIndex() >= 0 ? markIndex() : getIndex();
        if (s > 0) {
            int length = putIndex() - s;
            if (length > 0) {
                System.arraycopy(this._bytes, s, this._bytes, 0, length);
            }
            if (markIndex() > 0) {
                setMarkIndex(markIndex() - s);
            }
            setGetIndex(getIndex() - s);
            setPutIndex(putIndex() - s);
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Buffer)) {
            return false;
        }
        if (obj instanceof Buffer.CaseInsensitve) {
            return equalsIgnoreCase((Buffer) obj);
        }
        Buffer b = (Buffer) obj;
        if (b.length() != length()) {
            return false;
        }
        if (this._hash != 0 && (obj instanceof AbstractBuffer)) {
            AbstractBuffer ab = (AbstractBuffer) obj;
            if (ab._hash != 0 && this._hash != ab._hash) {
                return false;
            }
        }
        int get = getIndex();
        int bi = b.putIndex();
        int i = putIndex();
        while (true) {
            int i2 = i - 1;
            if (i <= get) {
                return true;
            }
            byte b1 = this._bytes[i2];
            bi--;
            byte b2 = b.peek(bi);
            if (b1 != b2) {
                return false;
            }
            i = i2;
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public boolean equalsIgnoreCase(Buffer b) {
        if (b == this) {
            return true;
        }
        if (b == null || b.length() != length()) {
            return false;
        }
        if (this._hash != 0 && (b instanceof AbstractBuffer)) {
            AbstractBuffer ab = (AbstractBuffer) b;
            if (ab._hash != 0 && this._hash != ab._hash) {
                return false;
            }
        }
        int get = getIndex();
        int bi = b.putIndex();
        byte[] barray = b.array();
        if (barray == null) {
            int i = putIndex();
            while (true) {
                int i2 = i - 1;
                if (i <= get) {
                    break;
                }
                byte b1 = this._bytes[i2];
                bi--;
                byte b2 = b.peek(bi);
                if (b1 != b2) {
                    if (97 <= b1 && b1 <= 122) {
                        b1 = (byte) ((b1 - 97) + 65);
                    }
                    if (97 <= b2 && b2 <= 122) {
                        b2 = (byte) ((b2 - 97) + 65);
                    }
                    if (b1 != b2) {
                        return false;
                    }
                }
                i = i2;
            }
        } else {
            int i3 = putIndex();
            while (true) {
                int i4 = i3 - 1;
                if (i3 <= get) {
                    break;
                }
                byte b12 = this._bytes[i4];
                bi--;
                byte b22 = barray[bi];
                if (b12 != b22) {
                    if (97 <= b12 && b12 <= 122) {
                        b12 = (byte) ((b12 - 97) + 65);
                    }
                    if (97 <= b22 && b22 <= 122) {
                        b22 = (byte) ((b22 - 97) + 65);
                    }
                    if (b12 != b22) {
                        return false;
                    }
                }
                i3 = i4;
            }
        }
        return true;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public byte get() {
        byte[] bArr = this._bytes;
        int i = this._get;
        this._get = i + 1;
        return bArr[i];
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer
    public int hashCode() {
        if (this._hash == 0 || this._hashGet != this._get || this._hashPut != this._put) {
            int get = getIndex();
            int i = putIndex();
            while (true) {
                int i2 = i - 1;
                if (i <= get) {
                    break;
                }
                byte b = this._bytes[i2];
                if (97 <= b && b <= 122) {
                    b = (byte) ((b - 97) + 65);
                }
                this._hash = (31 * this._hash) + b;
                i = i2;
            }
            if (this._hash == 0) {
                this._hash = -1;
            }
            this._hashGet = this._get;
            this._hashPut = this._put;
        }
        int get2 = this._hash;
        return get2;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte peek(int index) {
        return this._bytes[index];
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int peek(int index, byte[] b, int offset, int length) {
        int l = length;
        if ((index + l <= capacity() || (l = capacity() - index) != 0) && l >= 0) {
            System.arraycopy(this._bytes, index, b, offset, l);
            return l;
        }
        return -1;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void poke(int index, byte b) {
        this._bytes[index] = b;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, Buffer src) {
        int i = 0;
        this._hash = 0;
        int length = src.length();
        if (index + length > capacity()) {
            length = capacity() - index;
        }
        byte[] src_array = src.array();
        if (src_array != null) {
            System.arraycopy(src_array, src.getIndex(), this._bytes, index, length);
        } else {
            int s = src.getIndex();
            while (i < length) {
                this._bytes[index] = src.peek(s);
                i++;
                index++;
                s++;
            }
        }
        return length;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int poke(int index, byte[] b, int offset, int length) {
        this._hash = 0;
        if (index + length > capacity()) {
            length = capacity() - index;
        }
        System.arraycopy(b, offset, this._bytes, index, length);
        return length;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public void writeTo(OutputStream out) throws IOException {
        int len = length();
        if (MAX_WRITE > 0 && len > MAX_WRITE) {
            int off = getIndex();
            while (len > 0) {
                int c = len > MAX_WRITE ? MAX_WRITE : len;
                out.write(this._bytes, off, c);
                off += c;
                len -= c;
            }
        } else {
            out.write(this._bytes, getIndex(), len);
        }
        if (!isImmutable()) {
            clear();
        }
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int readFrom(InputStream in, int max) throws IOException {
        if (max < 0 || max > space()) {
            max = space();
        }
        int p = putIndex();
        int total = 0;
        int len = 0;
        int p2 = p;
        int available = max;
        while (total < max) {
            len = in.read(this._bytes, p2, available);
            if (len < 0) {
                break;
            }
            if (len > 0) {
                p2 += len;
                total += len;
                available -= len;
                setPutIndex(p2);
            }
            if (in.available() <= 0) {
                break;
            }
        }
        if (len < 0 && total == 0) {
            return -1;
        }
        return total;
    }

    @Override // org.eclipse.jetty.io.AbstractBuffer, org.eclipse.jetty.io.Buffer
    public int space() {
        return this._bytes.length - this._put;
    }

    /* loaded from: classes.dex */
    public static class CaseInsensitive extends ByteArrayBuffer implements Buffer.CaseInsensitve {
        public CaseInsensitive(String s) {
            super(s);
        }

        public CaseInsensitive(byte[] b, int o, int l, int rw) {
            super(b, o, l, rw);
        }

        @Override // org.eclipse.jetty.io.ByteArrayBuffer, org.eclipse.jetty.io.AbstractBuffer
        public boolean equals(Object obj) {
            return (obj instanceof Buffer) && equalsIgnoreCase((Buffer) obj);
        }
    }
}
