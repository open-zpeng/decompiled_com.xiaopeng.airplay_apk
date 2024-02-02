package com.sun.net.httpserver;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class Headers implements Map<String, List<String>> {
    HashMap<String, List<String>> map = new HashMap<>(32);

    private String normalize(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        char[] cArr = new char[length];
        char[] charArray = str.toCharArray();
        if (charArray[0] >= 'a' && charArray[0] <= 'z') {
            charArray[0] = (char) (charArray[0] - ' ');
        }
        for (int i = 1; i < length; i++) {
            if (charArray[i] >= 'A' && charArray[i] <= 'Z') {
                charArray[i] = (char) (charArray[i] + ' ');
            }
        }
        return new String(charArray);
    }

    @Override // java.util.Map
    public int size() {
        return this.map.size();
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override // java.util.Map
    public boolean containsKey(Object obj) {
        if (obj == null || !(obj instanceof String)) {
            return false;
        }
        return this.map.containsKey(normalize((String) obj));
    }

    @Override // java.util.Map
    public boolean containsValue(Object obj) {
        return this.map.containsValue(obj);
    }

    @Override // java.util.Map
    public List<String> get(Object obj) {
        return this.map.get(normalize((String) obj));
    }

    public String getFirst(String str) {
        List<String> list = this.map.get(normalize(str));
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    @Override // java.util.Map
    public List<String> put(String str, List<String> list) {
        return this.map.put(normalize(str), list);
    }

    public void add(String str, String str2) {
        String normalize = normalize(str);
        List<String> list = this.map.get(normalize);
        if (list == null) {
            list = new LinkedList<>();
            this.map.put(normalize, list);
        }
        list.add(str2);
    }

    public void set(String str, String str2) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(str2);
        put(str, (List<String>) linkedList);
    }

    @Override // java.util.Map
    public List<String> remove(Object obj) {
        return this.map.remove(normalize((String) obj));
    }

    @Override // java.util.Map
    public void putAll(Map<? extends String, ? extends List<String>> map) {
        this.map.putAll(map);
    }

    @Override // java.util.Map
    public void clear() {
        this.map.clear();
    }

    @Override // java.util.Map
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override // java.util.Map
    public Collection<List<String>> values() {
        return this.map.values();
    }

    @Override // java.util.Map
    public Set<Map.Entry<String, List<String>>> entrySet() {
        return this.map.entrySet();
    }

    @Override // java.util.Map
    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    @Override // java.util.Map
    public int hashCode() {
        return this.map.hashCode();
    }
}
