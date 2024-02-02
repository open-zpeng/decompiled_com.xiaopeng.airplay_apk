package com.xpeng.airplay.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.animation.ScaleAnimation;
import com.xpeng.airplay.model.NALPacket;
import com.xpeng.airplay.model.PCMPacket;
import com.xpeng.airplay.player.AudioPlayer;
import com.xpeng.airplay.player.VideoPlayer;
import java.util.concurrent.atomic.AtomicBoolean;
/* loaded from: classes.dex */
public class RaopServer extends AbstractServer implements AudioPlayer.AudioFocusChangedListener {
    private static final String TAG = "RaopServer";
    private AudioPlayer mAudioPlayer;
    private FrameCallbackHandler mCallbackHandler;
    private Context mContext;
    private NsdHelper mNsdHelper;
    private NsdManager.RegistrationListener mRegisterListener;
    private VideoPlayer mVideoPlayer;
    private long mServerId = 0;
    private boolean mIsUseNativeDecoder = isUseNativeDecoder();
    private AtomicBoolean mIsRearBtOn = new AtomicBoolean(false);
    private boolean mAudioStarted = false;

    private static native void classInitNative();

    private native void destroyConnection(long j);

    private native int getPort(long j);

    private native boolean isUseNativeDecoder();

    private native void setNativeSurface(long j, Surface surface);

    private native long start();

    private native void stop(long j);

    static {
        System.loadLibrary("raop_server");
    }

    /* loaded from: classes.dex */
    private final class FrameCallbackHandler extends Handler implements VideoPlayer.FrameCallback {
        public static final int EVENT_POST_RENDER = 2;
        public static final int EVENT_PRE_RENDER = 1;
        public final String TAG;

        public FrameCallbackHandler(Looper looper) {
            super(looper);
            this.TAG = FrameCallbackHandler.class.getSimpleName();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    handlePreRenderEvent();
                    return;
                case 2:
                    handlePostRenderEvent();
                    return;
                default:
                    return;
            }
        }

        @Override // com.xpeng.airplay.player.VideoPlayer.FrameCallback
        public void onPreRender(long pts) {
            Message msg = obtainMessage(1, Long.valueOf(pts));
            msg.sendToTarget();
        }

        @Override // com.xpeng.airplay.player.VideoPlayer.FrameCallback
        public void postRender() {
            sendEmptyMessage(2);
        }

        private void handlePostRenderEvent() {
            Log.d(this.TAG, "handlePostRenderEvent()");
            ScaleAnimation scaleIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 0.5f, 1, 0.5f);
            scaleIn.setDuration(300L);
        }

        private void handlePreRenderEvent() {
            Log.d(this.TAG, "handlePreRenderEvent()");
        }
    }

    public RaopServer(Context context) {
        this.mContext = context;
        this.mNsdHelper = NsdHelper.getInstance(context);
    }

    @Override // com.xpeng.airplay.player.AudioPlayer.AudioFocusChangedListener
    public void onAudioFocusChanged(int requestFocus) {
        Log.i(TAG, "onAudioFocusChanged(): " + requestFocus);
        if (requestFocus == -1) {
            if (this.mStateListener != null) {
                this.mStateListener.onAudioFocusChanged(true);
            }
        } else if (this.mAudioPlayer != null) {
            if (!isRearServer() || !this.mIsRearBtOn.get()) {
                if (requestFocus == 1) {
                    this.mAudioPlayer.requestAudioFocus();
                } else if (requestFocus == 0) {
                    this.mAudioPlayer.abandonAudioFocus();
                }
            } else if (this.mIsRearBtOn.get()) {
                this.mAudioPlayer.updateRearBtState(true);
                this.mAudioPlayer.abandonAudioFocus();
            }
        }
    }

    public void onRecvVideoData(byte[] nal, int nalType, long dts, long pts) {
        Log.d(TAG, "onRecvVideoData pts = " + pts + ", nalType = " + nalType + ", nal length = " + nal.length);
        if (this.mVideoPlayer != null) {
            NALPacket nalPacket = new NALPacket();
            nalPacket.nalData = nal;
            nalPacket.nalType = nalType;
            nalPacket.pts = pts;
            this.mVideoPlayer.addPacket(nalPacket);
        }
    }

    public void onRecvAudioData(short[] pcm, long pts) {
        Log.d(TAG, "onRecvAudioData pcm length = " + pcm.length + ", pts = " + pts);
        if (this.mAudioPlayer != null) {
            PCMPacket pcmPacket = new PCMPacket();
            pcmPacket.data = pcm;
            pcmPacket.pts = pts;
            this.mAudioPlayer.addPacket(pcmPacket);
        }
    }

    public void onAudioPlayStarted() {
        Log.i(TAG, "onAudioPlayStarted()");
        if (!this.mAudioStarted) {
            this.mAudioStarted = true;
            MediaPlayInfo playInfo = new MediaPlayInfo();
            playInfo.setStreamType(1);
            playInfo.setType(1);
            if (this.mStateListener != null) {
                this.mStateListener.onVideoPlay(playInfo);
            }
        }
    }

    public void onMetadataUpdated(MediaMetaData metadata) {
        Log.d(TAG, "onMetadataUpdated(): " + metadata);
        if (this.mStateListener != null) {
            this.mStateListener.onMetadataUpdated(metadata);
        }
    }

    public void onAudioProgressUpdated(MediaPlaybackInfo playbackInfo) {
        Log.d(TAG, "onAudioProgressUpdated(): " + playbackInfo);
        if (this.mStateListener != null) {
            this.mStateListener.onAudioProgressUpdated(playbackInfo);
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void setMirrorSurface(Surface surface) {
        Log.i(TAG, "setSurface()");
        if (this.mIsUseNativeDecoder) {
            if (this.mServerId != 0) {
                setNativeSurface(this.mServerId, surface);
            }
        } else if (this.mVideoPlayer == null) {
            this.mVideoPlayer = new VideoPlayer(surface);
            this.mVideoPlayer.setFrameCallback(this.mCallbackHandler);
            this.mVideoPlayer.startPlay();
        }
    }

    public void onVolumeChanged(float volume) {
        Log.d(TAG, "onVolumeChanged(): volume = " + volume);
        if (this.mStateListener != null) {
            this.mStateListener.onVolumeChanged(this.mConfig.getType(), volume);
        }
        if (this.mAudioPlayer != null) {
            this.mAudioPlayer.setAudioVolume(volume);
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void startServer() {
        if (this.mRegisterListener == null) {
            this.mRegisterListener = new SdServiceRegisterListener();
        }
        if (this.mServerId == 0) {
            this.mServerId = start();
            registerRaop(getPort());
        } else {
            Log.w(TAG, "RAOP server is already registered");
        }
        Log.d(TAG, "startServer()");
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void stopServer(boolean shutdown) {
        if (this.mServerId != 0) {
            stop(this.mServerId);
        }
        this.mServerId = 0L;
        if (this.mVideoPlayer != null) {
            this.mVideoPlayer.stopPlay();
        }
        if (this.mRegisterListener != null) {
            this.mNsdHelper.unregisterService(this.mRegisterListener);
        }
        Log.d(TAG, "stopServer()");
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void destroyConnection() {
        Log.i(TAG, "destroyConnection()");
        if (this.mServerId != 0) {
            destroyConnection(this.mServerId);
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public boolean isAudioStream() {
        return this.mAudioStarted;
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public boolean isServerActive() {
        return this.mServerId != 0;
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public int getServerPort() {
        return getPort();
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void setVideoPlaybackState(int state) {
        Log.d(TAG, "setVideoPlaybackState()");
        if (state == 5) {
            if (this.mAudioPlayer != null) {
                this.mAudioPlayer.abandonAudioFocus();
            }
        } else if (state == 6 && this.mAudioPlayer != null) {
            this.mAudioPlayer.requestAudioFocus();
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void updateRearBtState(boolean on) {
        Log.d(TAG, "updateRearBtState()");
        this.mIsRearBtOn.set(on);
        if (this.mAudioPlayer != null && isRearServer()) {
            this.mAudioPlayer.updateRearBtState(on);
            this.mAudioPlayer.abandonAudioFocus();
        }
    }

    public void onClientConnected() {
        Log.i(TAG, "onClientConnected(): screen id = " + this.mConfig.getScreenId());
        if (this.mStateListener != null) {
            this.mStateListener.onClientConnected(this.mConfig.getType(), this.mConfig.getScreenId());
        }
        if (this.mAudioPlayer == null) {
            this.mAudioPlayer = new AudioPlayer(this.mContext);
            this.mAudioPlayer.setAudioFocusChangedListener(this);
            this.mAudioPlayer.startPlay();
        }
    }

    public void onClientDisconnected() {
        Log.i(TAG, "onClientDisconnect(): screen id = " + this.mConfig.getScreenId());
        this.mAudioStarted = false;
        if (this.mStateListener != null) {
            this.mStateListener.onClientDisconnected(this.mConfig.getType(), 2);
        }
        if (this.mVideoPlayer != null) {
            this.mVideoPlayer.stopPlay();
            this.mVideoPlayer = null;
        }
        if (this.mAudioPlayer != null) {
            this.mAudioPlayer.stopPlay();
            this.mAudioPlayer.unsetAudioFocusChangedListener();
            this.mAudioPlayer = null;
        }
    }

    public void onScreenMirrorStarted() {
        Log.d(TAG, "onScreenMirrorStarted()");
        if (this.mStateListener != null) {
            this.mStateListener.onScreenMirrorStarted();
        }
    }

    public void onMirrorSizeChanged(int width, int height) {
        Log.d(TAG, "onMirrorSizeChanged()");
        if (this.mStateListener != null) {
            this.mStateListener.onMirrorSizeChanged(width, height);
        }
    }

    private int getPort() {
        if (this.mServerId != 0) {
            return getPort(this.mServerId);
        }
        return 0;
    }

    private void registerRaop(int port) {
        Log.d(TAG, "registerRaop(): port = " + port);
        NsdServiceInfo nsi = new NsdServiceInfo(this.mConfig.getMacAddr().replace(":", "") + "@" + this.mConfig.getName(), NsdConstants.RAOP_SERVER_TYPE);
        nsi.setPort(port);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_CH, "2");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_CN, NsdConstants.AIRPLAY_TXT_VALUE_CN);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_DA, NsdConstants.AIRPLAY_TXT_VALUE_DA);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_ET, NsdConstants.AIRPLAY_TXT_VALUE_ET);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_VV, "2");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_FT, NsdConstants.AIRPLAY_TXT_VALUE_FEATURES);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_AM, NsdConstants.AIRPLAY_TXT_VALUE_MODEL);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_RHD, NsdConstants.AIRPLAY_TXT_VALUE_RHD);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_PW, "false");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_SR, NsdConstants.AIRPLAY_TXT_VALUE_SR);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_SS, NsdConstants.AIRPLAY_TXT_VALUE_SS);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_SV, "false");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_TP, NsdConstants.AIRPLAY_TXT_VALUE_TP);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_TXTVERS, NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_SF, "0x4");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_VS, "220.68");
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_VN, NsdConstants.AIRPLAY_TXT_VALUE_VN);
        nsi.setAttribute(NsdConstants.AIRPLAY_TXT_KEY_PK, NsdConstants.AIRPLAY_TXT_VALUE_PK);
        this.mNsdHelper.registerService(nsi, this.mRegisterListener);
    }
}
