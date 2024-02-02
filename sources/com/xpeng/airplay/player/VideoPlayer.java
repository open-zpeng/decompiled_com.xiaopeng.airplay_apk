package com.xpeng.airplay.player;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import com.xpeng.airplay.model.NALPacket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/* loaded from: classes.dex */
public class VideoPlayer extends Thread implements IPlayer<NALPacket> {
    private static final String DEFAULT_MIME_TYPE = "video/avc";
    private static final String TAG = "VideoPlayer";
    private static final boolean VERBOSE_LOG = true;
    private Surface mSurface;
    private int mVideoWidth = 540;
    private int mVideoHeight = 1080;
    private MediaCodec mDecoder = null;
    private boolean mNeedStop = false;
    private boolean mIsRunning = false;
    private long mFirstInputTimeNs = -1;
    private final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private ArrayBlockingQueue<NALPacket> mVideoBuf = new ArrayBlockingQueue<>(10);
    private FrameCallback mCallback = null;
    private AtomicBoolean mIsVideoSizeChanged = new AtomicBoolean(false);

    /* loaded from: classes.dex */
    public interface FrameCallback {
        void onPreRender(long j);

        void postRender();
    }

    public VideoPlayer(Surface surface) {
        this.mSurface = null;
        this.mSurface = surface;
    }

    public void setFrameCallback(FrameCallback callback) {
        this.mCallback = callback;
    }

    public void setSurface(Surface surface) {
        if (this.mSurface != surface) {
            this.mSurface = surface;
            if (this.mDecoder != null) {
                this.mDecoder.setOutputSurface(this.mSurface);
            }
            Log.d(TAG, "setSurface(): surface has changed");
        }
    }

    public void setVideoSize(int width, int height) {
        Log.d(TAG, "setVideoSize(): width = " + width + ", height = " + height);
        if (this.mVideoWidth != width || this.mVideoHeight != height) {
            this.mIsVideoSizeChanged.set(true);
            this.mVideoWidth = width;
            this.mVideoHeight = height;
            try {
                if (this.mDecoder != null) {
                    this.mDecoder.flush();
                }
            } catch (Exception e) {
                Log.e(TAG, "fail to flush decoder");
                e.printStackTrace();
            }
            initDecoder();
        }
    }

    public void initDecoder() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(DEFAULT_MIME_TYPE, this.mVideoWidth, this.mVideoHeight);
            if (this.mDecoder == null) {
                this.mDecoder = MediaCodec.createDecoderByType(DEFAULT_MIME_TYPE);
            } else {
                this.mDecoder.stop();
            }
            this.mDecoder.setOnFrameRenderedListener(new MediaCodec.OnFrameRenderedListener() { // from class: com.xpeng.airplay.player.VideoPlayer.1
                @Override // android.media.MediaCodec.OnFrameRenderedListener
                public void onFrameRendered(MediaCodec mediaCodec, long pts, long nanoTime) {
                    Log.d(VideoPlayer.TAG, "onFrameRendered(): pts = " + pts);
                }
            }, null);
            this.mDecoder.configure(format, this.mSurface, (MediaCrypto) null, 0);
            this.mDecoder.setVideoScalingMode(1);
            this.mDecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.xpeng.airplay.player.IPlayer
    public void startPlay() {
        Log.d(TAG, "startPlay()");
        if (!this.mIsRunning) {
            initDecoder();
            start();
            this.mIsRunning = true;
        }
    }

    @Override // com.xpeng.airplay.player.IPlayer
    public void addPacket(NALPacket nalPacket) {
        try {
            this.mVideoBuf.offer(nalPacket, 10L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "fail to offer packet: " + e.getCause());
            e.printStackTrace();
        }
    }

    @Override // com.xpeng.airplay.player.IPlayer
    public void stopPlay() {
        Log.d(TAG, "stopPlayer()");
        if (this.mVideoBuf != null) {
            this.mVideoBuf.clear();
        }
        try {
            if (this.mDecoder != null) {
                this.mDecoder.stop();
            }
        } catch (Exception e) {
        }
        interrupt();
        this.mNeedStop = true;
        this.mIsRunning = false;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        while (!this.mNeedStop) {
            NALPacket nalPacket = null;
            try {
                nalPacket = this.mVideoBuf.poll(30L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "thread is interrupted");
            }
            if (nalPacket != null) {
                try {
                    doDecode(nalPacket);
                } catch (IllegalStateException e2) {
                    Log.d(TAG, "fail to decode: " + e2.getCause());
                    e2.printStackTrace();
                }
            }
        }
    }

    private void doDecode(NALPacket nalPacket) {
        ByteBuffer[] decoderInputBuffers = this.mDecoder.getInputBuffers();
        int inputBufIndex = this.mDecoder.dequeueInputBuffer(10000L);
        if (inputBufIndex >= 0) {
            if (this.mFirstInputTimeNs == -1) {
                this.mFirstInputTimeNs = System.nanoTime();
            }
            ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
            inputBuf.put(nalPacket.nalData);
            this.mDecoder.queueInputBuffer(inputBufIndex, 0, nalPacket.nalData.length, nalPacket.pts, 0);
        } else {
            Log.d(TAG, "input buffer is not available");
        }
        int outputBufferIndex = this.mDecoder.dequeueOutputBuffer(this.mBufferInfo, 10000L);
        if (outputBufferIndex < 0) {
            if (outputBufferIndex == -1) {
                Log.d(TAG, "no output buffer from decoder");
                return;
            } else if (outputBufferIndex != -3 && outputBufferIndex == -2) {
                MediaFormat newMf = this.mDecoder.getOutputFormat();
                Log.d(TAG, "decoder output format changed: " + newMf);
                return;
            } else {
                return;
            }
        }
        if (this.mFirstInputTimeNs != 0) {
            long nowNs = System.nanoTime();
            Log.d(TAG, "startup lag " + ((nowNs - this.mFirstInputTimeNs) / 1000000.0d) + "ms");
            this.mFirstInputTimeNs = 0L;
        }
        boolean doRender = this.mBufferInfo.size != 0;
        if (doRender && this.mCallback != null && this.mIsVideoSizeChanged.get()) {
            Log.d(TAG, "frame buffer will be rendered");
            this.mCallback.onPreRender(this.mBufferInfo.presentationTimeUs);
        }
        this.mDecoder.releaseOutputBuffer(outputBufferIndex, true);
        if (doRender && this.mCallback != null && this.mIsVideoSizeChanged.get()) {
            this.mIsVideoSizeChanged.set(false);
            this.mCallback.postRender();
        }
    }
}
