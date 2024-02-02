package com.xpeng.airplay.dlna;

import android.content.Context;
import android.util.Log;
import javax.enterprise.inject.spi.ObserverMethod;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidRouter;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.RegistrationException;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
/* loaded from: classes.dex */
public final class XpengUpnpServiceImpl {
    private static final String TAG = "XpengUpnpServiceImpl";
    private static XpengUpnpServiceImpl sInstance;
    private final Context mContext;
    private final UpnpService mUpnpService;

    private XpengUpnpServiceImpl(Context context) {
        this.mContext = context;
        UpnpServiceConfiguration serviceConfig = new AndroidUpnpServiceConfiguration() { // from class: com.xpeng.airplay.dlna.XpengUpnpServiceImpl.1
            @Override // org.fourthline.cling.android.AndroidUpnpServiceConfiguration, org.fourthline.cling.DefaultUpnpServiceConfiguration
            protected NetworkAddressFactory createNetworkAddressFactory(int listenPort) {
                return new DLNANetworkAddressFactory(listenPort);
            }

            @Override // org.fourthline.cling.android.AndroidUpnpServiceConfiguration, org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
            public int getRegistryMaintenanceIntervalMillis() {
                return 3000;
            }

            @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
            public int getAliveIntervalMillis() {
                return ObserverMethod.DEFAULT_PRIORITY;
            }

            @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
            public ServiceType[] getExclusiveServiceTypes() {
                return null;
            }
        };
        this.mUpnpService = new UpnpServiceImpl(serviceConfig, new RegistryListener[0]) { // from class: com.xpeng.airplay.dlna.XpengUpnpServiceImpl.2
            @Override // org.fourthline.cling.UpnpServiceImpl
            protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
                return new AndroidRouter(getConfiguration(), protocolFactory, XpengUpnpServiceImpl.this.mContext);
            }

            @Override // org.fourthline.cling.UpnpServiceImpl, org.fourthline.cling.UpnpService
            public synchronized void shutdown() {
                Log.d(XpengUpnpServiceImpl.TAG, "shutdown()");
                ((AndroidRouter) getRouter()).unregisterBroadcastReceiver();
                super.shutdown(false);
            }
        };
        this.mUpnpService.getRegistry().addListener(new DeviceRegistryListener());
        ((AndroidRouter) this.mUpnpService.getRouter()).unregisterBroadcastReceiver();
    }

    public static XpengUpnpServiceImpl getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new XpengUpnpServiceImpl(context);
        }
        return sInstance;
    }

    public UpnpService getUpnpService() {
        return this.mUpnpService;
    }

    public void disableRouter() {
        try {
            this.mUpnpService.getRouter().disable();
            this.mUpnpService.getRegistry().pause();
        } catch (RouterException re) {
            Log.e(TAG, "fail to disable router");
            re.printStackTrace();
        }
    }

    public void shutdown() {
        Log.i(TAG, "shutdown()");
        this.mUpnpService.shutdown();
        this.mUpnpService.getRegistry().shutdown();
    }

    public boolean addDevice(LocalDevice device) {
        try {
            this.mUpnpService.getRegistry().addDevice(device);
            this.mUpnpService.getRegistry().setDiscoveryOptions(device.getIdentity().getUdn(), new DiscoveryOptions(true));
            this.mUpnpService.getRouter().enable();
            this.mUpnpService.getRegistry().resume();
            Log.i(TAG, "addDevice(): " + device.getDetails().getFriendlyName());
            return true;
        } catch (RegistrationException | RouterException e) {
            Log.e(TAG, "fail to add device");
            e.printStackTrace();
            return false;
        }
    }

    public void removeDevice(LocalDevice device) {
        this.mUpnpService.getRegistry().setDiscoveryOptions(device.getIdentity().getUdn(), new DiscoveryOptions(false));
        this.mUpnpService.getRegistry().removeDevice(device);
    }
}
