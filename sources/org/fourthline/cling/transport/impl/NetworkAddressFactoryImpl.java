package org.fourthline.cling.transport.impl;

import com.xpeng.airplay.service.NsdConstants;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.Constants;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.NoNetworkException;
import org.seamless.util.Iterators;
/* loaded from: classes.dex */
public class NetworkAddressFactoryImpl implements NetworkAddressFactory {
    public static final int DEFAULT_TCP_HTTP_LISTEN_PORT = 0;
    private static Logger log = Logger.getLogger(NetworkAddressFactoryImpl.class.getName());
    protected final List<InetAddress> bindAddresses;
    protected final List<NetworkInterface> networkInterfaces;
    protected int streamListenPort;
    protected final Set<String> useAddresses;
    protected final Set<String> useInterfaces;

    public NetworkAddressFactoryImpl() throws InitializationException {
        this(0);
    }

    public NetworkAddressFactoryImpl(int streamListenPort) throws InitializationException {
        this.useInterfaces = new HashSet();
        this.useAddresses = new HashSet();
        this.networkInterfaces = new ArrayList();
        this.bindAddresses = new ArrayList();
        System.setProperty("java.net.preferIPv4Stack", NsdConstants.AIRPLAY_TXT_VALUE_DA);
        String useInterfacesString = System.getProperty(NetworkAddressFactory.SYSTEM_PROPERTY_NET_IFACES);
        if (useInterfacesString != null) {
            String[] userInterfacesStrings = useInterfacesString.split(",");
            this.useInterfaces.addAll(Arrays.asList(userInterfacesStrings));
        }
        String useAddressesString = System.getProperty(NetworkAddressFactory.SYSTEM_PROPERTY_NET_ADDRESSES);
        if (useAddressesString != null) {
            String[] useAddressesStrings = useAddressesString.split(",");
            this.useAddresses.addAll(Arrays.asList(useAddressesStrings));
        }
        discoverNetworkInterfaces();
        discoverBindAddresses();
        if (this.networkInterfaces.size() == 0 || this.bindAddresses.size() == 0) {
            log.warning("No usable network interface or addresses found");
            if (requiresNetworkInterface()) {
                throw new NoNetworkException("Could not discover any usable network interfaces and/or addresses");
            }
        }
        this.streamListenPort = streamListenPort;
    }

    protected boolean requiresNetworkInterface() {
        return true;
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public void logInterfaceInformation() {
        synchronized (this.networkInterfaces) {
            if (this.networkInterfaces.isEmpty()) {
                log.info("No network interface to display!");
                return;
            }
            for (NetworkInterface networkInterface : this.networkInterfaces) {
                try {
                    logInterfaceInformation(networkInterface);
                } catch (SocketException ex) {
                    log.log(Level.WARNING, "Exception while logging network interface information", (Throwable) ex);
                }
            }
        }
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public InetAddress getMulticastGroup() {
        try {
            return InetAddress.getByName(Constants.IPV4_UPNP_MULTICAST_GROUP);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public int getMulticastPort() {
        return Constants.UPNP_MULTICAST_PORT;
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public int getStreamListenPort() {
        return this.streamListenPort;
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public Iterator<NetworkInterface> getNetworkInterfaces() {
        return new Iterators.Synchronized<NetworkInterface>(this.networkInterfaces) { // from class: org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl.1
            @Override // org.seamless.util.Iterators.Synchronized
            protected void synchronizedRemove(int index) {
                synchronized (NetworkAddressFactoryImpl.this.networkInterfaces) {
                    NetworkAddressFactoryImpl.this.networkInterfaces.remove(index);
                }
            }
        };
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public Iterator<InetAddress> getBindAddresses() {
        return new Iterators.Synchronized<InetAddress>(this.bindAddresses) { // from class: org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl.2
            @Override // org.seamless.util.Iterators.Synchronized
            protected void synchronizedRemove(int index) {
                synchronized (NetworkAddressFactoryImpl.this.bindAddresses) {
                    NetworkAddressFactoryImpl.this.bindAddresses.remove(index);
                }
            }
        };
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public boolean hasUsableNetwork() {
        return this.networkInterfaces.size() > 0 && this.bindAddresses.size() > 0;
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public byte[] getHardwareAddress(InetAddress inetAddress) {
        try {
            NetworkInterface iface = NetworkInterface.getByInetAddress(inetAddress);
            if (iface != null) {
                return iface.getHardwareAddress();
            }
            return null;
        } catch (Throwable ex) {
            Logger logger = log;
            Level level = Level.WARNING;
            logger.log(level, "Cannot get hardware address for: " + inetAddress, ex);
            return null;
        }
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public InetAddress getBroadcastAddress(InetAddress inetAddress) {
        synchronized (this.networkInterfaces) {
            for (NetworkInterface iface : this.networkInterfaces) {
                for (InterfaceAddress interfaceAddress : getInterfaceAddresses(iface)) {
                    if (interfaceAddress != null && interfaceAddress.getAddress().equals(inetAddress)) {
                        return interfaceAddress.getBroadcast();
                    }
                }
            }
            return null;
        }
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public Short getAddressNetworkPrefixLength(InetAddress inetAddress) {
        synchronized (this.networkInterfaces) {
            for (NetworkInterface iface : this.networkInterfaces) {
                for (InterfaceAddress interfaceAddress : getInterfaceAddresses(iface)) {
                    if (interfaceAddress != null && interfaceAddress.getAddress().equals(inetAddress)) {
                        short prefix = interfaceAddress.getNetworkPrefixLength();
                        if (prefix <= 0 || prefix >= 32) {
                            return null;
                        }
                        return Short.valueOf(prefix);
                    }
                }
            }
            return null;
        }
    }

    @Override // org.fourthline.cling.transport.spi.NetworkAddressFactory
    public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress) {
        InetAddress localIPInSubnet = getBindAddressInSubnetOf(remoteAddress);
        if (localIPInSubnet != null) {
            return localIPInSubnet;
        }
        Logger logger = log;
        logger.finer("Could not find local bind address in same subnet as: " + remoteAddress.getHostAddress());
        for (InetAddress interfaceAddress : getInetAddresses(networkInterface)) {
            if (isIPv6 && (interfaceAddress instanceof Inet6Address)) {
                return interfaceAddress;
            }
            if (!isIPv6 && (interfaceAddress instanceof Inet4Address)) {
                return interfaceAddress;
            }
        }
        throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());
    }

    protected List<InterfaceAddress> getInterfaceAddresses(NetworkInterface networkInterface) {
        return networkInterface.getInterfaceAddresses();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<InetAddress> getInetAddresses(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses());
    }

    protected InetAddress getBindAddressInSubnetOf(InetAddress inetAddress) {
        synchronized (this.networkInterfaces) {
            for (NetworkInterface iface : this.networkInterfaces) {
                for (InterfaceAddress ifaceAddress : getInterfaceAddresses(iface)) {
                    synchronized (this.bindAddresses) {
                        if (ifaceAddress != null && this.bindAddresses.contains(ifaceAddress.getAddress())) {
                            if (isInSubnet(inetAddress.getAddress(), ifaceAddress.getAddress().getAddress(), ifaceAddress.getNetworkPrefixLength())) {
                                return ifaceAddress.getAddress();
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    protected boolean isInSubnet(byte[] ip, byte[] network, short prefix) {
        if (ip.length == network.length && prefix / 8 <= ip.length) {
            short prefix2 = prefix;
            int i = 0;
            while (prefix2 >= 8 && i < ip.length) {
                if (ip[i] != network[i]) {
                    return false;
                }
                i++;
                prefix2 = (short) (prefix2 - 8);
            }
            if (i == ip.length) {
                return true;
            }
            byte mask = (byte) (~((1 << (8 - prefix2)) - 1));
            return (ip[i] & mask) == (network[i] & mask);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void discoverNetworkInterfaces() throws InitializationException {
        try {
            Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            Iterator it = Collections.list(interfaceEnumeration).iterator();
            while (it.hasNext()) {
                NetworkInterface iface = (NetworkInterface) it.next();
                Logger logger = log;
                logger.finer("Analyzing network interface: " + iface.getDisplayName());
                if (isUsableNetworkInterface(iface)) {
                    Logger logger2 = log;
                    logger2.fine("Discovered usable network interface: " + iface.getDisplayName());
                    synchronized (this.networkInterfaces) {
                        this.networkInterfaces.add(iface);
                    }
                } else {
                    Logger logger3 = log;
                    logger3.finer("Ignoring non-usable network interface: " + iface.getDisplayName());
                }
            }
        } catch (Exception ex) {
            throw new InitializationException("Could not not analyze local network interfaces: " + ex, ex);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isUsableNetworkInterface(NetworkInterface iface) throws Exception {
        if (!iface.isUp()) {
            Logger logger = log;
            logger.finer("Skipping network interface (down): " + iface.getDisplayName());
            return false;
        } else if (getInetAddresses(iface).size() == 0) {
            Logger logger2 = log;
            logger2.finer("Skipping network interface without bound IP addresses: " + iface.getDisplayName());
            return false;
        } else if (iface.getName().toLowerCase(Locale.ROOT).startsWith("vmnet") || (iface.getDisplayName() != null && iface.getDisplayName().toLowerCase(Locale.ROOT).contains("vmnet"))) {
            Logger logger3 = log;
            logger3.finer("Skipping network interface (VMWare): " + iface.getDisplayName());
            return false;
        } else if (iface.getName().toLowerCase(Locale.ROOT).startsWith("vnic")) {
            Logger logger4 = log;
            logger4.finer("Skipping network interface (Parallels): " + iface.getDisplayName());
            return false;
        } else if (iface.getName().toLowerCase(Locale.ROOT).startsWith("vboxnet")) {
            Logger logger5 = log;
            logger5.finer("Skipping network interface (Virtual Box): " + iface.getDisplayName());
            return false;
        } else if (iface.getName().toLowerCase(Locale.ROOT).contains("virtual")) {
            Logger logger6 = log;
            logger6.finer("Skipping network interface (named '*virtual*'): " + iface.getDisplayName());
            return false;
        } else if (iface.getName().toLowerCase(Locale.ROOT).startsWith("ppp")) {
            Logger logger7 = log;
            logger7.finer("Skipping network interface (PPP): " + iface.getDisplayName());
            return false;
        } else if (iface.isLoopback()) {
            Logger logger8 = log;
            logger8.finer("Skipping network interface (ignoring loopback): " + iface.getDisplayName());
            return false;
        } else if (this.useInterfaces.size() > 0 && !this.useInterfaces.contains(iface.getName())) {
            Logger logger9 = log;
            logger9.finer("Skipping unwanted network interface (-Dorg.fourthline.cling.network.useInterfaces): " + iface.getName());
            return false;
        } else if (!iface.supportsMulticast()) {
            Logger logger10 = log;
            logger10.warning("Network interface may not be multicast capable: " + iface.getDisplayName());
            return true;
        } else {
            return true;
        }
    }

    protected void discoverBindAddresses() throws InitializationException {
        try {
            synchronized (this.networkInterfaces) {
                Iterator<NetworkInterface> it = this.networkInterfaces.iterator();
                while (it.hasNext()) {
                    NetworkInterface networkInterface = it.next();
                    Logger logger = log;
                    logger.finer("Discovering addresses of interface: " + networkInterface.getDisplayName());
                    int usableAddresses = 0;
                    for (InetAddress inetAddress : getInetAddresses(networkInterface)) {
                        if (inetAddress == null) {
                            Logger logger2 = log;
                            logger2.warning("Network has a null address: " + networkInterface.getDisplayName());
                        } else if (isUsableAddress(networkInterface, inetAddress)) {
                            Logger logger3 = log;
                            logger3.fine("Discovered usable network interface address: " + inetAddress.getHostAddress());
                            usableAddresses++;
                            synchronized (this.bindAddresses) {
                                this.bindAddresses.add(inetAddress);
                            }
                        } else {
                            Logger logger4 = log;
                            logger4.finer("Ignoring non-usable network interface address: " + inetAddress.getHostAddress());
                        }
                    }
                    if (usableAddresses == 0) {
                        Logger logger5 = log;
                        logger5.finer("Network interface has no usable addresses, removing: " + networkInterface.getDisplayName());
                        it.remove();
                    }
                }
            }
        } catch (Exception ex) {
            throw new InitializationException("Could not not analyze local network interfaces: " + ex, ex);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) {
        if (!(address instanceof Inet4Address)) {
            Logger logger = log;
            logger.finer("Skipping unsupported non-IPv4 address: " + address);
            return false;
        } else if (address.isLoopbackAddress()) {
            Logger logger2 = log;
            logger2.finer("Skipping loopback address: " + address);
            return false;
        } else if (this.useAddresses.size() > 0 && !this.useAddresses.contains(address.getHostAddress())) {
            Logger logger3 = log;
            logger3.finer("Skipping unwanted address: " + address);
            return false;
        } else {
            return true;
        }
    }

    protected void logInterfaceInformation(NetworkInterface networkInterface) throws SocketException {
        log.info("---------------------------------------------------------------------------------");
        log.info(String.format("Interface display name: %s", networkInterface.getDisplayName()));
        if (networkInterface.getParent() != null) {
            log.info(String.format("Parent Info: %s", networkInterface.getParent()));
        }
        log.info(String.format("Name: %s", networkInterface.getName()));
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        Iterator it = Collections.list(inetAddresses).iterator();
        while (it.hasNext()) {
            InetAddress inetAddress = (InetAddress) it.next();
            log.info(String.format("InetAddress: %s", inetAddress));
        }
        List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
        for (InterfaceAddress interfaceAddress : interfaceAddresses) {
            if (interfaceAddress == null) {
                log.warning("Skipping null InterfaceAddress!");
            } else {
                log.info(" Interface Address");
                Logger logger = log;
                logger.info("  Address: " + interfaceAddress.getAddress());
                Logger logger2 = log;
                logger2.info("  Broadcast: " + interfaceAddress.getBroadcast());
                Logger logger3 = log;
                logger3.info("  Prefix length: " + ((int) interfaceAddress.getNetworkPrefixLength()));
            }
        }
        Enumeration<NetworkInterface> subIfs = networkInterface.getSubInterfaces();
        Iterator it2 = Collections.list(subIfs).iterator();
        while (it2.hasNext()) {
            NetworkInterface subIf = (NetworkInterface) it2.next();
            if (subIf == null) {
                log.warning("Skipping null NetworkInterface sub-interface");
            } else {
                log.info(String.format("\tSub Interface Display name: %s", subIf.getDisplayName()));
                log.info(String.format("\tSub Interface Name: %s", subIf.getName()));
            }
        }
        log.info(String.format("Up? %s", Boolean.valueOf(networkInterface.isUp())));
        log.info(String.format("Loopback? %s", Boolean.valueOf(networkInterface.isLoopback())));
        log.info(String.format("PointToPoint? %s", Boolean.valueOf(networkInterface.isPointToPoint())));
        log.info(String.format("Supports multicast? %s", Boolean.valueOf(networkInterface.supportsMulticast())));
        log.info(String.format("Virtual? %s", Boolean.valueOf(networkInterface.isVirtual())));
        log.info(String.format("Hardware address: %s", Arrays.toString(networkInterface.getHardwareAddress())));
        log.info(String.format("MTU: %s", Integer.valueOf(networkInterface.getMTU())));
    }
}
