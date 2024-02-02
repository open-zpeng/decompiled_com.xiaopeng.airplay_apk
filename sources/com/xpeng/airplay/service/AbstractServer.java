package com.xpeng.airplay.service;

import android.view.Surface;
/* loaded from: classes.dex */
public abstract class AbstractServer implements IServer {
    protected ServerConfig mConfig;
    protected IStateListener mStateListener;

    @Override // com.xpeng.airplay.service.IServer
    public abstract int getServerPort();

    @Override // com.xpeng.airplay.service.IServer
    public abstract boolean isServerActive();

    @Override // com.xpeng.airplay.service.IServer
    public abstract void startServer();

    @Override // com.xpeng.airplay.service.IServer
    public abstract void stopServer(boolean z);

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isRearServer() {
        return this.mConfig.getScreenId() == 3;
    }

    @Override // com.xpeng.airplay.service.IServer
    public boolean isAudioStream() {
        return false;
    }

    @Override // com.xpeng.airplay.service.IServer
    public void addStateListener(IStateListener listener) {
        this.mStateListener = listener;
    }

    @Override // com.xpeng.airplay.service.IServer
    public void removeStateListener() {
        this.mStateListener = null;
    }

    @Override // com.xpeng.airplay.service.IServer
    public void setServerConfig(ServerConfig config) {
        this.mConfig = config;
    }

    @Override // com.xpeng.airplay.service.IServer
    public ServerConfig getServerConfig() {
        return this.mConfig;
    }

    @Override // com.xpeng.airplay.service.IServer
    public int getScreenId() {
        if (this.mConfig != null) {
            return this.mConfig.getScreenId();
        }
        return 0;
    }

    @Override // com.xpeng.airplay.service.IServer
    public void destroyConnection() {
    }

    @Override // com.xpeng.airplay.service.IServer
    public void setMirrorSurface(Surface surface) {
    }

    @Override // com.xpeng.airplay.service.IServer
    public MediaPlaybackInfo getMediaPlaybackInfo() {
        return null;
    }

    @Override // com.xpeng.airplay.service.IServer
    public void setMediaPlaybackInfo(MediaPlaybackInfo info) {
    }

    @Override // com.xpeng.airplay.service.IServer
    public void setVideoPlaybackState(int state) {
    }

    @Override // com.xpeng.airplay.service.IServer
    public void updateWifiMode(WifiMode mode) {
    }

    @Override // com.xpeng.airplay.service.IServer
    public void updateRearBtState(boolean on) {
        if (isRearServer() && this.mStateListener != null && isServerActive()) {
            setVideoPlaybackState(4);
            this.mStateListener.onVideoRateChanged(this.mConfig.getType(), on ? 1 : 0);
        }
    }
}
