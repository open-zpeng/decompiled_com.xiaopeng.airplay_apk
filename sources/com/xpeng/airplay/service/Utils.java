package com.xpeng.airplay.service;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public final class Utils {
    private static final int DISPLAY_TOPOLOGY_TYPE_2 = 2;
    private static final int DISPLAY_TOPOLOGY_TYPE_4 = 4;
    private static final String FAKE_HW_ADDR = "ba:53:05:8f:ec:e1";
    private static final String PROP_AIRPLAY_DEBUG_MODE = "persist.xp.airplay_debug_mode";
    private static final String PROP_AIRPLAY_SCREEN_ID = "sys.xp.airplay_screen_id";
    private static final String PROP_DISPLAY_TOPOLOGY = "ro.boot.hwi_disp_topo";
    private static final String PROP_DLNA_IGNORE_IFACE = "sys.xp.dlna_ignore_iface";
    private static final String PROP_DLNA_SEARCH_OPT = "sys.xp.dlna_search_opt";
    private static final String PROP_MULTIPLE_AIRPLAY_DEBUG = "persist.xp.multiple_airplay_debug";
    private static final String PROP_WIFI_ACTIVE_IFACE = "wifi.interface";
    public static final int SCREEN_IVI = 0;
    public static final int SCREEN_RSE = 3;
    private static final String WIFI_AP_IFACE_REGX = "wlan\\d|ap_br_wlan\\d";
    private static final String WIFI_STA_IFACE_REGX = "wlan\\d";
    private static final String TAG = Utils.class.getSimpleName();
    private static final Map<String, String> sModelName = new HashMap();

    static {
        sModelName.put("H93", "X9");
        sModelName.put("E38", "G9");
        sModelName.put("F30", "G6");
        sModelName.put("E28A", "P7i");
        sModelName.put("D55", "P5");
        sModelName.put("E28", "P7");
        sModelName.put("D22", "G3i");
        sModelName.put("D21", "G3");
        sModelName.put("D20", "G3");
    }

    private static String formatMacAddress(byte[] macBytes) {
        StringBuilder res = new StringBuilder();
        for (byte b : macBytes) {
            res.append(String.format("%02X:", Byte.valueOf(b)));
        }
        if (res.length() > 0) {
            res.deleteCharAt(res.length() - 1);
        }
        return res.toString();
    }

    public static String getWifiMacAddress(WifiMode mode) {
        try {
            String wifiIface = SystemProperties.get(PROP_WIFI_ACTIVE_IFACE, "wlan0");
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.isLoopback() && nif.isUp()) {
                    String name = nif.getName();
                    if (name.matches(WIFI_STA_IFACE_REGX) || name.matches(WIFI_AP_IFACE_REGX)) {
                        if ((mode == WifiMode.STA && name.equals(wifiIface)) || (mode == WifiMode.AP && name.startsWith("wlan"))) {
                            byte[] macBytes = nif.getHardwareAddress();
                            return formatMacAddress(macBytes);
                        } else if (0 == 0) {
                            return FAKE_HW_ADDR;
                        }
                    }
                }
            }
            return FAKE_HW_ADDR;
        } catch (Exception e) {
            e.printStackTrace();
            return FAKE_HW_ADDR;
        }
    }

    public static NetworkInterface getNetworkInterface(WifiMode mode) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            String wifiIface = SystemProperties.get(PROP_WIFI_ACTIVE_IFACE, "wlan0");
            for (NetworkInterface ni : interfaces) {
                if (!ni.isLoopback() && ni.isUp() && ((mode == WifiMode.STA && ni.getName().equals(wifiIface)) || (mode == WifiMode.AP && ni.getName().matches(WIFI_AP_IFACE_REGX) && !ni.getName().equals(wifiIface)))) {
                    return ni;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getWifiApIfaceIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            String wifiIface = SystemProperties.get(PROP_WIFI_ACTIVE_IFACE, "wlan0");
            for (NetworkInterface ni : interfaces) {
                if (!ni.isLoopback() && ni.isUp() && ni.getName().matches(WIFI_AP_IFACE_REGX) && !ni.getName().equals(wifiIface)) {
                    List<InetAddress> inetAddres = Collections.list(ni.getInetAddresses());
                    for (InetAddress ia : inetAddres) {
                        if (ia instanceof Inet4Address) {
                            String hostAddr = ia.getHostAddress();
                            String str = TAG;
                            Log.d(str, "getWifiApIfaceIpAddress(): " + hostAddr);
                            return hostAddr;
                        }
                    }
                    continue;
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String getWifiIpAddress(Context context) {
        WifiInfo wi;
        WifiManager wifiMgr = (WifiManager) context.getSystemService("wifi");
        StringBuilder sb = new StringBuilder();
        if (wifiMgr != null && (wi = wifiMgr.getConnectionInfo()) != null) {
            int ipAddr = wi.getIpAddress();
            sb.append(ipAddr & 255);
            sb.append(".");
            sb.append((ipAddr >> 8) & 255);
            sb.append(".");
            sb.append((ipAddr >> 16) & 255);
            sb.append(".");
            sb.append((ipAddr >> 24) & 255);
        }
        return sb.toString();
    }

    public static boolean isUserBuild() {
        return Build.TYPE.equals("user");
    }

    public static boolean hasRearDisplay() {
        int type = SystemProperties.getInt(PROP_DISPLAY_TOPOLOGY, 0);
        boolean res = type == 4 || type == 2;
        return res || (!isUserBuild() && SystemProperties.getBoolean(PROP_MULTIPLE_AIRPLAY_DEBUG, false));
    }

    public static boolean isDebugMode() {
        return SystemProperties.getBoolean(PROP_AIRPLAY_DEBUG_MODE, false);
    }

    public static String getActiveWifiIface() {
        return SystemProperties.get(PROP_WIFI_ACTIVE_IFACE, "wlan0");
    }

    public static void setDlnaIgnoreIface(String iface) {
        SystemProperties.set(PROP_DLNA_IGNORE_IFACE, iface);
    }

    public static String getDlnaIgnoreIface() {
        return SystemProperties.get(PROP_DLNA_IGNORE_IFACE, "");
    }

    public static void setDlnaSearchOpt(boolean val) {
        SystemProperties.set(PROP_DLNA_SEARCH_OPT, String.valueOf(val));
    }

    public static String getServerNamePrefix(String model) {
        return sModelName.get(model);
    }

    public static int getAirplayScreenId() {
        return SystemProperties.getInt(PROP_AIRPLAY_SCREEN_ID, -1);
    }

    public static void setIp(int type, String ip) {
        XpAirplayInteraction xpAirplayInteraction = XpAirplayInteraction.getInstance();
        xpAirplayInteraction.setIp(type, ip);
    }

    public static String getIp(int type) {
        XpAirplayInteraction xpAirplayInteraction = XpAirplayInteraction.getInstance();
        return xpAirplayInteraction.getIp(type);
    }

    public static void setPort(int type, int port) {
        XpAirplayInteraction xpAirplayInteraction = XpAirplayInteraction.getInstance();
        xpAirplayInteraction.setPort(type, port);
    }

    public static int getPort(int type) {
        XpAirplayInteraction xpAirplayInteraction = XpAirplayInteraction.getInstance();
        return xpAirplayInteraction.getPort(type);
    }
}
