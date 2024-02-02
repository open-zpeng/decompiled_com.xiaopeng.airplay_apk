package org.eclipse.jetty.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class AbstractBuffer implements Buffer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected static final String __IMMUTABLE = "IMMUTABLE";
    protected static final String __READONLY = "READONLY";
    protected static final String __READWRITE = "READWRITE";
    protected static final String __VOLATILE = "VOLATILE";
    protected int _access;
    protected int _get;
    protected int _hash;
    protected int _hashGet;
    protected int _hashPut;
    protected int _mark;
    protected int _put;
    protected String _string;
    protected View _view;
    protected boolean _volatile;
    private static final Logger LOG = Log.getLogger(AbstractBuffer.class);
    private static final boolean __boundsChecking = Boolean.getBoolean("org.eclipse.jetty.io.AbstractBuffer.boundsChecking");

    public AbstractBuffer(int access, boolean isVolatile) {
        if (access == 0 && isVolatile) {
            throw new IllegalArgumentException("IMMUTABLE && VOLATILE");
        }
        setMarkIndex(-1);
        this._access = access;
        this._volatile = isVolatile;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte[] asArray() {
        byte[] bytes = new byte[length()];
        byte[] array = array();
        if (array != null) {
            System.arraycopy(array, getIndex(), bytes, 0, bytes.length);
        } else {
            peek(getIndex(), bytes, 0, length());
        }
        return bytes;
    }

    public ByteArrayBuffer duplicate(int access) {
        Buffer b = buffer();
        if ((this instanceof Buffer.CaseInsensitve) || (b instanceof Buffer.CaseInsensitve)) {
            return new ByteArrayBuffer.CaseInsensitive(asArray(), 0, length(), access);
        }
        return new ByteArrayBuffer(asArray(), 0, length(), access);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer asNonVolatileBuffer() {
        return !isVolatile() ? this : duplicate(this._access);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer asImmutableBuffer() {
        return isImmutable() ? this : duplicate(0);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer asReadOnlyBuffer() {
        return isReadOnly() ? this : new View(this, markIndex(), getIndex(), putIndex(), 1);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer asMutableBuffer() {
        if (isImmutable()) {
            Buffer b = buffer();
            if (b.isReadOnly()) {
                return duplicate(2);
            }
            return new View(b, markIndex(), getIndex(), putIndex(), this._access);
        }
        return this;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer buffer() {
        return this;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void clear() {
        setMarkIndex(-1);
        setGetIndex(0);
        setPutIndex(0);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void compact() {
        if (isReadOnly()) {
            throw new IllegalStateException(__READONLY);
        }
        int s = markIndex() >= 0 ? markIndex() : getIndex();
        if (s > 0) {
            byte[] array = array();
            int length = putIndex() - s;
            if (length > 0) {
                if (array != null) {
                    System.arraycopy(array(), s, array(), 0, length);
                } else {
                    poke(0, peek(s, length));
                }
            }
            if (markIndex() > 0) {
                setMarkIndex(markIndex() - s);
            }
            setGetIndex(getIndex() - s);
            setPutIndex(putIndex() - s);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Buffer)) {
            return false;
        }
        Buffer b = (Buffer) obj;
        if ((this instanceof Buffer.CaseInsensitve) || (b instanceof Buffer.CaseInsensitve)) {
            return equalsIgnoreCase(b);
        }
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
            byte b1 = peek(i2);
            bi--;
            byte b2 = b.peek(bi);
            if (b1 != b2) {
                return false;
            }
            i = i2;
        }
    }

    @Override // org.eclipse.jetty.io.Buffer
    public boolean equalsIgnoreCase(Buffer b) {
        if (b == this) {
            return true;
        }
        if (b.length() != length()) {
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
        byte[] array = array();
        byte[] barray = b.array();
        if (array != null && barray != null) {
            int i = putIndex();
            while (true) {
                int i2 = i - 1;
                if (i <= get) {
                    break;
                }
                byte b1 = array[i2];
                bi--;
                byte b2 = barray[bi];
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
                byte b12 = peek(i4);
                bi--;
                byte b22 = b.peek(bi);
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

    @Override // org.eclipse.jetty.io.Buffer
    public byte get() {
        int i = this._get;
        this._get = i + 1;
        return peek(i);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int get(byte[] b, int offset, int length) {
        int gi = getIndex();
        int l = length();
        if (l == 0) {
            return -1;
        }
        if (length > l) {
            length = l;
        }
        int length2 = peek(gi, b, offset, length);
        if (length2 > 0) {
            setGetIndex(gi + length2);
        }
        return length2;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer get(int length) {
        int gi = getIndex();
        Buffer view = peek(gi, length);
        setGetIndex(gi + length);
        return view;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public final int getIndex() {
        return this._get;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public boolean hasContent() {
        return this._put > this._get;
    }

    public int hashCode() {
        if (this._hash == 0 || this._hashGet != this._get || this._hashPut != this._put) {
            int get = getIndex();
            byte[] array = array();
            if (array == null) {
                int i = putIndex();
                while (true) {
                    int i2 = i - 1;
                    if (i <= get) {
                        break;
                    }
                    byte b = peek(i2);
                    if (97 <= b && b <= 122) {
                        b = (byte) ((b - 97) + 65);
                    }
                    this._hash = (this._hash * 31) + b;
                    i = i2;
                }
            } else {
                int i3 = putIndex();
                while (true) {
                    int i4 = i3 - 1;
                    if (i3 <= get) {
                        break;
                    }
                    byte b2 = array[i4];
                    if (97 <= b2 && b2 <= 122) {
                        b2 = (byte) ((b2 - 97) + 65);
                    }
                    this._hash = (this._hash * 31) + b2;
                    i3 = i4;
                }
            }
            if (this._hash == 0) {
                this._hash = -1;
            }
            this._hashGet = this._get;
            this._hashPut = this._put;
        }
        return this._hash;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public boolean isImmutable() {
        return this._access <= 0;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public boolean isReadOnly() {
        return this._access <= 1;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public boolean isVolatile() {
        return this._volatile;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int length() {
        return this._put - this._get;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void mark() {
        setMarkIndex(this._get - 1);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void mark(int offset) {
        setMarkIndex(this._get + offset);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int markIndex() {
        return this._mark;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public byte peek() {
        return peek(this._get);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer peek(int index, int length) {
        if (this._view == null) {
            this._view = new View(this, -1, index, index + length, isReadOnly() ? 1 : 2);
        } else {
            this._view.update(buffer());
            this._view.setMarkIndex(-1);
            this._view.setGetIndex(0);
            this._view.setPutIndex(index + length);
            this._view.setGetIndex(index);
        }
        return this._view;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int poke(int index, Buffer src) {
        int i = 0;
        this._hash = 0;
        int length = src.length();
        if (index + length > capacity()) {
            length = capacity() - index;
        }
        byte[] src_array = src.array();
        byte[] dst_array = array();
        if (src_array != null && dst_array != null) {
            System.arraycopy(src_array, src.getIndex(), dst_array, index, length);
        } else if (src_array != null) {
            int s = src.getIndex();
            while (i < length) {
                poke(index, src_array[s]);
                i++;
                index++;
                s++;
            }
        } else if (dst_array != null) {
            int s2 = src.getIndex();
            while (i < length) {
                dst_array[index] = src.peek(s2);
                i++;
                index++;
                s2++;
            }
        } else {
            int s3 = src.getIndex();
            while (i < length) {
                poke(index, src.peek(s3));
                i++;
                index++;
                s3++;
            }
        }
        return length;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int poke(int index, byte[] b, int offset, int length) {
        int i = 0;
        this._hash = 0;
        if (index + length > capacity()) {
            length = capacity() - index;
        }
        byte[] dst_array = array();
        if (dst_array != null) {
            System.arraycopy(b, offset, dst_array, index, length);
        } else {
            int s = offset;
            while (i < length) {
                poke(index, b[s]);
                i++;
                index++;
                s++;
            }
        }
        return length;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int put(Buffer src) {
        int pi = putIndex();
        int l = poke(pi, src);
        setPutIndex(pi + l);
        return l;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void put(byte b) {
        int pi = putIndex();
        poke(pi, b);
        setPutIndex(pi + 1);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int put(byte[] b, int offset, int length) {
        int pi = putIndex();
        int l = poke(pi, b, offset, length);
        setPutIndex(pi + l);
        return l;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int put(byte[] b) {
        int pi = putIndex();
        int l = poke(pi, b, 0, b.length);
        setPutIndex(pi + l);
        return l;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public final int putIndex() {
        return this._put;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void reset() {
        if (markIndex() >= 0) {
            setGetIndex(markIndex());
        }
    }

    public void rewind() {
        setGetIndex(0);
        setMarkIndex(-1);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void setGetIndex(int getIndex) {
        this._get = getIndex;
        this._hash = 0;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void setMarkIndex(int index) {
        this._mark = index;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void setPutIndex(int putIndex) {
        this._put = putIndex;
        this._hash = 0;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int skip(int n) {
        if (length() < n) {
            n = length();
        }
        setGetIndex(getIndex() + n);
        return n;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer slice() {
        return peek(getIndex(), length());
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer sliceFromMark() {
        return sliceFromMark((getIndex() - markIndex()) - 1);
    }

    @Override // org.eclipse.jetty.io.Buffer
    public Buffer sliceFromMark(int length) {
        if (markIndex() < 0) {
            return null;
        }
        Buffer view = peek(markIndex(), length);
        setMarkIndex(-1);
        return view;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int space() {
        return capacity() - this._put;
    }

    @Override // org.eclipse.jetty.io.Buffer
    public String toDetailString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append(super.hashCode());
        buf.append(",");
        buf.append(buffer().hashCode());
        buf.append(",m=");
        buf.append(markIndex());
        buf.append(",g=");
        buf.append(getIndex());
        buf.append(",p=");
        buf.append(putIndex());
        buf.append(",c=");
        buf.append(capacity());
        buf.append("]={");
        if (markIndex() >= 0) {
            for (int i = markIndex(); i < getIndex(); i++) {
                byte b = peek(i);
                TypeUtil.toHex(b, (Appendable) buf);
            }
            buf.append("}{");
        }
        int count = 0;
        int i2 = getIndex();
        while (i2 < putIndex()) {
            byte b2 = peek(i2);
            TypeUtil.toHex(b2, (Appendable) buf);
            int count2 = count + 1;
            if (count == 50) {
                int count3 = putIndex();
                if (count3 - i2 > 20) {
                    buf.append(" ... ");
                    i2 = putIndex() - 20;
                }
            }
            i2++;
            count = count2;
        }
        buf.append('}');
        return buf.toString();
    }

    public String toString() {
        if (isImmutable()) {
            if (this._string == null) {
                this._string = new String(asArray(), 0, length());
            }
            return this._string;
        }
        return new String(asArray(), 0, length());
    }

    @Override // org.eclipse.jetty.io.Buffer
    public String toString(String charset) {
        try {
            byte[] bytes = array();
            if (bytes != null) {
                return new String(bytes, getIndex(), length(), charset);
            }
            return new String(asArray(), 0, length(), charset);
        } catch (Exception e) {
            LOG.warn(e);
            return new String(asArray(), 0, length());
        }
    }

    @Override // org.eclipse.jetty.io.Buffer
    public String toString(Charset charset) {
        try {
            byte[] bytes = array();
            if (bytes != null) {
                return new String(bytes, getIndex(), length(), charset);
            }
            return new String(asArray(), 0, length(), charset);
        } catch (Exception e) {
            LOG.warn(e);
            return new String(asArray(), 0, length());
        }
    }

    public String toDebugString() {
        return getClass() + "@" + super.hashCode();
    }

    @Override // org.eclipse.jetty.io.Buffer
    public void writeTo(OutputStream out) throws IOException {
        byte[] array = array();
        if (array != null) {
            out.write(array, getIndex(), length());
        } else {
            int len = length();
            byte[] buf = new byte[len <= 1024 ? len : 1024];
            int offset = this._get;
            while (len > 0) {
                int l = peek(offset, buf, 0, len > buf.length ? buf.length : len);
                out.write(buf, 0, l);
                offset += l;
                len -= l;
            }
        }
        clear();
    }

    @Override // org.eclipse.jetty.io.Buffer
    public int readFrom(InputStream in, int max) throws IOException {
        byte[] array = array();
        int s = space();
        if (s > max) {
            s = max;
        }
        if (array != null) {
            int l = in.read(array, this._put, s);
            if (l > 0) {
                this._put += l;
            }
            return l;
        }
        byte[] buf = new byte[s <= 1024 ? s : 1024];
        int s2 = s;
        while (s2 > 0) {
            int l2 = in.read(buf, 0, buf.length);
            if (l2 < 0) {
                return 0 > 0 ? 0 : -1;
            }
            put(buf, 0, l2);
            s2 -= l2;
        }
        return 0;
    }
}
