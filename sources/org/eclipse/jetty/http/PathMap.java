package org.eclipse.jetty.http;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.StringMap;
/* loaded from: classes.dex */
public class PathMap extends HashMap implements Externalizable {
    private static String __pathSpecSeparators = ":,";
    Entry _default;
    List _defaultSingletonList;
    final Set _entrySet;
    final StringMap _exactMap;
    boolean _nodefault;
    Entry _prefixDefault;
    final StringMap _prefixMap;
    final StringMap _suffixMap;

    public static void setPathSpecSeparators(String s) {
        __pathSpecSeparators = s;
    }

    public PathMap() {
        super(11);
        this._prefixMap = new StringMap();
        this._suffixMap = new StringMap();
        this._exactMap = new StringMap();
        this._defaultSingletonList = null;
        this._prefixDefault = null;
        this._default = null;
        this._nodefault = false;
        this._entrySet = entrySet();
    }

    public PathMap(boolean nodefault) {
        super(11);
        this._prefixMap = new StringMap();
        this._suffixMap = new StringMap();
        this._exactMap = new StringMap();
        this._defaultSingletonList = null;
        this._prefixDefault = null;
        this._default = null;
        this._nodefault = false;
        this._entrySet = entrySet();
        this._nodefault = nodefault;
    }

    public PathMap(int capacity) {
        super(capacity);
        this._prefixMap = new StringMap();
        this._suffixMap = new StringMap();
        this._exactMap = new StringMap();
        this._defaultSingletonList = null;
        this._prefixDefault = null;
        this._default = null;
        this._nodefault = false;
        this._entrySet = entrySet();
    }

    public PathMap(Map m) {
        this._prefixMap = new StringMap();
        this._suffixMap = new StringMap();
        this._exactMap = new StringMap();
        this._defaultSingletonList = null;
        this._prefixDefault = null;
        this._default = null;
        this._nodefault = false;
        putAll(m);
        this._entrySet = entrySet();
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput out) throws IOException {
        HashMap map = new HashMap(this);
        out.writeObject(map);
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        HashMap map = (HashMap) in.readObject();
        putAll(map);
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public Object put(Object pathSpec, Object object) {
        String str = pathSpec.toString();
        if ("".equals(str.trim())) {
            Entry entry = new Entry("", object);
            entry.setMapped("");
            this._exactMap.put("", (Object) entry);
            return super.put("", object);
        }
        StringTokenizer tok = new StringTokenizer(str, __pathSpecSeparators);
        Object old = null;
        while (tok.hasMoreTokens()) {
            String spec = tok.nextToken();
            if (!spec.startsWith("/") && !spec.startsWith("*.")) {
                throw new IllegalArgumentException("PathSpec " + spec + ". must start with '/' or '*.'");
            }
            old = super.put(spec, object);
            Entry entry2 = new Entry(spec, object);
            if (entry2.getKey().equals(spec)) {
                if (spec.equals("/*")) {
                    this._prefixDefault = entry2;
                } else if (spec.endsWith("/*")) {
                    String mapped = spec.substring(0, spec.length() - 2);
                    entry2.setMapped(mapped);
                    this._prefixMap.put(mapped, (Object) entry2);
                    this._exactMap.put(mapped, (Object) entry2);
                    this._exactMap.put(spec.substring(0, spec.length() - 1), (Object) entry2);
                } else if (spec.startsWith("*.")) {
                    this._suffixMap.put(spec.substring(2), (Object) entry2);
                } else if (spec.equals("/")) {
                    if (this._nodefault) {
                        this._exactMap.put(spec, (Object) entry2);
                    } else {
                        this._default = entry2;
                        this._defaultSingletonList = Collections.singletonList(this._default);
                    }
                } else {
                    entry2.setMapped(spec);
                    this._exactMap.put(spec, (Object) entry2);
                }
            }
        }
        return old;
    }

    public Object match(String path) {
        Map.Entry entry = getMatch(path);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    public Entry getMatch(String path) {
        Map.Entry entry;
        Map.Entry entry2;
        Map.Entry entry3;
        if (path == null) {
            return null;
        }
        int l = path.length();
        if (l == 1 && path.charAt(0) == '/' && (entry3 = (Map.Entry) this._exactMap.get("")) != null) {
            return (Entry) entry3;
        }
        Map.Entry entry4 = this._exactMap.getEntry(path, 0, l);
        if (entry4 != null) {
            return (Entry) entry4.getValue();
        }
        int i = l;
        do {
            int lastIndexOf = path.lastIndexOf(47, i - 1);
            i = lastIndexOf;
            if (lastIndexOf >= 0) {
                entry2 = this._prefixMap.getEntry(path, 0, i);
            } else if (this._prefixDefault != null) {
                return this._prefixDefault;
            } else {
                int i2 = 0;
                do {
                    int indexOf = path.indexOf(46, i2 + 1);
                    i2 = indexOf;
                    if (indexOf > 0) {
                        entry = this._suffixMap.getEntry(path, i2 + 1, (l - i2) - 1);
                    } else {
                        return this._default;
                    }
                } while (entry == null);
                return (Entry) entry.getValue();
            }
        } while (entry2 == null);
        return (Entry) entry2.getValue();
    }

    public Object getLazyMatches(String path) {
        if (path == null) {
            return LazyList.getList(null);
        }
        int l = path.length();
        Map.Entry entry = this._exactMap.getEntry(path, 0, l);
        Object entries = entry != null ? LazyList.add(null, entry.getValue()) : null;
        int i = l - 1;
        while (true) {
            int lastIndexOf = path.lastIndexOf(47, i - 1);
            i = lastIndexOf;
            if (lastIndexOf < 0) {
                break;
            }
            Map.Entry entry2 = this._prefixMap.getEntry(path, 0, i);
            if (entry2 != null) {
                entries = LazyList.add(entries, entry2.getValue());
            }
        }
        if (this._prefixDefault != null) {
            entries = LazyList.add(entries, this._prefixDefault);
        }
        int i2 = 0;
        while (true) {
            int indexOf = path.indexOf(46, i2 + 1);
            i2 = indexOf;
            if (indexOf <= 0) {
                break;
            }
            Map.Entry entry3 = this._suffixMap.getEntry(path, i2 + 1, (l - i2) - 1);
            if (entry3 != null) {
                entries = LazyList.add(entries, entry3.getValue());
            }
        }
        if (this._default != null) {
            if (entries == null) {
                return this._defaultSingletonList;
            }
            return LazyList.add(entries, this._default);
        }
        return entries;
    }

    public List getMatches(String path) {
        return LazyList.getList(getLazyMatches(path));
    }

    public boolean containsMatch(String path) {
        Entry match = getMatch(path);
        return (match == null || match.equals(this._default)) ? false : true;
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public Object remove(Object pathSpec) {
        if (pathSpec != null) {
            String spec = (String) pathSpec;
            if (spec.equals("/*")) {
                this._prefixDefault = null;
            } else if (spec.endsWith("/*")) {
                this._prefixMap.remove(spec.substring(0, spec.length() - 2));
                this._exactMap.remove(spec.substring(0, spec.length() - 1));
                this._exactMap.remove(spec.substring(0, spec.length() - 2));
            } else if (spec.startsWith("*.")) {
                this._suffixMap.remove(spec.substring(2));
            } else if (spec.equals("/")) {
                this._default = null;
                this._defaultSingletonList = null;
            } else {
                this._exactMap.remove(spec);
            }
        }
        return super.remove(pathSpec);
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public void clear() {
        this._exactMap.clear();
        this._prefixMap.clear();
        this._suffixMap.clear();
        this._default = null;
        this._defaultSingletonList = null;
        super.clear();
    }

    public static boolean match(String pathSpec, String path) throws IllegalArgumentException {
        return match(pathSpec, path, false);
    }

    public static boolean match(String pathSpec, String path, boolean noDefault) throws IllegalArgumentException {
        if (pathSpec.length() == 0) {
            return "/".equals(path);
        }
        char c = pathSpec.charAt(0);
        if (c == '/') {
            if ((!noDefault && pathSpec.length() == 1) || pathSpec.equals(path) || isPathWildcardMatch(pathSpec, path)) {
                return true;
            }
        } else if (c == '*') {
            return path.regionMatches((path.length() - pathSpec.length()) + 1, pathSpec, 1, pathSpec.length() - 1);
        }
        return false;
    }

    private static boolean isPathWildcardMatch(String pathSpec, String path) {
        int cpl = pathSpec.length() - 2;
        return pathSpec.endsWith("/*") && path.regionMatches(0, pathSpec, 0, cpl) && (path.length() == cpl || '/' == path.charAt(cpl));
    }

    public static String pathMatch(String pathSpec, String path) {
        char c = pathSpec.charAt(0);
        if (c == '/') {
            if (pathSpec.length() == 1) {
                return path;
            }
            if (pathSpec.equals(path)) {
                return path;
            }
            if (isPathWildcardMatch(pathSpec, path)) {
                return path.substring(0, pathSpec.length() - 2);
            }
            return null;
        } else if (c == '*' && path.regionMatches(path.length() - (pathSpec.length() - 1), pathSpec, 1, pathSpec.length() - 1)) {
            return path;
        } else {
            return null;
        }
    }

    public static String pathInfo(String pathSpec, String path) {
        if ("".equals(pathSpec)) {
            return path;
        }
        char c = pathSpec.charAt(0);
        if (c != '/' || pathSpec.length() == 1) {
            return null;
        }
        boolean wildcard = isPathWildcardMatch(pathSpec, path);
        if ((!pathSpec.equals(path) || wildcard) && wildcard && path.length() != pathSpec.length() - 2) {
            return path.substring(pathSpec.length() - 2);
        }
        return null;
    }

    public static String relativePath(String base, String pathSpec, String path) {
        String info = pathInfo(pathSpec, path);
        if (info == null) {
            info = path;
        }
        if (info.startsWith("./")) {
            info = info.substring(2);
        }
        if (base.endsWith("/")) {
            if (info.startsWith("/")) {
                return base + info.substring(1);
            }
            return base + info;
        } else if (info.startsWith("/")) {
            return base + info;
        } else {
            return base + "/" + info;
        }
    }

    /* loaded from: classes.dex */
    public static class Entry implements Map.Entry {
        private final Object key;
        private String mapped;
        private transient String string;
        private final Object value;

        Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override // java.util.Map.Entry
        public Object getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object o) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            if (this.string == null) {
                this.string = this.key + "=" + this.value;
            }
            return this.string;
        }

        public String getMapped() {
            return this.mapped;
        }

        void setMapped(String mapped) {
            this.mapped = mapped;
        }
    }
}
