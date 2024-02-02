package com.xpeng.airplay.dlna;

import android.util.Log;
import com.xpeng.airplay.service.MediaPlaybackInfo;
import java.util.ArrayList;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
/* loaded from: classes.dex */
public final class AudioController implements IAudioControl {
    protected static final int MAX_VOLUME = 100;
    private static final String TAG = "AbstractAudioControl";
    private static final float VOLUME_NOT_MUTED = -1.0f;
    private final ArrayList<UnsignedIntegerFourBytes> mInstanceIds = new ArrayList<>(1);
    private float mLastVolume;
    private final DLNAServer mServer;

    public AudioController(DLNAServer server) {
        this.mServer = server;
        this.mInstanceIds.add(new UnsignedIntegerFourBytes(server.getInstanceId()));
        this.mLastVolume = VOLUME_NOT_MUTED;
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public UnsignedIntegerFourBytes[] getInstanceIds() {
        return (UnsignedIntegerFourBytes[]) this.mInstanceIds.toArray(new UnsignedIntegerFourBytes[0]);
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public boolean hasInstanceId(UnsignedIntegerFourBytes id) {
        return this.mInstanceIds.contains(id);
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public void setMute(String channelName, boolean desiredMute) {
        Log.d(TAG, "setMute(): channel = " + channelName + ", isMute = " + desiredMute);
        if (this.mServer != null) {
            float currentVol = this.mServer.getMediaPlaybackInfo().getVolume();
            if (desiredMute) {
                this.mServer.onVolumeChanged(0.0f);
                this.mLastVolume = currentVol;
                return;
            }
            if (Float.compare(this.mLastVolume, VOLUME_NOT_MUTED) != 0 && Float.compare(currentVol, 0.0f) == 0) {
                this.mServer.onVolumeChanged(this.mLastVolume);
            }
            this.mLastVolume = VOLUME_NOT_MUTED;
        }
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public boolean getMute(String channelName) {
        Log.d(TAG, "getMute(); channelName = " + channelName);
        if (this.mServer != null) {
            float vol = this.mServer.getMediaPlaybackInfo().getVolume();
            return Float.compare(vol, 0.0f) == 0;
        }
        return false;
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public void setVolume(String channelName, UnsignedIntegerTwoBytes targetVol) {
        Log.d(TAG, "setVolume(): channelName = " + channelName + ", targetVol = " + targetVol);
        if (this.mServer != null) {
            float scaledVol = Double.valueOf(targetVol.getValue().intValue() / 100.0d).floatValue();
            this.mServer.onVolumeChanged(scaledVol);
        }
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public UnsignedIntegerTwoBytes getVolume(String channelName) {
        Log.d(TAG, "getVolume(): channelName = " + channelName);
        if (this.mServer != null) {
            MediaPlaybackInfo playbackInfo = this.mServer.getMediaPlaybackInfo();
            return new UnsignedIntegerTwoBytes(playbackInfo.getVolume() * 100.0f);
        }
        return new UnsignedIntegerTwoBytes(0L);
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public void setBrightness(UnsignedIntegerTwoBytes targetBright) {
        Log.d(TAG, "setBrightness(): targetBright = " + targetBright);
    }

    @Override // com.xpeng.airplay.dlna.IAudioControl
    public UnsignedIntegerTwoBytes getBrightness() {
        Log.d(TAG, "getBrightness()");
        return new UnsignedIntegerTwoBytes(0L);
    }
}
