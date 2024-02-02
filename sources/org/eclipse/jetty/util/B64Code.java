package org.eclipse.jetty.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
/* loaded from: classes.dex */
public class B64Code {
    static final char __pad = '=';
    static final char[] __rfc1421alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    static final byte[] __rfc1421nibbles = new byte[CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE];

    static {
        for (int i = 0; i < 256; i++) {
            __rfc1421nibbles[i] = -1;
        }
        for (byte b = 0; b < 64; b = (byte) (b + 1)) {
            __rfc1421nibbles[(byte) __rfc1421alphabet[b]] = b;
        }
        __rfc1421nibbles[61] = 0;
    }

    public static String encode(String s) {
        try {
            return encode(s, (String) null);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public static String encode(String s, String charEncoding) throws UnsupportedEncodingException {
        byte[] bytes;
        if (charEncoding == null) {
            bytes = s.getBytes(StringUtil.__ISO_8859_1);
        } else {
            bytes = s.getBytes(charEncoding);
        }
        return new String(encode(bytes));
    }

    public static char[] encode(byte[] b) {
        if (b == null) {
            return null;
        }
        int bLen = b.length;
        int cLen = ((bLen + 2) / 3) * 4;
        char[] c = new char[cLen];
        int ci = 0;
        int bi = 0;
        int stop = (bLen / 3) * 3;
        while (bi < stop) {
            int bi2 = bi + 1;
            byte b0 = b[bi];
            int bi3 = bi2 + 1;
            byte b1 = b[bi2];
            int bi4 = bi3 + 1;
            byte b2 = b[bi3];
            int ci2 = ci + 1;
            c[ci] = __rfc1421alphabet[(b0 >>> 2) & 63];
            int ci3 = ci2 + 1;
            c[ci2] = __rfc1421alphabet[((b0 << 4) & 63) | ((b1 >>> 4) & 15)];
            int ci4 = ci3 + 1;
            c[ci3] = __rfc1421alphabet[((b1 << 2) & 63) | ((b2 >>> 6) & 3)];
            ci = ci4 + 1;
            c[ci4] = __rfc1421alphabet[b2 & 63];
            bi = bi4;
        }
        if (bLen != bi) {
            switch (bLen % 3) {
                case 1:
                    int i = bi + 1;
                    byte b02 = b[bi];
                    int bi5 = ci + 1;
                    c[ci] = __rfc1421alphabet[(b02 >>> 2) & 63];
                    int ci5 = bi5 + 1;
                    c[bi5] = __rfc1421alphabet[(b02 << 4) & 63];
                    int ci6 = ci5 + 1;
                    c[ci5] = __pad;
                    int ci7 = ci6 + 1;
                    c[ci6] = __pad;
                    break;
                case 2:
                    int bi6 = bi + 1;
                    byte b03 = b[bi];
                    int i2 = bi6 + 1;
                    byte b12 = b[bi6];
                    int ci8 = ci + 1;
                    c[ci] = __rfc1421alphabet[(b03 >>> 2) & 63];
                    int ci9 = ci8 + 1;
                    c[ci8] = __rfc1421alphabet[((b03 << 4) & 63) | ((b12 >>> 4) & 15)];
                    int ci10 = ci9 + 1;
                    c[ci9] = __rfc1421alphabet[(b12 << 2) & 63];
                    int ci11 = ci10 + 1;
                    c[ci10] = __pad;
                    break;
            }
        }
        return c;
    }

    public static char[] encode(byte[] b, boolean rfc2045) {
        if (b == null) {
            return null;
        }
        if (!rfc2045) {
            return encode(b);
        }
        int bLen = b.length;
        int cLen = ((bLen + 2) / 3) * 4;
        char[] c = new char[cLen + 2 + ((cLen / 76) * 2)];
        int ci = 0;
        int bi = 0;
        int stop = (bLen / 3) * 3;
        int l = 0;
        while (bi < stop) {
            int bi2 = bi + 1;
            byte b0 = b[bi];
            int bi3 = bi2 + 1;
            byte b1 = b[bi2];
            int i = bi3 + 1;
            byte b2 = b[bi3];
            int ci2 = ci + 1;
            c[ci] = __rfc1421alphabet[(b0 >>> 2) & 63];
            int ci3 = ci2 + 1;
            c[ci2] = __rfc1421alphabet[((b0 << 4) & 63) | ((b1 >>> 4) & 15)];
            int ci4 = ci3 + 1;
            c[ci3] = __rfc1421alphabet[((b1 << 2) & 63) | ((b2 >>> 6) & 3)];
            ci = ci4 + 1;
            c[ci4] = __rfc1421alphabet[b2 & 63];
            l += 4;
            if (l % 76 == 0) {
                int ci5 = ci + 1;
                c[ci] = '\r';
                ci = ci5 + 1;
                c[ci5] = '\n';
            }
            bi = i;
        }
        if (bLen != bi) {
            switch (bLen % 3) {
                case 1:
                    int i2 = bi + 1;
                    byte b02 = b[bi];
                    int bi4 = ci + 1;
                    c[ci] = __rfc1421alphabet[(b02 >>> 2) & 63];
                    int ci6 = bi4 + 1;
                    c[bi4] = __rfc1421alphabet[(b02 << 4) & 63];
                    int ci7 = ci6 + 1;
                    c[ci6] = __pad;
                    ci = ci7 + 1;
                    c[ci7] = __pad;
                    break;
                case 2:
                    int bi5 = bi + 1;
                    byte b03 = b[bi];
                    int i3 = bi5 + 1;
                    byte b12 = b[bi5];
                    int ci8 = ci + 1;
                    c[ci] = __rfc1421alphabet[(b03 >>> 2) & 63];
                    int ci9 = ci8 + 1;
                    c[ci8] = __rfc1421alphabet[((b03 << 4) & 63) | ((b12 >>> 4) & 15)];
                    int ci10 = ci9 + 1;
                    c[ci9] = __rfc1421alphabet[(b12 << 2) & 63];
                    ci = ci10 + 1;
                    c[ci10] = __pad;
                    break;
            }
            int bi6 = ci + 1;
            c[ci] = '\r';
            int ci11 = bi6 + 1;
            c[bi6] = '\n';
            return c;
        }
        int bi62 = ci + 1;
        c[ci] = '\r';
        int ci112 = bi62 + 1;
        c[bi62] = '\n';
        return c;
    }

    public static String decode(String encoded, String charEncoding) throws UnsupportedEncodingException {
        byte[] decoded = decode(encoded);
        if (charEncoding == null) {
            return new String(decoded);
        }
        return new String(decoded, charEncoding);
    }

    public static byte[] decode(char[] b) {
        int bi;
        int ri;
        if (b == null) {
            return null;
        }
        int bLen = b.length;
        if (bLen % 4 == 0) {
            int li = bLen - 1;
            while (li >= 0 && b[li] == '=') {
                li--;
            }
            if (li < 0) {
                return new byte[0];
            }
            int rLen = ((li + 1) * 3) / 4;
            byte[] r = new byte[rLen];
            int ri2 = 0;
            int bi2 = 0;
            int stop = (rLen / 3) * 3;
            while (ri2 < stop) {
                try {
                    int bi3 = bi2 + 1;
                    try {
                        byte b0 = __rfc1421nibbles[b[bi2]];
                        bi = bi3 + 1;
                        try {
                            byte b1 = __rfc1421nibbles[b[bi3]];
                            int bi4 = bi + 1;
                            byte b2 = __rfc1421nibbles[b[bi]];
                            bi = bi4 + 1;
                            byte b3 = __rfc1421nibbles[b[bi4]];
                            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                                throw new IllegalArgumentException("Not B64 encoded");
                            }
                            int ri3 = ri2 + 1;
                            try {
                                r[ri2] = (byte) ((b0 << 2) | (b1 >>> 4));
                                int ri4 = ri3 + 1;
                                r[ri3] = (byte) ((b1 << 4) | (b2 >>> 2));
                                int ri5 = ri4 + 1;
                                r[ri4] = (byte) ((b2 << 6) | b3);
                                bi2 = bi;
                                ri2 = ri5;
                            } catch (IndexOutOfBoundsException e) {
                                throw new IllegalArgumentException("char " + bi + " was not B64 encoded");
                            }
                        } catch (IndexOutOfBoundsException e2) {
                        }
                    } catch (IndexOutOfBoundsException e3) {
                        bi = bi3;
                    }
                } catch (IndexOutOfBoundsException e4) {
                    bi = bi2;
                }
            }
            if (rLen != ri2) {
                switch (rLen % 3) {
                    case 1:
                        int bi5 = bi2 + 1;
                        byte b02 = __rfc1421nibbles[b[bi2]];
                        bi = bi5 + 1;
                        byte b12 = __rfc1421nibbles[b[bi5]];
                        if (b02 < 0 || b12 < 0) {
                            throw new IllegalArgumentException("Not B64 encoded");
                        }
                        ri = ri2 + 1;
                        try {
                            r[ri2] = (byte) ((b02 << 2) | (b12 >>> 4));
                            break;
                        } catch (IndexOutOfBoundsException e5) {
                            e = e5;
                            throw new IllegalArgumentException("char " + bi + " was not B64 encoded");
                        }
                    case 2:
                        int bi6 = bi2 + 1;
                        byte b03 = __rfc1421nibbles[b[bi2]];
                        int bi7 = bi6 + 1;
                        byte b13 = __rfc1421nibbles[b[bi6]];
                        int bi8 = bi7 + 1;
                        byte b22 = __rfc1421nibbles[b[bi7]];
                        if (b03 < 0 || b13 < 0 || b22 < 0) {
                            throw new IllegalArgumentException("Not B64 encoded");
                        }
                        ri = ri2 + 1;
                        try {
                            r[ri2] = (byte) ((b03 << 2) | (b13 >>> 4));
                            int ri6 = ri + 1;
                            r[ri] = (byte) ((b13 << 4) | (b22 >>> 2));
                            break;
                        } catch (IndexOutOfBoundsException e6) {
                            e = e6;
                            bi = bi8;
                            throw new IllegalArgumentException("char " + bi + " was not B64 encoded");
                        }
                }
            }
            return r;
        }
        throw new IllegalArgumentException("Input block size is not 4");
    }

    public static byte[] decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream((4 * encoded.length()) / 3);
        decode(encoded, bout);
        return bout.toByteArray();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public static void decode(String encoded, ByteArrayOutputStream bout) {
        if (encoded == null) {
            return;
        }
        if (bout == null) {
            throw new IllegalArgumentException("No outputstream for decoded bytes");
        }
        byte[] nibbles = new byte[4];
        int ci = 0;
        int s = 0;
        while (ci < encoded.length()) {
            int ci2 = ci + 1;
            char c = encoded.charAt(ci);
            if (c != '=') {
                if (!Character.isWhitespace(c)) {
                    byte nibble = __rfc1421nibbles[c];
                    if (nibble < 0) {
                        throw new IllegalArgumentException("Not B64 encoded");
                    }
                    int s2 = s + 1;
                    nibbles[s] = __rfc1421nibbles[c];
                    switch (s2) {
                        case 1:
                        default:
                            s = s2;
                            break;
                        case 2:
                            bout.write((nibbles[0] << 2) | (nibbles[1] >>> 4));
                            s = s2;
                            break;
                        case 3:
                            bout.write((nibbles[2] >>> 2) | (nibbles[1] << 4));
                            s = s2;
                            break;
                        case 4:
                            bout.write((nibbles[2] << 6) | nibbles[3]);
                            s = 0;
                            break;
                    }
                }
                ci = ci2;
            } else {
                return;
            }
        }
    }

    public static void encode(int value, Appendable buf) throws IOException {
        buf.append(__rfc1421alphabet[(((-67108864) & value) >> 26) & 63]);
        buf.append(__rfc1421alphabet[((66060288 & value) >> 20) & 63]);
        buf.append(__rfc1421alphabet[((1032192 & value) >> 14) & 63]);
        buf.append(__rfc1421alphabet[((16128 & value) >> 8) & 63]);
        buf.append(__rfc1421alphabet[((252 & value) >> 2) & 63]);
        buf.append(__rfc1421alphabet[((3 & value) << 4) & 63]);
        buf.append(__pad);
    }

    public static void encode(long lvalue, Appendable buf) throws IOException {
        int value = (int) ((lvalue >> 32) & (-4));
        buf.append(__rfc1421alphabet[(((-67108864) & value) >> 26) & 63]);
        buf.append(__rfc1421alphabet[((66060288 & value) >> 20) & 63]);
        buf.append(__rfc1421alphabet[((1032192 & value) >> 14) & 63]);
        buf.append(__rfc1421alphabet[((16128 & value) >> 8) & 63]);
        buf.append(__rfc1421alphabet[((252 & value) >> 2) & 63]);
        buf.append(__rfc1421alphabet[(((3 & value) << 4) + (((int) (lvalue >> 28)) & 15)) & 63]);
        int value2 = 268435455 & ((int) lvalue);
        buf.append(__rfc1421alphabet[((264241152 & value2) >> 22) & 63]);
        buf.append(__rfc1421alphabet[((4128768 & value2) >> 16) & 63]);
        buf.append(__rfc1421alphabet[((64512 & value2) >> 10) & 63]);
        buf.append(__rfc1421alphabet[((1008 & value2) >> 4) & 63]);
        buf.append(__rfc1421alphabet[((15 & value2) << 2) & 63]);
    }
}
