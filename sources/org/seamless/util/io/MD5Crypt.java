package org.seamless.util.io;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
/* loaded from: classes.dex */
public class MD5Crypt {
    private static final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String to64(long v, int size) {
        StringBuffer result = new StringBuffer();
        while (true) {
            size--;
            if (size >= 0) {
                result.append(itoa64.charAt((int) (63 & v)));
                v >>>= 6;
            } else {
                return result.toString();
            }
        }
    }

    private static final void clearbits(byte[] bits) {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = 0;
        }
    }

    private static final int bytes2u(byte inp) {
        return inp & 255;
    }

    public static final String crypt(String password) {
        StringBuffer salt = new StringBuffer();
        Random rnd = new Random();
        while (salt.length() < 8) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.substring(index, index + 1));
        }
        return crypt(password, salt.toString(), "$1$");
    }

    public static final String crypt(String password, String salt) {
        return crypt(password, salt, "$1$");
    }

    public static final String crypt(String password, String salt, String magic) {
        try {
            MessageDigest ctx = MessageDigest.getInstance("md5");
            MessageDigest ctx1 = MessageDigest.getInstance("md5");
            if (salt.startsWith(magic)) {
                salt = salt.substring(magic.length());
            }
            if (salt.indexOf(36) != -1) {
                salt = salt.substring(0, salt.indexOf(36));
            }
            if (salt.length() > 8) {
                salt = salt.substring(0, 8);
            }
            ctx.update(password.getBytes());
            ctx.update(magic.getBytes());
            ctx.update(salt.getBytes());
            ctx1.update(password.getBytes());
            ctx1.update(salt.getBytes());
            ctx1.update(password.getBytes());
            byte[] finalState = ctx1.digest();
            int pl = password.length();
            while (true) {
                int i = 16;
                if (pl <= 0) {
                    break;
                }
                if (pl <= 16) {
                    i = pl;
                }
                ctx.update(finalState, 0, i);
                pl -= 16;
            }
            clearbits(finalState);
            for (int i2 = password.length(); i2 != 0; i2 >>>= 1) {
                if ((i2 & 1) != 0) {
                    ctx.update(finalState, 0, 1);
                } else {
                    ctx.update(password.getBytes(), 0, 1);
                }
            }
            byte[] finalState2 = ctx.digest();
            for (int i3 = 0; i3 < 1000; i3++) {
                try {
                    MessageDigest ctx12 = MessageDigest.getInstance("md5");
                    if ((i3 & 1) != 0) {
                        ctx12.update(password.getBytes());
                    } else {
                        ctx12.update(finalState2, 0, 16);
                    }
                    if (i3 % 3 != 0) {
                        ctx12.update(salt.getBytes());
                    }
                    if (i3 % 7 != 0) {
                        ctx12.update(password.getBytes());
                    }
                    if ((i3 & 1) != 0) {
                        ctx12.update(finalState2, 0, 16);
                    } else {
                        ctx12.update(password.getBytes());
                    }
                    finalState2 = ctx12.digest();
                } catch (NoSuchAlgorithmException e) {
                    return null;
                }
            }
            StringBuffer result = new StringBuffer();
            result.append(magic);
            result.append(salt);
            result.append("$");
            long l = (bytes2u(finalState2[0]) << 16) | (bytes2u(finalState2[6]) << 8) | bytes2u(finalState2[12]);
            result.append(to64(l, 4));
            long l2 = (bytes2u(finalState2[1]) << 16) | (bytes2u(finalState2[7]) << 8) | bytes2u(finalState2[13]);
            result.append(to64(l2, 4));
            long l3 = (bytes2u(finalState2[2]) << 16) | (bytes2u(finalState2[8]) << 8) | bytes2u(finalState2[14]);
            result.append(to64(l3, 4));
            long l4 = (bytes2u(finalState2[3]) << 16) | (bytes2u(finalState2[9]) << 8) | bytes2u(finalState2[15]);
            result.append(to64(l4, 4));
            long l5 = (bytes2u(finalState2[10]) << 8) | (bytes2u(finalState2[4]) << 16) | bytes2u(finalState2[5]);
            result.append(to64(l5, 4));
            long l6 = bytes2u(finalState2[11]);
            result.append(to64(l6, 2));
            clearbits(finalState2);
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex);
            return null;
        }
    }

    public static boolean isEqual(String clear, String encrypted) {
        return isEqual(clear.toCharArray(), encrypted);
    }

    public static boolean isEqual(char[] clear, String encrypted) {
        String[] split = encrypted.split("\\$");
        if (split.length != 4) {
            return false;
        }
        char[] a = encrypted.toCharArray();
        char[] b = crypt(new String(clear), split[2], "$" + split[1] + "$").toCharArray();
        if (a == null || b == null) {
            return a == b;
        } else if (a.length != b.length) {
            return false;
        } else {
            boolean equals = true;
            for (int i = 0; i < a.length && equals; i++) {
                equals = a[i] == b[i];
            }
            boolean result = equals;
            return result;
        }
    }
}
