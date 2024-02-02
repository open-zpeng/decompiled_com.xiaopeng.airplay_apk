package org.eclipse.jetty.util;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
/* loaded from: classes.dex */
public class IPAddressMap<TYPE> extends HashMap<String, TYPE> {
    private final HashMap<String, IPAddrPattern> _patterns;

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public /* bridge */ /* synthetic */ Object put(Object x0, Object x1) {
        return put((String) x0, (String) x1);
    }

    public IPAddressMap() {
        super(11);
        this._patterns = new HashMap<>();
    }

    public IPAddressMap(int capacity) {
        super(capacity);
        this._patterns = new HashMap<>();
    }

    public TYPE put(String addrSpec, TYPE object) throws IllegalArgumentException {
        if (addrSpec == null || addrSpec.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid IP address pattern: " + addrSpec);
        }
        String spec = addrSpec.trim();
        if (this._patterns.get(spec) == null) {
            this._patterns.put(spec, new IPAddrPattern(spec));
        }
        return (TYPE) super.put((IPAddressMap<TYPE>) spec, (String) object);
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public TYPE get(Object key) {
        return (TYPE) super.get(key);
    }

    public TYPE match(String addr) {
        Map.Entry<String, TYPE> entry = getMatch(addr);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public Map.Entry<String, TYPE> getMatch(String addr) {
        if (addr != null) {
            for (Map.Entry<String, TYPE> entry : super.entrySet()) {
                if (this._patterns.get(entry.getKey()).match(addr)) {
                    return entry;
                }
            }
            return null;
        }
        return null;
    }

    public Object getLazyMatches(String addr) {
        if (addr == null) {
            return LazyList.getList(super.entrySet());
        }
        Object entries = null;
        for (Map.Entry<String, TYPE> entry : super.entrySet()) {
            if (this._patterns.get(entry.getKey()).match(addr)) {
                entries = LazyList.add(entries, entry);
            }
        }
        return entries;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class IPAddrPattern {
        private final OctetPattern[] _octets = new OctetPattern[4];

        public IPAddrPattern(String value) throws IllegalArgumentException {
            if (value == null || value.trim().length() == 0) {
                throw new IllegalArgumentException("Invalid IP address pattern: " + value);
            }
            try {
                StringTokenizer parts = new StringTokenizer(value, ".");
                for (int idx = 0; idx < 4; idx++) {
                    String part = parts.hasMoreTokens() ? parts.nextToken().trim() : "0-255";
                    int len = part.length();
                    if (len == 0 && parts.hasMoreTokens()) {
                        throw new IllegalArgumentException("Invalid IP address pattern: " + value);
                    }
                    this._octets[idx] = new OctetPattern(len == 0 ? "0-255" : part);
                }
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid IP address pattern: " + value, ex);
            }
        }

        public boolean match(String value) throws IllegalArgumentException {
            if (value == null || value.trim().length() == 0) {
                throw new IllegalArgumentException("Invalid IP address: " + value);
            }
            try {
                StringTokenizer parts = new StringTokenizer(value, ".");
                boolean result = true;
                for (int idx = 0; idx < 4; idx++) {
                    if (!parts.hasMoreTokens()) {
                        throw new IllegalArgumentException("Invalid IP address: " + value);
                    }
                    boolean match = this._octets[idx].match(parts.nextToken()) & result;
                    result = match;
                    if (!match) {
                        break;
                    }
                }
                return result;
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid IP address: " + value, ex);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class OctetPattern extends BitSet {
        private final BitSet _mask = new BitSet(CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE);

        public OctetPattern(String octetSpec) throws IllegalArgumentException {
            if (octetSpec != null) {
                try {
                    String spec = octetSpec.trim();
                    if (spec.length() == 0) {
                        this._mask.set(0, 255);
                        return;
                    }
                    StringTokenizer parts = new StringTokenizer(spec, ",");
                    while (parts.hasMoreTokens()) {
                        String part = parts.nextToken().trim();
                        if (part.length() > 0) {
                            if (part.indexOf(45) < 0) {
                                Integer value = Integer.valueOf(part);
                                this._mask.set(value.intValue());
                            } else {
                                String[] bounds = part.split("-", -2);
                                if (bounds.length != 2) {
                                    throw new IllegalArgumentException("Invalid octet spec: " + octetSpec);
                                }
                                int low = bounds[0].length() > 0 ? Integer.parseInt(bounds[0]) : 0;
                                int high = bounds[1].length() > 0 ? Integer.parseInt(bounds[1]) : 255;
                                if (low > high) {
                                    throw new IllegalArgumentException("Invalid octet spec: " + octetSpec);
                                }
                                this._mask.set(low, high + 1);
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid octet spec: " + octetSpec, ex);
                }
            }
        }

        public boolean match(String value) throws IllegalArgumentException {
            if (value == null || value.trim().length() == 0) {
                throw new IllegalArgumentException("Invalid octet: " + value);
            }
            try {
                int number = Integer.parseInt(value);
                return match(number);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid octet: " + value);
            }
        }

        public boolean match(int number) throws IllegalArgumentException {
            if (number < 0 || number > 255) {
                throw new IllegalArgumentException("Invalid octet: " + number);
            }
            return this._mask.get(number);
        }
    }
}
