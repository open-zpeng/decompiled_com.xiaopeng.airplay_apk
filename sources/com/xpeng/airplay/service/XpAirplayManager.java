package com.xpeng.airplay.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.xpeng.airplay.service.IXpAirplayCallbacks;
import com.xpeng.airplay.service.IXpAirplayManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/* loaded from: classes.dex */
public class XpAirplayManager {
    public static final int DISCONNECT_REASON_NETWORK_ERROR = 1;
    public static final int DISCONNECT_REASON_UNKNOWN = 0;
    public static final int DISCONNECT_REASON_USER_REQUSET = 2;
    public static final int MEDIA_TYPE_AUDIO = 1;
    public static final int MEDIA_TYPE_NONE = 0;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int PLAYBACK_STATE_COMPLETE = 1;
    public static final int PLAYBACK_STATE_ERROR = 2;
    public static final int PLAYBACK_STATE_GONE = 5;
    public static final int PLAYBACK_STATE_PAUSE = 4;
    public static final int PLAYBACK_STATE_PLAYING = 0;
    public static final int PLAYBACK_STATE_RESUME = 6;
    public static final int PLAYBACK_STATE_STOP = 3;
    public static final int SERVER_TYPE_AIRPLAY = 2;
    public static final int SERVER_TYPE_DLNA = 3;
    public static final int SERVER_TYPE_MIRROR = 1;
    public static final int SERVER_TYPE_NONE = 0;
    private static final long SERVICE_BINDING_TIMEOUT = 10000;
    private static final String TAG = "XpAirplayManager";
    private static final String XP_AIRPLAY_SERVICE_CLASS_NAME = "com.xpeng.airplay.service.XpAirplayService";
    private static final String XP_AIRPLAY_SERVICE_PACKAGE_NAME = "com.xiaopeng.airplay";
    private IXpAirplayManager mAirplayService;
    private Context mContext;
    private Intent mIntent;
    private AirplayServiceConnection mServiceConnection;
    private Object mLock = new Object();
    private boolean mCanUnbindService = false;

    /* loaded from: classes.dex */
    public static abstract class DefaultAirplayCallbacks extends IXpAirplayCallbacks.Stub {
        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onAudioProgressUpdated(MediaPlaybackInfo mediaPlaybackInfo);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onClientConnected(int i);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onClientDisconnected(int i, int i2);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onMetaDataUpdated(MediaMetaData mediaMetaData);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onMirrorSizeChanged(int i, int i2);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onMirrorStarted();

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onMirrorStopped();

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onServerNameUpdated(String str);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onVideoPlayStarted(MediaPlayInfo mediaPlayInfo);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onVideoPlayStopped();

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onVideoRateChanged(int i);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onVideoScrubbed(int i);

        @Override // com.xpeng.airplay.service.IXpAirplayCallbacks
        public abstract void onVolumeChanged(float f);
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface DisconnectReason {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface MediaType {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface PlaybackState {
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface ServerType {
    }

    public static XpAirplayManager from(Context context) {
        return new XpAirplayManager(context);
    }

    private XpAirplayManager(Context context) {
        this.mContext = context;
        this.mIntent = new Intent(this.mContext.getPackageName());
        this.mIntent.setComponent(new ComponentName(XP_AIRPLAY_SERVICE_PACKAGE_NAME, XP_AIRPLAY_SERVICE_CLASS_NAME));
        this.mServiceConnection = new AirplayServiceConnection();
        warningOnMainThread();
    }

    public String[] getServerNames() {
        Log.d(TAG, "getServerNames()");
        if (this.mAirplayService != null) {
            try {
                return this.mAirplayService.getServerNames();
            } catch (RemoteException e) {
                Log.e(TAG, "fail to get server names");
                return null;
            }
        }
        Log.w(TAG, "AirplayService is not connected");
        return null;
    }

    public String getServerName(int screenId) {
        Log.d(TAG, "getServerName()");
        if (this.mAirplayService != null) {
            try {
                return this.mAirplayService.getServerName(screenId);
            } catch (RemoteException e) {
                Log.e(TAG, "fail to get server name for screen id " + screenId);
                return null;
            }
        }
        return null;
    }

    public int createSession(String serverName) {
        if (this.mAirplayService != null) {
            try {
                SessionParams sp = new SessionParams(this.mContext.getPackageName(), serverName);
                return this.mAirplayService.createSession(sp);
            } catch (RemoteException e) {
                Log.e(TAG, "fail to create session for " + serverName);
                return -1;
            }
        }
        Log.w(TAG, "AirplayService is not connected");
        return -1;
    }

    public int createSession(int screenId) {
        if (this.mAirplayService != null) {
            try {
                SessionParams sp = new SessionParams(this.mContext.getPackageName(), null, screenId);
                return this.mAirplayService.createSession(sp);
            } catch (RemoteException e) {
                Log.e(TAG, "fail to create session for " + screenId);
                return -1;
            }
        }
        Log.w(TAG, "AirplayService is not connected");
        return -1;
    }

    public IXpAirplaySession openSession(int sessionId) {
        if (this.mAirplayService != null) {
            try {
                return this.mAirplayService.openSession(sessionId);
            } catch (RemoteException e) {
                Log.e(TAG, "fail to open session for " + sessionId);
                return null;
            }
        }
        Log.w(TAG, "AirplayService is not connected");
        return null;
    }

    public void closeSession(int sessionId) {
        if (this.mAirplayService != null) {
            try {
                this.mAirplayService.closeSession(sessionId);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        Log.w(TAG, "AirplayService is not connected");
    }

    public long getTetheringDataUsage() {
        Log.d(TAG, "getTetherDataUsage()");
        if (this.mAirplayService != null) {
            try {
                return this.mAirplayService.getTetheringDataUsage();
            } catch (RemoteException e) {
                Log.e(TAG, "fail to get tether data usage");
                return 0L;
            }
        }
        Log.e(TAG, "AirplayService is not connected");
        return 0L;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        unbindServiceLocked();
    }

    public void bindAirplayService(int screenID, int uidOffset) {
        synchronized (this.mLock) {
            if (this.mAirplayService != null) {
                return;
            }
            bindServiceLocked(screenID, uidOffset);
        }
    }

    private void bindServiceLocked(int screenID, int offset) {
        Log.d(TAG, "bindServiceLocked()");
        if (this.mAirplayService != null) {
            return;
        }
        try {
            try {
                if (screenID == 0) {
                    this.mContext.bindService(this.mIntent, this.mServiceConnection, 1);
                } else {
                    this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, 1, UserHandle.of(offset));
                }
                long startMs = SystemClock.uptimeMillis();
                while (this.mAirplayService == null) {
                    long elapsedMs = SystemClock.uptimeMillis() - startMs;
                    long remainingMs = SERVICE_BINDING_TIMEOUT - elapsedMs;
                    if (remainingMs <= 0) {
                        this.mLock.wait(SERVICE_BINDING_TIMEOUT);
                    } else {
                        this.mLock.wait(remainingMs);
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "fail to bind service");
            }
        } finally {
            this.mCanUnbindService = true;
            this.mLock.notifyAll();
        }
    }

    private void unbindServiceLocked() {
        Log.d(TAG, "unBindServiceLocked()");
        if (this.mAirplayService == null) {
            return;
        }
        while (!this.mCanUnbindService) {
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
            }
        }
        this.mAirplayService = null;
        this.mContext.unbindService(this.mServiceConnection);
    }

    private void warningOnMainThread() {
        if (Thread.currentThread() == this.mContext.getMainLooper().getThread()) {
            Log.w(TAG, "Invoke on main thread, it may be blocked");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class AirplayServiceConnection implements ServiceConnection {
        private AirplayServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(XpAirplayManager.TAG, "onServiceConnected(): " + className);
            synchronized (XpAirplayManager.this.mLock) {
                XpAirplayManager.this.mAirplayService = IXpAirplayManager.Stub.asInterface(service);
                XpAirplayManager.this.mLock.notifyAll();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            Log.d(XpAirplayManager.TAG, "onServiceDisconnected(): " + className);
            synchronized (XpAirplayManager.this.mLock) {
                if (XpAirplayManager.this.mAirplayService != null) {
                    XpAirplayManager.this.mAirplayService = null;
                    XpAirplayManager.this.mLock.notifyAll();
                }
            }
        }
    }
}
