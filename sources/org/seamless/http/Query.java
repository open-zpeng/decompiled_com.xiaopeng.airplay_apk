package org.seamless.http;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class Query {
    protected final Map<String, List<String>> parameters;

    public static Query newInstance(Map<String, List<String>> parameters) {
        Query query = new Query();
        query.parameters.putAll(parameters);
        return query;
    }

    public Query() {
        this.parameters = new LinkedHashMap();
    }

    public Query(Map<String, String[]> parameters) {
        this.parameters = new LinkedHashMap();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            List<String> list = Arrays.asList(entry.getValue() != null ? entry.getValue() : new String[0]);
            this.parameters.put(entry.getKey(), list);
        }
    }

    public Query(URL url) {
        this(url.getQuery());
    }

    public Query(String qs) {
        String name;
        String value;
        this.parameters = new LinkedHashMap();
        if (qs == null) {
            return;
        }
        String[] pairs = qs.split("&");
        for (String pair : pairs) {
            int pos = pair.indexOf(61);
            if (pos == -1) {
                name = pair;
                value = null;
            } else {
                try {
                    String name2 = pair.substring(0, pos);
                    name = URLDecoder.decode(name2, StringUtil.__UTF8);
                    value = URLDecoder.decode(pair.substring(pos + 1, pair.length()), StringUtil.__UTF8);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("Query string is not UTF-8");
                }
            }
            List<String> list = this.parameters.get(name);
            if (list == null) {
                list = new ArrayList();
                this.parameters.put(name, list);
            }
            list.add(value);
        }
    }

    public String get(String name) {
        List<String> values = this.parameters.get(name);
        if (values == null || values.size() == 0) {
            return "";
        }
        return values.get(0);
    }

    public String[] getValues(String name) {
        List<String> values = this.parameters.get(name);
        if (values == null) {
            return null;
        }
        return (String[]) values.toArray(new String[values.size()]);
    }

    public List<String> getValuesAsList(String name) {
        if (this.parameters.containsKey(name)) {
            return Collections.unmodifiableList(this.parameters.get(name));
        }
        return null;
    }

    public Enumeration<String> getNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    public Map<String, String[]> getMap() {
        String[] values;
        Map<String, String[]> map = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : this.parameters.entrySet()) {
            List<String> list = entry.getValue();
            if (list == null) {
                values = null;
            } else {
                values = (String[]) list.toArray(new String[list.size()]);
            }
            map.put(entry.getKey(), values);
        }
        return map;
    }

    public Map<String, List<String>> getMapWithLists() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public boolean isEmpty() {
        return this.parameters.size() == 0;
    }

    public Query cloneAndAdd(String name, String... values) {
        Map<String, List<String>> params = new HashMap<>(getMapWithLists());
        List<String> existingValues = params.get(name);
        if (existingValues == null) {
            existingValues = new ArrayList<>();
            params.put(name, existingValues);
        }
        existingValues.addAll(Arrays.asList(values));
        return newInstance(params);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : this.parameters.entrySet()) {
            for (String v : entry.getValue()) {
                if (v != null && v.length() != 0) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(entry.getKey());
                    sb.append("=");
                    sb.append(v);
                }
            }
        }
        return sb.toString();
    }
}
