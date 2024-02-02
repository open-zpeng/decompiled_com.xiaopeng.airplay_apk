package com.xpeng.airplay.dlna;

import android.util.Log;
import com.xpeng.airplay.service.MediaPlaybackInfo;
import com.xpeng.airplay.service.Utils;
import java.util.ArrayList;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.TransportStatus;
/* loaded from: classes.dex */
public final class AVTransportController implements IAVTransportControl {
    private static final String DC_TITLE = "title";
    private static final String ERROR_MSG_NO_SERVER = "No Server Is Found";
    private static final String ERROR_MSG_UNSUPPORTED_SEEK_MODE = "Unsupported Seek Mode";
    private static final String TAG = "AVTransportController";
    private static final String UPNP_CLASS = "class";
    private static final String UPNP_CLASS_AUDIO = "object.item.audioItem";
    private DeviceCapabilities mDeviceCaps;
    private MediaInfo mMediaInfo;
    private PositionInfo mPositionInfo;
    private DLNAServer mServer;
    private TransportInfo mTransportInfo;
    private TransportSettings mTransportSettings;
    private static final TransportAction[] TRANSPORT_ACTION_STOPPED = {TransportAction.Play};
    private static final TransportAction[] TRANSPORT_ACTION_PLAYING = {TransportAction.Stop, TransportAction.Pause, TransportAction.Seek};
    private static final TransportAction[] TRANSPORT_ACTION_PAUSE = {TransportAction.Play, TransportAction.Seek, TransportAction.Stop};
    private TransportState mActiveTransportState = TransportState.STOPPED;
    private ArrayList<UnsignedIntegerFourBytes> mInstanceIds = new ArrayList<>(1);

    public AVTransportController(DLNAServer server) {
        this.mServer = server;
        this.mInstanceIds.add(new UnsignedIntegerFourBytes(this.mServer.getInstanceId()));
        this.mTransportInfo = new TransportInfo(TransportState.STOPPED, TransportStatus.OK);
        this.mMediaInfo = new MediaInfo();
        this.mPositionInfo = new PositionInfo();
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public UnsignedIntegerFourBytes[] getInstanceIds() {
        return (UnsignedIntegerFourBytes[]) this.mInstanceIds.toArray(new UnsignedIntegerFourBytes[0]);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public boolean hasInstanceId(UnsignedIntegerFourBytes id) {
        return this.mInstanceIds.contains(id);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public TransportAction[] getCurrentTransportActions() {
        Log.d(TAG, "getCurrentTransportActions()");
        switch (this.mTransportInfo.getCurrentTransportState()) {
            case PLAYING:
                return TRANSPORT_ACTION_PLAYING;
            case PAUSED_PLAYBACK:
                return TRANSPORT_ACTION_PAUSE;
            default:
                return TRANSPORT_ACTION_STOPPED;
        }
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public DeviceCapabilities getDeviceCapabilities() {
        Log.d(TAG, "getDeviceCapabilities()");
        if (this.mDeviceCaps == null) {
            this.mDeviceCaps = new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
        }
        return this.mDeviceCaps;
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public TransportInfo getTransportInfo() {
        String ipRequest = Utils.getIp(1);
        String ipCurrent = Utils.getIp(0);
        Log.d(TAG, "getTransportInfo request : " + ipRequest + ", Current : " + ipCurrent);
        if (ipCurrent == null) {
            Log.d(TAG, "getTransportInfo ipCurrent null : " + this.mTransportInfo);
            this.mTransportInfo.setCurrentTransportState(this.mActiveTransportState);
        } else if (ipRequest.equals(ipCurrent)) {
            Log.d(TAG, "getTransportInfo : " + this.mTransportInfo);
            this.mTransportInfo.setCurrentTransportState(this.mActiveTransportState);
        } else {
            Log.d(TAG, "getTransportInfo old socket set stop: " + this.mTransportInfo);
            this.mTransportInfo.setCurrentTransportState(TransportState.STOPPED);
        }
        return this.mTransportInfo;
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public MediaInfo getMediaInfo() {
        Log.d(TAG, "getMediaInfo()");
        if (this.mServer != null) {
            MediaPlaybackInfo playbackInfo = this.mServer.getMediaPlaybackInfo();
            this.mMediaInfo.setMediaDuration((long) playbackInfo.getDuration());
        }
        return this.mMediaInfo;
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public PositionInfo getPositionInfo() {
        if (this.mServer != null) {
            MediaPlaybackInfo playbackInfo = this.mServer.getMediaPlaybackInfo();
            this.mPositionInfo.setTrackDuration((long) playbackInfo.getDuration());
            this.mPositionInfo.setRelTime((long) playbackInfo.getPosition());
            this.mPositionInfo.setAbsTime((long) playbackInfo.getPosition());
        }
        Log.d(TAG, "getPositionInfo():" + this.mPositionInfo);
        return this.mPositionInfo;
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public TransportSettings getTransportSettings() {
        Log.d(TAG, "getTransportSettings()");
        if (this.mTransportSettings == null) {
            this.mTransportSettings = new TransportSettings();
        }
        return this.mTransportSettings;
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public void setAVTransportURI(String url, String metaData) throws AVTransportException {
        Log.d(TAG, "setAVTransportURI(): url = " + url + ", metaData = " + metaData);
        if (this.mServer != null) {
            this.mServer.onClientConnected();
            String ip = Utils.getIp(1);
            Utils.setIp(0, ip);
        }
        this.mMediaInfo.setCurrentURI(url);
        this.mMediaInfo.setCurrentURIMetaData(metaData);
        this.mPositionInfo.setTrack(1L);
        this.mPositionInfo.setTrackURI(url);
        this.mPositionInfo.setTrackMetaData(metaData);
        if (this.mServer != null) {
            String title = getTitleFromXml(metaData);
            Log.d(TAG, "setAVTransportURI(): title = " + title);
            this.mServer.onVideoPlay(url, title, -1, 0);
            return;
        }
        throw new AVTransportException(ErrorCode.ACTION_FAILED, ERROR_MSG_NO_SERVER);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public void setTransportState(TransportState state) {
        this.mActiveTransportState = state;
        this.mTransportInfo.setCurrentTransportState(state);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public void play(String speed) throws AVTransportException {
        Log.d(TAG, "play(): speed = " + speed);
        if (this.mServer != null) {
            this.mTransportInfo.setCurrentSpeed(speed);
            this.mServer.onVideoRate(1);
            return;
        }
        throw new AVTransportException(ErrorCode.ACTION_FAILED, ERROR_MSG_NO_SERVER);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public void pause() throws AVTransportException {
        Log.d(TAG, "pause()");
        if (this.mServer != null) {
            this.mServer.onVideoRate(0);
            return;
        }
        throw new AVTransportException(ErrorCode.ACTION_FAILED, ERROR_MSG_NO_SERVER);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public void stop() throws AVTransportException {
        Log.d(TAG, "stop()");
        if (this.mServer != null) {
            String ipRequest = Utils.getIp(1);
            String ipCurrent = Utils.getIp(0);
            Log.d(TAG, "stop request : " + ipRequest + ", Current : " + ipCurrent);
            if (ipCurrent == null) {
                this.mServer.onVideoStop();
                this.mServer.onClientDisconnected();
                return;
            } else if (ipRequest.equals(ipCurrent)) {
                this.mServer.onVideoStop();
                this.mServer.onClientDisconnected();
                return;
            } else {
                Log.d(TAG, "request is not match Current, not stop");
                return;
            }
        }
        throw new AVTransportException(ErrorCode.ACTION_FAILED, ERROR_MSG_NO_SERVER);
    }

    @Override // com.xpeng.airplay.dlna.IAVTransportControl
    public void seek(String seekMode, String position) throws AVTransportException {
        Log.d(TAG, "seek(): pos = " + position);
        if (this.mServer != null) {
            try {
                SeekMode.valueOrExceptionOf(seekMode);
                Long pos = Long.valueOf(ModelUtil.fromTimeString(position));
                this.mServer.onVideoScrub(pos.intValue());
                this.mPositionInfo.setAbsTime(pos.longValue());
                this.mPositionInfo.setRelTime(pos.longValue());
                Log.d(TAG, "seek(): seeked pos = " + pos);
                return;
            } catch (IllegalArgumentException e) {
                throw new AVTransportException(AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, ERROR_MSG_UNSUPPORTED_SEEK_MODE);
            }
        }
        throw new AVTransportException(ErrorCode.ACTION_FAILED, ERROR_MSG_NO_SERVER);
    }

    /* JADX WARN: Code restructure failed: missing block: B:26:0x0058, code lost:
        if (r1 == false) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x005b, code lost:
        return r0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private java.lang.String getTitleFromXml(java.lang.String r10) {
        /*
            r9 = this;
            java.lang.String r0 = ""
            r1 = 0
            r2 = 2
            r3 = 1
            java.lang.String r4 = ""
            org.xmlpull.v1.XmlPullParser r5 = org.seamless.xml.XmlPullParserUtils.createParser(r10)     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            int r6 = r5.getEventType()     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
        Lf:
            if (r6 == r3) goto L40
            java.lang.String r7 = r5.getName()     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            switch(r6) {
                case 3: goto L1f;
                case 4: goto L19;
                default: goto L18;
            }     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
        L18:
            goto L3a
        L19:
            java.lang.String r8 = r5.getText()     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            r4 = r8
            goto L3a
        L1f:
            java.lang.String r8 = "title"
            boolean r8 = r7.equals(r8)     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            if (r8 == 0) goto L29
            r0 = r4
            goto L3a
        L29:
            java.lang.String r8 = "class"
            boolean r8 = r7.equals(r8)     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            if (r8 == 0) goto L3a
            java.lang.String r8 = "object.item.audioItem"
            boolean r8 = r4.contains(r8)     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            if (r8 == 0) goto L3a
            r1 = 1
        L3a:
            int r8 = r5.next()     // Catch: java.lang.Throwable -> L4e java.lang.Throwable -> L50
            r6 = r8
            goto Lf
        L40:
            if (r1 == 0) goto L48
        L42:
            com.xpeng.airplay.dlna.DLNAServer r2 = r9.mServer
            r2.setMediaType(r3)
            goto L5b
        L48:
            com.xpeng.airplay.dlna.DLNAServer r3 = r9.mServer
            r3.setMediaType(r2)
            goto L5b
        L4e:
            r4 = move-exception
            goto L5c
        L50:
            r4 = move-exception
            java.lang.String r5 = "AVTransportController"
            java.lang.String r6 = "fail to create XML pull parser"
            android.util.Log.e(r5, r6)     // Catch: java.lang.Throwable -> L4e
            if (r1 == 0) goto L48
            goto L42
        L5b:
            return r0
        L5c:
            if (r1 == 0) goto L64
            com.xpeng.airplay.dlna.DLNAServer r2 = r9.mServer
            r2.setMediaType(r3)
            goto L69
        L64:
            com.xpeng.airplay.dlna.DLNAServer r3 = r9.mServer
            r3.setMediaType(r2)
        L69:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xpeng.airplay.dlna.AVTransportController.getTitleFromXml(java.lang.String):java.lang.String");
    }
}
