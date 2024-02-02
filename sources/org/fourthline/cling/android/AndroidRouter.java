package org.fourthline.cling.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.util.security.Constraint;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.RouterImpl;
import org.fourthline.cling.transport.spi.InitializationException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public class AndroidRouter extends RouterImpl {
    private static final Logger log = Logger.getLogger(Router.class.getName());
    protected BroadcastReceiver broadcastReceiver;
    private final Context context;
    protected WifiManager.MulticastLock multicastLock;
    protected NetworkInfo networkInfo;
    protected WifiManager.WifiLock wifiLock;
    private final WifiManager wifiManager;

    public AndroidRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Context context) throws InitializationException {
        super(configuration, protocolFactory);
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService("wifi");
        this.networkInfo = NetworkUtils.getConnectedNetworkInfo(context);
        if (!ModelUtil.ANDROID_EMULATOR) {
            this.broadcastReceiver = createConnectivityBroadcastReceiver();
            context.registerReceiver(this.broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    protected BroadcastReceiver createConnectivityBroadcastReceiver() {
        return new ConnectivityBroadcastReceiver();
    }

    @Override // org.fourthline.cling.transport.RouterImpl
    protected int getLockTimeoutMillis() {
        return 18000;
    }

    @Override // org.fourthline.cling.transport.RouterImpl, org.fourthline.cling.transport.Router
    public void shutdown() throws RouterException {
        super.shutdown();
        unregisterBroadcastReceiver();
    }

    @Override // org.fourthline.cling.transport.RouterImpl, org.fourthline.cling.transport.Router
    public boolean enable() throws RouterException {
        lock(this.writeLock);
        try {
            boolean enabled = super.enable();
            if (enabled && isWifi()) {
                setWiFiMulticastLock(true);
                setWifiLock(true);
            }
            return enabled;
        } finally {
            unlock(this.writeLock);
        }
    }

    @Override // org.fourthline.cling.transport.RouterImpl, org.fourthline.cling.transport.Router
    public boolean disable() throws RouterException {
        lock(this.writeLock);
        try {
            if (isWifi()) {
                setWiFiMulticastLock(false);
                setWifiLock(false);
            }
            return super.disable();
        } finally {
            unlock(this.writeLock);
        }
    }

    public NetworkInfo getNetworkInfo() {
        return this.networkInfo;
    }

    public boolean isMobile() {
        return NetworkUtils.isMobile(this.networkInfo);
    }

    public boolean isWifi() {
        return NetworkUtils.isWifi(this.networkInfo);
    }

    public boolean isEthernet() {
        return NetworkUtils.isEthernet(this.networkInfo);
    }

    public boolean enableWiFi() {
        log.info("Enabling WiFi...");
        try {
            return this.wifiManager.setWifiEnabled(true);
        } catch (Throwable t) {
            log.log(Level.WARNING, "SetWifiEnabled failed", t);
            return false;
        }
    }

    public void unregisterBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.context.unregisterReceiver(this.broadcastReceiver);
            this.broadcastReceiver = null;
        }
    }

    protected void setWiFiMulticastLock(boolean enable) {
        if (this.multicastLock == null) {
            this.multicastLock = this.wifiManager.createMulticastLock(getClass().getSimpleName());
        }
        if (enable) {
            if (this.multicastLock.isHeld()) {
                log.warning("WiFi multicast lock already acquired");
                return;
            }
            log.info("WiFi multicast lock acquired");
            this.multicastLock.acquire();
        } else if (this.multicastLock.isHeld()) {
            log.info("WiFi multicast lock released");
            this.multicastLock.release();
        } else {
            log.warning("WiFi multicast lock already released");
        }
    }

    protected void setWifiLock(boolean enable) {
        if (this.wifiLock == null) {
            this.wifiLock = this.wifiManager.createWifiLock(3, getClass().getSimpleName());
        }
        if (enable) {
            if (this.wifiLock.isHeld()) {
                log.warning("WiFi lock already acquired");
                return;
            }
            log.info("WiFi lock acquired");
            this.wifiLock.acquire();
        } else if (this.wifiLock.isHeld()) {
            log.info("WiFi lock released");
            this.wifiLock.release();
        } else {
            log.warning("WiFi lock already released");
        }
    }

    protected void onNetworkTypeChange(NetworkInfo oldNetwork, NetworkInfo newNetwork) throws RouterException {
        Logger logger = log;
        Object[] objArr = new Object[2];
        objArr[0] = oldNetwork == null ? "" : oldNetwork.getTypeName();
        objArr[1] = newNetwork == null ? Constraint.NONE : newNetwork.getTypeName();
        logger.info(String.format("Network type changed %s => %s", objArr));
        if (disable()) {
            Logger logger2 = log;
            Object[] objArr2 = new Object[1];
            objArr2[0] = oldNetwork == null ? Constraint.NONE : oldNetwork.getTypeName();
            logger2.info(String.format("Disabled router on network type change (old network: %s)", objArr2));
        }
        this.networkInfo = newNetwork;
        if (enable()) {
            Logger logger3 = log;
            Object[] objArr3 = new Object[1];
            objArr3[0] = newNetwork == null ? Constraint.NONE : newNetwork.getTypeName();
            logger3.info(String.format("Enabled router on network type change (new network: %s)", objArr3));
        }
    }

    protected void handleRouterExceptionOnNetworkTypeChange(RouterException ex) {
        Throwable cause = Exceptions.unwrap(ex);
        if (cause instanceof InterruptedException) {
            Logger logger = log;
            Level level = Level.INFO;
            logger.log(level, "Router was interrupted: " + ex, cause);
            return;
        }
        Logger logger2 = log;
        Level level2 = Level.WARNING;
        logger2.log(level2, "Router error on network change: " + ex, (Throwable) ex);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        ConnectivityBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            NetworkInfo newNetworkInfo;
            if (!intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                return;
            }
            displayIntentInfo(intent);
            NetworkInfo newNetworkInfo2 = NetworkUtils.getConnectedNetworkInfo(context);
            if (AndroidRouter.this.networkInfo != null && newNetworkInfo2 == null) {
                newNetworkInfo = newNetworkInfo2;
                for (int i = 1; i <= 3; i++) {
                    try {
                        Thread.sleep(1000L);
                        AndroidRouter.log.warning(String.format("%s => NONE network transition, waiting for new network... retry #%d", AndroidRouter.this.networkInfo.getTypeName(), Integer.valueOf(i)));
                        newNetworkInfo = NetworkUtils.getConnectedNetworkInfo(context);
                        if (newNetworkInfo != null) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                newNetworkInfo = newNetworkInfo2;
            }
            if (isSameNetworkType(AndroidRouter.this.networkInfo, newNetworkInfo)) {
                AndroidRouter.log.info("No actual network change... ignoring event!");
                return;
            }
            try {
                AndroidRouter.this.onNetworkTypeChange(AndroidRouter.this.networkInfo, newNetworkInfo);
            } catch (RouterException ex) {
                AndroidRouter.this.handleRouterExceptionOnNetworkTypeChange(ex);
            }
        }

        protected boolean isSameNetworkType(NetworkInfo network1, NetworkInfo network2) {
            if (network1 == null && network2 == null) {
                return true;
            }
            if (network1 != null && network2 != null && network1.getType() == network2.getType()) {
                return true;
            }
            return false;
        }

        protected void displayIntentInfo(Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra("noConnectivity", false);
            String reason = intent.getStringExtra("reason");
            boolean isFailover = intent.getBooleanExtra("isFailover", false);
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra("otherNetwork");
            AndroidRouter.log.info("Connectivity change detected...");
            Logger logger = AndroidRouter.log;
            logger.info("EXTRA_NO_CONNECTIVITY: " + noConnectivity);
            Logger logger2 = AndroidRouter.log;
            logger2.info("EXTRA_REASON: " + reason);
            Logger logger3 = AndroidRouter.log;
            logger3.info("EXTRA_IS_FAILOVER: " + isFailover);
            Logger logger4 = AndroidRouter.log;
            StringBuilder sb = new StringBuilder();
            sb.append("EXTRA_NETWORK_INFO: ");
            sb.append(currentNetworkInfo == null ? "none" : currentNetworkInfo);
            logger4.info(sb.toString());
            Logger logger5 = AndroidRouter.log;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("EXTRA_OTHER_NETWORK_INFO: ");
            sb2.append(otherNetworkInfo == null ? "none" : otherNetworkInfo);
            logger5.info(sb2.toString());
            Logger logger6 = AndroidRouter.log;
            logger6.info("EXTRA_EXTRA_INFO: " + intent.getStringExtra("extraInfo"));
        }
    }
}
