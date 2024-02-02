package com.xpeng.airplay.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.text.TextUtils;
import android.util.Log;
/* loaded from: classes.dex */
public class AirplayServer extends AbstractServer implements IAirplayServer {
    public static String TAG = "AirplayServer";
    private NsdHelper mNsdHelper;
    private NsdManager.RegistrationListener mRegisterListener;
    private long mServerId = 0;
    private MediaPlaybackInfo mPlaybackInfo = new MediaPlaybackInfo();

    private native void destroyConnection(long j);

    private native int getPort(long j);

    private native long start(String str);

    private native void stop(long j);

    static {
        System.loadLibrary("airplay_server");
    }

    public AirplayServer(Context context) {
        this.mNsdHelper = NsdHelper.getInstance(context);
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onVideoPlay(String url, String title, int volume, int pos) {
        if (!TextUtils.isEmpty(url)) {
            this.mPlaybackInfo.reset();
            String str = TAG;
            Log.d(str, "videoPlay: url " + url + ", volume = " + volume + ", position = " + pos);
            if (this.mStateListener != null) {
                MediaPlayInfo playInfo = new MediaPlayInfo(url, title, -1.0f, pos);
                playInfo.setType(this.mConfig.getType());
                playInfo.setStreamType(2);
                this.mStateListener.onVideoPlay(playInfo);
            }
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onVideoStop() {
        Log.d(TAG, "onVideoStop()");
        if (this.mStateListener != null) {
            this.mStateListener.onVideoStopped(this.mConfig.getType());
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onVideoScrub(int pos) {
        String str = TAG;
        Log.d(str, "onVideoScrub(): pos = " + pos);
        this.mPlaybackInfo.setPosition((double) pos);
        if (this.mStateListener != null) {
            this.mStateListener.onVideoScrubbed(this.mConfig.getType(), pos);
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onVideoRate(int rate) {
        String str = TAG;
        Log.d(str, "onVideoRate(): rate = " + rate);
        if (this.mStateListener != null) {
            this.mStateListener.onVideoRateChanged(this.mConfig.getType(), rate);
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public MediaPlaybackInfo getMediaPlaybackInfo() {
        MediaPlaybackInfo mediaPlaybackInfo;
        synchronized (this.mPlaybackInfo) {
            mediaPlaybackInfo = this.mPlaybackInfo;
        }
        return mediaPlaybackInfo;
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void setMediaPlaybackInfo(MediaPlaybackInfo info) {
        Log.d(TAG, "setMediaPlaybackInfo()");
        synchronized (this.mPlaybackInfo) {
            this.mPlaybackInfo.copy(info);
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void setVideoPlaybackState(int state) {
        Log.d(TAG, "setVideoPlaybackState()");
        synchronized (this.mPlaybackInfo) {
            try {
                if (state == 1 || state == 2 || state == 3) {
                    if (state == 1) {
                        this.mPlaybackInfo.setRate(2);
                    } else if (state == 2) {
                        this.mPlaybackInfo.reset();
                        this.mPlaybackInfo.setRate(3);
                    } else {
                        this.mPlaybackInfo.reset();
                    }
                } else if (state == 0) {
                    this.mPlaybackInfo.setRate(1);
                } else {
                    this.mPlaybackInfo.setRate(0);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void startServer() {
        if (this.mRegisterListener == null) {
            this.mRegisterListener = new SdServiceRegisterListener();
        }
        if (this.mServerId == 0) {
            this.mServerId = start(this.mConfig.getMacAddr());
            registerAirplay(getPort());
            return;
        }
        Log.w(TAG, "Airplay Server is already registered");
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void stopServer(boolean shutdown) {
        if (this.mServerId != 0) {
            stop(this.mServerId);
            this.mServerId = 0L;
        }
        if (this.mRegisterListener != null) {
            try {
                this.mNsdHelper.unregisterService(this.mRegisterListener);
            } catch (IllegalArgumentException e) {
            }
        }
        this.mPlaybackInfo.reset();
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onClientConnected() {
        String str = TAG;
        Log.i(str, "onClientConnected(): screen id = " + this.mConfig.getScreenId());
        if (this.mStateListener != null) {
            this.mStateListener.onClientConnected(this.mConfig.getType(), this.mConfig.getScreenId());
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onClientDisconnected() {
        String str = TAG;
        Log.i(str, "onClientDisconnect(): screen id = " + this.mConfig.getScreenId());
        if (this.mStateListener != null) {
            this.mStateListener.onClientDisconnected(this.mConfig.getType(), 2);
        }
        this.mPlaybackInfo.reset();
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void destroyConnection() {
        Log.d(TAG, "destroyConnection()");
        if (this.mServerId != 0) {
            destroyConnection(this.mServerId);
        }
        this.mPlaybackInfo.reset();
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public boolean isServerActive() {
        return this.mServerId != 0;
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public int getServerPort() {
        return getPort();
    }

    private int getPort() {
        if (this.mServerId != 0) {
            return getPort(this.mServerId);
        }
        return 0;
    }

    private void registerAirplay(int port) {
        String str = TAG;
        Log.d(str, "registerAirplay(): port = " + port);
        NsdServiceInfo nsi = new NsdServiceInfo(this.mConfig.getName(), NsdConstants.AIRPLAY_SERVER_TYPE);
        nsi.setPort(port);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_ID, this.mConfig.getMacAddr());
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_FEATURES, NsdConstants.AIRPLAY_TXT_VALUE_FEATURES);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_SRCVERS, "220.68");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_FLAGS, "0x4");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_VV, "2");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_MODEL, NsdConstants.AIRPLAY_TXT_VALUE_MODEL);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_PW, "false");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_RHD, NsdConstants.AIRPLAY_TXT_VALUE_RHD);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_PK, NsdConstants.AIRPLAY_TXT_VALUE_PK);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_PI, NsdConstants.AIRPLAY_TXT_VALUE_PI);
        this.mNsdHelper.registerService(nsi, this.mRegisterListener);
    }
}
