package com.xpeng.airplay.service;

import android.view.Surface;
/* loaded from: classes.dex */
public interface IServer {
    public static final int VIDEO_RATE_PAUSE = 0;
    public static final int VIDEO_RATE_PLAY = 1;

    void addStateListener(IStateListener iStateListener);

    void destroyConnection();

    MediaPlaybackInfo getMediaPlaybackInfo();

    int getScreenId();

    ServerConfig getServerConfig();

    int getServerPort();

    boolean isAudioStream();

    boolean isServerActive();

    void removeStateListener();

    void setMediaPlaybackInfo(MediaPlaybackInfo mediaPlaybackInfo);

    void setMirrorSurface(Surface surface);

    void setServerConfig(ServerConfig serverConfig);

    void setVideoPlaybackState(int i);

    void startServer();

    void stopServer(boolean z);

    void updateRearBtState(boolean z);

    void updateWifiMode(WifiMode wifiMode);
}
