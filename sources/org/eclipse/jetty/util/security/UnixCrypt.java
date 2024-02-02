package org.eclipse.jetty.util.security;

import com.apple.dnssd.DNSSD;
import java.io.PrintStream;
import java.lang.reflect.Array;
import org.eclipse.jetty.http.HttpTokens;
/* loaded from: classes.dex */
public class UnixCrypt {
    private static final byte[] IP = {HttpTokens.COLON, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, HttpTokens.SPACE, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, HttpTokens.SEMI_COLON, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, HttpTokens.CARRIAGE_RETURN, 5, 63, 55, 47, 39, 31, 23, 15, 7};
    private static final byte[] ExpandTr = {HttpTokens.SPACE, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, HttpTokens.CARRIAGE_RETURN, 12, HttpTokens.CARRIAGE_RETURN, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, HttpTokens.SPACE, 1};
    private static final byte[] PC1 = {57, 49, 41, 33, 25, 17, 9, 1, HttpTokens.COLON, 50, 42, 34, 26, 18, 10, 2, HttpTokens.SEMI_COLON, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, HttpTokens.CARRIAGE_RETURN, 5, 28, 20, 12, 4};
    private static final byte[] Rotates = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
    private static final byte[] PC2 = {9, 18, 14, 17, 11, 24, 1, 5, 22, 25, 3, 28, 15, 6, 21, 10, 35, 38, 23, 19, 12, 4, 26, 8, 43, 54, 16, 7, 27, 20, HttpTokens.CARRIAGE_RETURN, 2, 0, 0, 41, 52, 31, 37, 47, 55, 0, 0, 30, 40, 51, 45, 33, 48, 0, 0, 44, 49, 39, 56, 34, 53, 0, 0, 46, 42, 50, 36, 29, HttpTokens.SPACE};
    private static final byte[][] S = {new byte[]{14, 4, HttpTokens.CARRIAGE_RETURN, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7, 0, 15, 7, 4, 14, 2, HttpTokens.CARRIAGE_RETURN, 1, 10, 6, 12, 11, 9, 5, 3, 8, 4, 1, 14, 8, HttpTokens.CARRIAGE_RETURN, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0, 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, HttpTokens.CARRIAGE_RETURN}, new byte[]{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, HttpTokens.CARRIAGE_RETURN, 12, 0, 5, 10, 3, HttpTokens.CARRIAGE_RETURN, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5, 0, 14, 7, 11, 10, 4, HttpTokens.CARRIAGE_RETURN, 1, 5, 8, 12, 6, 9, 3, 2, 15, HttpTokens.CARRIAGE_RETURN, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}, new byte[]{10, 0, 9, 14, 6, 3, 15, 5, 1, HttpTokens.CARRIAGE_RETURN, 12, 7, 11, 4, 2, 8, HttpTokens.CARRIAGE_RETURN, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1, HttpTokens.CARRIAGE_RETURN, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7, 1, 10, HttpTokens.CARRIAGE_RETURN, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}, new byte[]{7, HttpTokens.CARRIAGE_RETURN, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15, HttpTokens.CARRIAGE_RETURN, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9, 10, 6, 9, 0, 12, 11, 7, HttpTokens.CARRIAGE_RETURN, 15, 1, 3, 14, 5, 2, 8, 4, 3, 15, 0, 6, 10, 1, HttpTokens.CARRIAGE_RETURN, 8, 9, 4, 5, 11, 12, 7, 2, 14}, new byte[]{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, HttpTokens.CARRIAGE_RETURN, 0, 14, 9, 14, 11, 2, 12, 4, 7, HttpTokens.CARRIAGE_RETURN, 1, 5, 0, 15, 10, 3, 9, 8, 6, 4, 2, 1, 11, 10, HttpTokens.CARRIAGE_RETURN, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14, 11, 8, 12, 7, 1, 14, 2, HttpTokens.CARRIAGE_RETURN, 6, 15, 0, 9, 10, 4, 5, 3}, new byte[]{12, 1, 10, 15, 9, 2, 6, 8, 0, HttpTokens.CARRIAGE_RETURN, 3, 4, 14, 7, 5, 11, 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, HttpTokens.CARRIAGE_RETURN, 14, 0, 11, 3, 8, 9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, HttpTokens.CARRIAGE_RETURN, 11, 6, 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, HttpTokens.CARRIAGE_RETURN}, new byte[]{4, 11, 2, 14, 15, 0, 8, HttpTokens.CARRIAGE_RETURN, 3, 12, 9, 7, 5, 10, 6, 1, HttpTokens.CARRIAGE_RETURN, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6, 1, 4, 11, HttpTokens.CARRIAGE_RETURN, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2, 6, 11, HttpTokens.CARRIAGE_RETURN, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}, new byte[]{HttpTokens.CARRIAGE_RETURN, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7, 1, 15, HttpTokens.CARRIAGE_RETURN, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2, 7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, HttpTokens.CARRIAGE_RETURN, 15, 3, 5, 8, 2, 1, 14, 7, 4, 10, 8, HttpTokens.CARRIAGE_RETURN, 15, 12, 9, 0, 3, 5, 6, 11}};
    private static final byte[] P32Tr = {16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, HttpTokens.SPACE, 27, 3, 9, 19, HttpTokens.CARRIAGE_RETURN, 30, 6, 22, 11, 4, 25};
    private static final byte[] CIFP = {1, 2, 3, 4, 17, 18, 19, 20, 5, 6, 7, 8, 21, 22, 23, 24, 9, 10, 11, 12, 25, 26, 27, 28, HttpTokens.CARRIAGE_RETURN, 14, 15, 16, 29, 30, 31, HttpTokens.SPACE, 33, 34, 35, 36, 49, 50, 51, 52, 37, 38, 39, 40, 53, 54, 55, 56, 41, 42, 43, 44, 57, HttpTokens.COLON, HttpTokens.SEMI_COLON, 60, 45, 46, 47, 48, 61, 62, 63, 64};
    private static final byte[] ITOA64 = {46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};
    private static final byte[] A64TOI = new byte[DNSSD.REGISTRATION_DOMAINS];
    private static final long[][] PC1ROT = (long[][]) Array.newInstance(long.class, 16, 16);
    private static final long[][][] PC2ROT = (long[][][]) Array.newInstance(long.class, 2, 16, 16);
    private static final long[][] IE3264 = (long[][]) Array.newInstance(long.class, 8, 16);
    private static final long[][] SPE = (long[][]) Array.newInstance(long.class, 8, 64);
    private static final long[][] CF6464 = (long[][]) Array.newInstance(long.class, 16, 16);

    static {
        int i = 64;
        int i2 = 8;
        int i3 = 2;
        int i4 = 3;
        int i5 = 5;
        int i6 = 32;
        byte[] perm = new byte[64];
        byte[] temp = new byte[64];
        for (int i7 = 0; i7 < 64; i7++) {
            A64TOI[ITOA64[i7]] = (byte) i7;
        }
        for (int i8 = 0; i8 < 64; i8++) {
            perm[i8] = 0;
        }
        for (int i9 = 0; i9 < 64; i9++) {
            int k = PC2[i9];
            if (k != 0) {
                int k2 = k + (Rotates[0] - 1);
                if (k2 % 28 < Rotates[0]) {
                    k2 -= 28;
                }
                int k3 = PC1[k2];
                if (k3 > 0) {
                    int k4 = k3 - 1;
                    k3 = ((k4 | 7) - (k4 & 7)) + 1;
                }
                perm[i9] = (byte) k3;
            }
        }
        init_perm(PC1ROT, perm, 8);
        for (int j = 0; j < 2; j++) {
            for (int i10 = 0; i10 < 64; i10++) {
                temp[i10] = 0;
                perm[i10] = 0;
            }
            for (int i11 = 0; i11 < 64; i11++) {
                int k5 = PC2[i11];
                if (k5 != 0) {
                    temp[k5 - 1] = (byte) (i11 + 1);
                }
            }
            for (int i12 = 0; i12 < 64; i12++) {
                int k6 = PC2[i12];
                if (k6 != 0) {
                    int k7 = k6 + j;
                    if (k7 % 28 <= j) {
                        k7 -= 28;
                    }
                    perm[i12] = temp[k7];
                }
            }
            init_perm(PC2ROT[j], perm, 8);
        }
        for (int i13 = 0; i13 < 8; i13++) {
            int j2 = 0;
            while (j2 < 8) {
                int k8 = j2 < 2 ? 0 : IP[ExpandTr[((i13 * 6) + j2) - 2] - 1];
                if (k8 > 32) {
                    k8 -= 32;
                } else if (k8 > 0) {
                    k8--;
                }
                if (k8 > 0) {
                    int k9 = k8 - 1;
                    k8 = ((k9 | 7) - (k9 & 7)) + 1;
                }
                perm[(i13 * 8) + j2] = (byte) k8;
                j2++;
            }
        }
        init_perm(IE3264, perm, 8);
        for (int i14 = 0; i14 < 64; i14++) {
            int k10 = IP[CIFP[i14] - 1];
            if (k10 > 0) {
                int k11 = k10 - 1;
                k10 = ((k11 | 7) - (k11 & 7)) + 1;
            }
            perm[k10 - 1] = (byte) (i14 + 1);
        }
        init_perm(CF6464, perm, 8);
        for (int i15 = 0; i15 < 48; i15++) {
            perm[i15] = P32Tr[ExpandTr[i15] - 1];
        }
        int t = 0;
        while (t < i2) {
            int j3 = 0;
            while (j3 < i) {
                int k12 = S[t][(((j3 >> 0) & 1) << i5) | (((j3 >> 1) & 1) << i4) | (((j3 >> 2) & 1) << i3) | (((j3 >> 3) & 1) << 1) | (((j3 >> 4) & 1) << 0) | (((j3 >> 5) & 1) << 4)];
                int k13 = (((k12 >> 3) & 1) << 0) | (((k12 >> 2) & 1) << 1) | (((k12 >> 1) & 1) << i3) | (((k12 >> 0) & 1) << i4);
                for (int i16 = 0; i16 < i6; i16++) {
                    temp[i16] = 0;
                }
                for (int i17 = 0; i17 < 4; i17++) {
                    temp[(4 * t) + i17] = (byte) ((k13 >> i17) & 1);
                }
                int i18 = 24;
                long kk = 0;
                while (true) {
                    i18--;
                    if (i18 >= 0) {
                        kk = (temp[perm[i18] - 1] << i6) | (kk << 1) | temp[perm[i18 + 24] - 1];
                        k13 = k13;
                        i6 = 32;
                    }
                }
                SPE[t][j3] = to_six_bit(kk);
                j3++;
                i = 64;
                i6 = 32;
                i3 = 2;
                i4 = 3;
                i5 = 5;
            }
            t++;
            i = 64;
            i2 = 8;
            i6 = 32;
            i3 = 2;
            i4 = 3;
            i5 = 5;
        }
    }

    private UnixCrypt() {
    }

    private static int to_six_bit(int num) {
        return ((num << 26) & (-67108864)) | ((num << 12) & 16515072) | ((num >> 2) & 64512) | ((num >> 16) & 252);
    }

    private static long to_six_bit(long num) {
        return ((num << 26) & (-288230371923853312L)) | ((num << 12) & 70931694147600384L) | ((num >> 2) & 277076930264064L) | ((num >> 16) & 1082331758844L);
    }

    private static long perm6464(long c, long[][] p) {
        long out = 0;
        long out2 = c;
        int i = 8;
        while (true) {
            i--;
            if (i >= 0) {
                int t = (int) (255 & out2);
                out2 >>= 8;
                long tp = p[i << 1][t & 15];
                long out3 = out | tp;
                long tp2 = p[(i << 1) + 1][t >> 4];
                out = out3 | tp2;
            } else {
                return out;
            }
        }
    }

    private static long perm3264(int c, long[][] p) {
        long out = 0;
        int i = 4;
        while (true) {
            i--;
            if (i >= 0) {
                int t = 255 & c;
                c >>= 8;
                long tp = p[i << 1][t & 15];
                long out2 = out | tp;
                long tp2 = p[(i << 1) + 1][t >> 4];
                out = out2 | tp2;
            } else {
                return out;
            }
        }
    }

    private static long[] des_setkey(long keyword) {
        long K = perm6464(keyword, PC1ROT);
        long[] KS = new long[16];
        KS[0] = K & (-217020518463700993L);
        long K2 = K;
        for (int i = 1; i < 16; i++) {
            KS[i] = K2;
            K2 = perm6464(K2, PC2ROT[Rotates[i] - 1]);
            KS[i] = K2 & (-217020518463700993L);
        }
        return KS;
    }

    private static long des_cipher(long in, int salt, int num_iter, long[] KS) {
        int salt2 = to_six_bit(salt);
        long L = in & 6148914691236517205L;
        int i = 1;
        long R = ((-6148914694099828736L) & in) | ((in >> 1) & 1431655765);
        char c = ' ';
        long j = 4294967295L;
        long L2 = perm3264((int) (((((L << 1) | (L << 32)) & (-4294967296L)) | (((R >> 32) | R) & 4294967295L)) >> 32), IE3264);
        long k = perm3264((int) (L2 & (-1)), IE3264);
        long L3 = L2;
        int num_iter2 = num_iter;
        while (true) {
            num_iter2--;
            if (num_iter2 >= 0) {
                char c2 = 0;
                long L4 = L3;
                int loop_count = 0;
                while (loop_count < 8) {
                    long kp = KS[loop_count << 1];
                    long k2 = ((k >> c) ^ k) & salt2 & j;
                    long B = ((k2 | (k2 << c)) ^ k) ^ kp;
                    long R2 = k;
                    L4 ^= ((((((SPE[c2][(int) ((B >> 58) & 63)] ^ SPE[i][(int) ((B >> 50) & 63)]) ^ SPE[2][(int) ((B >> 42) & 63)]) ^ SPE[3][(int) ((B >> 34) & 63)]) ^ SPE[4][(int) ((B >> 26) & 63)]) ^ SPE[5][(int) ((B >> 18) & 63)]) ^ SPE[6][(int) ((B >> 10) & 63)]) ^ SPE[7][(int) ((B >> 2) & 63)];
                    long kp2 = KS[(loop_count << 1) + i];
                    long k3 = ((L4 >> 32) ^ L4) & salt2 & 4294967295L;
                    long B2 = ((k3 | (k3 << 32)) ^ L4) ^ kp2;
                    k = R2 ^ (((((((SPE[i][(int) ((B2 >> 50) & 63)] ^ SPE[0][(int) ((B2 >> 58) & 63)]) ^ SPE[2][(int) ((B2 >> 42) & 63)]) ^ SPE[3][(int) ((B2 >> 34) & 63)]) ^ SPE[4][(int) ((B2 >> 26) & 63)]) ^ SPE[5][(int) ((B2 >> 18) & 63)]) ^ SPE[6][(int) ((B2 >> 10) & 63)]) ^ SPE[7][(int) ((B2 >> 2) & 63)]);
                    loop_count++;
                    c2 = 0;
                    i = 1;
                    c = ' ';
                    j = 4294967295L;
                }
                long R3 = k;
                long L5 = L4 ^ R3;
                k = R3 ^ L5;
                L3 = L5 ^ k;
                i = 1;
                c = ' ';
                j = 4294967295L;
            } else {
                return perm6464(((((L3 >> 35) & 252645135) | (((L3 & (-1)) << 1) & 4042322160L)) << 32) | ((((-1) & k) << 1) & 4042322160L) | ((k >> 35) & 252645135), CF6464);
            }
        }
    }

    private static void init_perm(long[][] perm, byte[] p, int chars_out) {
        for (int k = 0; k < chars_out * 8; k++) {
            int l = p[k] - 1;
            if (l >= 0) {
                int i = l >> 2;
                int l2 = 1 << (l & 3);
                for (int j = 0; j < 16; j++) {
                    int s = (k & 7) + ((7 - (k >> 3)) << 3);
                    if ((j & l2) != 0) {
                        long[] jArr = perm[i];
                        jArr[j] = jArr[j] | (1 << s);
                    }
                }
            }
        }
    }

    public static String crypt(String key, String setting) {
        int i;
        byte[] cryptresult = new byte[13];
        if (key != null && setting != null) {
            int keylen = key.length();
            long keyword = 0;
            int i2 = 0;
            while (true) {
                i = 2;
                if (i2 >= 8) {
                    break;
                }
                keyword = (keyword << 8) | (i2 < keylen ? key.charAt(i2) * 2 : 0);
                i2++;
                cryptresult = cryptresult;
            }
            byte[] cryptresult2 = cryptresult;
            long[] KS = des_setkey(keyword);
            int salt = 0;
            int i3 = 2;
            while (true) {
                i3--;
                if (i3 < 0) {
                    break;
                }
                char c = i3 < setting.length() ? setting.charAt(i3) : '.';
                cryptresult2[i3] = (byte) c;
                salt = (salt << 6) | (255 & A64TOI[c]);
            }
            long rsltblock = des_cipher(0L, salt, 25, KS);
            int i4 = 12;
            cryptresult2[12] = ITOA64[(((int) rsltblock) << 2) & 63];
            long rsltblock2 = rsltblock >> 4;
            while (true) {
                i4--;
                if (i4 >= i) {
                    cryptresult2[i4] = ITOA64[((int) rsltblock2) & 63];
                    rsltblock2 >>= 6;
                    i = 2;
                } else {
                    return new String(cryptresult2, 0, 13);
                }
            }
        }
        return "*";
    }

    public static void main(String[] arg) {
        if (arg.length != 2) {
            System.err.println("Usage - java org.eclipse.util.UnixCrypt <key> <salt>");
            System.exit(1);
        }
        PrintStream printStream = System.err;
        printStream.println("Crypt=" + crypt(arg[0], arg[1]));
    }
}
