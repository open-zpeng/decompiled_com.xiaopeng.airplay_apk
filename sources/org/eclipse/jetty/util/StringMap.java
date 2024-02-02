package org.eclipse.jetty.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class StringMap extends AbstractMap implements Externalizable {
    public static final boolean CASE_INSENSTIVE = true;
    protected static final int __HASH_WIDTH = 17;
    protected HashSet _entrySet;
    protected boolean _ignoreCase;
    protected NullEntry _nullEntry;
    protected Object _nullValue;
    protected Node _root;
    protected Set _umEntrySet;
    protected int _width;

    public StringMap() {
        this._width = 17;
        this._root = new Node();
        this._ignoreCase = false;
        this._nullEntry = null;
        this._nullValue = null;
        this._entrySet = new HashSet(3);
        this._umEntrySet = Collections.unmodifiableSet(this._entrySet);
    }

    public StringMap(boolean ignoreCase) {
        this();
        this._ignoreCase = ignoreCase;
    }

    public StringMap(boolean ignoreCase, int width) {
        this();
        this._ignoreCase = ignoreCase;
        this._width = width;
    }

    public void setIgnoreCase(boolean ic) {
        if (this._root._children != null) {
            throw new IllegalStateException("Must be set before first put");
        }
        this._ignoreCase = ic;
    }

    public boolean isIgnoreCase() {
        return this._ignoreCase;
    }

    public void setWidth(int width) {
        this._width = width;
    }

    public int getWidth() {
        return this._width;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Object put(Object key, Object value) {
        if (key == null) {
            return put((String) null, value);
        }
        return put(key.toString(), value);
    }

    public Object put(String key, Object value) {
        if (key == null) {
            Object oldValue = this._nullValue;
            this._nullValue = value;
            if (this._nullEntry == null) {
                this._nullEntry = new NullEntry();
                this._entrySet.add(this._nullEntry);
            }
            return oldValue;
        }
        Node node = this._root;
        int ni = -1;
        Node parent = null;
        Node prev = null;
        Node node2 = node;
        int i = 0;
        while (true) {
            if (i >= key.length()) {
                break;
            }
            char c = key.charAt(i);
            if (ni == -1) {
                parent = node2;
                prev = null;
                ni = 0;
                node2 = node2._children == null ? null : node2._children[c % this._width];
            }
            while (node2 != null) {
                if (node2._char[ni] == c || (this._ignoreCase && node2._ochar[ni] == c)) {
                    prev = null;
                    ni++;
                    if (ni == node2._char.length) {
                        ni = -1;
                    }
                } else if (ni == 0) {
                    prev = node2;
                    node2 = node2._next;
                } else {
                    node2.split(this, ni);
                    i--;
                    ni = -1;
                }
                i++;
            }
            node2 = new Node(this._ignoreCase, key, i);
            if (prev != null) {
                prev._next = node2;
            } else if (parent != null) {
                if (parent._children == null) {
                    parent._children = new Node[this._width];
                }
                parent._children[c % this._width] = node2;
                int oi = node2._ochar[0] % this._width;
                if (node2._ochar != null && node2._char[0] % this._width != oi) {
                    if (parent._children[oi] == null) {
                        parent._children[oi] = node2;
                    } else {
                        Node n = parent._children[oi];
                        while (n._next != null) {
                            n = n._next;
                        }
                        n._next = node2;
                    }
                }
            } else {
                this._root = node2;
            }
        }
        if (node2 == null) {
            return null;
        }
        if (ni > 0) {
            node2.split(this, ni);
        }
        Object old = node2._value;
        node2._key = key;
        node2._value = value;
        this._entrySet.add(node2);
        return old;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Object get(Object key) {
        if (key == null) {
            return this._nullValue;
        }
        if (key instanceof String) {
            return get((String) key);
        }
        return get(key.toString());
    }

    public Object get(String key) {
        if (key == null) {
            return this._nullValue;
        }
        Map.Entry entry = getEntry(key, 0, key.length());
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public Map.Entry getEntry(String key, int offset, int length) {
        if (key == null) {
            return this._nullEntry;
        }
        Node node = this._root;
        int ni = -1;
        for (int i = 0; i < length; i++) {
            char c = key.charAt(offset + i);
            if (ni == -1) {
                ni = 0;
                node = node._children == null ? null : node._children[c % this._width];
            }
            while (node != null) {
                if (node._char[ni] == c || (this._ignoreCase && node._ochar[ni] == c)) {
                    ni++;
                    if (ni == node._char.length) {
                        ni = -1;
                    }
                } else if (ni > 0) {
                    return null;
                } else {
                    node = node._next;
                }
            }
            return null;
        }
        if (ni > 0) {
            return null;
        }
        if (node == null || node._key != null) {
            return node;
        }
        return null;
    }

    public Map.Entry getEntry(char[] key, int offset, int length) {
        if (key == null) {
            return this._nullEntry;
        }
        Node node = this._root;
        int ni = -1;
        for (int i = 0; i < length; i++) {
            char c = key[offset + i];
            if (ni == -1) {
                ni = 0;
                node = node._children == null ? null : node._children[c % this._width];
            }
            while (node != null) {
                if (node._char[ni] == c || (this._ignoreCase && node._ochar[ni] == c)) {
                    ni++;
                    if (ni == node._char.length) {
                        ni = -1;
                    }
                } else if (ni > 0) {
                    return null;
                } else {
                    node = node._next;
                }
            }
            return null;
        }
        if (ni > 0) {
            return null;
        }
        if (node == null || node._key != null) {
            return node;
        }
        return null;
    }

    public Map.Entry getBestEntry(byte[] key, int offset, int maxLength) {
        if (key == null) {
            return this._nullEntry;
        }
        Node node = this._root;
        int ni = -1;
        for (int i = 0; i < maxLength; i++) {
            char c = (char) key[offset + i];
            if (ni == -1) {
                ni = 0;
                Node child = node._children == null ? null : node._children[c % this._width];
                if (child == null && i > 0) {
                    return node;
                }
                node = child;
            }
            while (node != null) {
                if (node._char[ni] == c || (this._ignoreCase && node._ochar[ni] == c)) {
                    ni++;
                    if (ni == node._char.length) {
                        ni = -1;
                    }
                } else if (ni > 0) {
                    return null;
                } else {
                    node = node._next;
                }
            }
            return null;
        }
        if (ni > 0) {
            return null;
        }
        if (node == null || node._key != null) {
            return node;
        }
        return null;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Object remove(Object key) {
        if (key == null) {
            return remove((String) null);
        }
        return remove(key.toString());
    }

    public Object remove(String key) {
        if (key == null) {
            Object oldValue = this._nullValue;
            if (this._nullEntry != null) {
                this._entrySet.remove(this._nullEntry);
                this._nullEntry = null;
                this._nullValue = null;
            }
            return oldValue;
        }
        Node node = this._root;
        int ni = -1;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (ni == -1) {
                ni = 0;
                node = node._children == null ? null : node._children[c % this._width];
            }
            while (node != null) {
                if (node._char[ni] == c || (this._ignoreCase && node._ochar[ni] == c)) {
                    ni++;
                    if (ni == node._char.length) {
                        ni = -1;
                    }
                } else if (ni > 0) {
                    return null;
                } else {
                    node = node._next;
                }
            }
            return null;
        }
        if (ni > 0) {
            return null;
        }
        if (node != null && node._key == null) {
            return null;
        }
        Object old = node._value;
        this._entrySet.remove(node);
        node._value = null;
        node._key = null;
        return old;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Set entrySet() {
        return this._umEntrySet;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public int size() {
        return this._entrySet.size();
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean isEmpty() {
        return this._entrySet.isEmpty();
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean containsKey(Object key) {
        if (key == null) {
            return this._nullEntry != null;
        }
        return getEntry(key.toString(), 0, key == null ? 0 : key.toString().length()) != null;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public void clear() {
        this._root = new Node();
        this._nullEntry = null;
        this._nullValue = null;
        this._entrySet.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Node implements Map.Entry {
        char[] _char;
        Node[] _children;
        String _key;
        Node _next;
        char[] _ochar;
        Object _value;

        Node() {
        }

        Node(boolean ignoreCase, String s, int offset) {
            int l = s.length() - offset;
            this._char = new char[l];
            this._ochar = new char[l];
            for (int i = 0; i < l; i++) {
                char c = s.charAt(offset + i);
                this._char[i] = c;
                if (ignoreCase) {
                    char o = c;
                    if (Character.isUpperCase(c)) {
                        o = Character.toLowerCase(c);
                    } else if (Character.isLowerCase(c)) {
                        o = Character.toUpperCase(c);
                    }
                    this._ochar[i] = o;
                }
            }
        }

        Node split(StringMap map, int offset) {
            Node split = new Node();
            int sl = this._char.length - offset;
            char[] tmp = this._char;
            this._char = new char[offset];
            split._char = new char[sl];
            System.arraycopy(tmp, 0, this._char, 0, offset);
            System.arraycopy(tmp, offset, split._char, 0, sl);
            if (this._ochar != null) {
                char[] tmp2 = this._ochar;
                this._ochar = new char[offset];
                split._ochar = new char[sl];
                System.arraycopy(tmp2, 0, this._ochar, 0, offset);
                System.arraycopy(tmp2, offset, split._ochar, 0, sl);
            }
            split._key = this._key;
            split._value = this._value;
            this._key = null;
            this._value = null;
            if (map._entrySet.remove(this)) {
                map._entrySet.add(split);
            }
            split._children = this._children;
            this._children = new Node[map._width];
            this._children[split._char[0] % map._width] = split;
            if (split._ochar != null && this._children[split._ochar[0] % map._width] != split) {
                this._children[split._ochar[0] % map._width] = split;
            }
            return split;
        }

        @Override // java.util.Map.Entry
        public Object getKey() {
            return this._key;
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            return this._value;
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object o) {
            Object old = this._value;
            this._value = o;
            return old;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            toString(buf);
            return buf.toString();
        }

        private void toString(StringBuilder buf) {
            buf.append("{[");
            int i = 0;
            if (this._char == null) {
                buf.append('-');
            } else {
                for (int i2 = 0; i2 < this._char.length; i2++) {
                    buf.append(this._char[i2]);
                }
            }
            buf.append(':');
            buf.append(this._key);
            buf.append('=');
            buf.append(this._value);
            buf.append(']');
            if (this._children != null) {
                while (true) {
                    int i3 = i;
                    if (i3 >= this._children.length) {
                        break;
                    }
                    buf.append('|');
                    if (this._children[i3] != null) {
                        this._children[i3].toString(buf);
                    } else {
                        buf.append("-");
                    }
                    i = i3 + 1;
                }
            }
            buf.append('}');
            if (this._next != null) {
                buf.append(",\n");
                this._next.toString(buf);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class NullEntry implements Map.Entry {
        private NullEntry() {
        }

        @Override // java.util.Map.Entry
        public Object getKey() {
            return null;
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            return StringMap.this._nullValue;
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object o) {
            Object old = StringMap.this._nullValue;
            StringMap.this._nullValue = o;
            return old;
        }

        public String toString() {
            return "[:null=" + StringMap.this._nullValue + "]";
        }
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput out) throws IOException {
        HashMap map = new HashMap(this);
        out.writeBoolean(this._ignoreCase);
        out.writeObject(map);
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        boolean ic = in.readBoolean();
        HashMap map = (HashMap) in.readObject();
        setIgnoreCase(ic);
        putAll(map);
    }
}
