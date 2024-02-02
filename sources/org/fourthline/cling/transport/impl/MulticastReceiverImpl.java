package org.fourthline.cling.transport.impl;

import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.logging.Logger;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
/* loaded from: classes.dex */
public class MulticastReceiverImpl implements MulticastReceiver<MulticastReceiverConfigurationImpl> {
    private static final String PROP_DLNA_SEARCH_OPT = "sys.xp.dlna_search_opt";
    private static Logger log = Logger.getLogger(MulticastReceiver.class.getName());
    protected final MulticastReceiverConfigurationImpl configuration;
    protected DatagramProcessor datagramProcessor;
    protected InetSocketAddress multicastAddress;
    protected NetworkInterface multicastInterface;
    protected NetworkAddressFactory networkAddressFactory;
    protected Router router;
    protected MulticastSocket socket;

    public MulticastReceiverImpl(MulticastReceiverConfigurationImpl configuration) {
        this.configuration = configuration;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.transport.spi.MulticastReceiver
    public MulticastReceiverConfigurationImpl getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.transport.spi.MulticastReceiver
    public synchronized void init(NetworkInterface networkInterface, Router router, NetworkAddressFactory networkAddressFactory, DatagramProcessor datagramProcessor) throws InitializationException {
        this.router = router;
        this.networkAddressFactory = networkAddressFactory;
        this.datagramProcessor = datagramProcessor;
        this.multicastInterface = networkInterface;
        try {
            Logger logger = log;
            logger.info("Creating wildcard socket (for receiving multicast datagrams) on port: " + this.configuration.getPort());
            this.multicastAddress = new InetSocketAddress(this.configuration.getGroup(), this.configuration.getPort());
            this.socket = new MulticastSocket(this.configuration.getPort());
            this.socket.setReuseAddress(true);
            this.socket.setReceiveBufferSize(32768);
            Logger logger2 = log;
            logger2.info("Joining multicast group: " + this.multicastAddress + " on network interface: " + this.multicastInterface.getDisplayName());
            this.socket.joinGroup(this.multicastAddress, this.multicastInterface);
        } catch (Exception ex) {
            throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.MulticastReceiver
    public synchronized void stop() {
        if (this.socket != null && !this.socket.isClosed()) {
            try {
                log.fine("Leaving multicast group");
                this.socket.leaveGroup(this.multicastAddress, this.multicastInterface);
            } catch (Exception ex) {
                Logger logger = log;
                logger.fine("Could not leave multicast group: " + ex);
            }
            this.socket.close();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:39:0x00cc A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:42:0x002c A[SYNTHETIC] */
    @Override // java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void run() {
        /*
            Method dump skipped, instructions count: 286
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.transport.impl.MulticastReceiverImpl.run():void");
    }

    private int toInt(byte[] addr) {
        return ((addr[0] & 255) << 24) | ((addr[1] & 255) << 16) | ((addr[2] & 255) << 8) | (addr[3] & 255);
    }

    private boolean compareAddress(byte[] addr1, byte[] addr2, int prefix) {
        int a1 = toInt(addr1);
        int a2 = toInt(addr2);
        int netmask = 65535 << (32 - prefix);
        return (a1 & netmask) == (a2 & netmask);
    }
}
