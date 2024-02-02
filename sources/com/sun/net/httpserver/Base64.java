package com.sun.net.httpserver;

import org.eclipse.jetty.http.HttpTokens;
/* compiled from: BasicAuthenticator.java */
/* loaded from: classes.dex */
class Base64 {
    private static final char[] intToBase64 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final char[] intToAltBase64 = {'!', '\"', '#', '$', '%', '&', '\'', '(', ')', ',', '-', '.', ':', ';', '<', '>', '@', '[', ']', '^', '`', '_', '{', '|', '}', '~', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '?'};
    private static final byte[] base64ToInt = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, HttpTokens.COLON, HttpTokens.SEMI_COLON, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, HttpTokens.CARRIAGE_RETURN, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, HttpTokens.SPACE, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
    private static final byte[] altBase64ToInt = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, -1, 62, 9, 10, 11, -1, 52, 53, 54, 55, 56, 57, HttpTokens.COLON, HttpTokens.SEMI_COLON, 60, 61, 12, HttpTokens.CARRIAGE_RETURN, 14, -1, 15, 63, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 17, -1, 18, 19, 21, 20, 26, 27, 28, 29, 30, 31, HttpTokens.SPACE, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 22, 23, 24, 25};

    Base64() {
    }

    static String byteArrayToBase64(byte[] bArr) {
        return byteArrayToBase64(bArr, false);
    }

    static String byteArrayToAltBase64(byte[] bArr) {
        return byteArrayToBase64(bArr, true);
    }

    private static String byteArrayToBase64(byte[] bArr, boolean z) {
        int length = bArr.length;
        int i = length / 3;
        int i2 = length - (3 * i);
        StringBuffer stringBuffer = new StringBuffer(((length + 2) / 3) * 4);
        char[] cArr = z ? intToAltBase64 : intToBase64;
        int i3 = 0;
        int i4 = 0;
        while (i3 < i) {
            int i5 = i4 + 1;
            int i6 = bArr[i4] & 255;
            int i7 = i5 + 1;
            int i8 = bArr[i5] & 255;
            int i9 = i7 + 1;
            int i10 = bArr[i7] & 255;
            stringBuffer.append(cArr[i6 >> 2]);
            stringBuffer.append(cArr[((i6 << 4) & 63) | (i8 >> 4)]);
            stringBuffer.append(cArr[((i8 << 2) & 63) | (i10 >> 6)]);
            stringBuffer.append(cArr[i10 & 63]);
            i3++;
            i4 = i9;
        }
        if (i2 != 0) {
            int i11 = i4 + 1;
            int i12 = bArr[i4] & 255;
            stringBuffer.append(cArr[i12 >> 2]);
            if (i2 == 1) {
                stringBuffer.append(cArr[(i12 << 4) & 63]);
                stringBuffer.append("==");
            } else {
                int i13 = bArr[i11] & 255;
                stringBuffer.append(cArr[((i12 << 4) & 63) | (i13 >> 4)]);
                stringBuffer.append(cArr[(i13 << 2) & 63]);
                stringBuffer.append('=');
            }
        }
        return stringBuffer.toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static byte[] base64ToByteArray(String str) {
        return base64ToByteArray(str, false);
    }

    static byte[] altBase64ToByteArray(String str) {
        return base64ToByteArray(str, true);
    }

    private static byte[] base64ToByteArray(String str, boolean z) {
        int i;
        int i2;
        int i3;
        byte[] bArr = z ? altBase64ToInt : base64ToInt;
        int length = str.length();
        int i4 = length / 4;
        if (4 * i4 != length) {
            throw new IllegalArgumentException("String length must be a multiple of four.");
        }
        int i5 = 0;
        if (length == 0) {
            i = i4;
            i2 = 0;
        } else {
            if (str.charAt(length - 1) != '=') {
                i = i4;
                i3 = 0;
            } else {
                i = i4 - 1;
                i3 = 1;
            }
            if (str.charAt(length - 2) == '=') {
                i2 = i3 + 1;
            } else {
                i2 = i3;
            }
        }
        byte[] bArr2 = new byte[(3 * i4) - i2];
        int i6 = 0;
        int i7 = 0;
        while (i5 < i) {
            int i8 = i6 + 1;
            int base64toInt = base64toInt(str.charAt(i6), bArr);
            int i9 = i8 + 1;
            int base64toInt2 = base64toInt(str.charAt(i8), bArr);
            int i10 = i9 + 1;
            int base64toInt3 = base64toInt(str.charAt(i9), bArr);
            int i11 = i10 + 1;
            int base64toInt4 = base64toInt(str.charAt(i10), bArr);
            int i12 = i7 + 1;
            bArr2[i7] = (byte) ((base64toInt << 2) | (base64toInt2 >> 4));
            int i13 = i12 + 1;
            bArr2[i12] = (byte) ((base64toInt2 << 4) | (base64toInt3 >> 2));
            i7 = i13 + 1;
            bArr2[i13] = (byte) ((base64toInt3 << 6) | base64toInt4);
            i5++;
            i6 = i11;
        }
        if (i2 != 0) {
            int i14 = i6 + 1;
            int base64toInt5 = base64toInt(str.charAt(i6), bArr);
            int i15 = i14 + 1;
            int base64toInt6 = base64toInt(str.charAt(i14), bArr);
            int i16 = i7 + 1;
            bArr2[i7] = (byte) ((base64toInt5 << 2) | (base64toInt6 >> 4));
            if (i2 == 1) {
                bArr2[i16] = (byte) ((base64toInt(str.charAt(i15), bArr) >> 2) | (base64toInt6 << 4));
            }
        }
        return bArr2;
    }

    private static int base64toInt(char c, byte[] bArr) {
        byte b = bArr[c];
        if (b < 0) {
            throw new IllegalArgumentException("Illegal character " + c);
        }
        return b;
    }
}
