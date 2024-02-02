package org.eclipse.jetty.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/* loaded from: classes.dex */
public class HostMap<TYPE> extends HashMap<String, TYPE> {
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public /* bridge */ /* synthetic */ Object put(Object x0, Object x1) {
        return put((String) x0, (String) x1);
    }

    public HostMap() {
        super(11);
    }

    public HostMap(int capacity) {
        super(capacity);
    }

    public TYPE put(String host, TYPE object) throws IllegalArgumentException {
        return (TYPE) super.put((HostMap<TYPE>) host, (String) object);
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public TYPE get(Object key) {
        return (TYPE) super.get(key);
    }

    public Object getLazyMatches(String host) {
        int idx;
        if (host == null) {
            return LazyList.getList(super.entrySet());
        }
        String domain = host.trim();
        HashSet<String> domains = new HashSet<>();
        do {
            domains.add(domain);
            idx = domain.indexOf(46);
            if (idx > 0) {
                domain = domain.substring(idx + 1);
                continue;
            }
        } while (idx > 0);
        Object entries = null;
        for (Map.Entry<String, TYPE> entry : super.entrySet()) {
            if (domains.contains(entry.getKey())) {
                entries = LazyList.add(entries, entry);
            }
        }
        return entries;
    }
}
