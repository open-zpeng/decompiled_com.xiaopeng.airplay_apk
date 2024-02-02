package com.xpeng.airplay.dlna;

import com.xpeng.airplay.service.IAirplayServer;
/* loaded from: classes.dex */
public interface IDLNAServer extends IAirplayServer {
    public static final int AUDIO_RENDER_INSTANCE_ID = 0;
    public static final int AV_TRANSPORT_INSTANCE_ID = 0;

    int getInstanceId();

    int getPlaybackState();

    void onVolumeChanged(float f);

    void setMediaType(int i);
}
