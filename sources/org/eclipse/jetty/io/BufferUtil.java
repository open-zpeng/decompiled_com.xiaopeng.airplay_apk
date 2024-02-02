package org.eclipse.jetty.io;

import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class BufferUtil {
    static final byte MINUS = 45;
    static final byte SPACE = 32;
    static final byte[] DIGIT = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
    private static final int[] decDivisors = {1000000000, 100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1};
    private static final int[] hexDivisors = {268435456, 16777216, 1048576, 65536, 4096, CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE, 16, 1};
    private static final long[] decDivisorsL = {1000000000000000000L, 100000000000000000L, 10000000000000000L, 1000000000000000L, 100000000000000L, 10000000000000L, 1000000000000L, 100000000000L, 10000000000L, 1000000000, 100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1};

    public static int toInt(Buffer buffer) {
        int val = 0;
        boolean started = false;
        boolean minus = false;
        for (int i = buffer.getIndex(); i < buffer.putIndex(); i++) {
            byte b = buffer.peek(i);
            if (b <= 32) {
                if (started) {
                    break;
                }
            } else {
                if (b >= 48 && b <= 57) {
                    started = true;
                    val = (val * 10) + (b - 48);
                } else if (b != 45 || started) {
                    break;
                } else {
                    minus = true;
                }
            }
        }
        if (started) {
            return minus ? -val : val;
        }
        throw new NumberFormatException(buffer.toString());
    }

    public static long toLong(Buffer buffer) {
        long val = 0;
        boolean started = false;
        boolean minus = false;
        for (int i = buffer.getIndex(); i < buffer.putIndex(); i++) {
            byte b = buffer.peek(i);
            if (b <= 32) {
                if (started) {
                    break;
                }
            } else {
                if (b >= 48 && b <= 57) {
                    started = true;
                    val = (10 * val) + (b - 48);
                } else if (b != 45 || started) {
                    break;
                } else {
                    minus = true;
                }
            }
        }
        if (started) {
            return minus ? -val : val;
        }
        throw new NumberFormatException(buffer.toString());
    }

    public static void putHexInt(Buffer buffer, int n) {
        if (n < 0) {
            buffer.put(MINUS);
            if (n == Integer.MIN_VALUE) {
                buffer.put((byte) 56);
                buffer.put((byte) 48);
                buffer.put((byte) 48);
                buffer.put((byte) 48);
                buffer.put((byte) 48);
                buffer.put((byte) 48);
                buffer.put((byte) 48);
                buffer.put((byte) 48);
                return;
            }
            n = -n;
        }
        if (n < 16) {
            buffer.put(DIGIT[n]);
            return;
        }
        boolean started = false;
        for (int i = 0; i < hexDivisors.length; i++) {
            if (n < hexDivisors[i]) {
                if (started) {
                    buffer.put((byte) 48);
                }
            } else {
                started = true;
                int d = n / hexDivisors[i];
                buffer.put(DIGIT[d]);
                n -= hexDivisors[i] * d;
            }
        }
    }

    public static void prependHexInt(Buffer buffer, int n) {
        if (n == 0) {
            int gi = buffer.getIndex();
            int gi2 = gi - 1;
            buffer.poke(gi2, (byte) 48);
            buffer.setGetIndex(gi2);
            return;
        }
        boolean minus = false;
        if (n < 0) {
            minus = true;
            n = -n;
        }
        int gi3 = buffer.getIndex();
        while (n > 0) {
            int d = 15 & n;
            n >>= 4;
            gi3--;
            buffer.poke(gi3, DIGIT[d]);
        }
        if (minus) {
            gi3--;
            buffer.poke(gi3, MINUS);
        }
        buffer.setGetIndex(gi3);
    }

    public static void putDecInt(Buffer buffer, int n) {
        if (n < 0) {
            buffer.put(MINUS);
            if (n == Integer.MIN_VALUE) {
                buffer.put((byte) 50);
                n = 147483648;
            } else {
                n = -n;
            }
        }
        if (n < 10) {
            buffer.put(DIGIT[n]);
            return;
        }
        boolean started = false;
        for (int i = 0; i < decDivisors.length; i++) {
            if (n < decDivisors[i]) {
                if (started) {
                    buffer.put((byte) 48);
                }
            } else {
                started = true;
                int d = n / decDivisors[i];
                buffer.put(DIGIT[d]);
                n -= decDivisors[i] * d;
            }
        }
    }

    public static void putDecLong(Buffer buffer, long n) {
        if (n < 0) {
            buffer.put(MINUS);
            if (n == Long.MIN_VALUE) {
                buffer.put((byte) 57);
                n = 223372036854775808L;
            } else {
                n = -n;
            }
        }
        if (n < 10) {
            buffer.put(DIGIT[(int) n]);
            return;
        }
        boolean started = false;
        for (int i = 0; i < decDivisorsL.length; i++) {
            if (n < decDivisorsL[i]) {
                if (started) {
                    buffer.put((byte) 48);
                }
            } else {
                started = true;
                long d = n / decDivisorsL[i];
                buffer.put(DIGIT[(int) d]);
                n -= decDivisorsL[i] * d;
            }
        }
    }

    public static Buffer toBuffer(long value) {
        ByteArrayBuffer buf = new ByteArrayBuffer(32);
        putDecLong(buf, value);
        return buf;
    }

    public static void putCRLF(Buffer buffer) {
        buffer.put(HttpTokens.CARRIAGE_RETURN);
        buffer.put((byte) 10);
    }

    public static boolean isPrefix(Buffer prefix, Buffer buffer) {
        if (prefix.length() > buffer.length()) {
            return false;
        }
        int bi = buffer.getIndex();
        int i = prefix.getIndex();
        while (i < prefix.putIndex()) {
            int bi2 = bi + 1;
            if (prefix.peek(i) != buffer.peek(bi)) {
                return false;
            }
            i++;
            bi = bi2;
        }
        return true;
    }

    public static String to8859_1_String(Buffer buffer) {
        if (buffer instanceof BufferCache.CachedBuffer) {
            return buffer.toString();
        }
        return buffer.toString(StringUtil.__ISO_8859_1_CHARSET);
    }
}
