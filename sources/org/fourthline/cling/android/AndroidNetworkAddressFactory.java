package org.fourthline.cling.android;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.spi.InitializationException;
/* loaded from: classes.dex */
public class AndroidNetworkAddressFactory extends NetworkAddressFactoryImpl {
    private static final Logger log = Logger.getLogger(AndroidUpnpServiceConfiguration.class.getName());

    public AndroidNetworkAddressFactory(int streamListenPort) {
        super(streamListenPort);
    }

    @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl
    protected boolean requiresNetworkInterface() {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl
    public boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) {
        Field field0;
        Object target;
        boolean result = super.isUsableAddress(networkInterface, address);
        if (result) {
            String hostName = address.getHostAddress();
            try {
                try {
                    Field field02 = InetAddress.class.getDeclaredField("holder");
                    field02.setAccessible(true);
                    target = field02.get(address);
                    field0 = target.getClass().getDeclaredField("hostName");
                } catch (NoSuchFieldException e) {
                    field0 = InetAddress.class.getDeclaredField("hostName");
                    target = address;
                }
                if (field0 == null || target == null || hostName == null) {
                    return false;
                }
                field0.setAccessible(true);
                field0.set(target, hostName);
            } catch (Exception ex) {
                Logger logger = log;
                Level level = Level.SEVERE;
                logger.log(level, "Failed injecting hostName to work around Android InetAddress DNS bug: " + address, (Throwable) ex);
                return false;
            }
        }
        return result;
    }

    @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl, org.fourthline.cling.transport.spi.NetworkAddressFactory
    public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress) {
        for (InetAddress localAddress : getInetAddresses(networkInterface)) {
            if (isIPv6 && (localAddress instanceof Inet6Address)) {
                return localAddress;
            }
            if (!isIPv6 && (localAddress instanceof Inet4Address)) {
                return localAddress;
            }
        }
        throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl
    public void discoverNetworkInterfaces() throws InitializationException {
        try {
            super.discoverNetworkInterfaces();
        } catch (Exception ex) {
            Logger logger = log;
            logger.warning("Exception while enumerating network interfaces, trying once more: " + ex);
            super.discoverNetworkInterfaces();
        }
    }
}
