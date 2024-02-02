package org.eclipse.jetty.util;

import java.io.UnsupportedEncodingException;
import org.eclipse.jetty.http.HttpHeaders;
/* loaded from: classes.dex */
public class URIUtil implements Cloneable {
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTTPS_COLON = "https:";
    public static final String HTTP_COLON = "http:";
    public static final String SLASH = "/";
    public static final String __CHARSET = System.getProperty("org.eclipse.jetty.util.URI.charset", StringUtil.__UTF8);

    private URIUtil() {
    }

    public static String encodePath(String path) {
        if (path == null || path.length() == 0) {
            return path;
        }
        StringBuilder buf = encodePath(null, path);
        return buf == null ? path : buf.toString();
    }

    public static StringBuilder encodePath(StringBuilder buf, String path) {
        byte[] bytes = null;
        int i = 0;
        if (buf == null) {
            int i2 = 0;
            while (true) {
                if (i2 < path.length()) {
                    char c = path.charAt(i2);
                    switch (c) {
                        case ' ':
                        case HttpHeaders.MAX_FORWARDS_ORDINAL /* 34 */:
                        case HttpHeaders.PROXY_AUTHORIZATION_ORDINAL /* 35 */:
                        case HttpHeaders.REQUEST_RANGE_ORDINAL /* 37 */:
                        case HttpHeaders.TE_ORDINAL /* 39 */:
                        case HttpHeaders.X_FORWARDED_PROTO_ORDINAL /* 59 */:
                        case HttpHeaders.X_FORWARDED_SERVER_ORDINAL /* 60 */:
                        case '>':
                        case '?':
                            buf = new StringBuilder(path.length() * 2);
                            break;
                        default:
                            if (c <= 127) {
                                i2++;
                            } else {
                                try {
                                    bytes = path.getBytes(__CHARSET);
                                    buf = new StringBuilder(path.length() * 2);
                                    break;
                                } catch (UnsupportedEncodingException e) {
                                    throw new IllegalStateException(e);
                                }
                            }
                    }
                }
            }
            if (buf == null) {
                return null;
            }
        }
        synchronized (buf) {
            try {
                if (bytes != null) {
                    while (i < bytes.length) {
                        byte c2 = bytes[i];
                        switch (c2) {
                            case 32:
                                buf.append("%20");
                                break;
                            case HttpHeaders.MAX_FORWARDS_ORDINAL /* 34 */:
                                buf.append("%22");
                                break;
                            case HttpHeaders.PROXY_AUTHORIZATION_ORDINAL /* 35 */:
                                buf.append("%23");
                                break;
                            case HttpHeaders.REQUEST_RANGE_ORDINAL /* 37 */:
                                buf.append("%25");
                                break;
                            case HttpHeaders.TE_ORDINAL /* 39 */:
                                buf.append("%27");
                                break;
                            case HttpHeaders.X_FORWARDED_PROTO_ORDINAL /* 59 */:
                                buf.append("%3B");
                                break;
                            case HttpHeaders.X_FORWARDED_SERVER_ORDINAL /* 60 */:
                                buf.append("%3C");
                                break;
                            case 62:
                                buf.append("%3E");
                                break;
                            case 63:
                                buf.append("%3F");
                                break;
                            default:
                                if (c2 < 0) {
                                    buf.append('%');
                                    TypeUtil.toHex(c2, (Appendable) buf);
                                    break;
                                } else {
                                    buf.append((char) c2);
                                    break;
                                }
                        }
                        i++;
                    }
                } else {
                    while (i < path.length()) {
                        char c3 = path.charAt(i);
                        switch (c3) {
                            case ' ':
                                buf.append("%20");
                                break;
                            case HttpHeaders.MAX_FORWARDS_ORDINAL /* 34 */:
                                buf.append("%22");
                                break;
                            case HttpHeaders.PROXY_AUTHORIZATION_ORDINAL /* 35 */:
                                buf.append("%23");
                                break;
                            case HttpHeaders.REQUEST_RANGE_ORDINAL /* 37 */:
                                buf.append("%25");
                                break;
                            case HttpHeaders.TE_ORDINAL /* 39 */:
                                buf.append("%27");
                                break;
                            case HttpHeaders.X_FORWARDED_PROTO_ORDINAL /* 59 */:
                                buf.append("%3B");
                                break;
                            case HttpHeaders.X_FORWARDED_SERVER_ORDINAL /* 60 */:
                                buf.append("%3C");
                                break;
                            case '>':
                                buf.append("%3E");
                                break;
                            case '?':
                                buf.append("%3F");
                                break;
                            default:
                                buf.append(c3);
                                break;
                        }
                        i++;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return buf;
    }

    public static StringBuilder encodeString(StringBuilder buf, String path, String encode) {
        if (buf == null) {
            for (int i = 0; i < path.length(); i++) {
                char c = path.charAt(i);
                if (c == '%' || encode.indexOf(c) >= 0) {
                    buf = new StringBuilder(path.length() << 1);
                    break;
                }
            }
            if (buf == null) {
                return null;
            }
        }
        synchronized (buf) {
            for (int i2 = 0; i2 < path.length(); i2++) {
                char c2 = path.charAt(i2);
                if (c2 != '%' && encode.indexOf(c2) < 0) {
                    buf.append(c2);
                }
                buf.append('%');
                StringUtil.append(buf, (byte) (255 & c2), 16);
            }
        }
        return buf;
    }

    public static String decodePath(String path) {
        String s;
        String s2;
        if (path == null) {
            return null;
        }
        char[] chars = null;
        int n = 0;
        byte[] bytes = null;
        int b = 0;
        int len = path.length();
        int i = 0;
        while (true) {
            if (i >= len) {
                break;
            }
            char c = path.charAt(i);
            if (c == '%' && i + 2 < len) {
                if (chars == null) {
                    chars = new char[len];
                    bytes = new byte[len];
                    path.getChars(0, i, chars, 0);
                }
                bytes[b] = (byte) (255 & TypeUtil.parseInt(path, i + 1, 2, 16));
                i += 2;
                b++;
            } else if (c == ';') {
                if (chars == null) {
                    chars = new char[len];
                    path.getChars(0, i, chars, 0);
                    n = i;
                }
            } else if (bytes == null) {
                n++;
            } else {
                if (b > 0) {
                    try {
                        s2 = new String(bytes, 0, b, __CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        s2 = new String(bytes, 0, b);
                    }
                    s2.getChars(0, s2.length(), chars, n);
                    n += s2.length();
                    b = 0;
                }
                chars[n] = c;
                n++;
            }
            i++;
        }
        if (chars == null) {
            return path;
        }
        if (b > 0) {
            try {
                s = new String(bytes, 0, b, __CHARSET);
            } catch (UnsupportedEncodingException e2) {
                s = new String(bytes, 0, b);
            }
            s.getChars(0, s.length(), chars, n);
            n += s.length();
        }
        return new String(chars, 0, n);
    }

    public static String decodePath(byte[] buf, int offset, int length) {
        int n = 0;
        byte[] bytes = null;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            byte b = buf[i + offset];
            if (b == 37 && i + 2 < length) {
                b = (byte) (255 & TypeUtil.parseInt(buf, i + offset + 1, 2, 16));
                i += 2;
            } else if (b == 59) {
                length = i;
                break;
            } else if (bytes == null) {
                n++;
                i++;
            }
            if (bytes == null) {
                bytes = new byte[length];
                for (int j = 0; j < n; j++) {
                    bytes[j] = buf[j + offset];
                }
            }
            int j2 = n + 1;
            bytes[n] = b;
            n = j2;
            i++;
        }
        if (bytes != null) {
            return StringUtil.toString(bytes, 0, n, __CHARSET);
        }
        return StringUtil.toString(buf, offset, length, __CHARSET);
    }

    public static String addPaths(String p1, String p2) {
        if (p1 == null || p1.length() == 0) {
            if (p1 != null && p2 == null) {
                return p1;
            }
            return p2;
        } else if (p2 == null || p2.length() == 0) {
            return p1;
        } else {
            int split = p1.indexOf(59);
            if (split < 0) {
                split = p1.indexOf(63);
            }
            if (split == 0) {
                return p2 + p1;
            }
            if (split < 0) {
                split = p1.length();
            }
            StringBuilder buf = new StringBuilder(p1.length() + p2.length() + 2);
            buf.append(p1);
            if (buf.charAt(split - 1) == '/') {
                if (p2.startsWith("/")) {
                    buf.deleteCharAt(split - 1);
                    buf.insert(split - 1, p2);
                } else {
                    buf.insert(split, p2);
                }
            } else if (p2.startsWith("/")) {
                buf.insert(split, p2);
            } else {
                buf.insert(split, '/');
                buf.insert(split + 1, p2);
            }
            return buf.toString();
        }
    }

    public static String parentPath(String p) {
        int slash;
        if (p == null || "/".equals(p) || (slash = p.lastIndexOf(47, p.length() - 2)) < 0) {
            return null;
        }
        return p.substring(0, slash + 1);
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0042 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0043  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String canonicalPath(java.lang.String r11) {
        /*
            Method dump skipped, instructions count: 366
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.URIUtil.canonicalPath(java.lang.String):java.lang.String");
    }

    public static String compactPath(String path) {
        if (path == null || path.length() == 0) {
            return path;
        }
        int end = path.length();
        int state = 0;
        int state2 = 0;
        while (state2 < end) {
            char c = path.charAt(state2);
            if (c == '/') {
                state++;
                if (state == 2) {
                    break;
                }
            } else if (c == '?') {
                return path;
            } else {
                state = 0;
            }
            state2++;
        }
        if (state < 2) {
            return path;
        }
        StringBuffer buf = new StringBuffer(path.length());
        buf.append((CharSequence) path, 0, state2);
        while (true) {
            if (state2 >= end) {
                break;
            }
            char c2 = path.charAt(state2);
            if (c2 == '/') {
                int state3 = state + 1;
                if (state == 0) {
                    buf.append(c2);
                }
                state = state3;
            } else if (c2 == '?') {
                buf.append((CharSequence) path, state2, end);
                break;
            } else {
                state = 0;
                buf.append(c2);
            }
            state2++;
        }
        return buf.toString();
    }

    public static boolean hasScheme(String uri) {
        for (int i = 0; i < uri.length(); i++) {
            char c = uri.charAt(i);
            if (c == ':') {
                return true;
            }
            if ((c < 'a' || c > 'z') && ((c < 'A' || c > 'Z') && (i <= 0 || ((c < '0' || c > '9') && c != '.' && c != '+' && c != '-')))) {
                break;
            }
        }
        return false;
    }
}
