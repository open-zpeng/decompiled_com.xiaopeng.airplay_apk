package org.eclipse.jetty.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.StringMap;
/* loaded from: classes.dex */
public class BufferCache {
    private final HashMap _bufferMap = new HashMap();
    private final StringMap _stringMap = new StringMap(true);
    private final ArrayList _index = new ArrayList();

    public CachedBuffer add(String value, int ordinal) {
        CachedBuffer buffer = new CachedBuffer(value, ordinal);
        this._bufferMap.put(buffer, buffer);
        this._stringMap.put(value, (Object) buffer);
        while (ordinal - this._index.size() >= 0) {
            this._index.add(null);
        }
        if (this._index.get(ordinal) == null) {
            this._index.add(ordinal, buffer);
        }
        return buffer;
    }

    public CachedBuffer get(int ordinal) {
        if (ordinal < 0 || ordinal >= this._index.size()) {
            return null;
        }
        return (CachedBuffer) this._index.get(ordinal);
    }

    public CachedBuffer get(Buffer buffer) {
        return (CachedBuffer) this._bufferMap.get(buffer);
    }

    public CachedBuffer get(String value) {
        return (CachedBuffer) this._stringMap.get(value);
    }

    public Buffer lookup(Buffer buffer) {
        if (buffer instanceof CachedBuffer) {
            return buffer;
        }
        Buffer b = get(buffer);
        if (b == null) {
            if (buffer instanceof Buffer.CaseInsensitve) {
                return buffer;
            }
            return new ByteArrayBuffer.CaseInsensitive(buffer.asArray(), 0, buffer.length(), 0);
        }
        return b;
    }

    public CachedBuffer getBest(byte[] value, int offset, int maxLength) {
        Map.Entry entry = this._stringMap.getBestEntry(value, offset, maxLength);
        if (entry != null) {
            return (CachedBuffer) entry.getValue();
        }
        return null;
    }

    public Buffer lookup(String value) {
        Buffer b = get(value);
        if (b == null) {
            return new CachedBuffer(value, -1);
        }
        return b;
    }

    public String toString(Buffer buffer) {
        return lookup(buffer).toString();
    }

    public int getOrdinal(String value) {
        CachedBuffer buffer = (CachedBuffer) this._stringMap.get(value);
        if (buffer == null) {
            return -1;
        }
        return buffer.getOrdinal();
    }

    public int getOrdinal(Buffer buffer) {
        if (buffer instanceof CachedBuffer) {
            return ((CachedBuffer) buffer).getOrdinal();
        }
        Buffer buffer2 = lookup(buffer);
        if (buffer2 != null && (buffer2 instanceof CachedBuffer)) {
            return ((CachedBuffer) buffer2).getOrdinal();
        }
        return -1;
    }

    /* loaded from: classes.dex */
    public static class CachedBuffer extends ByteArrayBuffer.CaseInsensitive {
        private HashMap _associateMap;
        private final int _ordinal;

        public CachedBuffer(String value, int ordinal) {
            super(value);
            this._associateMap = null;
            this._ordinal = ordinal;
        }

        public int getOrdinal() {
            return this._ordinal;
        }

        public CachedBuffer getAssociate(Object key) {
            if (this._associateMap == null) {
                return null;
            }
            return (CachedBuffer) this._associateMap.get(key);
        }

        public void setAssociate(Object key, CachedBuffer associate) {
            if (this._associateMap == null) {
                this._associateMap = new HashMap();
            }
            this._associateMap.put(key, associate);
        }
    }

    public String toString() {
        return "CACHE[bufferMap=" + this._bufferMap + ",stringMap=" + this._stringMap + ",index=" + this._index + "]";
    }
}
