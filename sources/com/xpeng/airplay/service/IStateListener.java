package com.xpeng.airplay.service;
/* loaded from: classes.dex */
public interface IStateListener {
    void onAudioFocusChanged(boolean z);

    void onAudioProgressUpdated(MediaPlaybackInfo mediaPlaybackInfo);

    void onClientConnected(int i, int i2);

    void onClientDisconnected(int i, int i2);

    void onMetadataUpdated(MediaMetaData mediaMetaData);

    void onMirrorSizeChanged(int i, int i2);

    void onScreenMirrorStarted();

    void onVideoPlay(MediaPlayInfo mediaPlayInfo);

    void onVideoRateChanged(int i, int i2);

    void onVideoScrubbed(int i, int i2);

    void onVideoStopped(int i);

    void onVolumeChanged(int i, float f);
}
