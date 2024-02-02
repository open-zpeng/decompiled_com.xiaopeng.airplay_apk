package org.seamless.http;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.server.HttpWriter;
/* loaded from: classes.dex */
public class Headers implements Map<String, List<String>> {
    static final byte CR = 13;
    static final byte LF = 10;
    final Map<String, List<String>> map;
    private boolean normalizeHeaders;

    public Headers() {
        this.map = new HashMap(32);
        this.normalizeHeaders = true;
    }

    public Headers(Map<String, List<String>> map) {
        this.map = new HashMap(32);
        this.normalizeHeaders = true;
        putAll(map);
    }

    public Headers(ByteArrayInputStream inputStream) {
        this.map = new HashMap(32);
        this.normalizeHeaders = true;
        StringBuilder sb = new StringBuilder((int) CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE);
        Headers headers = new Headers();
        String line = readLine(sb, inputStream);
        String lastHeader = null;
        if (line.length() != 0) {
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = headers.get((Object) lastHeader);
                    int lastPos = current.size() - 1;
                    String newString = current.get(lastPos) + line.trim();
                    current.set(lastPos, newString);
                } else {
                    String[] header = splitHeader(line);
                    headers.add(header[0], header[1]);
                    lastHeader = header[0];
                }
                sb.delete(0, sb.length());
                line = readLine(sb, inputStream);
            } while (line.length() != 0);
            putAll(headers);
        }
        putAll(headers);
    }

    public Headers(boolean normalizeHeaders) {
        this.map = new HashMap(32);
        this.normalizeHeaders = true;
        this.normalizeHeaders = normalizeHeaders;
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
    public boolean containsKey(Object key) {
        return key != null && (key instanceof String) && this.map.containsKey(normalize((String) key));
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override // java.util.Map
    public List<String> get(Object key) {
        return this.map.get(normalize((String) key));
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // java.util.Map
    public List<String> put(String key, List<String> value) {
        return this.map.put(normalize(key), value);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // java.util.Map
    public List<String> remove(Object key) {
        return this.map.remove(normalize((String) key));
    }

    @Override // java.util.Map
    public void putAll(Map<? extends String, ? extends List<String>> t) {
        for (Map.Entry<? extends String, ? extends List<String>> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
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
    public boolean equals(Object o) {
        return this.map.equals(o);
    }

    @Override // java.util.Map
    public int hashCode() {
        return this.map.hashCode();
    }

    public String getFirstHeader(String key) {
        List<String> l = this.map.get(normalize(key));
        if (l == null || l.size() <= 0) {
            return null;
        }
        return l.get(0);
    }

    public void add(String key, String value) {
        String k = normalize(key);
        List<String> l = this.map.get(k);
        if (l == null) {
            l = new LinkedList();
            this.map.put(k, l);
        }
        l.add(value);
    }

    public void set(String key, String value) {
        LinkedList<String> l = new LinkedList<>();
        l.add(value);
        put(key, (List<String>) l);
    }

    private String normalize(String key) {
        if (!this.normalizeHeaders) {
            return key;
        }
        if (key == null) {
            return null;
        }
        if (key.length() == 0) {
            return key;
        }
        char[] b = key.toCharArray();
        if (b[0] >= 'a' && b[0] <= 'z') {
            b[0] = (char) (b[0] - ' ');
        }
        int length = key.length();
        for (int i = 1; i < length; i++) {
            if (b[i] >= 'A' && b[i] <= 'Z') {
                b[i] = (char) (b[i] + ' ');
            }
        }
        String result = new String(b);
        return result;
    }

    public static String readLine(ByteArrayInputStream is) {
        return readLine(new StringBuilder((int) CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE), is);
    }

    public static String readLine(StringBuilder sb, ByteArrayInputStream is) {
        while (true) {
            int nextByte = is.read();
            if (nextByte == -1) {
                break;
            }
            char nextChar = (char) nextByte;
            if (nextChar == '\r') {
                if (((char) is.read()) == 10) {
                    break;
                }
                sb.append(nextChar);
            } else if (nextChar == '\n') {
                break;
            } else {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }

    protected String[] splitHeader(String sb) {
        char ch;
        int nameStart = findNonWhitespace(sb, 0);
        int nameEnd = nameStart;
        while (nameEnd < sb.length() && (ch = sb.charAt(nameEnd)) != ':' && !Character.isWhitespace(ch)) {
            nameEnd++;
        }
        int colonEnd = nameEnd;
        while (true) {
            if (colonEnd >= sb.length()) {
                break;
            } else if (sb.charAt(colonEnd) != ':') {
                colonEnd++;
            } else {
                colonEnd++;
                break;
            }
        }
        int valueStart = findNonWhitespace(sb, colonEnd);
        int valueEnd = findEndOfString(sb);
        String[] strArr = new String[2];
        strArr[0] = sb.substring(nameStart, nameEnd);
        strArr[1] = (sb.length() < valueStart || sb.length() < valueEnd || valueStart >= valueEnd) ? null : sb.substring(valueStart, valueEnd);
        return strArr;
    }

    protected int findNonWhitespace(String sb, int offset) {
        int result = offset;
        while (result < sb.length() && Character.isWhitespace(sb.charAt(result))) {
            result++;
        }
        return result;
    }

    protected int findEndOfString(String sb) {
        int result = sb.length();
        while (result > 0 && Character.isWhitespace(sb.charAt(result - 1))) {
            result--;
        }
        return result;
    }

    public String toString() {
        StringBuilder headerString = new StringBuilder((int) HttpWriter.MAX_OUTPUT_CHARS);
        for (Map.Entry<String, List<String>> headerEntry : entrySet()) {
            headerString.append(headerEntry.getKey());
            headerString.append(": ");
            for (String v : headerEntry.getValue()) {
                headerString.append(v);
                headerString.append(",");
            }
            headerString.delete(headerString.length() - 1, headerString.length());
            headerString.append("\r\n");
        }
        return headerString.toString();
    }
}
