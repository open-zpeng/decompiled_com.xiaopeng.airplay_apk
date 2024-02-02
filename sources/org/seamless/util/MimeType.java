package org.seamless.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
/* loaded from: classes.dex */
public class MimeType {
    public static final String WILDCARD = "*";
    private Map<String, String> parameters;
    private String subtype;
    private String type;

    public MimeType() {
        this("*", "*");
    }

    public MimeType(String type, String subtype, Map<String, String> parameters) {
        this.type = type == null ? "*" : type;
        this.subtype = subtype == null ? "*" : subtype;
        if (parameters == null) {
            this.parameters = Collections.EMPTY_MAP;
            return;
        }
        Map<String, String> map = new TreeMap<>(new Comparator<String>() { // from class: org.seamless.util.MimeType.1
            @Override // java.util.Comparator
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        for (Map.Entry<String, String> e : parameters.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }
        this.parameters = Collections.unmodifiableMap(map);
    }

    public MimeType(String type, String subtype) {
        this(type, subtype, Collections.EMPTY_MAP);
    }

    public String getType() {
        return this.type;
    }

    public boolean isWildcardType() {
        return getType().equals("*");
    }

    public String getSubtype() {
        return this.subtype;
    }

    public boolean isWildcardSubtype() {
        return getSubtype().equals("*");
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public boolean isCompatible(MimeType other) {
        if (other == null) {
            return false;
        }
        if (this.type.equals("*") || other.type.equals("*")) {
            return true;
        }
        if (this.type.equalsIgnoreCase(other.type) && (this.subtype.equals("*") || other.subtype.equals("*"))) {
            return true;
        }
        return this.type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype);
    }

    public static MimeType valueOf(String stringValue) throws IllegalArgumentException {
        if (stringValue == null) {
            throw new IllegalArgumentException("String value is null");
        }
        String params = null;
        int semicolonIndex = stringValue.indexOf(";");
        if (semicolonIndex > -1) {
            params = stringValue.substring(semicolonIndex + 1).trim();
            stringValue = stringValue.substring(0, semicolonIndex);
        }
        String major = null;
        String subtype = null;
        String[] paths = stringValue.split("/");
        if (paths.length < 2 && stringValue.equals("*")) {
            major = "*";
            subtype = "*";
        } else if (paths.length == 2) {
            major = paths[0].trim();
            subtype = paths[1].trim();
        } else if (paths.length != 2) {
            throw new IllegalArgumentException("Error parsing string: " + stringValue);
        }
        if (params != null && params.length() > 0) {
            HashMap<String, String> map = new HashMap<>();
            for (int start = 0; start < params.length(); start = readParamsIntoMap(map, params, start)) {
            }
            return new MimeType(major, subtype, map);
        }
        return new MimeType(major, subtype);
    }

    public static int readParamsIntoMap(Map<String, String> map, String params, int start) {
        int end = getEnd(params, start);
        String name = params.substring(start, end).trim();
        if (end < params.length() && params.charAt(end) == '=') {
            end++;
        }
        StringBuilder buffer = new StringBuilder(params.length() - end);
        boolean backslash = false;
        boolean backslash2 = false;
        int i = end;
        while (i < params.length()) {
            char c = params.charAt(i);
            if (c != '\"') {
                if (c != ';') {
                    if (c == '\\') {
                        if (backslash) {
                            backslash = false;
                            buffer.append(c);
                        } else {
                            backslash = true;
                        }
                    } else {
                        buffer.append(c);
                    }
                } else if (!backslash2) {
                    String value = buffer.toString().trim();
                    map.put(name, value);
                    return i + 1;
                } else {
                    buffer.append(c);
                }
            } else if (backslash) {
                backslash = false;
                buffer.append(c);
            } else {
                backslash2 = !backslash2;
            }
            i++;
        }
        String value2 = buffer.toString().trim();
        map.put(name, value2);
        return i;
    }

    protected static int getEnd(String params, int start) {
        int equals = params.indexOf(61, start);
        int semicolon = params.indexOf(59, start);
        if (equals == -1 && semicolon == -1) {
            return params.length();
        }
        if (equals == -1) {
            return semicolon;
        }
        if (semicolon != -1 && equals >= semicolon) {
            return semicolon;
        }
        return equals;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MimeType mimeType = (MimeType) o;
        if (this.parameters == null ? mimeType.parameters != null : !this.parameters.equals(mimeType.parameters)) {
            return false;
        }
        if (this.subtype.equalsIgnoreCase(mimeType.subtype) && this.type.equalsIgnoreCase(mimeType.type)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.type.toLowerCase().hashCode();
        return (31 * ((31 * result) + this.subtype.toLowerCase().hashCode())) + (this.parameters != null ? this.parameters.hashCode() : 0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringNoParameters());
        if (getParameters() != null || getParameters().size() > 0) {
            for (String name : getParameters().keySet()) {
                sb.append(";");
                sb.append(name);
                sb.append("=\"");
                sb.append(getParameters().get(name));
                sb.append("\"");
            }
        }
        return sb.toString();
    }

    public String toStringNoParameters() {
        return getType() + "/" + getSubtype();
    }
}
