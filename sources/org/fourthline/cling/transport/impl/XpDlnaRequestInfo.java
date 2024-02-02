package org.fourthline.cling.transport.impl;
/* loaded from: classes.dex */
public class XpDlnaRequestInfo {
    private static XpDlnaRequestInfo xpDlnaRequestInfo;
    private String mIp;
    private int mPort;

    public static XpDlnaRequestInfo getInstance() {
        if (xpDlnaRequestInfo == null) {
            xpDlnaRequestInfo = new XpDlnaRequestInfo();
        }
        return xpDlnaRequestInfo;
    }

    public void setIp(String ip) {
        this.mIp = ip;
    }

    public String getIp() {
        return this.mIp;
    }

    public void setPort(int port) {
        this.mPort = port;
    }

    public int getPort() {
        return this.mPort;
    }
}
