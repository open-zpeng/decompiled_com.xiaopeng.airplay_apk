package org.seamless.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.eclipse.jetty.http.HttpTokens;
/* loaded from: classes.dex */
public class Base64Coder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int DECODE = 0;
    public static final int DONT_GUNZIP = 4;
    public static final int DO_BREAK_LINES = 8;
    public static final int ENCODE = 1;
    public static final int GZIP = 2;
    private static final int MAX_LINE_LENGTH = 76;
    private static final byte NEW_LINE = 10;
    public static final int NO_OPTIONS = 0;
    public static final int ORDERED = 32;
    private static final String PREFERRED_ENCODING = "US-ASCII";
    public static final int URL_SAFE = 16;
    private static final byte[] _STANDARD_ALPHABET = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte WHITE_SPACE_ENC = -5;
    private static final byte EQUALS_SIGN = 61;
    private static final byte EQUALS_SIGN_ENC = -1;
    private static final byte[] _STANDARD_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, HttpTokens.COLON, HttpTokens.SEMI_COLON, 60, EQUALS_SIGN, -9, -9, -9, EQUALS_SIGN_ENC, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, HttpTokens.CARRIAGE_RETURN, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, HttpTokens.SPACE, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
    private static final byte[] _URL_SAFE_ALPHABET = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
    private static final byte[] _URL_SAFE_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, 52, 53, 54, 55, 56, 57, HttpTokens.COLON, HttpTokens.SEMI_COLON, 60, EQUALS_SIGN, -9, -9, -9, EQUALS_SIGN_ENC, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, HttpTokens.CARRIAGE_RETURN, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, HttpTokens.SPACE, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};
    private static final byte[] _ORDERED_ALPHABET = {45, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 95, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};
    private static final byte[] _ORDERED_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, WHITE_SPACE_ENC, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 0, -9, -9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -9, -9, -9, EQUALS_SIGN_ENC, -9, -9, -9, 11, 12, HttpTokens.CARRIAGE_RETURN, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, HttpTokens.SPACE, 33, 34, 35, 36, -9, -9, -9, -9, 37, -9, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, HttpTokens.COLON, HttpTokens.SEMI_COLON, 60, EQUALS_SIGN, 62, 63, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};

    private static final byte[] getAlphabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_ALPHABET;
        }
        if ((options & 32) == 32) {
            return _ORDERED_ALPHABET;
        }
        return _STANDARD_ALPHABET;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final byte[] getDecodabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_DECODABET;
        }
        if ((options & 32) == 32) {
            return _ORDERED_DECODABET;
        }
        return _STANDARD_DECODABET;
    }

    private Base64Coder() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options) {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
        return b4;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options) {
        byte[] ALPHABET = getAlphabet(options);
        int inBuff = (numSigBytes > 2 ? (source[srcOffset + 2] << 24) >>> 24 : 0) | (numSigBytes > 0 ? (source[srcOffset] << 24) >>> 8 : 0) | (numSigBytes > 1 ? (source[srcOffset + 1] << 24) >>> 16 : 0);
        switch (numSigBytes) {
            case 1:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = EQUALS_SIGN;
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;
            case 2:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;
            case 3:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = ALPHABET[inBuff & 63];
                return destination;
            default:
                return destination;
        }
    }

    public static void encode(ByteBuffer raw, ByteBuffer encoded) {
        byte[] raw3 = new byte[3];
        byte[] enc4 = new byte[4];
        while (raw.hasRemaining()) {
            int rem = Math.min(3, raw.remaining());
            raw.get(raw3, 0, rem);
            encode3to4(enc4, raw3, rem, 0);
            encoded.put(enc4);
        }
    }

    public static void encode(ByteBuffer raw, CharBuffer encoded) {
        byte[] raw3 = new byte[3];
        byte[] enc4 = new byte[4];
        while (raw.hasRemaining()) {
            int rem = Math.min(3, raw.remaining());
            raw.get(raw3, 0, rem);
            encode3to4(enc4, raw3, rem, 0);
            for (int i = 0; i < 4; i++) {
                encoded.put((char) (enc4[i] & EQUALS_SIGN_ENC));
            }
        }
    }

    public static String encodeObject(Serializable serializableObject) throws IOException {
        return encodeObject(serializableObject, 0);
    }

    public static String encodeObject(Serializable serializableObject, int options) throws IOException {
        if (serializableObject == null) {
            throw new NullPointerException("Cannot serialize a null object.");
        }
        ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        GZIPOutputStream gzos = null;
        ObjectOutputStream oos = null;
        try {
            try {
                baos = new ByteArrayOutputStream();
                b64os = new OutputStream(baos, 1 | options);
                if ((options & 2) != 0) {
                    gzos = new GZIPOutputStream(b64os);
                    oos = new ObjectOutputStream(gzos);
                } else {
                    oos = new ObjectOutputStream(b64os);
                }
                oos.writeObject(serializableObject);
                try {
                    oos.close();
                } catch (Exception e) {
                }
                try {
                    gzos.close();
                } catch (Exception e2) {
                }
                try {
                    b64os.close();
                } catch (Exception e3) {
                }
                try {
                    baos.close();
                } catch (Exception e4) {
                }
                try {
                    return new String(baos.toByteArray(), PREFERRED_ENCODING);
                } catch (UnsupportedEncodingException e5) {
                    return new String(baos.toByteArray());
                }
            } catch (Throwable uue) {
                try {
                    oos.close();
                } catch (Exception e6) {
                }
                try {
                    gzos.close();
                } catch (Exception e7) {
                }
                try {
                    b64os.close();
                } catch (Exception e8) {
                }
                try {
                    baos.close();
                } catch (Exception e9) {
                }
                throw uue;
            }
        } catch (IOException e10) {
            throw e10;
        }
    }

    public static String encodeBytes(byte[] source) {
        try {
            String encoded = encodeBytes(source, 0, source.length, 0);
            return encoded;
        } catch (IOException e) {
            return null;
        }
    }

    public static String encodeBytes(byte[] source, int options) throws IOException {
        return encodeBytes(source, 0, source.length, options);
    }

    public static String encodeBytes(byte[] source, int off, int len) {
        try {
            String encoded = encodeBytes(source, off, len, 0);
            return encoded;
        } catch (IOException e) {
            return null;
        }
    }

    public static String encodeBytes(byte[] source, int off, int len, int options) throws IOException {
        byte[] encoded = encodeBytesToBytes(source, off, len, options);
        try {
            return new String(encoded, PREFERRED_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return new String(encoded);
        }
    }

    public static byte[] encodeBytesToBytes(byte[] source) {
        try {
            byte[] encoded = encodeBytesToBytes(source, 0, source.length, 0);
            return encoded;
        } catch (IOException e) {
            return null;
        }
    }

    /* JADX WARN: Finally extract failed */
    public static byte[] encodeBytesToBytes(byte[] source, int off, int len, int options) throws IOException {
        if (source == null) {
            throw new NullPointerException("Cannot serialize a null array.");
        }
        if (off < 0) {
            throw new IllegalArgumentException("Cannot have negative offset: " + off);
        } else if (len < 0) {
            throw new IllegalArgumentException("Cannot have length offset: " + len);
        } else if (off + len <= source.length) {
            if ((options & 2) != 0) {
                ByteArrayOutputStream baos = null;
                GZIPOutputStream gzos = null;
                OutputStream b64os = null;
                try {
                    try {
                        baos = new ByteArrayOutputStream();
                        b64os = new OutputStream(baos, 1 | options);
                        gzos = new GZIPOutputStream(b64os);
                        gzos.write(source, off, len);
                        gzos.close();
                        try {
                            gzos.close();
                        } catch (Exception e) {
                        }
                        try {
                            b64os.close();
                        } catch (Exception e2) {
                        }
                        try {
                            baos.close();
                        } catch (Exception e3) {
                        }
                        return baos.toByteArray();
                    } catch (IOException e4) {
                        throw e4;
                    }
                } catch (Throwable th) {
                    ByteArrayOutputStream baos2 = baos;
                    try {
                        gzos.close();
                    } catch (Exception e5) {
                    }
                    try {
                        b64os.close();
                    } catch (Exception e6) {
                    }
                    try {
                        baos2.close();
                    } catch (Exception e7) {
                    }
                    throw th;
                }
            }
            boolean breakLines = (options & 8) != 0;
            int encLen = ((len / 3) * 4) + (len % 3 > 0 ? 4 : 0);
            if (breakLines) {
                encLen += encLen / MAX_LINE_LENGTH;
            }
            byte[] outBuff = new byte[encLen];
            int len2 = len - 2;
            int d = 0;
            int e8 = 0;
            int lineLength = 0;
            while (true) {
                int lineLength2 = lineLength;
                if (d >= len2) {
                    break;
                }
                int i = d + off;
                int d2 = d;
                int d3 = e8;
                int len22 = len2;
                encode3to4(source, i, 3, outBuff, d3, options);
                lineLength = lineLength2 + 4;
                if (breakLines && lineLength >= MAX_LINE_LENGTH) {
                    outBuff[e8 + 4] = 10;
                    e8++;
                    lineLength = 0;
                }
                d = d2 + 3;
                e8 += 4;
                len2 = len22;
            }
            int d4 = d;
            if (d4 < len) {
                encode3to4(source, d4 + off, len - d4, outBuff, e8, options);
                e8 += 4;
            }
            int e9 = e8;
            if (e9 <= outBuff.length - 1) {
                byte[] finalOut = new byte[e9];
                System.arraycopy(outBuff, 0, finalOut, 0, e9);
                return finalOut;
            }
            return outBuff;
        } else {
            throw new IllegalArgumentException(String.format("Cannot have offset of %d and length of %d with array of length %d", Integer.valueOf(off), Integer.valueOf(len), Integer.valueOf(source.length)));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options) {
        if (source == null) {
            throw new NullPointerException("Source array was null.");
        }
        if (destination != null) {
            if (srcOffset < 0 || srcOffset + 3 >= source.length) {
                throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and still process four bytes.", Integer.valueOf(source.length), Integer.valueOf(srcOffset)));
            }
            if (destOffset < 0 || destOffset + 2 >= destination.length) {
                throw new IllegalArgumentException(String.format("Destination array with length %d cannot have offset of %d and still store three bytes.", Integer.valueOf(destination.length), Integer.valueOf(destOffset)));
            }
            byte[] DECODABET = getDecodabet(options);
            if (source[srcOffset + 2] == 61) {
                destination[destOffset] = (byte) ((((DECODABET[source[srcOffset]] & EQUALS_SIGN_ENC) << 18) | ((DECODABET[source[srcOffset + 1]] & EQUALS_SIGN_ENC) << 12)) >>> 16);
                return 1;
            } else if (source[srcOffset + 3] == 61) {
                int outBuff = ((DECODABET[source[srcOffset]] & EQUALS_SIGN_ENC) << 18) | ((DECODABET[source[srcOffset + 1]] & EQUALS_SIGN_ENC) << 12) | ((DECODABET[source[srcOffset + 2]] & EQUALS_SIGN_ENC) << 6);
                destination[destOffset] = (byte) (outBuff >>> 16);
                destination[destOffset + 1] = (byte) (outBuff >>> 8);
                return 2;
            } else {
                int outBuff2 = ((DECODABET[source[srcOffset]] & EQUALS_SIGN_ENC) << 18) | ((DECODABET[source[srcOffset + 1]] & EQUALS_SIGN_ENC) << 12) | ((DECODABET[source[srcOffset + 2]] & EQUALS_SIGN_ENC) << 6) | (DECODABET[source[srcOffset + 3]] & EQUALS_SIGN_ENC);
                destination[destOffset] = (byte) (outBuff2 >> 16);
                destination[destOffset + 1] = (byte) (outBuff2 >> 8);
                destination[destOffset + 2] = (byte) outBuff2;
                return 3;
            }
        }
        throw new NullPointerException("Destination array was null.");
    }

    public static byte[] decode(byte[] source) throws IOException {
        byte[] decoded = decode(source, 0, source.length, 0);
        return decoded;
    }

    public static byte[] decode(byte[] source, int off, int len, int options) throws IOException {
        if (source != null) {
            int i = 3;
            if (off < 0 || off + len > source.length) {
                throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and process %d bytes.", Integer.valueOf(source.length), Integer.valueOf(off), Integer.valueOf(len)));
            }
            if (len == 0) {
                return new byte[0];
            }
            if (len < 4) {
                throw new IllegalArgumentException("Base64-encoded string must have at least four characters, but length specified was " + len);
            }
            byte[] DECODABET = getDecodabet(options);
            int len34 = (len * 3) / 4;
            byte[] outBuff = new byte[len34];
            int outBuffPosn = 0;
            byte[] b4 = new byte[4];
            int b4Posn = 0;
            int i2 = off;
            while (i2 < off + len) {
                byte sbiDecode = DECODABET[source[i2] & EQUALS_SIGN_ENC];
                if (sbiDecode >= -5) {
                    if (sbiDecode >= -1) {
                        int b4Posn2 = b4Posn + 1;
                        b4[b4Posn] = source[i2];
                        if (b4Posn2 > i) {
                            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options);
                            b4Posn2 = 0;
                            if (source[i2] == 61) {
                                break;
                            }
                        }
                        b4Posn = b4Posn2;
                    }
                    i2++;
                    i = 3;
                } else {
                    throw new IOException(String.format("Bad Base64 input character decimal %d in array position %d", Integer.valueOf(source[i2] & EQUALS_SIGN_ENC), Integer.valueOf(i2)));
                }
            }
            byte[] out = new byte[outBuffPosn];
            System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
            return out;
        }
        throw new NullPointerException("Cannot decode null source array.");
    }

    public static byte[] decode(String s) throws IOException {
        return decode(s, 0);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:32:0x006e -> B:62:0x0094). Please submit an issue!!! */
    public static byte[] decode(String s, int options) throws IOException {
        byte[] bytes;
        if (s != null) {
            try {
                bytes = s.getBytes(PREFERRED_ENCODING);
            } catch (UnsupportedEncodingException e) {
                bytes = s.getBytes();
            }
            byte[] bytes2 = decode(bytes, 0, bytes.length, options);
            boolean dontGunzip = (options & 4) != 0;
            if (bytes2 != null && bytes2.length >= 4 && !dontGunzip) {
                int head = ((bytes2[1] << 8) & 65280) | (bytes2[0] & EQUALS_SIGN_ENC);
                if (35615 == head) {
                    ByteArrayInputStream bais = null;
                    GZIPInputStream gzis = null;
                    ByteArrayOutputStream baos = null;
                    byte[] buffer = new byte[2048];
                    try {
                        try {
                            try {
                                baos = new ByteArrayOutputStream();
                                bais = new ByteArrayInputStream(bytes2);
                                gzis = new GZIPInputStream(bais);
                                while (true) {
                                    int length = gzis.read(buffer);
                                    if (length < 0) {
                                        break;
                                    }
                                    baos.write(buffer, 0, length);
                                }
                                bytes2 = baos.toByteArray();
                                try {
                                    baos.close();
                                } catch (Exception e2) {
                                }
                                try {
                                    gzis.close();
                                } catch (Exception e3) {
                                }
                                bais.close();
                            } catch (Throwable th) {
                                try {
                                    baos.close();
                                } catch (Exception e4) {
                                }
                                try {
                                    gzis.close();
                                } catch (Exception e5) {
                                }
                                try {
                                    bais.close();
                                } catch (Exception e6) {
                                }
                                throw th;
                            }
                        } catch (IOException e7) {
                            e7.printStackTrace();
                            try {
                                baos.close();
                            } catch (Exception e8) {
                            }
                            try {
                                gzis.close();
                            } catch (Exception e9) {
                            }
                            bais.close();
                        }
                    } catch (Exception e10) {
                    }
                }
            }
            return bytes2;
        }
        throw new NullPointerException("Input string was null.");
    }

    public static Object decodeToObject(String encodedObject) throws IOException, ClassNotFoundException {
        return decodeToObject(encodedObject, 0, null);
    }

    public static Object decodeToObject(String encodedObject, int options, final ClassLoader loader) throws IOException, ClassNotFoundException {
        byte[] objBytes = decode(encodedObject, options);
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            try {
                bais = new ByteArrayInputStream(objBytes);
                if (loader == null) {
                    ois = new ObjectInputStream(bais);
                } else {
                    ois = new ObjectInputStream(bais) { // from class: org.seamless.util.io.Base64Coder.1
                        @Override // java.io.ObjectInputStream
                        public Class<?> resolveClass(ObjectStreamClass streamClass) throws IOException, ClassNotFoundException {
                            Class c = Class.forName(streamClass.getName(), false, loader);
                            if (c == null) {
                                return super.resolveClass(streamClass);
                            }
                            return c;
                        }
                    };
                }
                Object obj = ois.readObject();
                try {
                    bais.close();
                } catch (Exception e) {
                }
                try {
                    ois.close();
                } catch (Exception e2) {
                }
                return obj;
            } catch (IOException e3) {
                throw e3;
            } catch (ClassNotFoundException e4) {
                throw e4;
            }
        } catch (Throwable th) {
            try {
                bais.close();
            } catch (Exception e5) {
            }
            try {
                ois.close();
            } catch (Exception e6) {
            }
            throw th;
        }
    }

    public static void encodeToFile(byte[] dataToEncode, String filename) throws IOException {
        if (dataToEncode == null) {
            throw new NullPointerException("Data to encode was null.");
        }
        OutputStream bos = null;
        try {
            try {
                bos = new OutputStream(new FileOutputStream(filename), 1);
                bos.write(dataToEncode);
                try {
                    bos.close();
                } catch (Exception e) {
                }
            } catch (Throwable th) {
                try {
                    bos.close();
                } catch (Exception e2) {
                }
                throw th;
            }
        } catch (IOException e3) {
            throw e3;
        }
    }

    public static void decodeToFile(String dataToDecode, String filename) throws IOException {
        OutputStream bos = null;
        try {
            try {
                bos = new OutputStream(new FileOutputStream(filename), 0);
                bos.write(dataToDecode.getBytes(PREFERRED_ENCODING));
                try {
                    bos.close();
                } catch (Exception e) {
                }
            } catch (IOException e2) {
                throw e2;
            }
        } catch (Throwable th) {
            try {
                bos.close();
            } catch (Exception e3) {
            }
            throw th;
        }
    }

    public static byte[] decodeFromFile(String filename) throws IOException {
        InputStream bis = null;
        try {
            try {
                File file = new File(filename);
                int length = 0;
                if (file.length() > 2147483647L) {
                    throw new IOException("File is too big for this convenience method (" + file.length() + " bytes).");
                }
                byte[] buffer = new byte[(int) file.length()];
                InputStream bis2 = new InputStream(new BufferedInputStream(new FileInputStream(file)), 0);
                while (true) {
                    int numBytes = bis2.read(buffer, length, 4096);
                    if (numBytes < 0) {
                        break;
                    }
                    length += numBytes;
                }
                byte[] decodedData = new byte[length];
                System.arraycopy(buffer, 0, decodedData, 0, length);
                try {
                    bis2.close();
                } catch (Exception e) {
                }
                return decodedData;
            } catch (IOException e2) {
                throw e2;
            }
        } catch (Throwable th) {
            try {
                bis.close();
            } catch (Exception e3) {
            }
            throw th;
        }
    }

    public static String encodeFromFile(String filename) throws IOException {
        InputStream bis = null;
        try {
            try {
                File file = new File(filename);
                byte[] buffer = new byte[Math.max((int) ((file.length() * 1.4d) + 1.0d), 40)];
                int length = 0;
                bis = new InputStream(new BufferedInputStream(new FileInputStream(file)), 1);
                while (true) {
                    int numBytes = bis.read(buffer, length, 4096);
                    if (numBytes < 0) {
                        break;
                    }
                    length += numBytes;
                }
                String encodedData = new String(buffer, 0, length, PREFERRED_ENCODING);
                try {
                    bis.close();
                } catch (Exception e) {
                }
                return encodedData;
            } catch (IOException e2) {
                throw e2;
            }
        } catch (Throwable th) {
            try {
                bis.close();
            } catch (Exception e3) {
            }
            throw th;
        }
    }

    public static void encodeFileToFile(String infile, String outfile) throws IOException {
        String encoded = encodeFromFile(infile);
        java.io.OutputStream out = null;
        try {
            try {
                out = new BufferedOutputStream(new FileOutputStream(outfile));
                out.write(encoded.getBytes(PREFERRED_ENCODING));
                try {
                    out.close();
                } catch (Exception e) {
                }
            } catch (Throwable th) {
                try {
                    out.close();
                } catch (Exception e2) {
                }
                throw th;
            }
        } catch (IOException e3) {
            throw e3;
        }
    }

    public static void decodeFileToFile(String infile, String outfile) throws IOException {
        byte[] decoded = decodeFromFile(infile);
        java.io.OutputStream out = null;
        try {
            try {
                out = new BufferedOutputStream(new FileOutputStream(outfile));
                out.write(decoded);
                try {
                    out.close();
                } catch (Exception e) {
                }
            } catch (Throwable th) {
                try {
                    out.close();
                } catch (Exception e2) {
                }
                throw th;
            }
        } catch (IOException e3) {
            throw e3;
        }
    }

    /* loaded from: classes.dex */
    public static class InputStream extends FilterInputStream {
        private boolean breakLines;
        private byte[] buffer;
        private int bufferLength;
        private byte[] decodabet;
        private boolean encode;
        private int lineLength;
        private int numSigBytes;
        private int options;
        private int position;

        public InputStream(java.io.InputStream in) {
            this(in, 0);
        }

        public InputStream(java.io.InputStream in, int options) {
            super(in);
            this.options = options;
            this.breakLines = (options & 8) > 0;
            this.encode = (options & 1) > 0;
            this.bufferLength = this.encode ? 4 : 3;
            this.buffer = new byte[this.bufferLength];
            this.position = -1;
            this.lineLength = 0;
            this.decodabet = Base64Coder.getDecodabet(options);
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read() throws IOException {
            int b;
            if (this.position < 0) {
                if (this.encode) {
                    byte[] b3 = new byte[3];
                    int numBinaryBytes = 0;
                    for (int numBinaryBytes2 = 0; numBinaryBytes2 < 3; numBinaryBytes2++) {
                        int b2 = this.in.read();
                        if (b2 < 0) {
                            break;
                        }
                        b3[numBinaryBytes2] = (byte) b2;
                        numBinaryBytes++;
                    }
                    if (numBinaryBytes <= 0) {
                        return -1;
                    }
                    Base64Coder.encode3to4(b3, 0, numBinaryBytes, this.buffer, 0, this.options);
                    this.position = 0;
                    this.numSigBytes = 4;
                } else {
                    byte[] b4 = new byte[4];
                    int i = 0;
                    while (i < 4) {
                        do {
                            b = this.in.read();
                            if (b < 0) {
                                break;
                            }
                        } while (this.decodabet[b & 127] <= -5);
                        if (b < 0) {
                            break;
                        }
                        b4[i] = (byte) b;
                        i++;
                    }
                    if (i == 4) {
                        this.numSigBytes = Base64Coder.decode4to3(b4, 0, this.buffer, 0, this.options);
                        this.position = 0;
                    } else if (i == 0) {
                        return -1;
                    } else {
                        throw new IOException("Improperly padded Base64 input.");
                    }
                }
            }
            if (this.position >= 0) {
                if (this.position >= this.numSigBytes) {
                    return -1;
                }
                if (this.encode && this.breakLines && this.lineLength >= Base64Coder.MAX_LINE_LENGTH) {
                    this.lineLength = 0;
                    return 10;
                }
                this.lineLength++;
                byte[] bArr = this.buffer;
                int i2 = this.position;
                this.position = i2 + 1;
                int b5 = bArr[i2];
                if (this.position >= this.bufferLength) {
                    this.position = -1;
                }
                return b5 & 255;
            }
            throw new IOException("Error in Base64 code reading stream.");
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read(byte[] dest, int off, int len) throws IOException {
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                }
                int b = read();
                if (b >= 0) {
                    dest[off + i] = (byte) b;
                    i++;
                } else if (i == 0) {
                    return -1;
                }
            }
            return i;
        }
    }

    /* loaded from: classes.dex */
    public static class OutputStream extends FilterOutputStream {
        private byte[] b4;
        private boolean breakLines;
        private byte[] buffer;
        private int bufferLength;
        private byte[] decodabet;
        private boolean encode;
        private int lineLength;
        private int options;
        private int position;
        private boolean suspendEncoding;

        public OutputStream(java.io.OutputStream out) {
            this(out, 1);
        }

        public OutputStream(java.io.OutputStream out, int options) {
            super(out);
            this.breakLines = (options & 8) != 0;
            this.encode = (options & 1) != 0;
            this.bufferLength = this.encode ? 3 : 4;
            this.buffer = new byte[this.bufferLength];
            this.position = 0;
            this.lineLength = 0;
            this.suspendEncoding = false;
            this.b4 = new byte[4];
            this.options = options;
            this.decodabet = Base64Coder.getDecodabet(options);
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream
        public void write(int theByte) throws IOException {
            if (this.suspendEncoding) {
                this.out.write(theByte);
            } else if (this.encode) {
                byte[] bArr = this.buffer;
                int i = this.position;
                this.position = i + 1;
                bArr[i] = (byte) theByte;
                if (this.position >= this.bufferLength) {
                    this.out.write(Base64Coder.encode3to4(this.b4, this.buffer, this.bufferLength, this.options));
                    this.lineLength += 4;
                    if (this.breakLines && this.lineLength >= Base64Coder.MAX_LINE_LENGTH) {
                        this.out.write(10);
                        this.lineLength = 0;
                    }
                    this.position = 0;
                }
            } else if (this.decodabet[theByte & 127] > -5) {
                byte[] bArr2 = this.buffer;
                int i2 = this.position;
                this.position = i2 + 1;
                bArr2[i2] = (byte) theByte;
                if (this.position >= this.bufferLength) {
                    int len = Base64Coder.decode4to3(this.buffer, 0, this.b4, 0, this.options);
                    this.out.write(this.b4, 0, len);
                    this.position = 0;
                }
            } else if (this.decodabet[theByte & 127] != -5) {
                throw new IOException("Invalid character in Base64 data.");
            }
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream
        public void write(byte[] theBytes, int off, int len) throws IOException {
            if (this.suspendEncoding) {
                this.out.write(theBytes, off, len);
                return;
            }
            for (int i = 0; i < len; i++) {
                write(theBytes[off + i]);
            }
        }

        public void flushBase64() throws IOException {
            if (this.position > 0) {
                if (this.encode) {
                    this.out.write(Base64Coder.encode3to4(this.b4, this.buffer, this.position, this.options));
                    this.position = 0;
                    return;
                }
                throw new IOException("Base64 input not properly padded.");
            }
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            flushBase64();
            super.close();
            this.buffer = null;
            this.out = null;
        }

        public void suspendEncoding() throws IOException {
            flushBase64();
            this.suspendEncoding = true;
        }

        public void resumeEncoding() {
            this.suspendEncoding = false;
        }
    }

    public static byte[] encode(byte[] bytes) {
        return encodeBytes(bytes).getBytes();
    }

    public static String encodeString(String s) {
        return encodeBytes(s.getBytes());
    }

    public static String decodeString(String s) {
        try {
            return new String(decode(s));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
