package com.xpeng.airplay.service;

import android.os.Handler;
import android.view.Surface;
import java.util.Collection;
/* loaded from: classes.dex */
public interface IServerProxy {
    void addCallbackHandler(Handler handler);

    void destroyConnection();

    ServerConfig getServerConfig(int i);

    int getServerPort(int i);

    String getServerTypeName(int i);

    boolean hasActiveConnection();

    boolean isServerActive(int i);

    void onClientDied();

    void onRearBtStateChanged(boolean z);

    void removeCallbackHandler();

    void saveSystemVolume(int i);

    void setMediaPlaybackInfo(MediaPlaybackInfo mediaPlaybackInfo);

    void setMirrorSurface(Surface surface);

    void setVideoPlaybackState(int i);

    void startServer();

    void stopServer(boolean z);

    void updateAirplayState(int i);

    void updateServerConfig(Collection<ServerConfig> collection);

    void updateWifiMode(WifiMode wifiMode);
}
