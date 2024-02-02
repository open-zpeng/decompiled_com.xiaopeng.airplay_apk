package com.xpeng.airplay.service;
/* loaded from: classes.dex */
public final class ServerConfig {
    private String ipAddr;
    private String macAddr;
    private String name;
    private int screenId;
    private int type;

    public ServerConfig(int type, String name, String ip, String macAddr) {
        this.type = type;
        this.name = name;
        this.ipAddr = ip;
        this.macAddr = macAddr;
        this.screenId = 0;
    }

    public ServerConfig(int screenId, int type, String name, String ip, String macAddr) {
        this.screenId = screenId;
        this.type = type;
        this.name = name;
        this.ipAddr = ip;
        this.macAddr = macAddr;
    }

    public void update(int screenId, String name, String ipAddr, String macAddr) {
        this.screenId = screenId;
        this.name = name;
        this.ipAddr = ipAddr;
        this.macAddr = macAddr;
    }

    public int getType() {
        return this.type;
    }

    public int getScreenId() {
        return this.screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setIpAddr(String ip) {
        this.ipAddr = ip;
    }

    public String getIpAddr() {
        return this.ipAddr;
    }

    public void setMacAddr(String mac) {
        this.macAddr = mac;
    }

    public String getMacAddr() {
        return this.macAddr;
    }

    public String toString() {
        return "ServerConfig {screenId = " + this.screenId + ",type = " + this.type + ",name = " + this.name + ",ip = " + this.ipAddr + ",mac = " + this.macAddr + "}";
    }

    public boolean equals(Object obj) {
        if (obj instanceof ServerConfig) {
            if (obj == this) {
                return true;
            }
            ServerConfig other = (ServerConfig) obj;
            return other.type == this.type && other.name.equals(this.name);
        }
        return false;
    }

    public int hashCode() {
        int hashVal = this.type ^ (this.type >>> 32);
        return (31 * ((31 * ((31 * hashVal) + this.name.hashCode())) + this.ipAddr.hashCode())) + this.macAddr.hashCode();
    }
}
