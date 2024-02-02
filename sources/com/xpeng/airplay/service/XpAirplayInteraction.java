package com.xpeng.airplay.service;

import org.fourthline.cling.transport.impl.XpDlnaRequestInfo;
/* loaded from: classes.dex */
public class XpAirplayInteraction {
    public static final int CURRENT_DLNA = 0;
    public static final int REQUEST_DLNA = 1;
    private static XpAirplayInteraction xpAirplayInteraction;
    private String mCurrentDlnaIp;
    private int mCurrentDlnaPort;
    private String mRequestDlnaIp;
    private int mRequestDlnaPort;

    public static XpAirplayInteraction getInstance() {
        if (xpAirplayInteraction == null) {
            xpAirplayInteraction = new XpAirplayInteraction();
        }
        return xpAirplayInteraction;
    }

    public void setIp(int type, String ip) {
        switch (type) {
            case 0:
                this.mCurrentDlnaIp = ip;
                return;
            case 1:
                XpDlnaRequestInfo xpDlnaRequestInfo = XpDlnaRequestInfo.getInstance();
                this.mRequestDlnaIp = ip;
                xpDlnaRequestInfo.setIp(ip);
                return;
            default:
                return;
        }
    }

    public String getIp(int type) {
        switch (type) {
            case 0:
                String ip = this.mCurrentDlnaIp;
                return ip;
            case 1:
                XpDlnaRequestInfo xpDlnaRequestInfo = XpDlnaRequestInfo.getInstance();
                this.mRequestDlnaIp = xpDlnaRequestInfo.getIp();
                String ip2 = this.mRequestDlnaIp;
                return ip2;
            default:
                return null;
        }
    }

    public void setPort(int type, int port) {
        switch (type) {
            case 0:
                this.mCurrentDlnaPort = port;
                return;
            case 1:
                XpDlnaRequestInfo xpDlnaRequestInfo = XpDlnaRequestInfo.getInstance();
                this.mRequestDlnaPort = port;
                xpDlnaRequestInfo.setPort(port);
                return;
            default:
                return;
        }
    }

    public int getPort(int type) {
        switch (type) {
            case 0:
                int port = this.mCurrentDlnaPort;
                return port;
            case 1:
                XpDlnaRequestInfo xpDlnaRequestInfo = XpDlnaRequestInfo.getInstance();
                this.mRequestDlnaPort = xpDlnaRequestInfo.getPort();
                int port2 = this.mRequestDlnaPort;
                return port2;
            default:
                return 0;
        }
    }
}
