package com.xpeng.airplay.service;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import com.xpeng.airplay.service.IXpAirplaySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* loaded from: classes.dex */
public final class XpAirplaySession extends IXpAirplaySession.Stub implements ISessionInterface {
    private static final long CLIENT_CONNECTION_TIMEOUT = 33000;
    private static final String TAG = "XpAirplaySession";
    private final CallbackHandler mCallbackHandler;
    private final List<IXpAirplayCallbacks> mCallbacks = Collections.synchronizedList(new ArrayList());
    private RemoteConnection mRemoteConnection;
    private final IServerProxy mServerProxy;
    private final Handler mServiceHandler;
    private final SessionParams mSessionParams;

    public XpAirplaySession(SessionParams params, IServerProxy serverProxy, Handler handler) {
        this.mSessionParams = params;
        this.mServerProxy = serverProxy;
        this.mCallbackHandler = new CallbackHandler(handler.getLooper());
        this.mServiceHandler = handler;
    }

    @Override // com.xpeng.airplay.service.ISessionInterface
    public void start() {
        Log.d(TAG, "start()");
    }

    @Override // com.xpeng.airplay.service.ISessionInterface
    public void stop() {
        Log.d(TAG, "stop()");
        if (this.mRemoteConnection != null) {
            this.mRemoteConnection.unlinkToDeath();
        }
        this.mRemoteConnection = null;
        if (this.mServerProxy != null) {
            this.mServerProxy.removeCallbackHandler();
        }
        this.mCallbacks.clear();
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public String getServerName() {
        ServerConfig sc = this.mServerProxy.getServerConfig(2);
        if (sc != null) {
            return sc.getName();
        }
        return null;
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public boolean hasActiveConnection() {
        return this.mServerProxy != null && this.mServerProxy.hasActiveConnection();
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public void registerAirplayCallbacks(IXpAirplayCallbacks callback) {
        Log.i(TAG, "registerAirplayCallbacks()");
        if (!this.mCallbacks.contains(callback)) {
            this.mCallbacks.add(callback);
            if (this.mServerProxy != null) {
                this.mServerProxy.addCallbackHandler(this.mCallbackHandler);
            }
            this.mRemoteConnection = new RemoteConnection(callback);
        }
        Log.d(TAG, "airplaycallback size = " + this.mCallbacks.size());
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public void unregisterAirplayCallbacks(IXpAirplayCallbacks callback) {
        Log.i(TAG, "unregisterAirplayCallback()");
        if (this.mRemoteConnection != null) {
            this.mRemoteConnection.unlinkToDeath();
        }
        this.mCallbacks.remove(callback);
        if (this.mServerProxy != null) {
            this.mServerProxy.removeCallbackHandler();
        }
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public void setMirrorSurface(Surface surface) {
        Log.d(TAG, "setMirrorSurface()");
        if (this.mServerProxy != null) {
            this.mServerProxy.setMirrorSurface(surface);
        }
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public void setVideoPlaybackState(int state) {
        Log.i(TAG, "setVideoPlaybackState(): state = " + state);
        if (this.mServerProxy != null) {
            this.mServerProxy.setVideoPlaybackState(state);
        }
        this.mCallbackHandler.removeMessages(10);
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    public void setMediaPlaybackInfo(MediaPlaybackInfo info) {
        Log.d(TAG, "setMediaPlaybackInfo(): " + info);
        if (this.mServerProxy != null) {
            this.mServerProxy.setMediaPlaybackInfo(info);
        }
    }

    @Override // com.xpeng.airplay.service.IXpAirplaySession
    @Deprecated
    public void saveSystemVolume(int volume) {
        Log.i(TAG, "saveSystemVolume(): " + volume);
        if (this.mServerProxy != null) {
            this.mServerProxy.saveSystemVolume(volume);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class RemoteConnection implements IBinder.DeathRecipient {
        private final IXpAirplayCallbacks mCallback;

        RemoteConnection(IXpAirplayCallbacks cb) {
            this.mCallback = cb;
            linkToDeath();
        }

        void linkToDeath() {
            Log.d(XpAirplaySession.TAG, "linkToDeath()");
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        void unlinkToDeath() {
            Log.d(XpAirplaySession.TAG, "unlinkToDeath()");
            this.mCallback.asBinder().unlinkToDeath(this, 0);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.w(XpAirplaySession.TAG, "binderDied()");
            Message msg = XpAirplaySession.this.mServiceHandler.obtainMessage(9, XpAirplaySession.this.mSessionParams.getPackageName());
            msg.sendToTarget();
            if (XpAirplaySession.this.mServerProxy != null) {
                XpAirplaySession.this.mServerProxy.onClientDied();
            }
        }
    }

    /* loaded from: classes.dex */
    public final class CallbackHandler extends Handler {
        public static final int EVENT_AUDIO_PROGRESS_UPDATED = 15;
        public static final int EVENT_CLIENT_CONNECTED = 1;
        public static final int EVENT_CLIENT_DISCONNECTED = 2;
        public static final int EVENT_CONNECTION_TIMEOUT = 10;
        public static final int EVENT_METADATA_UPDATED = 13;
        public static final int EVENT_MIRROR_SIZE_CHANGED = 4;
        public static final int EVENT_MIRROR_STARTED = 3;
        public static final int EVENT_MIRROR_STOPPED = 12;
        public static final int EVENT_RESTORE_TIMEOUT = 11;
        public static final int EVENT_SERVER_NAME_UPDATED = 14;
        public static final int EVENT_VIDEO_PLAY_STARTED = 5;
        public static final int EVENT_VIDEO_PLAY_STOPPED = 6;
        public static final int EVENT_VIDEO_RATE_CHANGED = 7;
        public static final int EVENT_VIDEO_SCRUBBED = 8;
        public static final int EVENT_VOLUME_CHANGED = 9;

        public CallbackHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Log.d(XpAirplaySession.TAG, "handleMessage: what = " + msg.what);
            switch (msg.what) {
                case 1:
                    for (IXpAirplayCallbacks cb : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb.onClientConnected(((Integer) msg.obj).intValue());
                        } catch (RemoteException e) {
                            Log.e(XpAirplaySession.TAG, "fail to call onClientConnected");
                        }
                    }
                    sendEmptyMessageDelayed(10, XpAirplaySession.CLIENT_CONNECTION_TIMEOUT);
                    return;
                case 2:
                    for (IXpAirplayCallbacks cb2 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb2.onClientDisconnected(msg.arg1, msg.arg2);
                        } catch (RemoteException e2) {
                            Log.e(XpAirplaySession.TAG, "fail to call onClientDisconnected");
                        }
                    }
                    removeMessages(10);
                    return;
                case 3:
                    for (IXpAirplayCallbacks cb3 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb3.onMirrorStarted();
                        } catch (RemoteException e3) {
                            Log.e(XpAirplaySession.TAG, "fail to call onMirrorStarted");
                        }
                    }
                    removeMessages(10);
                    return;
                case 4:
                    Size sz = (Size) msg.obj;
                    for (IXpAirplayCallbacks cb4 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb4.onMirrorSizeChanged(sz.getWidth(), sz.getHeight());
                        } catch (RemoteException e4) {
                            Log.e(XpAirplaySession.TAG, "fail to call onMirrorSizeChanged");
                        }
                    }
                    return;
                case 5:
                    MediaPlayInfo info = (MediaPlayInfo) msg.obj;
                    for (IXpAirplayCallbacks cb5 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb5.onVideoPlayStarted(info);
                        } catch (RemoteException e5) {
                            Log.e(XpAirplaySession.TAG, "fail to call onVideoPlayStarted");
                        }
                    }
                    removeMessages(10);
                    return;
                case 6:
                    for (IXpAirplayCallbacks cb6 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb6.onVideoPlayStopped();
                        } catch (RemoteException e6) {
                            Log.e(XpAirplaySession.TAG, "fail to call onVideoPlayStopped");
                        }
                    }
                    return;
                case 7:
                    int rate = ((Integer) msg.obj).intValue();
                    for (IXpAirplayCallbacks cb7 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb7.onVideoRateChanged(rate);
                        } catch (RemoteException e7) {
                            Log.e(XpAirplaySession.TAG, "fail to call onVideoRateChanged");
                        }
                    }
                    return;
                case 8:
                    int pos = ((Integer) msg.obj).intValue();
                    for (IXpAirplayCallbacks cb8 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb8.onVideoScrubbed(pos);
                        } catch (RemoteException e8) {
                            Log.e(XpAirplaySession.TAG, "fail to call onVideoScrubbed");
                        }
                    }
                    return;
                case 9:
                    float vol = ((Float) msg.obj).floatValue();
                    for (IXpAirplayCallbacks cb9 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb9.onVolumeChanged(vol);
                        } catch (RemoteException e9) {
                            Log.e(XpAirplaySession.TAG, "fail to call onVideoScrubbed");
                        }
                    }
                    return;
                case 10:
                    Log.d(XpAirplaySession.TAG, "connection timeout, disconnect it!");
                    if (XpAirplaySession.this.mServerProxy != null) {
                        XpAirplaySession.this.mServerProxy.destroyConnection();
                        return;
                    }
                    return;
                case 11:
                    if (XpAirplaySession.this.mServerProxy != null) {
                        XpAirplaySession.this.mServerProxy.updateAirplayState(4);
                        return;
                    }
                    return;
                case 12:
                    for (IXpAirplayCallbacks cb10 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb10.onMirrorStopped();
                        } catch (RemoteException e10) {
                            Log.e(XpAirplaySession.TAG, "fail to call onVideoPlayStopped");
                        }
                    }
                    return;
                case 13:
                    MediaMetaData metaData = (MediaMetaData) msg.obj;
                    for (IXpAirplayCallbacks cb11 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb11.onMetaDataUpdated(metaData);
                        } catch (RemoteException e11) {
                            Log.e(XpAirplaySession.TAG, "fail to update meta data");
                        }
                    }
                    return;
                case 14:
                    for (IXpAirplayCallbacks cb12 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb12.onServerNameUpdated((String) msg.obj);
                        } catch (RemoteException e12) {
                            Log.e(XpAirplaySession.TAG, "fail to update meta data");
                        }
                    }
                    return;
                case 15:
                    MediaPlaybackInfo playbackInfo = (MediaPlaybackInfo) msg.obj;
                    for (IXpAirplayCallbacks cb13 : XpAirplaySession.this.mCallbacks) {
                        try {
                            cb13.onAudioProgressUpdated(playbackInfo);
                        } catch (RemoteException e13) {
                            Log.e(XpAirplaySession.TAG, "fail to update audio progress");
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
