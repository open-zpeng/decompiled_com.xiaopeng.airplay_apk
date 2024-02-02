package org.eclipse.jetty.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class AttributesMap implements Attributes {
    protected final Map<String, Object> _map;

    public AttributesMap() {
        this._map = new HashMap();
    }

    public AttributesMap(Map<String, Object> map) {
        this._map = map;
    }

    public AttributesMap(AttributesMap map) {
        this._map = new HashMap(map._map);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void removeAttribute(String name) {
        this._map.remove(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void setAttribute(String name, Object attribute) {
        if (attribute == null) {
            this._map.remove(name);
        } else {
            this._map.put(name, attribute);
        }
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Object getAttribute(String name) {
        return this._map.get(name);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this._map.keySet());
    }

    public Set<String> getAttributeNameSet() {
        return this._map.keySet();
    }

    public Set<Map.Entry<String, Object>> getAttributeEntrySet() {
        return this._map.entrySet();
    }

    public static Enumeration<String> getAttributeNamesCopy(Attributes attrs) {
        if (attrs instanceof AttributesMap) {
            return Collections.enumeration(((AttributesMap) attrs)._map.keySet());
        }
        List<String> names = new ArrayList<>();
        names.addAll(Collections.list(attrs.getAttributeNames()));
        return Collections.enumeration(names);
    }

    @Override // org.eclipse.jetty.util.Attributes
    public void clearAttributes() {
        this._map.clear();
    }

    public int size() {
        return this._map.size();
    }

    public String toString() {
        return this._map.toString();
    }

    public Set<String> keySet() {
        return this._map.keySet();
    }

    public void addAll(Attributes attributes) {
        Enumeration<String> e = attributes.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            setAttribute(name, attributes.getAttribute(name));
        }
    }
}
