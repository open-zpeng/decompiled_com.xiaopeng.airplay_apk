package com.xpeng.airplay.dlna;

import android.util.Log;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
/* loaded from: classes.dex */
public final class DeviceRegistryListener extends DefaultRegistryListener {
    private static final String TAG = "DeviceRegistryListener";

    @Override // org.fourthline.cling.registry.DefaultRegistryListener, org.fourthline.cling.registry.RegistryListener
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        Log.d(TAG, "remoteDeviceAdded(): device = " + device);
    }

    @Override // org.fourthline.cling.registry.DefaultRegistryListener, org.fourthline.cling.registry.RegistryListener
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        Log.d(TAG, "remoteDeviceRemoved(): device = " + device);
    }

    @Override // org.fourthline.cling.registry.DefaultRegistryListener, org.fourthline.cling.registry.RegistryListener
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        Log.d(TAG, "localDeviceAdded(): device = " + device);
    }

    @Override // org.fourthline.cling.registry.DefaultRegistryListener, org.fourthline.cling.registry.RegistryListener
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        Log.d(TAG, "localDeviceRemoved(): device = " + device);
    }
}
