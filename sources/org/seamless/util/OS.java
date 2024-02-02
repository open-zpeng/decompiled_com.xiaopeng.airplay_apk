package org.seamless.util;
/* loaded from: classes.dex */
public class OS {
    public static boolean checkForLinux() {
        return checkForPresence("os.name", "linux");
    }

    public static boolean checkForHp() {
        return checkForPresence("os.name", "hp");
    }

    public static boolean checkForSolaris() {
        return checkForPresence("os.name", "sun");
    }

    public static boolean checkForWindows() {
        return checkForPresence("os.name", "win");
    }

    public static boolean checkForMac() {
        return checkForPresence("os.name", "mac");
    }

    private static boolean checkForPresence(String key, String value) {
        try {
            String tmp = System.getProperty(key);
            if (tmp != null) {
                return tmp.trim().toLowerCase().startsWith(value);
            }
            return false;
        } catch (Throwable th) {
            return false;
        }
    }
}
