package org.eclipse.jetty.util;

import com.apple.dnssd.DNSSD;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
/* loaded from: classes.dex */
public class MultiMap<K> implements ConcurrentMap<K, Object>, Serializable {
    private static final long serialVersionUID = -6878723138353851005L;
    ConcurrentMap<K, Object> _cmap;
    Map<K, Object> _map;

    public MultiMap() {
        this._map = new HashMap();
    }

    public MultiMap(Map<K, Object> map) {
        if (map instanceof ConcurrentMap) {
            ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(map);
            this._cmap = concurrentHashMap;
            this._map = concurrentHashMap;
            return;
        }
        this._map = new HashMap(map);
    }

    public MultiMap(MultiMap<K> map) {
        if (map._cmap != null) {
            ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(map._cmap);
            this._cmap = concurrentHashMap;
            this._map = concurrentHashMap;
            return;
        }
        this._map = new HashMap(map._map);
    }

    public MultiMap(int capacity) {
        this._map = new HashMap(capacity);
    }

    public MultiMap(boolean concurrent) {
        if (concurrent) {
            ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
            this._cmap = concurrentHashMap;
            this._map = concurrentHashMap;
            return;
        }
        this._map = new HashMap();
    }

    public List getValues(Object name) {
        return LazyList.getList(this._map.get(name), true);
    }

    public Object getValue(Object name, int i) {
        Object l = this._map.get(name);
        if (i == 0 && LazyList.size(l) == 0) {
            return null;
        }
        return LazyList.get(l, i);
    }

    public String getString(Object name) {
        Object l = this._map.get(name);
        int i = 0;
        switch (LazyList.size(l)) {
            case 0:
                return null;
            case 1:
                Object o = LazyList.get(l, 0);
                if (o == null) {
                    return null;
                }
                return o.toString();
            default:
                StringBuilder values = new StringBuilder((int) DNSSD.REGISTRATION_DOMAINS);
                while (true) {
                    int i2 = i;
                    int i3 = LazyList.size(l);
                    if (i2 < i3) {
                        Object e = LazyList.get(l, i2);
                        if (e != null) {
                            if (values.length() > 0) {
                                values.append(',');
                            }
                            values.append(e.toString());
                        }
                        i = i2 + 1;
                    } else {
                        return values.toString();
                    }
                }
        }
    }

    @Override // java.util.Map
    public Object get(Object name) {
        Object l = this._map.get(name);
        switch (LazyList.size(l)) {
            case 0:
                return null;
            case 1:
                Object o = LazyList.get(l, 0);
                return o;
            default:
                return LazyList.getList(l, true);
        }
    }

    @Override // java.util.Map
    public Object put(K name, Object value) {
        return this._map.put(name, LazyList.add(null, value));
    }

    public Object putValues(K name, List<? extends Object> values) {
        return this._map.put(name, values);
    }

    public Object putValues(K name, String... values) {
        Object list = null;
        for (String str : values) {
            list = LazyList.add(list, str);
        }
        return this._map.put(name, list);
    }

    public void add(K name, Object value) {
        Object lo = this._map.get(name);
        Object ln = LazyList.add(lo, value);
        if (lo != ln) {
            this._map.put(name, ln);
        }
    }

    public void addValues(K name, List<? extends Object> values) {
        Object lo = this._map.get(name);
        Object ln = LazyList.addCollection(lo, values);
        if (lo != ln) {
            this._map.put(name, ln);
        }
    }

    public void addValues(K name, String[] values) {
        Object lo = this._map.get(name);
        Object ln = LazyList.addCollection(lo, Arrays.asList(values));
        if (lo != ln) {
            this._map.put(name, ln);
        }
    }

    public boolean removeValue(K name, Object value) {
        Object lo = this._map.get(name);
        Object ln = lo;
        int s = LazyList.size(lo);
        if (s > 0) {
            ln = LazyList.remove(lo, value);
            if (ln == null) {
                this._map.remove(name);
            } else {
                this._map.put(name, ln);
            }
        }
        return LazyList.size(ln) != s;
    }

    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends Object> m) {
        boolean multi = m instanceof MultiMap;
        if (multi) {
            for (Map.Entry<? extends K, ? extends Object> entry : m.entrySet()) {
                this._map.put(entry.getKey(), LazyList.clone(entry.getValue()));
            }
            return;
        }
        this._map.putAll(m);
    }

    public Map<K, String[]> toStringArrayMap() {
        HashMap<K, String[]> map = new HashMap<K, String[]>((this._map.size() * 3) / 2) { // from class: org.eclipse.jetty.util.MultiMap.1
            @Override // java.util.AbstractMap
            public String toString() {
                StringBuilder b = new StringBuilder();
                b.append('{');
                for (K k : keySet()) {
                    if (b.length() > 1) {
                        b.append(',');
                    }
                    b.append(k);
                    b.append('=');
                    b.append(Arrays.asList(get(k)));
                }
                b.append('}');
                return b.toString();
            }
        };
        for (Map.Entry<K, Object> entry : this._map.entrySet()) {
            String[] a = LazyList.toStringArray(entry.getValue());
            map.put(entry.getKey(), a);
        }
        return map;
    }

    public String toString() {
        return (this._cmap == null ? this._map : this._cmap).toString();
    }

    @Override // java.util.Map
    public void clear() {
        this._map.clear();
    }

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        return this._map.containsKey(key);
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        return this._map.containsValue(value);
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, Object>> entrySet() {
        return this._map.entrySet();
    }

    @Override // java.util.Map
    public boolean equals(Object o) {
        return this._map.equals(o);
    }

    @Override // java.util.Map
    public int hashCode() {
        return this._map.hashCode();
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this._map.isEmpty();
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return this._map.keySet();
    }

    @Override // java.util.Map
    public Object remove(Object key) {
        return this._map.remove(key);
    }

    @Override // java.util.Map
    public int size() {
        return this._map.size();
    }

    @Override // java.util.Map
    public Collection<Object> values() {
        return this._map.values();
    }

    @Override // java.util.concurrent.ConcurrentMap, java.util.Map
    public Object putIfAbsent(K key, Object value) {
        if (this._cmap == null) {
            throw new UnsupportedOperationException();
        }
        return this._cmap.putIfAbsent(key, value);
    }

    @Override // java.util.concurrent.ConcurrentMap, java.util.Map
    public boolean remove(Object key, Object value) {
        if (this._cmap == null) {
            throw new UnsupportedOperationException();
        }
        return this._cmap.remove(key, value);
    }

    @Override // java.util.concurrent.ConcurrentMap, java.util.Map
    public boolean replace(K key, Object oldValue, Object newValue) {
        if (this._cmap == null) {
            throw new UnsupportedOperationException();
        }
        return this._cmap.replace(key, oldValue, newValue);
    }

    @Override // java.util.concurrent.ConcurrentMap, java.util.Map
    public Object replace(K key, Object value) {
        if (this._cmap == null) {
            throw new UnsupportedOperationException();
        }
        return this._cmap.replace(key, value);
    }
}
