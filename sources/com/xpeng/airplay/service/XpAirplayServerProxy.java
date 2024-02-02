package com.xpeng.airplay.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import com.xpeng.airplay.dlna.DLNAServer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
/* loaded from: classes.dex */
public class XpAirplayServerProxy implements IServerProxy {
    private static final String AIRPLAY_PACKAGE_NAME = "com.xiaopeng.wirelessprojection";
    private static final String AIRPLAY_PERMISSION = "com.xpeng.permission.ACCESS_AIRPLAY";
    private static final String AIRPLAY_RENDER_ACTIVITY_NAME = "com.xiaopeng.wirelessprojection.dmr.RendererActivity";
    private static final long AIRPLAY_RESTORE_TIMEOUT = 15000;
    protected static final int AIRPLAY_STATE_ACTIVE = 1;
    protected static final int AIRPLAY_STATE_BACKGROUND = 2;
    protected static final int AIRPLAY_STATE_COMPLETED = 5;
    protected static final int AIRPLAY_STATE_FAILED = 4;
    protected static final int AIRPLAY_STATE_INACTIVE = 0;
    protected static final int AIRPLAY_STATE_RESTORING = 3;
    private static final String BROADCAST_AIRPLAY_STATE_CHANGED = "com.xiaopeng.airplay.AIRPLAY_STATE_CHANGED";
    private static final String MAIN_ACTIVITY_CLASS_NAME = "com.xpeng.airplay.ui.MainActivity";
    private static final String MAIN_ACTIVITY_PACKAGE_NAME = "com.xiaopeng.airplay";
    private static final String SERVER_TYPE_NAME_AIRPLAY = "AIRPLAY";
    private static final String SERVER_TYPE_NAME_DLNA = "DLNA";
    private static final String SERVER_TYPE_NAME_MIRROR = "MIRROR";
    private static final String SERVER_TYPE_NAME_NONE = "NONE";
    public static final String TAG = "XpAirplayServerProxy";
    private final AudioManager mAudioMgr;
    private final Context mContext;
    private Size mMirrorSize;
    private AtomicReference<Handler> mHandler = new AtomicReference<>();
    private final HashMap<Integer, IServer> mServerMap = new HashMap<>();
    private final ServerStateListener mStateListener = new ServerStateListener();
    private final AtomicInteger mActiveServerType = new AtomicInteger(0);
    private final AtomicInteger mPrevVideoType = new AtomicInteger(0);
    private final AtomicInteger mAirplayState = new AtomicInteger(0);
    private final List<EventInfo> mPendingEvents = new ArrayList();
    private final MediaPlayInfo mPlayInfo = new MediaPlayInfo();
    private final AtomicInteger mSystemVol = new AtomicInteger(-1);

    public XpAirplayServerProxy(Context context) {
        this.mContext = context;
        this.mAudioMgr = (AudioManager) this.mContext.getSystemService("audio");
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void startServer() {
        Log.d(TAG, "startServer()");
        for (IServer server : this.mServerMap.values()) {
            if (server != null) {
                server.startServer();
                server.addStateListener(this.mStateListener);
            }
        }
        this.mActiveServerType.set(0);
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void stopServer(boolean shutdown) {
        Log.d(TAG, "stopServer()");
        for (IServer server : this.mServerMap.values()) {
            if (server != null) {
                server.removeStateListener();
                server.stopServer(shutdown);
            }
        }
        if (this.mActiveServerType.get() != 0) {
            sendMessageWithArg(2, this.mActiveServerType.get(), 2);
        }
        updateAirplayState(0);
        this.mActiveServerType.set(0);
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void addCallbackHandler(Handler handler) {
        this.mHandler.set(handler);
        restorePreviousSession();
        if (this.mPendingEvents.size() > 0) {
            for (EventInfo ei : this.mPendingEvents) {
                Message pendMsg = handler.obtainMessage(ei.event, ei.obj);
                pendMsg.sendToTarget();
            }
            this.mPendingEvents.clear();
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void removeCallbackHandler() {
        removeMessage(11);
        this.mHandler.set(null);
        this.mPendingEvents.clear();
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void setMirrorSurface(Surface surface) {
        Log.d(TAG, "setMirrorSurface()");
        IServer server = this.mServerMap.get(1);
        if (server != null) {
            server.setMirrorSurface(surface);
        }
        if (surface != null) {
            removeMessage(11);
            updateAirplayState(1);
            return;
        }
        updateAirplayState(2);
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void setVideoPlaybackState(int state) {
        MediaPlaybackInfo playbackInfo;
        Log.i(TAG, "setVideoPlaybackState(): state = " + getPlaybackStateStr(state));
        int type = this.mActiveServerType.get();
        IServer server = this.mServerMap.get(Integer.valueOf(type));
        if (server != null) {
            server.setVideoPlaybackState(state);
        }
        switch (state) {
            case 0:
                removeMessage(11);
                updateAirplayState(1);
                return;
            case 1:
                this.mActiveServerType.set(0);
                updateAirplayState(5);
                sendMessageWithoutObj(6);
                return;
            case 2:
                sendMessageWithoutObj(6);
                return;
            case 3:
                destroyConnection();
                sendMessageWithoutObj(6);
                return;
            case 4:
            default:
                return;
            case 5:
                if (server != null && (playbackInfo = server.getMediaPlaybackInfo()) != null) {
                    this.mPlayInfo.setPosition((int) playbackInfo.getPosition());
                }
                updateAirplayState(2);
                return;
            case 6:
                restorePreviousSession();
                return;
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void setMediaPlaybackInfo(MediaPlaybackInfo info) {
        IServer server = this.mServerMap.get(Integer.valueOf(this.mActiveServerType.get()));
        if (server != null) {
            server.setMediaPlaybackInfo(info);
        }
        if (info.getRate() == 1 && this.mAirplayState.get() == 3) {
            removeMessage(11);
            updateAirplayState(1);
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void updateWifiMode(WifiMode mode) {
        Log.i(TAG, "updateWifiMode(): " + mode.toString());
        for (IServer server : this.mServerMap.values()) {
            if (server != null) {
                server.updateWifiMode(mode);
            }
        }
        if (mode != WifiMode.DUAL && this.mActiveServerType.get() != 0) {
            sendMessageWithArg(2, this.mActiveServerType.get(), 2);
            updateAirplayState(0);
            this.mActiveServerType.set(0);
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public boolean isServerActive(int serverType) {
        IServer server = this.mServerMap.get(Integer.valueOf(serverType));
        if (server != null) {
            return server.isServerActive();
        }
        return false;
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void onClientDied() {
        Log.w(TAG, "onClientDied()");
        removeMessage(11);
        int state = this.mAirplayState.get();
        if (state == 3) {
            updateAirplayState(4);
        } else if (state == 2) {
            updateAirplayState(0);
        } else if (state == 1) {
            updateAirplayState(2);
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public int getServerPort(int serverType) {
        IServer server = this.mServerMap.get(Integer.valueOf(serverType));
        if (server != null) {
            return server.getServerPort();
        }
        return 0;
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public String getServerTypeName(int serverType) {
        return getServerNameByType(serverType);
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public ServerConfig getServerConfig(int serverType) {
        IServer server = this.mServerMap.get(Integer.valueOf(serverType));
        if (server != null) {
            return server.getServerConfig();
        }
        return null;
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void updateServerConfig(Collection<ServerConfig> configs) {
        Log.d(TAG, "updateServerConfig(): size = " + configs.size());
        String serverName = null;
        boolean serverNameChanged = true;
        for (ServerConfig config : configs) {
            IServer server = null;
            if (config != null) {
                int type = config.getType();
                switch (type) {
                    case 1:
                        if (!this.mServerMap.containsKey(1)) {
                            IServer server2 = new RaopServer(this.mContext);
                            this.mServerMap.put(1, server2);
                            server = server2;
                            break;
                        }
                        break;
                    case 2:
                        if (!this.mServerMap.containsKey(2)) {
                            IServer server3 = new AirplayServer(this.mContext);
                            this.mServerMap.put(2, server3);
                            server = server3;
                            break;
                        }
                        break;
                    case 3:
                        if (!this.mServerMap.containsKey(3)) {
                            IServer server4 = new DLNAServer(this.mContext);
                            this.mServerMap.put(3, server4);
                            server = server4;
                            break;
                        }
                        break;
                }
                if (server != null) {
                    server.setServerConfig(config);
                } else {
                    IServer server5 = this.mServerMap.get(Integer.valueOf(type));
                    if (server5 != null) {
                        ServerConfig sc = server5.getServerConfig();
                        serverNameChanged = !sc.getName().equals(config.getName());
                        serverName = config.getName();
                        sc.update(config.getScreenId(), config.getName(), config.getIpAddr(), config.getMacAddr());
                    }
                }
            }
        }
        if (serverNameChanged) {
            Log.d(TAG, "updateServerConfig(): server name changed(" + serverName + ")");
            sendMessageWithObj(14, serverName);
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void destroyConnection() {
        IServer raopServer;
        int type = this.mActiveServerType.get();
        Log.d(TAG, "destroyConnection(): type = " + type);
        IServer server = this.mServerMap.get(Integer.valueOf(type));
        if (server != null) {
            server.setVideoPlaybackState(3);
            server.destroyConnection();
            if (type == 2 && (raopServer = this.mServerMap.get(1)) != null) {
                raopServer.destroyConnection();
            }
        }
        updateAirplayState(0);
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void updateAirplayState(int state) {
        Log.i(TAG, "updateAirplayState(): " + getAirplayStateStr(state));
        boolean sendBroadcast = false;
        int prevState = this.mAirplayState.get();
        if (state != 4) {
            switch (state) {
                case 0:
                    if (prevState == 2 || prevState == 1 || prevState == 3) {
                        sendBroadcast = true;
                        break;
                    }
                case 1:
                    if (prevState == 3) {
                        sendBroadcast = true;
                        break;
                    }
                    break;
                case 2:
                    if (prevState == 1) {
                        sendBroadcast = true;
                        break;
                    }
                    break;
            }
        } else if (prevState == 3) {
            sendBroadcast = true;
        }
        if (state == 0 || state == 2 || state == 5) {
            restoreSystemVolume();
        }
        this.mAirplayState.set(state);
        if (sendBroadcast) {
            sendAirplayStateChanged(state);
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void onRearBtStateChanged(boolean on) {
        Log.d(TAG, "onRearBtStateChanged(): " + on);
        IServer server = this.mServerMap.get(Integer.valueOf(this.mActiveServerType.get()));
        if (server != null) {
            server.updateRearBtState(on);
        }
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public boolean hasActiveConnection() {
        return this.mActiveServerType.get() != 0;
    }

    @Override // com.xpeng.airplay.service.IServerProxy
    public void saveSystemVolume(int vol) {
        Log.d(TAG, "saveSystemVolume(): " + this.mSystemVol.getAndSet(vol));
    }

    private void restoreSystemVolume() {
        Log.d(TAG, "restoreSystemVolume()");
        int vol = this.mSystemVol.get();
        if (this.mAudioMgr != null && vol > 0) {
            this.mAudioMgr.setStreamVolume(3, vol, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeMessage(int event) {
        if (this.mHandler.get() != null) {
            this.mHandler.get().removeMessages(event);
        }
    }

    private void sendAirplayStateChanged(int state) {
        IServer server = this.mServerMap.get(Integer.valueOf(this.mActiveServerType.get()));
        if (server != null) {
            int screenId = Utils.getAirplayScreenId();
            if (screenId < 0) {
                screenId = server.getScreenId();
            }
            Log.i(TAG, "sendAirplayStateChanged(): screen id=" + screenId);
            Intent it = new Intent(BROADCAST_AIRPLAY_STATE_CHANGED);
            it.putExtra("state", state);
            it.putExtra("screen_id", screenId);
            this.mContext.sendBroadcastAsUser(it, UserHandle.ALL, "com.xpeng.permission.ACCESS_AIRPLAY");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAirplayActivity(int screenId) {
        Log.d(TAG, "startAirplayActivity(): screen id = " + screenId);
        Intent intent = new Intent();
        intent.addFlags(268435456);
        if (Utils.isDebugMode()) {
            intent.setClassName(MAIN_ACTIVITY_PACKAGE_NAME, MAIN_ACTIVITY_CLASS_NAME);
            this.mContext.startActivity(intent);
            return;
        }
        intent.setClassName(AIRPLAY_PACKAGE_NAME, AIRPLAY_RENDER_ACTIVITY_NAME);
        this.mContext.startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void restorePreviousSession() {
        Log.d(TAG, "restorePreviousSession()");
        if (this.mAirplayState.get() == 2) {
            this.mAirplayState.set(3);
            int type = this.mActiveServerType.get();
            sendMessageWithObj(1, Integer.valueOf(type));
            if (type == 3 || type == 2) {
                sendMessageWithObj(5, this.mPlayInfo);
            } else if (type == 1) {
                IServer server = this.mServerMap.get(Integer.valueOf(type));
                if (server != null && server.isAudioStream()) {
                    sendMessageWithObj(5, this.mPlayInfo);
                } else {
                    sendMessageWithoutObj(3);
                    if (this.mMirrorSize != null) {
                        sendMessageWithObj(4, this.mMirrorSize);
                    }
                }
            }
            Handler handler = this.mHandler.get();
            if (handler != null) {
                handler.sendEmptyMessageDelayed(11, AIRPLAY_RESTORE_TIMEOUT);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendMessageWithObj(int event, Object obj) {
        if (this.mHandler.get() != null) {
            Message msg = this.mHandler.get().obtainMessage(event, obj);
            msg.sendToTarget();
            return;
        }
        this.mPendingEvents.add(new EventInfo(event, obj));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendMessageWithArg(int event, int arg1, int arg2) {
        if (this.mHandler.get() != null) {
            Message msg = this.mHandler.get().obtainMessage(event, arg1, arg2);
            msg.sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendMessageWithoutObj(int event) {
        if (this.mHandler.get() != null) {
            this.mHandler.get().sendEmptyMessage(event);
        } else {
            this.mPendingEvents.add(new EventInfo(event, null));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isVideoPlayType(int type) {
        return type == 2 || type == 3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class EventInfo {
        int event;
        Object obj;

        EventInfo(int event, Object obj) {
            this.event = event;
            this.obj = obj;
        }
    }

    /* loaded from: classes.dex */
    private final class ServerStateListener implements IStateListener {
        public ServerStateListener() {
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onClientConnected(int type, int screenId) {
            Log.i(XpAirplayServerProxy.TAG, "onClientConnected(): type = " + type + ", screenId = " + screenId);
            if (!XpAirplayServerProxy.this.isVideoPlayType(type)) {
                XpAirplayServerProxy.this.mActiveServerType.set(type);
            } else {
                XpAirplayServerProxy.this.mPrevVideoType.set(XpAirplayServerProxy.this.mActiveServerType.get());
            }
            if (XpAirplayServerProxy.this.mHandler.get() == null) {
                XpAirplayServerProxy.this.startAirplayActivity(screenId);
                XpAirplayServerProxy.this.mPendingEvents.add(new EventInfo(1, Integer.valueOf(type)));
                return;
            }
            XpAirplayServerProxy.this.sendMessageWithObj(1, Integer.valueOf(type));
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onClientDisconnected(int type, int reason) {
            Log.d(XpAirplayServerProxy.TAG, "onClientDisconnected(): " + XpAirplayServerProxy.this.getServerNameByType(type));
            if (XpAirplayServerProxy.this.mActiveServerType.get() == type) {
                XpAirplayServerProxy.this.sendMessageWithArg(2, type, reason);
                if (XpAirplayServerProxy.this.mAirplayState.get() == 3) {
                    XpAirplayServerProxy.this.removeMessage(11);
                    XpAirplayServerProxy.this.updateAirplayState(4);
                } else {
                    XpAirplayServerProxy.this.updateAirplayState(0);
                }
                XpAirplayServerProxy.this.mActiveServerType.set(0);
            } else {
                Log.w(XpAirplayServerProxy.TAG, "onClientDisconnected(): server type is not matched");
            }
            XpAirplayServerProxy.this.mPendingEvents.clear();
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onVideoPlay(MediaPlayInfo info) {
            IServer raopServer;
            Log.d(XpAirplayServerProxy.TAG, "onVideoPlay(): " + info);
            int type = XpAirplayServerProxy.this.mPrevVideoType.get();
            XpAirplayServerProxy.this.mActiveServerType.set(info.getType());
            XpAirplayServerProxy.this.sendMessageWithObj(1, Integer.valueOf(info.getType()));
            XpAirplayServerProxy.this.sendMessageWithObj(5, info);
            XpAirplayServerProxy.this.mPlayInfo.copy(info);
            Log.d(XpAirplayServerProxy.TAG, "onVideoPlay(): current type = " + XpAirplayServerProxy.this.getServerNameByType(type));
            if (type != info.getType()) {
                Log.i(XpAirplayServerProxy.TAG, "video play is changed from " + XpAirplayServerProxy.this.getServerNameByType(type) + " to " + XpAirplayServerProxy.this.getServerNameByType(info.getType()));
                IServer server = (IServer) XpAirplayServerProxy.this.mServerMap.get(Integer.valueOf(type));
                if (server != null) {
                    server.setVideoPlaybackState(3);
                    server.destroyConnection();
                    if (type == 2 && (raopServer = (IServer) XpAirplayServerProxy.this.mServerMap.get(1)) != null) {
                        raopServer.destroyConnection();
                    }
                }
            }
            XpAirplayServerProxy.this.mAirplayState.set(1);
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onVideoRateChanged(int type, int rate) {
            Log.d(XpAirplayServerProxy.TAG, "onVideoRateChanged()");
            if (XpAirplayServerProxy.this.mActiveServerType.get() != type) {
                Log.w(XpAirplayServerProxy.TAG, "onVideoRateChanged(): server type is not matched");
                return;
            }
            if (rate == 1) {
                XpAirplayServerProxy.this.restorePreviousSession();
            }
            XpAirplayServerProxy.this.sendMessageWithObj(7, Integer.valueOf(rate));
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onVideoScrubbed(int type, int pos) {
            Log.d(XpAirplayServerProxy.TAG, "onVideoScrubbed()");
            if (XpAirplayServerProxy.this.mActiveServerType.get() == type) {
                XpAirplayServerProxy.this.restorePreviousSession();
                XpAirplayServerProxy.this.sendMessageWithObj(8, Integer.valueOf(pos));
                return;
            }
            Log.w(XpAirplayServerProxy.TAG, "onVideoScrubbed(): server type is not matched");
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onVideoStopped(int type) {
            Log.d(XpAirplayServerProxy.TAG, "onVideoStopped()");
            if (XpAirplayServerProxy.this.mActiveServerType.get() == type) {
                XpAirplayServerProxy.this.sendMessageWithoutObj(6);
            } else {
                Log.w(XpAirplayServerProxy.TAG, "onVideoStopped(): server type is not matched");
            }
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onVolumeChanged(int type, float vol) {
            Log.d(XpAirplayServerProxy.TAG, "onVolumeChanged()");
            if (XpAirplayServerProxy.this.mActiveServerType.get() != type) {
                if (XpAirplayServerProxy.this.mActiveServerType.get() == 2 && type == 1) {
                    XpAirplayServerProxy.this.sendMessageWithObj(9, Float.valueOf(vol));
                    return;
                } else {
                    Log.w(XpAirplayServerProxy.TAG, "onVolumeChanged(): server type is not matched");
                    return;
                }
            }
            XpAirplayServerProxy.this.sendMessageWithObj(9, Float.valueOf(vol));
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onScreenMirrorStarted() {
            Log.d(XpAirplayServerProxy.TAG, "onScreenMirrorStarted()");
            XpAirplayServerProxy.this.sendMessageWithoutObj(3);
            XpAirplayServerProxy.this.mAirplayState.set(1);
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onMirrorSizeChanged(int w, int h) {
            Log.d(XpAirplayServerProxy.TAG, "onMirrorSizeChanged()");
            Size sz = new Size(w, h);
            XpAirplayServerProxy.this.mMirrorSize = new Size(w, h);
            XpAirplayServerProxy.this.sendMessageWithObj(4, sz);
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onAudioFocusChanged(boolean loss) {
            Log.d(XpAirplayServerProxy.TAG, "onAudioFocusChanged(): " + loss);
            if (loss && XpAirplayServerProxy.this.mActiveServerType.get() == 1) {
                XpAirplayServerProxy.this.sendMessageWithoutObj(12);
            }
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onMetadataUpdated(MediaMetaData metadata) {
            Log.d(XpAirplayServerProxy.TAG, "onMetadataUpdated()");
            if (metadata != null) {
                XpAirplayServerProxy.this.sendMessageWithObj(13, metadata);
            }
        }

        @Override // com.xpeng.airplay.service.IStateListener
        public void onAudioProgressUpdated(MediaPlaybackInfo playbackInfo) {
            Log.d(XpAirplayServerProxy.TAG, "onAudioProgressUpdated()");
            if (playbackInfo != null) {
                XpAirplayServerProxy.this.sendMessageWithObj(15, playbackInfo);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getServerNameByType(int serverType) {
        switch (serverType) {
            case 1:
                return SERVER_TYPE_NAME_MIRROR;
            case 2:
                return SERVER_TYPE_NAME_AIRPLAY;
            case 3:
                return SERVER_TYPE_NAME_DLNA;
            default:
                return "NONE";
        }
    }

    private static String getAirplayStateStr(int state) {
        switch (state) {
            case 0:
                return "INACTIVE";
            case 1:
                return "ACTIVE";
            case 2:
                return "BACKGROUND";
            case 3:
                return "RESTORING";
            case 4:
                return AbstractLifeCycle.FAILED;
            case 5:
                return "COMPLETED";
            default:
                return "UNKNOWN";
        }
    }

    private static String getPlaybackStateStr(int state) {
        switch (state) {
            case 0:
                return "PLAYING";
            case 1:
                return "COMPLETE";
            case 2:
                return "ERROR";
            case 3:
                return "STOP";
            case 4:
                return "PAUSE";
            case 5:
                return "GONE";
            case 6:
                return "RESUME";
            default:
                return "UNKNOWN";
        }
    }
}
