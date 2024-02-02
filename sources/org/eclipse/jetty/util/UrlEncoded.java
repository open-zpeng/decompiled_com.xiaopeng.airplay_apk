package org.eclipse.jetty.util;

import com.apple.dnssd.DNSSD;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.util.Utf8Appendable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class UrlEncoded extends MultiMap implements Cloneable {
    private static final Logger LOG = Log.getLogger(UrlEncoded.class);
    public static final String ENCODING = System.getProperty("org.eclipse.jetty.util.UrlEncoding.charset", StringUtil.__UTF8);

    public UrlEncoded(UrlEncoded url) {
        super((MultiMap) url);
    }

    public UrlEncoded() {
        super(6);
    }

    public UrlEncoded(String s) {
        super(6);
        decode(s, ENCODING);
    }

    public UrlEncoded(String s, String charset) {
        super(6);
        decode(s, charset);
    }

    public void decode(String query) {
        decodeTo(query, this, ENCODING, -1);
    }

    public void decode(String query, String charset) {
        decodeTo(query, this, charset, -1);
    }

    public String encode() {
        return encode(ENCODING, false);
    }

    public String encode(String charset) {
        return encode(charset, false);
    }

    public synchronized String encode(String charset, boolean equalsForNullValue) {
        return encode(this, charset, equalsForNullValue);
    }

    public static String encode(MultiMap map, String charset, boolean equalsForNullValue) {
        if (charset == null) {
            charset = ENCODING;
        }
        StringBuilder result = new StringBuilder((int) DNSSD.REGISTRATION_DOMAINS);
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();
            Object list = entry.getValue();
            int s = LazyList.size(list);
            if (s == 0) {
                result.append(encodeString(key, charset));
                if (equalsForNullValue) {
                    result.append('=');
                }
            } else {
                for (int i = 0; i < s; i++) {
                    if (i > 0) {
                        result.append('&');
                    }
                    Object val = LazyList.get(list, i);
                    result.append(encodeString(key, charset));
                    if (val != null) {
                        String str = val.toString();
                        if (str.length() > 0) {
                            result.append('=');
                            result.append(encodeString(str, charset));
                        } else if (equalsForNullValue) {
                            result.append('=');
                        }
                    } else if (equalsForNullValue) {
                        result.append('=');
                    }
                }
            }
            if (iter.hasNext()) {
                result.append('&');
            }
        }
        return result.toString();
    }

    public static void decodeTo(String content, MultiMap map, String charset) {
        decodeTo(content, map, charset, -1);
    }

    public static void decodeTo(String content, MultiMap map, String charset, int maxKeys) {
        String charset2 = charset == null ? ENCODING : charset;
        synchronized (map) {
            boolean encoded = false;
            int mark = -1;
            String key = null;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '+') {
                    encoded = true;
                } else if (c == '=') {
                    if (key == null) {
                        key = encoded ? decodeString(content, mark + 1, (i - mark) - 1, charset2) : content.substring(mark + 1, i);
                        mark = i;
                        encoded = false;
                    }
                } else {
                    switch (c) {
                        case HttpHeaders.REQUEST_RANGE_ORDINAL /* 37 */:
                            encoded = true;
                            continue;
                        case HttpHeaders.REFERER_ORDINAL /* 38 */:
                            int l = (i - mark) - 1;
                            String value = l == 0 ? "" : encoded ? decodeString(content, mark + 1, l, charset2) : content.substring(mark + 1, i);
                            mark = i;
                            encoded = false;
                            if (key != null) {
                                map.add(key, value);
                            } else if (value != null && value.length() > 0) {
                                map.add(value, "");
                            }
                            key = null;
                            if (maxKeys > 0) {
                                if (map.size() <= maxKeys) {
                                    break;
                                } else {
                                    throw new IllegalStateException(String.format("Form with too many keys [%d > %d]", Integer.valueOf(map.size()), Integer.valueOf(maxKeys)));
                                }
                            } else {
                                continue;
                            }
                        default:
                            continue;
                    }
                }
            }
            if (key != null) {
                int l2 = (content.length() - mark) - 1;
                map.add(key, l2 == 0 ? "" : encoded ? decodeString(content, mark + 1, l2, charset2) : content.substring(mark + 1));
            } else if (mark < content.length()) {
                String key2 = encoded ? decodeString(content, mark + 1, (content.length() - mark) - 1, charset2) : content.substring(mark + 1);
                if (key2 != null && key2.length() > 0) {
                    map.add(key2, "");
                }
            }
        }
    }

    public static void decodeUtf8To(byte[] raw, int offset, int length, MultiMap map) {
        decodeUtf8To(raw, offset, length, map, new Utf8StringBuilder());
    }

    public static void decodeUtf8To(byte[] raw, int offset, int length, MultiMap map, Utf8StringBuilder buffer) {
        synchronized (map) {
            int end = offset + length;
            String key = null;
            int i = offset;
            while (i < end) {
                try {
                    byte b = raw[i];
                    char c = (char) (255 & b);
                    if (c == '+') {
                        buffer.append(HttpTokens.SPACE);
                    } else if (c == '=') {
                        if (key != null) {
                            buffer.append(b);
                        } else {
                            key = buffer.toString();
                            buffer.reset();
                        }
                    } else {
                        switch (c) {
                            case HttpHeaders.REQUEST_RANGE_ORDINAL /* 37 */:
                                if (i + 2 < end) {
                                    if (117 == raw[i + 1]) {
                                        int i2 = i + 1;
                                        if (i2 + 4 < end) {
                                            int i3 = i2 + 1;
                                            int i4 = i3 + 1;
                                            int i5 = i4 + 1;
                                            i = i5 + 1;
                                            buffer.getStringBuilder().append(Character.toChars((TypeUtil.convertHexDigit(raw[i3]) << 12) + (TypeUtil.convertHexDigit(raw[i4]) << 8) + (TypeUtil.convertHexDigit(raw[i5]) << 4) + TypeUtil.convertHexDigit(raw[i])));
                                            continue;
                                        } else {
                                            buffer.getStringBuilder().append(Utf8Appendable.REPLACEMENT);
                                            i = end;
                                            break;
                                        }
                                    } else {
                                        int i6 = i + 1;
                                        i = i6 + 1;
                                        buffer.append((byte) ((TypeUtil.convertHexDigit(raw[i6]) << 4) + TypeUtil.convertHexDigit(raw[i])));
                                        break;
                                    }
                                } else {
                                    buffer.getStringBuilder().append(Utf8Appendable.REPLACEMENT);
                                    i = end;
                                    break;
                                }
                            case HttpHeaders.REFERER_ORDINAL /* 38 */:
                                String value = buffer.length() == 0 ? "" : buffer.toString();
                                buffer.reset();
                                if (key != null) {
                                    map.add(key, value);
                                } else if (value != null && value.length() > 0) {
                                    map.add(value, "");
                                }
                                key = null;
                                continue;
                            default:
                                try {
                                    buffer.append(b);
                                    continue;
                                } catch (Utf8Appendable.NotUtf8Exception e) {
                                    LOG.warn(e.toString(), new Object[0]);
                                    LOG.debug(e);
                                    break;
                                }
                        }
                    }
                    i++;
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (key != null) {
                String value2 = buffer.length() == 0 ? "" : buffer.toReplacedString();
                buffer.reset();
                map.add(key, value2);
            } else if (buffer.length() > 0) {
                map.add(buffer.toReplacedString(), "");
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:50:0x00e1, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x00e3, code lost:
        if (r1 > r14) goto L19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x00ee, code lost:
        throw new java.lang.IllegalStateException("Form too large");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void decode88591To(java.io.InputStream r12, org.eclipse.jetty.util.MultiMap r13, int r14, int r15) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 290
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.UrlEncoded.decode88591To(java.io.InputStream, org.eclipse.jetty.util.MultiMap, int, int):void");
    }

    /* JADX WARN: Code restructure failed: missing block: B:58:0x0103, code lost:
        r0 = r9 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x0105, code lost:
        if (r0 > r18) goto L26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x0110, code lost:
        throw new java.lang.IllegalStateException("Form too large");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void decodeUtf8To(java.io.InputStream r16, org.eclipse.jetty.util.MultiMap r17, int r18, int r19) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 326
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.UrlEncoded.decodeUtf8To(java.io.InputStream, org.eclipse.jetty.util.MultiMap, int, int):void");
    }

    public static void decodeUtf16To(InputStream in, MultiMap map, int maxLength, int maxKeys) throws IOException {
        InputStreamReader input = new InputStreamReader(in, StringUtil.__UTF16);
        StringWriter buf = new StringWriter(CompressedResponseWrapper.DEFAULT_BUFFER_SIZE);
        IO.copy(input, buf, maxLength);
        decodeTo(buf.getBuffer().toString(), map, StringUtil.__UTF16, maxKeys);
    }

    /* JADX WARN: Removed duplicated region for block: B:77:0x013f A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:99:0x014a A[ADDED_TO_REGION, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void decodeTo(java.io.InputStream r19, org.eclipse.jetty.util.MultiMap r20, java.lang.String r21, int r22, int r23) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 382
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.UrlEncoded.decodeTo(java.io.InputStream, org.eclipse.jetty.util.MultiMap, java.lang.String, int, int):void");
    }

    /* JADX WARN: Code restructure failed: missing block: B:61:0x0100, code lost:
        r10 = r17;
        r0 = r16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x0117, code lost:
        r14 = new java.lang.StringBuffer(r22);
        r14.append((java.lang.CharSequence) r20, r21, (r21 + r0) + 1);
     */
    /* JADX WARN: Removed duplicated region for block: B:161:0x00e9 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:60:0x00ec A[Catch: UnsupportedEncodingException -> 0x0135, LOOP:1: B:23:0x005a->B:60:0x00ec, LOOP_END, TryCatch #4 {UnsupportedEncodingException -> 0x0135, blocks: (B:10:0x0028, B:16:0x0036, B:17:0x0041, B:68:0x0128, B:20:0x004a, B:21:0x0055, B:30:0x006c, B:32:0x0072, B:34:0x0076, B:36:0x008f, B:62:0x0104, B:60:0x00ec, B:51:0x00ba, B:39:0x009a, B:44:0x00ab, B:52:0x00c7, B:55:0x00d5, B:57:0x00de, B:64:0x0111, B:66:0x0117, B:67:0x0125, B:73:0x013b, B:76:0x0142, B:78:0x0149), top: B:145:0x0028 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String decodeString(java.lang.String r20, int r21, int r22, java.lang.String r23) {
        /*
            Method dump skipped, instructions count: 609
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.UrlEncoded.decodeString(java.lang.String, int, int, java.lang.String):java.lang.String");
    }

    public static String encodeString(String string) {
        return encodeString(string, ENCODING);
    }

    public static String encodeString(String string, String charset) {
        byte[] bytes;
        int n;
        int n2;
        if (charset == null) {
            charset = ENCODING;
        }
        try {
            bytes = string.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            bytes = string.getBytes();
        }
        byte[] encoded = new byte[bytes.length * 3];
        boolean noEncode = true;
        int n3 = 0;
        for (byte b : bytes) {
            if (b == 32) {
                noEncode = false;
                n2 = n3 + 1;
                encoded[n3] = 43;
            } else if ((b < 97 || b > 122) && ((b < 65 || b > 90) && (b < 48 || b > 57))) {
                noEncode = false;
                int n4 = n3 + 1;
                encoded[n3] = 37;
                int n5 = b & 240;
                byte nibble = (byte) (n5 >> 4);
                if (nibble >= 10) {
                    n = n4 + 1;
                    encoded[n4] = (byte) ((65 + nibble) - 10);
                } else {
                    n = n4 + 1;
                    encoded[n4] = (byte) (48 + nibble);
                }
                int n6 = b & 15;
                byte nibble2 = (byte) n6;
                if (nibble2 >= 10) {
                    encoded[n] = (byte) ((65 + nibble2) - 10);
                    n3 = n + 1;
                } else {
                    n2 = n + 1;
                    encoded[n] = (byte) (48 + nibble2);
                }
            } else {
                n2 = n3 + 1;
                encoded[n3] = b;
            }
            n3 = n2;
        }
        if (noEncode) {
            return string;
        }
        try {
            return new String(encoded, 0, n3, charset);
        } catch (UnsupportedEncodingException e2) {
            return new String(encoded, 0, n3);
        }
    }

    public Object clone() {
        return new UrlEncoded(this);
    }
}
