package com.xpeng.airplay.dlna;

import android.util.Log;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlErrorCode;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;
/* loaded from: classes.dex */
public final class AudioRenderControlImpl extends AbstractAudioRenderingControl {
    private static final String ERROR_MSG_NO_INSTANCE_ID = "No Such Instance Id";
    private static final String TAG = "AudioRenderControlImpl";
    private final IAudioControl mAudioController;
    private final Channel[] mMasterChannel;

    public AudioRenderControlImpl(LastChange lastchange, IAudioControl controller) {
        super(lastchange);
        this.mAudioController = controller;
        this.mMasterChannel = new Channel[]{Channel.Master};
    }

    @Override // org.fourthline.cling.support.lastchange.LastChangeDelegator
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        Log.d(TAG, "getCurrentInstanceIds()");
        return this.mAudioController.getInstanceIds();
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException {
        Log.d(TAG, "setMute(): " + channelName);
        if (this.mAudioController.hasInstanceId(instanceId)) {
            this.mAudioController.setMute(channelName, desiredMute);
            return;
        }
        throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        Log.d(TAG, "getMute(): " + channelName);
        if (this.mAudioController.hasInstanceId(instanceId)) {
            return this.mAudioController.getMute(channelName);
        }
        throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        Log.d(TAG, "setVolume(): " + channelName);
        if (this.mAudioController.hasInstanceId(instanceId)) {
            this.mAudioController.setVolume(channelName, desiredVolume);
            return;
        }
        throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        Log.d(TAG, "getVolume(): " + channelName);
        if (this.mAudioController.hasInstanceId(instanceId)) {
            return this.mAudioController.getVolume(channelName);
        }
        throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    public UnsignedIntegerTwoBytes getBrightness(UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        Log.d(TAG, "getBrightness()");
        if (this.mAudioController.hasInstanceId(instanceId)) {
            return this.mAudioController.getBrightness();
        }
        throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    public void setBrightness(UnsignedIntegerFourBytes instanceId, UnsignedIntegerTwoBytes desiredBrightness) throws RenderingControlException {
        Log.d(TAG, "setBrightness(): " + desiredBrightness);
        if (this.mAudioController.hasInstanceId(instanceId)) {
            this.mAudioController.setBrightness(desiredBrightness);
            return;
        }
        throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
    protected Channel[] getCurrentChannels() {
        return this.mMasterChannel;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyVolumeChanged(LastChange lastchange, float volume) {
        Log.d(TAG, "notifyVolumeChanged(): vol " + volume);
        if (lastchange != null) {
            int vol = ((int) volume) * 100;
            lastchange.setEventedValue(getCurrentInstanceIds()[0], new RenderingControlVariable.Volume(new ChannelVolume(this.mMasterChannel[0], Integer.valueOf(vol))));
        }
    }
}
