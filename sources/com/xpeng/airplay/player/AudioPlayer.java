package com.xpeng.airplay.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import com.xpeng.airplay.model.PCMPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/* loaded from: classes.dex */
public class AudioPlayer extends Thread implements IPlayer<PCMPacket>, AudioManager.OnAudioFocusChangeListener {
    private static final int DEFAULT_AUDIO_CHANNEL = 12;
    private static final int DEFAULT_AUDIO_FORMAT = 2;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final String TAG = "AudioPlayer";
    private final AudioManager mAudioMgr;
    private final AudioFocusRequest mFocusReq;
    private AudioFocusChangedListener mListener;
    private AudioTrack mTrack;
    private boolean mNeedStop = false;
    private AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private AtomicBoolean mCanPlayback = new AtomicBoolean(false);
    private AtomicBoolean mRearBtOn = new AtomicBoolean(false);
    private ArrayBlockingQueue<PCMPacket> mAudioBuf = new ArrayBlockingQueue<>(5);

    /* loaded from: classes.dex */
    public interface AudioFocusChangedListener {
        void onAudioFocusChanged(int i);
    }

    public AudioPlayer(Context context) {
        this.mAudioMgr = (AudioManager) context.getSystemService("audio");
        AudioAttributes aa = new AudioAttributes.Builder().setLegacyStreamType(3).setContentType(2).build();
        AudioFormat af = new AudioFormat.Builder().setSampleRate(DEFAULT_SAMPLE_RATE).setEncoding(2).setChannelMask(12).build();
        this.mTrack = new AudioTrack.Builder().setTransferMode(1).setAudioAttributes(aa).setAudioFormat(af).setBufferSizeInBytes(AudioTrack.getMinBufferSize(DEFAULT_SAMPLE_RATE, 12, 2)).build();
        this.mFocusReq = new AudioFocusRequest.Builder(1).setAudioAttributes(aa).setAcceptsDelayedFocusGain(true).setWillPauseWhenDucked(false).setOnAudioFocusChangeListener(this).build();
        this.mTrack.play();
    }

    public void setAudioFocusChangedListener(AudioFocusChangedListener listener) {
        this.mListener = listener;
    }

    public void unsetAudioFocusChangedListener() {
        this.mListener = null;
    }

    @Override // com.xpeng.airplay.player.IPlayer
    public void addPacket(PCMPacket packet) {
        try {
            this.mAudioBuf.offer(packet, 10L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e(TAG, "fail to offer packet");
            e.printStackTrace();
        }
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        while (!this.mNeedStop) {
            PCMPacket packet = null;
            try {
                packet = this.mAudioBuf.poll(30L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "polling is interrupted:" + e.getCause());
                e.printStackTrace();
            }
            if (packet != null && (this.mCanPlayback.get() || this.mRearBtOn.get())) {
                doPlay(packet);
            }
        }
    }

    @Override // android.media.AudioManager.OnAudioFocusChangeListener
    public void onAudioFocusChange(int focusChange) {
        Log.i(TAG, "onAudioFocusChange: " + focusChange);
        if (focusChange == 1) {
            this.mCanPlayback.set(true);
            return;
        }
        switch (focusChange) {
            case -3:
            case -2:
                this.mCanPlayback.set(false);
                return;
            case -1:
                this.mCanPlayback.set(false);
                if (this.mListener != null) {
                    this.mListener.onAudioFocusChanged(-1);
                    return;
                }
                return;
            default:
                return;
        }
    }

    @Override // com.xpeng.airplay.player.IPlayer
    public void startPlay() {
        Log.d(TAG, "startPlay()");
        start();
        this.mIsRunning.set(true);
    }

    @Override // com.xpeng.airplay.player.IPlayer
    public void stopPlay() {
        Log.d(TAG, "stopPlay()");
        abandonAudioFocus();
        if (this.mIsRunning.get()) {
            this.mIsRunning.set(false);
            interrupt();
            this.mNeedStop = true;
            if (this.mTrack != null) {
                try {
                    try {
                        this.mTrack.flush();
                        this.mTrack.stop();
                        this.mTrack.release();
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "fail to stop audio tracker");
                    }
                } finally {
                    this.mTrack = null;
                }
            }
            if (this.mAudioBuf != null) {
                this.mAudioBuf.clear();
            }
        }
    }

    public void updateRearBtState(boolean on) {
        this.mRearBtOn.set(on);
    }

    public void requestAudioFocus() {
        if (this.mAudioMgr != null) {
            int res = this.mAudioMgr.requestAudioFocus(this.mFocusReq);
            Log.i(TAG, "requestAudioFocus(): res " + res);
            switch (res) {
                case 0:
                    Log.w(TAG, "requestAudioFocus(): failed");
                    return;
                case 1:
                    this.mCanPlayback.set(true);
                    return;
                case 2:
                default:
                    return;
            }
        }
    }

    public void abandonAudioFocus() {
        Log.i(TAG, "abandonAudioFocus()");
        this.mCanPlayback.set(false);
        if (this.mAudioMgr != null) {
            int res = this.mAudioMgr.abandonAudioFocusRequest(this.mFocusReq);
            if (res == 0) {
                Log.w(TAG, "failed to abandon audio focus");
            }
        }
    }

    public void setAudioVolume(float volume) {
        Log.d(TAG, "setAudioVolume()");
        if (this.mTrack != null) {
            this.mTrack.setVolume(volume);
        }
    }

    private void doPlay(PCMPacket pcmPacket) {
        if (this.mTrack != null) {
            this.mTrack.write(pcmPacket.data, 0, pcmPacket.data.length);
        }
    }
}
