package org.seamless.util.io;

import java.io.UnsupportedEncodingException;
import org.eclipse.jetty.http.HttpStatus;
/* loaded from: classes.dex */
public final class HexBin {
    private static final int LOOKUPLENGTH = 16;
    private static final int BASELENGTH = 255;
    private static byte[] hexNumberTable = new byte[BASELENGTH];
    private static byte[] lookUpHexAlphabet = new byte[16];

    static {
        int i = 0;
        for (int i2 = 0; i2 < BASELENGTH; i2++) {
            hexNumberTable[i2] = -1;
        }
        for (int i3 = 57; i3 >= 48; i3--) {
            hexNumberTable[i3] = (byte) (i3 - 48);
        }
        for (int i4 = 70; i4 >= 65; i4--) {
            hexNumberTable[i4] = (byte) ((i4 - 65) + 10);
        }
        for (int i5 = HttpStatus.PROCESSING_102; i5 >= 97; i5--) {
            hexNumberTable[i5] = (byte) ((i5 - 97) + 10);
        }
        while (true) {
            int i6 = i;
            if (i6 >= 10) {
                break;
            }
            lookUpHexAlphabet[i6] = (byte) (48 + i6);
            i = i6 + 1;
        }
        for (int i7 = 10; i7 <= 15; i7++) {
            lookUpHexAlphabet[i7] = (byte) ((65 + i7) - 10);
        }
    }

    static boolean isHex(byte octect) {
        return hexNumberTable[octect] != -1;
    }

    public static String bytesToString(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        return new String(encode(binaryData));
    }

    public static String bytesToString(byte[] binaryData, String separator) {
        if (binaryData == null) {
            return null;
        }
        String s = new String(encode(binaryData));
        StringBuilder sb = new StringBuilder();
        int i = 1;
        char[] chars = s.toCharArray();
        for (char c : chars) {
            sb.append(c);
            if (i == 2) {
                sb.append(separator);
                i = 1;
            } else {
                i++;
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static byte[] stringToBytes(String hexEncoded) {
        return decode(hexEncoded.getBytes());
    }

    public static byte[] stringToBytes(String hexEncoded, String separator) {
        return decode(hexEncoded.replaceAll(separator, "").getBytes());
    }

    public static byte[] encode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        int lengthEncode = lengthData * 2;
        byte[] encodedData = new byte[lengthEncode];
        for (int i = 0; i < lengthData; i++) {
            encodedData[i * 2] = lookUpHexAlphabet[(binaryData[i] >> 4) & 15];
            encodedData[(i * 2) + 1] = lookUpHexAlphabet[binaryData[i] & 15];
        }
        return encodedData;
    }

    public static byte[] decode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        if (lengthData % 2 != 0) {
            return null;
        }
        int lengthDecode = lengthData / 2;
        byte[] decodedData = new byte[lengthDecode];
        for (int i = 0; i < lengthDecode; i++) {
            if (!isHex(binaryData[i * 2]) || !isHex(binaryData[(i * 2) + 1])) {
                return null;
            }
            decodedData[i] = (byte) ((hexNumberTable[binaryData[i * 2]] << 4) | hexNumberTable[binaryData[(i * 2) + 1]]);
        }
        return decodedData;
    }

    public static String decode(String binaryData) {
        if (binaryData == null) {
            return null;
        }
        byte[] decoded = null;
        try {
            decoded = decode(binaryData.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
        }
        if (decoded == null) {
            return null;
        }
        return new String(decoded);
    }

    public static String encode(String binaryData) {
        if (binaryData == null) {
            return null;
        }
        byte[] encoded = null;
        try {
            encoded = encode(binaryData.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
        }
        if (encoded == null) {
            return null;
        }
        return new String(encoded);
    }
}
