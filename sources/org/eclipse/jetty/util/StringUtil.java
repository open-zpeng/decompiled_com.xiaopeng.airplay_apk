package org.eclipse.jetty.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class StringUtil {
    public static final String ALL_INTERFACES = "0.0.0.0";
    public static final String CRLF = "\r\n";
    public static final String __UTF16 = "UTF-16";
    public static final String __UTF8Alt = "UTF8";
    private static final Logger LOG = Log.getLogger(StringUtil.class);
    public static final String __LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    public static final String __UTF8 = "UTF-8";
    public static final Charset __UTF8_CHARSET = Charset.forName(__UTF8);
    public static final String __ISO_8859_1 = "ISO-8859-1";
    public static final Charset __ISO_8859_1_CHARSET = Charset.forName(__ISO_8859_1);
    private static char[] lowercases = {0, 1, 2, 3, 4, 5, 6, 7, '\b', '\t', '\n', 11, '\f', '\r', 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, ' ', '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', 127};

    public static String asciiToLowerCase(String s) {
        int i;
        char c2;
        char[] c = null;
        int i2 = s.length();
        while (true) {
            i = i2 - 1;
            if (i2 > 0) {
                char c1 = s.charAt(i);
                if (c1 > 127 || c1 == (c2 = lowercases[c1])) {
                    i2 = i;
                } else {
                    c = s.toCharArray();
                    c[i] = c2;
                    break;
                }
            } else {
                break;
            }
        }
        while (true) {
            int i3 = i - 1;
            if (i <= 0) {
                break;
            }
            if (c[i3] <= 127) {
                c[i3] = lowercases[c[i3]];
            }
            i = i3;
        }
        return c == null ? s : new String(c);
    }

    public static boolean startsWithIgnoreCase(String s, String w) {
        if (w == null) {
            return true;
        }
        if (s == null || s.length() < w.length()) {
            return false;
        }
        for (int i = 0; i < w.length(); i++) {
            char c1 = s.charAt(i);
            char c2 = w.charAt(i);
            if (c1 != c2) {
                if (c1 <= 127) {
                    c1 = lowercases[c1];
                }
                if (c2 <= 127) {
                    c2 = lowercases[c2];
                }
                if (c1 != c2) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean endsWithIgnoreCase(String s, String w) {
        int sl;
        int wl;
        if (w == null) {
            return true;
        }
        if (s == null || (sl = s.length()) < (wl = w.length())) {
            return false;
        }
        int sl2 = sl;
        int sl3 = wl;
        while (true) {
            int i = sl3 - 1;
            if (sl3 <= 0) {
                return true;
            }
            sl2--;
            char c1 = s.charAt(sl2);
            char c2 = w.charAt(i);
            if (c1 != c2) {
                if (c1 <= 127) {
                    c1 = lowercases[c1];
                }
                if (c2 <= 127) {
                    c2 = lowercases[c2];
                }
                if (c1 != c2) {
                    return false;
                }
            }
            sl3 = i;
        }
    }

    public static int indexFrom(String s, String chars) {
        for (int i = 0; i < s.length(); i++) {
            if (chars.indexOf(s.charAt(i)) >= 0) {
                return i;
            }
        }
        return -1;
    }

    public static String replace(String s, String sub, String with) {
        int indexOf;
        int c = 0;
        int i = s.indexOf(sub, 0);
        if (i == -1) {
            return s;
        }
        StringBuilder buf = new StringBuilder(s.length() + with.length());
        do {
            buf.append(s.substring(c, i));
            buf.append(with);
            c = i + sub.length();
            indexOf = s.indexOf(sub, c);
            i = indexOf;
        } while (indexOf != -1);
        if (c < s.length()) {
            buf.append(s.substring(c, s.length()));
        }
        return buf.toString();
    }

    public static String unquote(String s) {
        return QuotedStringTokenizer.unquote(s);
    }

    public static void append(StringBuilder buf, String s, int offset, int length) {
        synchronized (buf) {
            int end = offset + length;
            for (int i = offset; i < end; i++) {
                try {
                    if (i < s.length()) {
                        buf.append(s.charAt(i));
                    }
                } finally {
                }
            }
        }
    }

    public static void append(StringBuilder buf, byte b, int base) {
        int bi = 255 & b;
        int c = ((bi / base) % base) + 48;
        if (c > 57) {
            c = 97 + ((c - 48) - 10);
        }
        buf.append((char) c);
        int c2 = 48 + (bi % base);
        if (c2 > 57) {
            c2 = 97 + ((c2 - 48) - 10);
        }
        buf.append((char) c2);
    }

    public static void append2digits(StringBuffer buf, int i) {
        if (i < 100) {
            buf.append((char) ((i / 10) + 48));
            buf.append((char) ((i % 10) + 48));
        }
    }

    public static void append2digits(StringBuilder buf, int i) {
        if (i < 100) {
            buf.append((char) ((i / 10) + 48));
            buf.append((char) ((i % 10) + 48));
        }
    }

    public static String nonNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static boolean equals(String s, char[] buf, int offset, int length) {
        if (s.length() != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (buf[offset + i] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static String toUTF8String(byte[] b, int offset, int length) {
        try {
            return new String(b, offset, length, __UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toString(byte[] b, int offset, int length, String charset) {
        try {
            return new String(b, offset, length, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isUTF8(String charset) {
        return __UTF8.equalsIgnoreCase(charset) || __UTF8Alt.equalsIgnoreCase(charset);
    }

    public static String printable(String name) {
        if (name == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isISOControl(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String printable(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            char c = (char) b[i];
            if (Character.isWhitespace(c) || (c > ' ' && c < 127)) {
                buf.append(c);
            } else {
                buf.append("0x");
                TypeUtil.toHex(b[i], (Appendable) buf);
            }
        }
        return buf.toString();
    }

    public static byte[] getBytes(String s) {
        try {
            return s.getBytes(__ISO_8859_1);
        } catch (Exception e) {
            LOG.warn(e);
            return s.getBytes();
        }
    }

    public static byte[] getBytes(String s, String charset) {
        try {
            return s.getBytes(charset);
        } catch (Exception e) {
            LOG.warn(e);
            return s.getBytes();
        }
    }

    public static String sidBytesToString(byte[] sidBytes) {
        StringBuilder sidString = new StringBuilder();
        sidString.append("S-");
        sidString.append(Byte.toString(sidBytes[0]));
        sidString.append('-');
        StringBuilder tmpBuilder = new StringBuilder();
        for (int i = 2; i <= 7; i++) {
            tmpBuilder.append(Integer.toHexString(sidBytes[i] & 255));
        }
        sidString.append(Long.parseLong(tmpBuilder.toString(), 16));
        int subAuthorityCount = sidBytes[1];
        for (int i2 = 0; i2 < subAuthorityCount; i2++) {
            int offset = i2 * 4;
            tmpBuilder.setLength(0);
            tmpBuilder.append(String.format("%02X%02X%02X%02X", Integer.valueOf(sidBytes[11 + offset] & 255), Integer.valueOf(sidBytes[10 + offset] & 255), Integer.valueOf(sidBytes[9 + offset] & 255), Integer.valueOf(sidBytes[8 + offset] & 255)));
            sidString.append('-');
            sidString.append(Long.parseLong(tmpBuilder.toString(), 16));
        }
        return sidString.toString();
    }

    public static byte[] sidStringToBytes(String sidString) {
        String[] sidTokens = sidString.split("-");
        int subAuthorityCount = sidTokens.length - 3;
        byte[] sidBytes = new byte[(4 * subAuthorityCount) + 8];
        int byteCount = 0 + 1;
        sidBytes[0] = (byte) Integer.parseInt(sidTokens[1]);
        int byteCount2 = byteCount + 1;
        sidBytes[byteCount] = (byte) subAuthorityCount;
        String hexStr = Long.toHexString(Long.parseLong(sidTokens[2]));
        while (hexStr.length() < 12) {
            hexStr = "0" + hexStr;
        }
        int i = 0;
        while (i < hexStr.length()) {
            sidBytes[byteCount2] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);
            i += 2;
            byteCount2++;
        }
        for (int i2 = 3; i2 < sidTokens.length; i2++) {
            String hexStr2 = Long.toHexString(Long.parseLong(sidTokens[i2]));
            while (hexStr2.length() < 8) {
                hexStr2 = "0" + hexStr2;
            }
            int j = hexStr2.length();
            while (j > 0) {
                sidBytes[byteCount2] = (byte) Integer.parseInt(hexStr2.substring(j - 2, j), 16);
                j -= 2;
                byteCount2++;
            }
        }
        return sidBytes;
    }
}
