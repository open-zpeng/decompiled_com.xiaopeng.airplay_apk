package com.xpeng.airplay.service;
/* loaded from: classes.dex */
public interface IAirplayServer {
    void onClientConnected();

    void onClientDisconnected();

    void onVideoPlay(String str, String str2, int i, int i2);

    void onVideoRate(int i);

    void onVideoScrub(int i);

    void onVideoStop();
}
