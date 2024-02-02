package com.xpeng.airplay.dlna;

import android.util.Log;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.fourthline.cling.support.model.TransportState;
/* loaded from: classes.dex */
public final class AVTransportServiceImpl extends AbstractAVTransportService {
    private static final String ERROR_MSG_NO_INSTANCE_ID = "No Such Instance Id";
    private static final String TAG = "AVTransportServiceImpl";
    private final IAVTransportControl mAVTransportController;

    public AVTransportServiceImpl(LastChange lastchange, IAVTransportControl controller) {
        super(lastchange);
        this.mAVTransportController = controller;
    }

    @Override // org.fourthline.cling.support.lastchange.LastChangeDelegator
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return this.mAVTransportController.getInstanceIds();
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "getCurrentTransportActions(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            return this.mAVTransportController.getCurrentTransportActions();
        }
        Log.e(TAG, "getCurrentTransportActions():No Such Instance Id");
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "getDeviceCapabilities(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            return this.mAVTransportController.getDeviceCapabilities();
        }
        Log.e(TAG, "getDeviceCapabilities():No Such Instance Id");
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "getMediaInfo(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            return this.mAVTransportController.getMediaInfo();
        }
        Log.e(TAG, "getMediaInfo():No Such Instance Id");
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "getPositionInfo(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            return this.mAVTransportController.getPositionInfo();
        }
        Log.e(TAG, "getPositionInfo():No Such Instance Id");
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "getTransportInfo(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            return this.mAVTransportController.getTransportInfo();
        }
        Log.e(TAG, "getTransportInfo(): No Such Instance Id");
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "getTransportSettings(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            return this.mAVTransportController.getTransportSettings();
        }
        Log.e(TAG, "getTransportSettings():No Such Instance Id");
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "pause(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            this.mAVTransportController.pause();
        } else {
            Log.e(TAG, "pause(): No Such Instance Id");
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
        }
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        Log.d(TAG, "play(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            this.mAVTransportController.play(speed);
        } else {
            Log.e(TAG, "play(): No Such Instance Id");
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
        }
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void seek(UnsignedIntegerFourBytes instanceId, String mode, String position) throws AVTransportException {
        Log.d(TAG, "seek(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            this.mAVTransportController.seek(mode, position);
        } else {
            Log.e(TAG, "seek(): No Such Instance Id");
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
        }
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String url, String metaData) throws AVTransportException {
        Log.d(TAG, "setAVTransportURI(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            this.mAVTransportController.setAVTransportURI(url, metaData);
        } else {
            Log.e(TAG, "setAVTransportURI(): No Such Instance Id");
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
        }
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        Log.d(TAG, "stop(): id = " + instanceId);
        if (this.mAVTransportController.hasInstanceId(instanceId)) {
            this.mAVTransportController.stop();
        } else {
            Log.e(TAG, "stop(): No Such Instance Id");
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID, ERROR_MSG_NO_INSTANCE_ID);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setVideoPlaybackState(LastChange lastchange, int state) {
        TransportState currentTs;
        Log.d(TAG, "setVideoPlaybackState(): state = " + state);
        if (state == 0) {
            currentTs = TransportState.PLAYING;
        } else {
            switch (state) {
                case 2:
                case 3:
                    currentTs = TransportState.STOPPED;
                    break;
                default:
                    currentTs = TransportState.PAUSED_PLAYBACK;
                    break;
            }
        }
        TransportInfo ti = this.mAVTransportController.getTransportInfo();
        if (lastchange != null && !currentTs.equals(ti.getCurrentTransportState())) {
            this.mAVTransportController.setTransportState(currentTs);
            lastchange.setEventedValue(getCurrentInstanceIds()[0], new AVTransportVariable.TransportState(currentTs), new AVTransportVariable.TransportStatus(ti.getCurrentTransportStatus()));
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            }
            Log.d(TAG, "setVideoPlaybackState(): last change = " + lastchange.toString());
            lastchange.fire(this.propertyChangeSupport);
        }
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void next(UnsignedIntegerFourBytes instanceId) {
        Log.d(TAG, "next()");
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void previous(UnsignedIntegerFourBytes instanceId) {
        Log.d(TAG, "previous()");
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void record(UnsignedIntegerFourBytes instanceId) {
        Log.d(TAG, "record()");
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String mode) {
        Log.d(TAG, "setPlayMode(): mode = " + mode);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String mode) {
        Log.d(TAG, "setRecordQualityMode(): mode = " + mode);
    }

    @Override // org.fourthline.cling.support.avtransport.AbstractAVTransportService
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String url, String metaData) {
        Log.d(TAG, "setNextAVTransportURI(): url = " + url);
    }
}
