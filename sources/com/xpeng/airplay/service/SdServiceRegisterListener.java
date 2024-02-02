package com.xpeng.airplay.service;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
/* loaded from: classes.dex */
public class SdServiceRegisterListener implements NsdManager.RegistrationListener {
    public static final String TAG = "SdServiceRegisterListener";

    @Override // android.net.nsd.NsdManager.RegistrationListener
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(TAG, "onRegistrationFailed(): " + serviceInfo.getServiceName());
    }

    @Override // android.net.nsd.NsdManager.RegistrationListener
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.w(TAG, "onUnregistrationFailed(): " + serviceInfo.getServiceName());
    }

    @Override // android.net.nsd.NsdManager.RegistrationListener
    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
        Log.i(TAG, "onServiceRegistered(): " + serviceInfo.getServiceName());
    }

    @Override // android.net.nsd.NsdManager.RegistrationListener
    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
        Log.i(TAG, "onServiceUnregistered(): " + serviceInfo.getServiceName());
    }
}
