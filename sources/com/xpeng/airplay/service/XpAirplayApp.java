package com.xpeng.airplay.service;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
/* loaded from: classes.dex */
public class XpAirplayApp extends Application {
    private static final String TAG = "XpAirplayApp";

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        Intent it = new Intent(this, XpAirplayService.class);
        startForegroundService(it);
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onLowMemory() {
        Log.w(TAG, "onLowMemory()");
    }
}
