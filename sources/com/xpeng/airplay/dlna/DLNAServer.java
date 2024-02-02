package com.xpeng.airplay.dlna;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.xpeng.airplay.service.AbstractServer;
import com.xpeng.airplay.service.MediaPlayInfo;
import com.xpeng.airplay.service.MediaPlaybackInfo;
import com.xpeng.airplay.service.Utils;
import com.xpeng.airplay.service.WifiMode;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.logging.LoggingUtil;
/* loaded from: classes.dex */
public final class DLNAServer extends AbstractServer implements IDLNAServer {
    private static final String DMS_DESC = "MPI MediaPlayer";
    private static final String ID_SALT = "MediaPlayer";
    private static final String TAG = "DLNAServer";
    private static final String TYPE_MEDIA_PLAYER = "MediaRenderer";
    private static final int VERSION = 1;
    private LastChange mAVTransportLastChange;
    private AVTransportServiceImpl mAVTransportService;
    private LastChange mAudioRenderLastChange;
    private AudioRenderControlImpl mAudioRenderService;
    private Context mContext;
    private LocalDevice mDlnaDevice;
    private int mMediaType;
    private XpengUpnpServiceImpl mUpnpService;
    private WifiMode mWifiMode = WifiMode.NONE;
    private MediaPlaybackInfo mPlaybackInfo = new MediaPlaybackInfo();
    private AtomicInteger mPlaybackState = new AtomicInteger(3);

    public DLNAServer(Context context) {
        this.mContext = context;
        this.mUpnpService = XpengUpnpServiceImpl.getInstance(context);
        LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());
        this.mMediaType = 2;
    }

    @Override // com.xpeng.airplay.dlna.IDLNAServer
    public int getInstanceId() {
        return 0;
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void startServer() {
        Log.d(TAG, "startServer()");
        if (this.mConfig != null) {
            if (this.mAVTransportLastChange == null) {
                this.mAVTransportLastChange = new LastChange(new AVTransportLastChangeParser());
            }
            if (this.mAVTransportService == null) {
                this.mAVTransportService = new AVTransportServiceImpl(this.mAVTransportLastChange, new AVTransportController(this));
            }
            if (this.mAudioRenderLastChange == null) {
                this.mAudioRenderLastChange = new LastChange(new RenderingControlLastChangeParser());
            }
            if (this.mAudioRenderService == null) {
                this.mAudioRenderService = new AudioRenderControlImpl(this.mAudioRenderLastChange, new AudioController(this));
            }
            try {
                this.mDlnaDevice = createRenderDevice(this.mConfig.getMacAddr());
                if (!this.mUpnpService.addDevice(this.mDlnaDevice)) {
                    Log.e(TAG, "fail to add device");
                }
            } catch (ValidationException ve) {
                Log.e(TAG, "fail to create device");
                ve.printStackTrace();
            }
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void stopServer(boolean shutdown) {
        Log.d(TAG, "stopServer()");
        if (this.mUpnpService != null) {
            if (this.mDlnaDevice != null) {
                this.mUpnpService.removeDevice(this.mDlnaDevice);
            }
            this.mUpnpService.disableRouter();
            this.mDlnaDevice = null;
            if (shutdown) {
                this.mUpnpService.shutdown();
            }
        }
        this.mPlaybackInfo.reset();
        Utils.setDlnaIgnoreIface("");
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void updateWifiMode(WifiMode mode) {
        Log.d(TAG, "updateWifiMode(): " + mode.toString());
        if (this.mWifiMode != mode) {
            stopServer(false);
            if (mode == WifiMode.DUAL) {
                Utils.setDlnaIgnoreIface(Utils.getActiveWifiIface());
            } else {
                Utils.setDlnaIgnoreIface("");
            }
            startServer();
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onClientConnected() {
        Log.i(TAG, "onClientConnected(): screen id = " + this.mConfig.getScreenId());
        if (this.mStateListener != null) {
            this.mStateListener.onClientConnected(this.mConfig.getType(), this.mConfig.getScreenId());
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onClientDisconnected() {
        Log.i(TAG, "onClientDisconnected(): screen id = " + this.mConfig.getScreenId());
        if (this.mStateListener != null) {
            this.mStateListener.onClientDisconnected(this.mConfig.getType(), 2);
        }
        synchronized (this.mPlaybackInfo) {
            this.mPlaybackInfo.reset();
        }
        this.mPlaybackState.set(3);
    }

    @Override // com.xpeng.airplay.dlna.IDLNAServer
    public void setMediaType(int type) {
        Log.d(TAG, "setMediaType(): type = " + type);
        this.mMediaType = type;
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onVideoPlay(String url, String title, int volume, int position) {
        Log.i(TAG, "onVideoPlay()");
        if (this.mStateListener != null) {
            this.mPlaybackInfo.reset();
            MediaPlayInfo playInfo = new MediaPlayInfo(url, title, volume, position);
            playInfo.setType(this.mConfig.getType());
            playInfo.setStreamType(this.mMediaType);
            this.mStateListener.onVideoPlay(playInfo);
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
        Log.d(TAG, "onVideoScrub(): pos = " + pos);
        this.mPlaybackInfo.setPosition((double) pos);
        if (this.mStateListener != null) {
            this.mStateListener.onVideoScrubbed(this.mConfig.getType(), pos);
        }
    }

    @Override // com.xpeng.airplay.service.IAirplayServer
    public void onVideoRate(int rate) {
        Log.d(TAG, "onVideoRate(): rate = " + rate);
        if (this.mStateListener != null) {
            this.mStateListener.onVideoRateChanged(this.mConfig.getType(), rate);
        }
    }

    @Override // com.xpeng.airplay.dlna.IDLNAServer
    public void onVolumeChanged(float vol) {
        Log.d(TAG, "onVolumeChanged()");
        if (this.mStateListener != null) {
            this.mStateListener.onVolumeChanged(this.mConfig.getType(), vol);
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
        synchronized (this.mPlaybackInfo) {
            if (Float.compare(this.mPlaybackInfo.getVolume(), info.getVolume()) != 0 && this.mAudioRenderService != null) {
                this.mAudioRenderService.notifyVolumeChanged(this.mAudioRenderLastChange, info.getVolume());
            }
            if (this.mPlaybackInfo.getRate() != info.getRate()) {
                if (info.getRate() == 1) {
                    this.mAVTransportService.setVideoPlaybackState(this.mAVTransportLastChange, 0);
                } else {
                    this.mAVTransportService.setVideoPlaybackState(this.mAVTransportLastChange, 4);
                }
            }
            this.mPlaybackInfo.copy(info);
        }
    }

    @Override // com.xpeng.airplay.dlna.IDLNAServer
    public int getPlaybackState() {
        return this.mPlaybackState.get();
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public void setVideoPlaybackState(int state) {
        Log.d(TAG, "setVideoPlaybackState()");
        this.mPlaybackState.set(state);
        synchronized (this.mPlaybackInfo) {
            if (state == 2 || state == 3) {
                this.mPlaybackInfo.reset();
            } else if (state == 0) {
                this.mPlaybackInfo.setRate(1);
            } else {
                this.mPlaybackInfo.setRate(0);
            }
        }
        if (this.mAVTransportService != null) {
            this.mAVTransportService.setVideoPlaybackState(this.mAVTransportLastChange, state);
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public boolean isServerActive() {
        boolean z = false;
        try {
            if (this.mUpnpService != null) {
                if (this.mUpnpService.getUpnpService().getRouter().isEnabled()) {
                    z = true;
                }
            }
            boolean isActive = z;
            return isActive;
        } catch (RouterException e) {
            return false;
        }
    }

    @Override // com.xpeng.airplay.service.AbstractServer, com.xpeng.airplay.service.IServer
    public int getServerPort() {
        if (this.mUpnpService != null) {
            try {
                InetAddress ia = InetAddress.parseNumericAddress(this.mConfig.getIpAddr());
                List<NetworkAddress> netAddres = this.mUpnpService.getUpnpService().getRouter().getActiveStreamServers(ia);
                if (netAddres.size() > 0) {
                    Log.d(TAG, "getServerPort(): port = " + netAddres.get(0).getPort());
                    return netAddres.get(0).getPort();
                }
            } catch (Exception e) {
                Log.e(TAG, "fail to get server port");
            }
        }
        return 0;
    }

    private LocalDevice createRenderDevice(String macAddr) throws ValidationException {
        String idSalt = ID_SALT + this.mConfig.getName();
        DeviceIdentity deviceIdentity = new DeviceIdentity(createUniqueSystemIdentifier(idSalt, macAddr));
        UDADeviceType deviceType = new UDADeviceType(TYPE_MEDIA_PLAYER, 1);
        DeviceDetails details = new DeviceDetails(this.mConfig.getName(), new ManufacturerDetails(Build.MANUFACTURER + String.valueOf(this.mConfig.getScreenId())), new ModelDetails(Build.MODEL + String.valueOf(this.mConfig.getScreenId())), DMS_DESC, "v1");
        return new LocalDevice(deviceIdentity, deviceType, details, createLocalServices());
    }

    private LocalService<?>[] createLocalServices() {
        Log.d(TAG, "createLocalServices()");
        LocalService<ConnectionManagerServiceImpl> connectionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerServiceImpl.class);
        connectionManagerService.setManager(new DefaultServiceManager<ConnectionManagerServiceImpl>(connectionManagerService, ConnectionManagerServiceImpl.class) { // from class: com.xpeng.airplay.dlna.DLNAServer.1
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.fourthline.cling.model.DefaultServiceManager
            public ConnectionManagerServiceImpl createServiceInstance() {
                return new ConnectionManagerServiceImpl();
            }
        });
        LocalService<AVTransportServiceImpl> avTransportService = new AnnotationLocalServiceBinder().read(AVTransportServiceImpl.class);
        avTransportService.setManager(new LastChangeAwareServiceManager<AVTransportServiceImpl>(avTransportService, new AVTransportLastChangeParser()) { // from class: com.xpeng.airplay.dlna.DLNAServer.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // org.fourthline.cling.model.DefaultServiceManager
            public AVTransportServiceImpl createServiceInstance() {
                return DLNAServer.this.mAVTransportService;
            }
        });
        LocalService<AudioRenderControlImpl> audioRenderService = new AnnotationLocalServiceBinder().read(AudioRenderControlImpl.class);
        audioRenderService.setManager(new LastChangeAwareServiceManager<AudioRenderControlImpl>(audioRenderService, new RenderingControlLastChangeParser()) { // from class: com.xpeng.airplay.dlna.DLNAServer.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // org.fourthline.cling.model.DefaultServiceManager
            public AudioRenderControlImpl createServiceInstance() {
                return DLNAServer.this.mAudioRenderService;
            }
        });
        return new LocalService[]{connectionManagerService, avTransportService, audioRenderService};
    }

    private UDN createUniqueSystemIdentifier(String salt, String macAddr) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest((macAddr + Build.MODEL + Build.MANUFACTURER).getBytes());
            return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
        } catch (Exception ex) {
            return new UDN(ex.getMessage() != null ? ex.getMessage() : "UNKNOWN");
        }
    }
}
