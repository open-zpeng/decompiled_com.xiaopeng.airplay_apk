package sun.net.httpserver;

import com.sun.net.httpserver.Headers;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
class UnmodifiableHeaders extends Headers {
    Headers map;

    /* JADX INFO: Access modifiers changed from: package-private */
    public UnmodifiableHeaders(Headers headers) {
        this.map = headers;
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public int size() {
        return this.map.size();
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public boolean containsKey(Object obj) {
        return this.map.containsKey(obj);
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public boolean containsValue(Object obj) {
        return this.map.containsValue(obj);
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public List<String> get(Object obj) {
        return this.map.get(obj);
    }

    @Override // com.sun.net.httpserver.Headers
    public String getFirst(String str) {
        return this.map.getFirst(str);
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public List<String> put(String str, List<String> list) {
        return this.map.put(str, list);
    }

    @Override // com.sun.net.httpserver.Headers
    public void add(String str, String str2) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override // com.sun.net.httpserver.Headers
    public void set(String str, String str2) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public List<String> remove(Object obj) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public void putAll(Map<? extends String, ? extends List<String>> map) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public void clear() {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public Set<String> keySet() {
        return Collections.unmodifiableSet(this.map.keySet());
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public Collection<List<String>> values() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public Set<Map.Entry<String, List<String>>> entrySet() {
        return Collections.unmodifiableSet(this.map.entrySet());
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    @Override // com.sun.net.httpserver.Headers, java.util.Map
    public int hashCode() {
        return this.map.hashCode();
    }
}
