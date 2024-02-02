package com.xpeng.airplay.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
/* loaded from: classes.dex */
public final class NsdHelper {
    private static final String TAG = "NsdHelper";
    private static NsdHelper sInstance;
    private final NsdManager mNsdMgr;

    public static NsdHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NsdHelper(context);
        }
        return sInstance;
    }

    private NsdHelper(Context context) {
        this.mNsdMgr = (NsdManager) context.getSystemService("servicediscovery");
    }

    public void registerService(NsdServiceInfo nsi, NsdManager.RegistrationListener listener) {
        Log.d(TAG, "registerService(): " + nsi);
        if (this.mNsdMgr != null) {
            try {
                this.mNsdMgr.registerService(nsi, 1, listener);
            } catch (Exception e) {
            }
        }
    }

    public void unregisterService(NsdManager.RegistrationListener listener) {
        Log.d(TAG, "unregisterService()");
        if (this.mNsdMgr != null) {
            try {
                this.mNsdMgr.unregisterService(listener);
            } catch (Exception e) {
            }
        }
    }
}
