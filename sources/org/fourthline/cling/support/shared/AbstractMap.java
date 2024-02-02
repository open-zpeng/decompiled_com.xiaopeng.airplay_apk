package org.fourthline.cling.support.shared;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public abstract class AbstractMap<K, V> implements Map<K, V> {
    Set<K> keySet;
    Collection<V> valuesCollection;

    @Override // java.util.Map
    public abstract Set<Map.Entry<K, V>> entrySet();

    /* loaded from: classes.dex */
    public static class SimpleImmutableEntry<K, V> implements Map.Entry<K, V>, Serializable {
        private static final long serialVersionUID = 7138329143949025153L;
        private final K key;
        private final V value;

        public SimpleImmutableEntry(K theKey, V theValue) {
            this.key = theKey;
            this.value = theValue;
        }

        public SimpleImmutableEntry(Map.Entry<? extends K, ? extends V> copyFrom) {
            this.key = copyFrom.getKey();
            this.value = copyFrom.getValue();
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public V setValue(V object) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Map.Entry
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) object;
                if (this.key != null ? this.key.equals(entry.getKey()) : entry.getKey() == null) {
                    if (this.value == null) {
                        if (entry.getValue() == null) {
                            return true;
                        }
                    } else if (this.value.equals(entry.getValue())) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override // java.util.Map.Entry
        public int hashCode() {
            int hashCode;
            if (this.key != null) {
                hashCode = this.key.hashCode();
            } else {
                hashCode = 0;
            }
            return hashCode ^ (this.value != null ? this.value.hashCode() : 0);
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    /* loaded from: classes.dex */
    public static class SimpleEntry<K, V> implements Map.Entry<K, V>, Serializable {
        private static final long serialVersionUID = -8499721149061103585L;
        private final K key;
        private V value;

        public SimpleEntry(K theKey, V theValue) {
            this.key = theKey;
            this.value = theValue;
        }

        public SimpleEntry(Map.Entry<? extends K, ? extends V> copyFrom) {
            this.key = copyFrom.getKey();
            this.value = copyFrom.getValue();
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public V setValue(V object) {
            V result = this.value;
            this.value = object;
            return result;
        }

        @Override // java.util.Map.Entry
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) object;
                if (this.key != null ? this.key.equals(entry.getKey()) : entry.getKey() == null) {
                    if (this.value == null) {
                        if (entry.getValue() == null) {
                            return true;
                        }
                    } else if (this.value.equals(entry.getValue())) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override // java.util.Map.Entry
        public int hashCode() {
            int hashCode;
            if (this.key != null) {
                hashCode = this.key.hashCode();
            } else {
                hashCode = 0;
            }
            return hashCode ^ (this.value != null ? this.value.hashCode() : 0);
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    protected AbstractMap() {
    }

    @Override // java.util.Map
    public void clear() {
        entrySet().clear();
    }

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                if (key.equals(it.next().getKey())) {
                    return true;
                }
            }
            return false;
        }
        while (it.hasNext()) {
            if (it.next().getKey() == null) {
                return true;
            }
        }
        return false;
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (value != null) {
            while (it.hasNext()) {
                if (value.equals(it.next().getValue())) {
                    return true;
                }
            }
            return false;
        }
        while (it.hasNext()) {
            if (it.next().getValue() == null) {
                return true;
            }
        }
        return false;
    }

    @Override // java.util.Map
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map) object;
            if (size() != map.size()) {
                return false;
            }
            try {
                for (Map.Entry<K, V> entry : entrySet()) {
                    K key = entry.getKey();
                    V mine = entry.getValue();
                    Object theirs = map.get(key);
                    if (mine == null) {
                        if (theirs != null || !map.containsKey(key)) {
                            return false;
                        }
                    } else if (!mine.equals(theirs)) {
                        return false;
                    }
                }
                return true;
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }
        return false;
    }

    @Override // java.util.Map
    public V get(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (key.equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return null;
        }
        while (it.hasNext()) {
            Map.Entry<K, V> entry2 = it.next();
            if (entry2.getKey() == null) {
                return entry2.getValue();
            }
        }
        return null;
    }

    @Override // java.util.Map
    public int hashCode() {
        int result = 0;
        for (Map.Entry<K, V> entry : entrySet()) {
            result += entry.hashCode();
        }
        return result;
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        if (this.keySet == null) {
            this.keySet = new AbstractSet<K>() { // from class: org.fourthline.cling.support.shared.AbstractMap.1
                @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
                public boolean contains(Object object) {
                    return AbstractMap.this.containsKey(object);
                }

                @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
                public int size() {
                    return AbstractMap.this.size();
                }

                @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
                public Iterator<K> iterator() {
                    return new Iterator<K>() { // from class: org.fourthline.cling.support.shared.AbstractMap.1.1
                        Iterator<Map.Entry<K, V>> setIterator;

                        {
                            this.setIterator = AbstractMap.this.entrySet().iterator();
                        }

                        @Override // java.util.Iterator
                        public boolean hasNext() {
                            return this.setIterator.hasNext();
                        }

                        @Override // java.util.Iterator
                        public K next() {
                            return this.setIterator.next().getKey();
                        }

                        @Override // java.util.Iterator
                        public void remove() {
                            this.setIterator.remove();
                        }
                    };
                }
            };
        }
        return this.keySet;
    }

    @Override // java.util.Map
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override // java.util.Map
    public V remove(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (key.equals(entry.getKey())) {
                    it.remove();
                    return entry.getValue();
                }
            }
            return null;
        }
        while (it.hasNext()) {
            Map.Entry<K, V> entry2 = it.next();
            if (entry2.getKey() == null) {
                it.remove();
                return entry2.getValue();
            }
        }
        return null;
    }

    @Override // java.util.Map
    public int size() {
        return entrySet().size();
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(size() * 28);
        buffer.append('{');
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            Object key = entry.getKey();
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)");
            }
            buffer.append('=');
            Object value = entry.getValue();
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    @Override // java.util.Map
    public Collection<V> values() {
        if (this.valuesCollection == null) {
            this.valuesCollection = new AbstractCollection<V>() { // from class: org.fourthline.cling.support.shared.AbstractMap.2
                @Override // java.util.AbstractCollection, java.util.Collection
                public int size() {
                    return AbstractMap.this.size();
                }

                @Override // java.util.AbstractCollection, java.util.Collection
                public boolean contains(Object object) {
                    return AbstractMap.this.containsValue(object);
                }

                @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
                public Iterator<V> iterator() {
                    return new Iterator<V>() { // from class: org.fourthline.cling.support.shared.AbstractMap.2.1
                        Iterator<Map.Entry<K, V>> setIterator;

                        {
                            this.setIterator = AbstractMap.this.entrySet().iterator();
                        }

                        @Override // java.util.Iterator
                        public boolean hasNext() {
                            return this.setIterator.hasNext();
                        }

                        @Override // java.util.Iterator
                        public V next() {
                            return this.setIterator.next().getValue();
                        }

                        @Override // java.util.Iterator
                        public void remove() {
                            this.setIterator.remove();
                        }
                    };
                }
            };
        }
        return this.valuesCollection;
    }

    protected Object clone() throws CloneNotSupportedException {
        AbstractMap<K, V> result = (AbstractMap) super.clone();
        result.keySet = null;
        result.valuesCollection = null;
        return result;
    }
}
