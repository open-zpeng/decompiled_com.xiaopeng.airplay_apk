package com.xpeng.airplay.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkStats;
import android.net.wifi.WifiClient;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.xiaopeng.airplay.R;
import com.xpeng.airplay.service.IXpAirplayManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
/* loaded from: classes.dex */
public final class XpAirplayServiceImpl extends IXpAirplayManager.Stub {
    private static final int KB = 1024;
    private static final int MB = 1048576;
    private static final String NETD_SERVICE_NAME = "netd";
    private static final String REAR_BLUETOOTH_STATE_CHANGED = "xiaopeng.bluetooth.action.CONNECTION_STATE_CHANGED";
    private static final String SERVICE_NAME = "xpairplay";
    private static final String SSID_BLACKLIST_REG = "\\S+XP-AUTO\\S+";
    public static final String TAG = "XpAirplayServiceImpl";
    private static final String USER_ACCOUNT_NAME_KEY = "name";
    private static final int WIFI_AP_READY_DELAY = 3000;
    private static final List<Integer> sServerTypes = new ArrayList();
    private final AccountManager mAccountMgr;
    private HashMap<Network, NetworkCapabilities> mActiveNetwork;
    private String mActiveTetheringIface;
    private ConnectivityManager mConnectivityMgr;
    private final Context mContext;
    private NetworkStatsManager mNetStatsMgr;
    private INetworkManagementService mNetworkMgr;
    private final Random mRandom;
    private RearBluetoothStateReceiver mRearBtReceiver;
    private final String mSecondMacAddr;
    private final Handler mServiceHandler;
    private XpAirplayShellCommand mShellCommand;
    private WifiSoftApCallback mSoftApCallback;
    private long mSoftApStartTime;
    private TetherStateReceiver mTetherReceiver;
    private long mTetheringDataUsage;
    private final SparseBooleanArray mUsedSessionIds;
    private Account mUserAccount;
    private AtomicBoolean mUserAccountChanged;
    private boolean mWifiApEnabled;
    private WifiManager mWifiMgr;
    private WifiNetworkCallback mWifiNetworkCallback;
    private boolean mWifiStaEnabled;
    private final Map<Integer, IServerProxy> mServerProxys = new ConcurrentHashMap();
    private final Map<Integer, XpAirplaySession> mAirplaySessions = new HashMap();
    private final Map<String, Integer> mClientSessions = new HashMap();
    private final Object mSessionLock = new Object();
    private final Map<Integer, String> mServerNames = new ConcurrentHashMap();
    private final Object mServerConfigLock = new Object();

    static {
        sServerTypes.add(1);
        sServerTypes.add(2);
        sServerTypes.add(3);
    }

    /* loaded from: classes.dex */
    public final class ServiceHandler extends Handler {
        public static final int EVENT_ACCOUNTS_UPDATED = 8;
        public static final int EVENT_BT_STATE_CHANGED = 10;
        public static final int EVENT_CLIENT_UNBIND = 9;
        public static final int EVENT_SERVICE_CREATE = 1;
        public static final int EVENT_SERVICE_DESTROY = 2;
        public static final int EVENT_SOFT_AP_DISABLED = 6;
        public static final int EVENT_SOFT_AP_ENABLED = 5;
        public static final int EVENT_TETHERING_CHANGED = 7;
        public static final int EVENT_WIFI_CONNECTED = 3;
        public static final int EVENT_WIFI_DICONNECTED = 4;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    XpAirplayServiceImpl.this.handleServiceCreated();
                    return;
                case 2:
                    XpAirplayServiceImpl.this.handleServiceDestroyed();
                    return;
                case 3:
                    XpAirplayServiceImpl.this.updateServerConfig(WifiMode.STA);
                    XpAirplayServiceImpl.this.mWifiStaEnabled = true;
                    if (!XpAirplayServiceImpl.this.mWifiApEnabled) {
                        XpAirplayServiceImpl.this.startAllServers();
                        return;
                    } else {
                        XpAirplayServiceImpl.this.updateWifiMode(WifiMode.DUAL);
                        return;
                    }
                case 4:
                    XpAirplayServiceImpl.this.mWifiStaEnabled = false;
                    if (!XpAirplayServiceImpl.this.mWifiApEnabled) {
                        XpAirplayServiceImpl.this.stopAllServers(false);
                        return;
                    } else {
                        XpAirplayServiceImpl.this.updateWifiMode(WifiMode.AP);
                        return;
                    }
                case 5:
                    WifiConfiguration apConfig = XpAirplayServiceImpl.this.mWifiMgr.getWifiApConfiguration();
                    Log.d(XpAirplayServiceImpl.TAG, "soft ap SSID: " + apConfig.SSID + ", band: " + apConfig.apBand);
                    XpAirplayServiceImpl.this.updateServerConfig(WifiMode.AP);
                    XpAirplayServiceImpl.this.mWifiApEnabled = true;
                    if (!XpAirplayServiceImpl.this.mWifiStaEnabled) {
                        XpAirplayServiceImpl.this.startAllServers();
                        return;
                    } else {
                        XpAirplayServiceImpl.this.updateWifiMode(WifiMode.DUAL);
                        return;
                    }
                case 6:
                    XpAirplayServiceImpl.this.mWifiApEnabled = false;
                    if (!XpAirplayServiceImpl.this.mWifiStaEnabled) {
                        XpAirplayServiceImpl.this.stopAllServers(false);
                    } else {
                        XpAirplayServiceImpl.this.updateWifiMode(WifiMode.STA);
                    }
                    XpAirplayServiceImpl.this.mActiveTetheringIface = null;
                    return;
                case 7:
                    XpAirplayServiceImpl.this.handleTetheringChanged((String) msg.obj);
                    return;
                case 8:
                    XpAirplayServiceImpl.this.handleAccountUpdated((Account) msg.obj);
                    return;
                case 9:
                    XpAirplayServiceImpl.this.handleClientUnbind((String) msg.obj);
                    return;
                case 10:
                    XpAirplayServiceImpl.this.handleRearBtStateChanged(((Integer) msg.obj).intValue());
                    return;
                default:
                    Log.e(XpAirplayServiceImpl.TAG, "unknown event " + msg.what);
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {
        private WifiNetworkCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            NetworkCapabilities nc = XpAirplayServiceImpl.this.mConnectivityMgr.getNetworkCapabilities(network);
            if (nc != null && nc.hasTransport(1)) {
                XpAirplayServiceImpl.this.mActiveNetwork.put(network, nc);
                WifiInfo wi = XpAirplayServiceImpl.this.mWifiMgr.getConnectionInfo();
                String ssid = wi.getSSID();
                Log.d(XpAirplayServiceImpl.TAG, "onAvailable(): WIFI(" + ssid + ")");
                if (ssid.matches(XpAirplayServiceImpl.SSID_BLACKLIST_REG)) {
                    Utils.setDlnaSearchOpt(true);
                } else {
                    Utils.setDlnaSearchOpt(false);
                }
                if (!XpAirplayServiceImpl.this.mServiceHandler.hasMessages(3)) {
                    XpAirplayServiceImpl.this.mServiceHandler.sendEmptyMessageDelayed(3, 3000L);
                }
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            Log.d(XpAirplayServiceImpl.TAG, "onLost()");
            NetworkCapabilities nc = (NetworkCapabilities) XpAirplayServiceImpl.this.mActiveNetwork.get(network);
            if (nc != null && nc.hasTransport(1)) {
                Log.d(XpAirplayServiceImpl.TAG, "onLost(): WIFI");
                XpAirplayServiceImpl.this.mServiceHandler.removeMessages(3);
                XpAirplayServiceImpl.this.mActiveNetwork.remove(network);
                XpAirplayServiceImpl.this.mServiceHandler.sendEmptyMessage(4);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class WifiSoftApCallback implements WifiManager.SoftApCallback {
        private WifiSoftApCallback() {
        }

        public void onStateChanged(int state, int failReason) {
            Log.d(XpAirplayServiceImpl.TAG, "onStateChanged(): state = " + state);
            if (state == 13) {
                if (!XpAirplayServiceImpl.this.mServiceHandler.hasMessages(5)) {
                    XpAirplayServiceImpl.this.mServiceHandler.sendEmptyMessageDelayed(5, 3000L);
                }
            } else if (state == 11 || state == 14) {
                XpAirplayServiceImpl.this.mServiceHandler.removeMessages(5);
                XpAirplayServiceImpl.this.mServiceHandler.sendEmptyMessage(6);
            }
        }

        public void onClientsUpdated(List<WifiClient> clients) {
            Log.d(XpAirplayServiceImpl.TAG, "onClientsUpdated(): clients size = " + clients.size());
        }

        public void onNumClientsChanged(int numclients) {
            Log.d(XpAirplayServiceImpl.TAG, "onNumClientsChanged(): numclients = " + numclients);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class TetherStateReceiver extends BroadcastReceiver {
        private TetherStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.d(XpAirplayServiceImpl.TAG, "onReceive(): " + intent.getAction());
            if ("android.net.conn.TETHER_STATE_CHANGED".equals(intent.getAction())) {
                List<String> tetherList = intent.getStringArrayListExtra("upstreamIfaces");
                if (tetherList == null || tetherList.size() <= 0) {
                    Message msg = XpAirplayServiceImpl.this.mServiceHandler.obtainMessage(7, null);
                    msg.sendToTarget();
                    return;
                }
                Message msg2 = XpAirplayServiceImpl.this.mServiceHandler.obtainMessage(7, tetherList.get(0));
                msg2.sendToTarget();
            }
        }
    }

    /* loaded from: classes.dex */
    private final class AccountsUpdateListener implements OnAccountsUpdateListener {
        private AccountsUpdateListener() {
        }

        @Override // android.accounts.OnAccountsUpdateListener
        public void onAccountsUpdated(Account[] accounts) {
            Log.d(XpAirplayServiceImpl.TAG, "onAccountsUpdated()");
            Account[] xpengAccounts = XpAirplayServiceImpl.this.mAccountMgr.getAccountsByType("com.xiaopeng.accountservice.ACCOUNT_TYPE_XP_VEHICLE");
            if (xpengAccounts == null || xpengAccounts.length <= 0) {
                Message msg = XpAirplayServiceImpl.this.mServiceHandler.obtainMessage(8, null);
                msg.sendToTarget();
                return;
            }
            Message msg2 = XpAirplayServiceImpl.this.mServiceHandler.obtainMessage(8, xpengAccounts[0]);
            msg2.sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class RearBluetoothStateReceiver extends BroadcastReceiver {
        private RearBluetoothStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(XpAirplayServiceImpl.TAG, "onReceive(): " + intent.getAction());
            if (intent.getAction().equals(XpAirplayServiceImpl.REAR_BLUETOOTH_STATE_CHANGED)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 2 || state == 0) {
                    Message msg = XpAirplayServiceImpl.this.mServiceHandler.obtainMessage(10, Integer.valueOf(state));
                    msg.sendToTarget();
                }
            }
        }
    }

    public XpAirplayServiceImpl(Context context, Looper looper) {
        this.mContext = context;
        this.mServiceHandler = new ServiceHandler(looper);
        this.mContext.getSystemService("servicediscovery");
        this.mRandom = new SecureRandom();
        this.mUsedSessionIds = new SparseBooleanArray();
        this.mWifiApEnabled = false;
        this.mWifiStaEnabled = false;
        this.mNetworkMgr = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.mUserAccountChanged = new AtomicBoolean(false);
        this.mAccountMgr = AccountManager.get(this.mContext);
        if (this.mAccountMgr != null) {
            this.mAccountMgr.addOnAccountsUpdatedListener(new AccountsUpdateListener(), this.mServiceHandler, true);
        }
        if (Utils.hasRearDisplay()) {
            this.mSecondMacAddr = getRandomMacAddr();
            this.mRearBtReceiver = new RearBluetoothStateReceiver();
        } else {
            this.mRearBtReceiver = null;
            this.mSecondMacAddr = null;
        }
        this.mSoftApStartTime = System.currentTimeMillis();
        this.mNetStatsMgr = (NetworkStatsManager) this.mContext.getSystemService("netstats");
        if (!Utils.isUserBuild()) {
            this.mShellCommand = new XpAirplayShellCommand(this);
            ServiceManager.addService(SERVICE_NAME, this);
        }
    }

    public void onCreate() {
        Log.d(TAG, "onCreate()");
        this.mServiceHandler.sendEmptyMessage(1);
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        this.mServiceHandler.sendEmptyMessage(2);
    }

    public void onClientUnbind(String pkgName) {
        Log.d(TAG, "onClientUnbind()");
        Message msg = this.mServiceHandler.obtainMessage(9, pkgName);
        msg.sendToTarget();
    }

    public String getServerState() {
        StringBuilder sb = new StringBuilder();
        int serverCnt = 0;
        for (IServerProxy serverProxy : this.mServerProxys.values()) {
            serverCnt++;
            sb.append("<--------");
            sb.append(getServerName(serverCnt > 1));
            sb.append("-------->\n");
            for (Integer num : sServerTypes) {
                int type = num.intValue();
                boolean isActive = serverProxy.isServerActive(type);
                sb.append("(");
                sb.append(serverProxy.getServerTypeName(type));
                sb.append(")");
                sb.append(": ");
                sb.append(isActive ? "running" : "stopped");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String formatTetheringDataUsage(long dataUsage) {
        if (dataUsage > 1048576) {
            return String.format("%d MB", Long.valueOf(dataUsage / 1048576));
        }
        if (dataUsage > 1024) {
            return String.format("%d KB", Long.valueOf(dataUsage / 1024));
        }
        return String.format("%d Bytes", Long.valueOf(dataUsage));
    }

    public String getServerInfo() {
        StringBuilder sb = new StringBuilder();
        int serverCnt = 0;
        for (IServerProxy serverProxy : this.mServerProxys.values()) {
            serverCnt++;
            sb.append("<--------");
            sb.append(getServerName(serverCnt > 1));
            sb.append("-------->\n");
            for (Integer num : sServerTypes) {
                int type = num.intValue();
                int port = serverProxy.getServerPort(type);
                sb.append("(");
                sb.append(serverProxy.getServerTypeName(type));
                sb.append(")");
                sb.append(": ");
                sb.append(port);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback shellCallback, ResultReceiver resultReceiver) {
        Log.d(TAG, "onShellCommand(): args = " + args);
        if (this.mShellCommand != null) {
            this.mShellCommand.exec(this, in, out, err, args, shellCallback, resultReceiver);
        }
    }

    @Override // com.xpeng.airplay.service.IXpAirplayManager
    public String[] getServerNames() {
        if (this.mServerNames.size() == 0) {
            try {
                synchronized (this.mServerConfigLock) {
                    this.mServerConfigLock.wait(6000L);
                }
            } catch (InterruptedException e) {
            }
        }
        List<String> names = new ArrayList<>(this.mServerNames.values());
        return (String[]) names.toArray(new String[0]);
    }

    @Override // com.xpeng.airplay.service.IXpAirplayManager
    public String getServerName(int screenId) {
        if (this.mServerNames.get(Integer.valueOf(screenId)) == null || this.mUserAccountChanged.getAndSet(false)) {
            try {
                synchronized (this.mServerConfigLock) {
                    this.mServerConfigLock.wait(6000L);
                }
            } catch (InterruptedException e) {
            }
        }
        return this.mServerNames.get(Integer.valueOf(screenId));
    }

    @Override // com.xpeng.airplay.service.IXpAirplayManager
    public int createSession(SessionParams params) {
        int screenId = params.getScreenId();
        Log.i(TAG, "createSession(): screen id=" + screenId);
        int sessionId = allocateSessionId();
        if (sessionId < 0) {
            Log.d(TAG, "fail to allocate session id");
            return -1;
        }
        synchronized (this.mSessionLock) {
            if (!this.mAirplaySessions.containsKey(Integer.valueOf(sessionId))) {
                IServerProxy serverProxy = this.mServerProxys.get(Integer.valueOf(screenId));
                if (serverProxy != null) {
                    XpAirplaySession xas = new XpAirplaySession(params, serverProxy, this.mServiceHandler);
                    this.mAirplaySessions.put(Integer.valueOf(sessionId), xas);
                } else {
                    Log.w(TAG, "no server found for screen " + screenId);
                }
            } else {
                Log.w(TAG, "screen " + screenId + " already has a created session");
            }
            this.mClientSessions.put(params.getPackageName(), Integer.valueOf(sessionId));
        }
        Log.d(TAG, "createSession(): id = " + sessionId);
        return sessionId;
    }

    @Override // com.xpeng.airplay.service.IXpAirplayManager
    public IXpAirplaySession openSession(int sessionId) {
        Log.d(TAG, "openSession(): session id = " + sessionId);
        synchronized (this.mSessionLock) {
            if (!this.mAirplaySessions.containsKey(Integer.valueOf(sessionId))) {
                Log.w(TAG, "donot found any session to open for id " + sessionId);
                return null;
            }
            XpAirplaySession xas = this.mAirplaySessions.get(Integer.valueOf(sessionId));
            xas.start();
            return xas;
        }
    }

    @Override // com.xpeng.airplay.service.IXpAirplayManager
    public void closeSession(int sessionId) {
        Log.d(TAG, "closeSession(): " + sessionId);
        synchronized (this.mSessionLock) {
            XpAirplaySession xas = this.mAirplaySessions.get(Integer.valueOf(sessionId));
            if (xas != null) {
                xas.stop();
            }
            this.mUsedSessionIds.put(sessionId, false);
            this.mAirplaySessions.remove(Integer.valueOf(sessionId));
            removeClientForSession(sessionId);
        }
    }

    @Override // com.xpeng.airplay.service.IXpAirplayManager
    public long getTetheringDataUsage() {
        int uid = Binder.getCallingUid();
        return getTotalDataUsage(uid);
    }

    @Override // android.os.Binder
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print(getServerState());
        pw.print(getServerInfo());
    }

    private void removeClientForSession(int sessionId) {
        Iterator<Map.Entry<String, Integer>> it = this.mClientSessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getValue().intValue() == sessionId) {
                it.remove();
            }
        }
    }

    private int allocateSessionId() {
        synchronized (this.mSessionLock) {
            int sessionId = 0;
            while (true) {
                int sessionId2 = this.mRandom.nextInt(2147483646) + 1;
                if (!this.mUsedSessionIds.get(sessionId2, false)) {
                    this.mUsedSessionIds.put(sessionId2, true);
                    return sessionId2;
                }
                int cnt = sessionId + 1;
                if (sessionId < 64) {
                    sessionId = cnt;
                } else {
                    return -1;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAllServers() {
        Log.d(TAG, "startAllServers(): server proxy size = " + this.mServerProxys.size());
        for (IServerProxy serverProxy : this.mServerProxys.values()) {
            if (serverProxy != null) {
                serverProxy.startServer();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopAllServers(boolean shutdown) {
        Log.d(TAG, "stopAllServers()");
        for (IServerProxy serverProxy : this.mServerProxys.values()) {
            if (serverProxy != null) {
                serverProxy.stopServer(shutdown);
            }
        }
    }

    private NetworkRequest buildWifiNetworkRequest() {
        NetworkRequest.Builder nb = new NetworkRequest.Builder().addCapability(12).addTransportType(1);
        return nb.build();
    }

    private void registerWifiNetworkCallback() {
        Log.d(TAG, "registerWifiNetworkCallback()");
        this.mConnectivityMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mActiveNetwork = new HashMap<>();
        this.mWifiNetworkCallback = new WifiNetworkCallback();
        this.mConnectivityMgr.registerNetworkCallback(buildWifiNetworkRequest(), this.mWifiNetworkCallback, this.mServiceHandler);
    }

    private void registerWifiSoftApCallback() {
        Log.d(TAG, "registerWifiSoftApCallback()");
        this.mWifiMgr = (WifiManager) this.mContext.getSystemService("wifi");
        this.mSoftApCallback = new WifiSoftApCallback();
        this.mWifiMgr.registerSoftApCallback(this.mSoftApCallback, this.mServiceHandler);
    }

    private void registerTetheringEventCallback() {
        Log.d(TAG, "registerTetheringEventCallback()");
        this.mTetherReceiver = new TetherStateReceiver();
        this.mContext.registerReceiver(this.mTetherReceiver, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"));
    }

    private String getServerName(boolean isSecond) {
        String namePrefix = getServerNamePrefix();
        return !isSecond ? this.mContext.getString(R.string.airplay_main_name, namePrefix) : this.mContext.getString(R.string.airplay_rear_name, namePrefix);
    }

    private String getServerNamePrefix() {
        String namePrefix = null;
        if (this.mAccountMgr != null && this.mUserAccount != null) {
            namePrefix = this.mAccountMgr.getUserData(this.mUserAccount, USER_ACCOUNT_NAME_KEY);
        }
        if (TextUtils.isEmpty(namePrefix)) {
            return Utils.getServerNamePrefix(new String(Build.MODEL).toUpperCase());
        }
        return namePrefix;
    }

    private void getCurrentAccount() {
        Account[] xpAccounts = this.mAccountMgr.getAccountsByType("com.xiaopeng.accountservice.ACCOUNT_TYPE_XP_VEHICLE");
        if (xpAccounts != null && xpAccounts.length > 0) {
            this.mUserAccount = xpAccounts[0];
        }
    }

    private String getRandomMacAddr() {
        byte[] macAddr = {-92, 4, 80, 0, 0, 0};
        byte[] randBytes = new byte[3];
        this.mRandom.nextBytes(randBytes);
        for (int i = 0; i < randBytes.length; i++) {
            macAddr[i + 3] = (byte) (randBytes[i] & 255);
        }
        String res = String.format("%02x:%02x:%02x:%02x:%02x:%02x", Byte.valueOf(macAddr[0]), Byte.valueOf(macAddr[1]), Byte.valueOf(macAddr[2]), Byte.valueOf(macAddr[3]), Byte.valueOf(macAddr[4]), Byte.valueOf(macAddr[5]));
        Log.d(TAG, "getRandomMacAddr(): " + res);
        return res;
    }

    private void createServerConfig(int screenId, String ipAddr, String serverName, String macAddr) {
        Log.d(TAG, "createServerConfig(): screenId = " + screenId + ",serverName = " + serverName);
        List<ServerConfig> configs = new ArrayList<>();
        IServerProxy proxy = this.mServerProxys.get(Integer.valueOf(screenId));
        if (proxy == null) {
            proxy = new XpAirplayServerProxy(this.mContext);
            this.mServerProxys.put(Integer.valueOf(screenId), proxy);
        }
        this.mServerNames.put(Integer.valueOf(screenId), serverName);
        for (Integer num : sServerTypes) {
            int type = num.intValue();
            configs.add(new ServerConfig(screenId, type, serverName, ipAddr, macAddr));
        }
        proxy.updateServerConfig(configs);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateServerConfig(WifiMode mode) {
        String ipAddr;
        String randMacAddr;
        Log.d(TAG, "updateServerConfig()");
        getCurrentAccount();
        if (mode == WifiMode.AP) {
            ipAddr = Utils.getWifiApIfaceIpAddress();
        } else {
            ipAddr = Utils.getWifiIpAddress(this.mContext);
        }
        String macAddr = Utils.getWifiMacAddress(mode);
        int i = 0;
        createServerConfig(0, ipAddr, getServerName(false), macAddr);
        if (Utils.hasRearDisplay()) {
            if (macAddr.equals(this.mSecondMacAddr)) {
                String str = this.mSecondMacAddr;
                while (true) {
                    randMacAddr = getRandomMacAddr();
                    if (!macAddr.equals(randMacAddr)) {
                        break;
                    }
                    int i2 = i + 1;
                    if (i >= 3) {
                        break;
                    }
                    i = i2;
                }
                createServerConfig(3, ipAddr, getServerName(true), randMacAddr);
            } else {
                createServerConfig(3, ipAddr, getServerName(true), this.mSecondMacAddr);
            }
        }
        synchronized (this.mServerConfigLock) {
            this.mServerConfigLock.notifyAll();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWifiMode(WifiMode mode) {
        Log.d(TAG, "updateWifiMode(): " + mode.toString());
        for (IServerProxy proxy : this.mServerProxys.values()) {
            if (proxy != null) {
                proxy.updateWifiMode(mode);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleServiceCreated() {
        Log.d(TAG, "handleServiceCreated()");
        registerWifiNetworkCallback();
        registerWifiSoftApCallback();
        registerTetheringEventCallback();
        if (this.mRearBtReceiver != null) {
            this.mContext.registerReceiver(this.mRearBtReceiver, new IntentFilter(REAR_BLUETOOTH_STATE_CHANGED));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleServiceDestroyed() {
        Log.d(TAG, "handleServiceDestroyed()");
        this.mConnectivityMgr.unregisterNetworkCallback(this.mWifiNetworkCallback);
        this.mWifiMgr.unregisterSoftApCallback(this.mSoftApCallback);
        this.mContext.unregisterReceiver(this.mTetherReceiver);
        if (this.mRearBtReceiver != null) {
            this.mContext.unregisterReceiver(this.mRearBtReceiver);
        }
        stopAllServers(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleClientUnbind(String pkgName) {
        Log.d(TAG, "handleClientUnbind(): " + pkgName);
        if (this.mClientSessions.get(pkgName) != null) {
            int sessionId = this.mClientSessions.get(pkgName).intValue();
            XpAirplaySession as = this.mAirplaySessions.get(Integer.valueOf(sessionId));
            if (as != null) {
                as.stop();
            }
            this.mAirplaySessions.remove(Integer.valueOf(sessionId));
            this.mClientSessions.remove(pkgName);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleTetheringChanged(String tetherIface) {
        Log.d(TAG, "handleTetheringChanged(): " + tetherIface);
        this.mActiveTetheringIface = tetherIface;
        this.mSoftApStartTime = System.currentTimeMillis();
        this.mTetheringDataUsage = getTetheringStats();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRearBtStateChanged(int state) {
        Log.d(TAG, "handleRearBtStateChanged(): " + state);
        if (Utils.hasRearDisplay()) {
            for (IServerProxy server : this.mServerProxys.values()) {
                if (server != null) {
                    server.onRearBtStateChanged(state == 2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAccountUpdated(Account account) {
        this.mUserAccountChanged.set(true);
        this.mUserAccount = account;
        String userName = null;
        if (this.mUserAccount != null) {
            userName = this.mAccountMgr.getUserData(this.mUserAccount, USER_ACCOUNT_NAME_KEY);
        }
        Log.d(TAG, "handleUserAccountUpdated(): " + userName);
        if (this.mWifiApEnabled || this.mWifiStaEnabled) {
            boolean hasActiveConnection = false;
            Iterator<IServerProxy> it = this.mServerProxys.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                IServerProxy proxy = it.next();
                if (proxy != null) {
                    hasActiveConnection = proxy.hasActiveConnection();
                    break;
                }
            }
            if (!hasActiveConnection) {
                stopAllServers(false);
                if (this.mWifiApEnabled && this.mWifiStaEnabled) {
                    updateServerConfig(WifiMode.DUAL);
                } else if (this.mWifiApEnabled) {
                    updateServerConfig(WifiMode.AP);
                } else if (this.mWifiStaEnabled) {
                    updateServerConfig(WifiMode.STA);
                }
                startAllServers();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public long getTetheringStats() {
        long dataUsage = 0;
        if (this.mNetworkMgr == null) {
            return 0L;
        }
        try {
            NetworkStats netstats = this.mNetworkMgr.getNetworkStatsTethering(1);
            dataUsage = netstats.getTotalBytes();
            Log.d(TAG, "getTetheringStats(): tethering consumed " + formatTetheringDataUsage(dataUsage));
            return dataUsage;
        } catch (Exception e) {
            Log.e(TAG, "fail to get tether stats");
            return dataUsage;
        }
    }

    private long getTotalDataUsage(int uid) {
        long appStats = getNetworkStatsForUid(uid);
        long tetherStats = getTetheringStats();
        long total = (appStats + tetherStats) - this.mTetheringDataUsage;
        Log.i(TAG, "getTotalDataUsage(): " + formatTetheringDataUsage(total));
        return total;
    }

    private long getNetworkStatsForUid(int uid) {
        Log.d(TAG, "getNetworkStatsForUid(): " + uid);
        long datausage = 0;
        if (this.mNetStatsMgr != null) {
            android.app.usage.NetworkStats netstats = this.mNetStatsMgr.queryDetailsForUid(9, null, this.mSoftApStartTime, System.currentTimeMillis(), uid);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (netstats != null && netstats.hasNextBucket()) {
                netstats.getNextBucket(bucket);
                datausage = datausage + bucket.getRxBytes() + bucket.getTxBytes();
            }
        }
        return datausage;
    }
}
