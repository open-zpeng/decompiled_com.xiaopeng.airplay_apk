package com.xpeng.airplay.dlna;

import com.xpeng.airplay.service.Utils;
import java.net.NetworkInterface;
import java.util.Locale;
import org.fourthline.cling.android.AndroidNetworkAddressFactory;
/* loaded from: classes.dex */
public final class DLNANetworkAddressFactory extends AndroidNetworkAddressFactory {
    private static final String TAG = "DLNANetworkAddressFactory";

    public DLNANetworkAddressFactory(int listenPort) {
        super(listenPort);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl
    public boolean isUsableNetworkInterface(NetworkInterface ni) throws Exception {
        if (ni.isUp() && (ni.getName().toLowerCase(Locale.ROOT).startsWith("eth") || ni.getName().toLowerCase(Locale.ROOT).startsWith("usb"))) {
            return false;
        }
        String ignoreIface = Utils.getDlnaIgnoreIface();
        if (ni.isUp() && ni.getName().toLowerCase(Locale.ROOT).equals(ignoreIface)) {
            return false;
        }
        return super.isUsableNetworkInterface(ni);
    }
}
