package org.fourthline.cling.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
/* loaded from: classes.dex */
public class NetworkUtils {
    private static final Logger log = Logger.getLogger(NetworkUtils.class.getName());

    public static NetworkInfo getConnectedNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            return networkInfo;
        }
        NetworkInfo networkInfo2 = connectivityManager.getNetworkInfo(1);
        if (networkInfo2 != null && networkInfo2.isAvailable() && networkInfo2.isConnected()) {
            return networkInfo2;
        }
        NetworkInfo networkInfo3 = connectivityManager.getNetworkInfo(0);
        if (networkInfo3 != null && networkInfo3.isAvailable() && networkInfo3.isConnected()) {
            return networkInfo3;
        }
        NetworkInfo networkInfo4 = connectivityManager.getNetworkInfo(6);
        if (networkInfo4 != null && networkInfo4.isAvailable() && networkInfo4.isConnected()) {
            return networkInfo4;
        }
        NetworkInfo networkInfo5 = connectivityManager.getNetworkInfo(9);
        if (networkInfo5 != null && networkInfo5.isAvailable() && networkInfo5.isConnected()) {
            return networkInfo5;
        }
        log.info("Could not find any connected network...");
        return null;
    }

    public static boolean isEthernet(NetworkInfo networkInfo) {
        return isNetworkType(networkInfo, 9);
    }

    public static boolean isWifi(NetworkInfo networkInfo) {
        return isNetworkType(networkInfo, 1) || ModelUtil.ANDROID_EMULATOR;
    }

    public static boolean isMobile(NetworkInfo networkInfo) {
        return isNetworkType(networkInfo, 0) || isNetworkType(networkInfo, 6);
    }

    public static boolean isNetworkType(NetworkInfo networkInfo, int type) {
        return networkInfo != null && networkInfo.getType() == type;
    }

    public static boolean isSSDPAwareNetwork(NetworkInfo networkInfo) {
        return isWifi(networkInfo) || isEthernet(networkInfo);
    }
}
