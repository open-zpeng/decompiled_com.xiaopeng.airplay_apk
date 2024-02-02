package com.xpeng.airplay.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
/* loaded from: classes.dex */
public class XpAirplayService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "XpAirplayService";
    private static final String NOTIFICATION_CHANNEL_NAME = "XpAirplay";
    private static final String NOTIFICATION_TEXT = "XpAirplay is going on";
    private static final String TAG = "XpAirplayService";
    private Notification mNotification;
    private XpAirplayServiceImpl mXpAirplay;

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.i("XpAirplayService", "onCreate()");
        HandlerThread thread = new HandlerThread("XpAirplayService");
        thread.start();
        this.mXpAirplay = new XpAirplayServiceImpl(getApplicationContext(), thread.getLooper());
        this.mXpAirplay.onCreate();
        this.mNotification = createNotification();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.d("XpAirplayService", "onBind(): action = " + intent.getAction());
        return this.mXpAirplay;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        String action = intent.getAction();
        Log.d("XpAirplayService", "onUnbind(): action = " + action);
        this.mXpAirplay.onClientUnbind(action);
        return false;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d("XpAirplayService", "onStartCommand(): action = " + intent.getAction());
        }
        startForeground(1, this.mNotification);
        return 1;
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d("XpAirplayService", "onDestroy()");
        if (this.mXpAirplay != null) {
            this.mXpAirplay.onDestroy();
        }
    }

    private Notification createNotification() {
        Log.d("XpAirplayService", "createNotification()");
        NotificationChannel nc = new NotificationChannel("XpAirplayService", NOTIFICATION_CHANNEL_NAME, 3);
        NotificationManager notificationMgr = (NotificationManager) getSystemService("notification");
        notificationMgr.createNotificationChannel(nc);
        Notification notification = new Notification.Builder(this, "XpAirplayService").setContentTitle(NOTIFICATION_CHANNEL_NAME).setContentText(NOTIFICATION_TEXT).build();
        return notification;
    }
}
