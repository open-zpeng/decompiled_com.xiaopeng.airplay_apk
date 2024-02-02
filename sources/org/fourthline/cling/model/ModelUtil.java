package org.fourthline.cling.model;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
/* loaded from: classes.dex */
public class ModelUtil {
    public static final boolean ANDROID_EMULATOR;
    public static final boolean ANDROID_RUNTIME;

    /* JADX WARN: Code restructure failed: missing block: B:13:0x004e, code lost:
        if ("sdk".equals(r2) != false) goto L17;
     */
    static {
        /*
            r0 = 0
            r1 = r0
            r2 = 0
            java.lang.Thread r3 = java.lang.Thread.currentThread()     // Catch: java.lang.Exception -> L22
            java.lang.ClassLoader r3 = r3.getContextClassLoader()     // Catch: java.lang.Exception -> L22
            java.lang.String r4 = "android.os.Build"
            java.lang.Class r3 = r3.loadClass(r4)     // Catch: java.lang.Exception -> L22
            java.lang.String r4 = "ID"
            java.lang.reflect.Field r4 = r3.getField(r4)     // Catch: java.lang.Exception -> L22
            java.lang.Object r4 = r4.get(r2)     // Catch: java.lang.Exception -> L22
            if (r4 == 0) goto L1f
            r4 = 1
            goto L20
        L1f:
            r4 = r0
        L20:
            r1 = r4
            goto L23
        L22:
            r3 = move-exception
        L23:
            org.fourthline.cling.model.ModelUtil.ANDROID_RUNTIME = r1
            java.lang.Thread r1 = java.lang.Thread.currentThread()     // Catch: java.lang.Exception -> L52
            java.lang.ClassLoader r1 = r1.getContextClassLoader()     // Catch: java.lang.Exception -> L52
            java.lang.String r3 = "android.os.Build"
            java.lang.Class r1 = r1.loadClass(r3)     // Catch: java.lang.Exception -> L52
            java.lang.String r3 = "PRODUCT"
            java.lang.reflect.Field r3 = r1.getField(r3)     // Catch: java.lang.Exception -> L52
            java.lang.Object r2 = r3.get(r2)     // Catch: java.lang.Exception -> L52
            java.lang.String r2 = (java.lang.String) r2     // Catch: java.lang.Exception -> L52
            java.lang.String r3 = "google_sdk"
            boolean r3 = r3.equals(r2)     // Catch: java.lang.Exception -> L52
            if (r3 != 0) goto L50
            java.lang.String r3 = "sdk"
            boolean r3 = r3.equals(r2)     // Catch: java.lang.Exception -> L52
            if (r3 == 0) goto L51
        L50:
            r0 = 1
        L51:
            goto L53
        L52:
            r1 = move-exception
        L53:
            org.fourthline.cling.model.ModelUtil.ANDROID_EMULATOR = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.model.ModelUtil.<clinit>():void");
    }

    public static boolean isStringConvertibleType(Set<Class> stringConvertibleTypes, Class clazz) {
        if (clazz.isEnum()) {
            return true;
        }
        for (Class toStringOutputType : stringConvertibleTypes) {
            if (toStringOutputType.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidUDAName(String name) {
        return ANDROID_RUNTIME ? (name == null || name.length() == 0) ? false : true : (name == null || name.length() == 0 || name.toLowerCase(Locale.ROOT).startsWith("xml") || !name.matches(Constants.REGEX_UDA_NAME)) ? false : true;
    }

    public static InetAddress getInetAddressByName(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String toCommaSeparatedList(Object[] o) {
        return toCommaSeparatedList(o, true, false);
    }

    public static String toCommaSeparatedList(Object[] o, boolean escapeCommas, boolean escapeDoubleQuotes) {
        if (o == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : o) {
            String objString = obj.toString().replaceAll("\\\\", "\\\\\\\\");
            if (escapeCommas) {
                objString = objString.replaceAll(",", "\\\\,");
            }
            if (escapeDoubleQuotes) {
                objString = objString.replaceAll("\"", "\\\"");
            }
            sb.append(objString);
            sb.append(",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String[] fromCommaSeparatedList(String s) {
        return fromCommaSeparatedList(s, true);
    }

    public static String[] fromCommaSeparatedList(String s, boolean unescapeCommas) {
        if (s == null || s.length() == 0) {
            return null;
        }
        if (unescapeCommas) {
            s = s.replaceAll("\\\\,", "XXX1122334455XXX");
        }
        String[] split = s.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("XXX1122334455XXX", ",");
            split[i] = split[i].replaceAll("\\\\\\\\", "\\\\");
        }
        return split;
    }

    public static String toTimeString(long seconds) {
        long hours = seconds / 3600;
        long remainder = seconds % 3600;
        long minutes = remainder / 60;
        long secs = remainder % 60;
        StringBuilder sb = new StringBuilder();
        sb.append(hours < 10 ? "0" : "");
        sb.append(hours);
        sb.append(":");
        sb.append(minutes < 10 ? "0" : "");
        sb.append(minutes);
        sb.append(":");
        sb.append(secs < 10 ? "0" : "");
        sb.append(secs);
        return sb.toString();
    }

    public static long fromTimeString(String s) {
        if (s.lastIndexOf(".") != -1) {
            s = s.substring(0, s.lastIndexOf("."));
        }
        String[] split = s.split(":");
        if (split.length != 3) {
            throw new IllegalArgumentException("Can't parse time string: " + s);
        }
        return (Long.parseLong(split[0]) * 3600) + (Long.parseLong(split[1]) * 60) + Long.parseLong(split[2]);
    }

    public static String commaToNewline(String s) {
        StringBuilder sb = new StringBuilder();
        String[] split = s.split(",");
        for (String splitString : split) {
            sb.append(splitString);
            sb.append(",");
            sb.append("\n");
        }
        if (sb.length() > 2) {
            sb.deleteCharAt(sb.length() - 2);
        }
        return sb.toString();
    }

    public static String getLocalHostName(boolean includeDomain) {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            if (!includeDomain && hostname.indexOf(".") != -1) {
                return hostname.substring(0, hostname.indexOf("."));
            }
            return hostname;
        } catch (Exception e) {
            return "UNKNOWN HOST";
        }
    }

    public static byte[] getFirstNetworkInterfaceHardwareAddress() {
        try {
            Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            Iterator it = Collections.list(interfaceEnumeration).iterator();
            while (it.hasNext()) {
                NetworkInterface iface = (NetworkInterface) it.next();
                if (!iface.isLoopback() && iface.isUp() && iface.getHardwareAddress() != null) {
                    return iface.getHardwareAddress();
                }
            }
            throw new RuntimeException("Could not discover first network interface hardware address");
        } catch (Exception e) {
            throw new RuntimeException("Could not discover first network interface hardware address");
        }
    }
}
