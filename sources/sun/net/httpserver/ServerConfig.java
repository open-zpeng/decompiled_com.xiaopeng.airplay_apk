package sun.net.httpserver;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetLongAction;
/* loaded from: classes.dex */
class ServerConfig {
    static boolean debug;
    static int defaultClockTick = 10000;
    static long defaultReadTimeout = 20;
    static long defaultWriteTimeout = 60;
    static long defaultIdleInterval = 300;
    static long defaultSelCacheTimeout = 120;
    static int defaultMaxIdleConnections = 200;
    static long defaultDrainAmount = 65536;
    static long idleInterval = ((Long) AccessController.doPrivileged((PrivilegedAction<Object>) new GetLongAction("sun.net.httpserver.idleInterval", defaultIdleInterval))).longValue() * 1000;
    static int clockTick = ((Integer) AccessController.doPrivileged((PrivilegedAction<Object>) new GetIntegerAction("sun.net.httpserver.clockTick", defaultClockTick))).intValue();
    static int maxIdleConnections = ((Integer) AccessController.doPrivileged((PrivilegedAction<Object>) new GetIntegerAction("sun.net.httpserver.maxIdleConnections", defaultMaxIdleConnections))).intValue();
    static long readTimeout = ((Long) AccessController.doPrivileged((PrivilegedAction<Object>) new GetLongAction("sun.net.httpserver.readTimeout", defaultReadTimeout))).longValue() * 1000;
    static long selCacheTimeout = ((Long) AccessController.doPrivileged((PrivilegedAction<Object>) new GetLongAction("sun.net.httpserver.selCacheTimeout", defaultSelCacheTimeout))).longValue() * 1000;
    static long writeTimeout = ((Long) AccessController.doPrivileged((PrivilegedAction<Object>) new GetLongAction("sun.net.httpserver.writeTimeout", defaultWriteTimeout))).longValue() * 1000;
    static long drainAmount = ((Long) AccessController.doPrivileged((PrivilegedAction<Object>) new GetLongAction("sun.net.httpserver.drainAmount", defaultDrainAmount))).longValue();

    ServerConfig() {
    }

    static {
        debug = false;
        debug = ((Boolean) AccessController.doPrivileged((PrivilegedAction<Object>) new GetBooleanAction("sun.net.httpserver.debug"))).booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getReadTimeout() {
        return readTimeout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getSelCacheTimeout() {
        return selCacheTimeout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean debugEnabled() {
        return debug;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getIdleInterval() {
        return idleInterval;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getClockTick() {
        return clockTick;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getWriteTimeout() {
        return writeTimeout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static long getDrainAmount() {
        return drainAmount;
    }
}
