package org.fourthline.cling.transport;

import android.util.Log;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.protocol.ProtocolCreationException;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.NoNetworkException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.seamless.util.Exceptions;
@ApplicationScoped
/* loaded from: classes.dex */
public class RouterImpl implements Router {
    private static Logger log = Logger.getLogger(Router.class.getName());
    protected UpnpServiceConfiguration configuration;
    protected volatile boolean enabled;
    protected NetworkAddressFactory networkAddressFactory;
    protected ProtocolFactory protocolFactory;
    protected StreamClient streamClient;
    private final String TAG = "RouterImpl";
    protected ReentrantReadWriteLock routerLock = new ReentrantReadWriteLock(true);
    protected Lock readLock = this.routerLock.readLock();
    protected Lock writeLock = this.routerLock.writeLock();
    protected final Map<NetworkInterface, MulticastReceiver> multicastReceivers = new HashMap();
    protected final Map<InetAddress, DatagramIO> datagramIOs = new HashMap();
    protected final Map<InetAddress, StreamServer> streamServers = new HashMap();

    protected RouterImpl() {
    }

    @Inject
    public RouterImpl(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory) {
        Logger logger = log;
        logger.info("Creating Router: " + getClass().getName());
        this.configuration = configuration;
        this.protocolFactory = protocolFactory;
    }

    public boolean enable(@Observes @Default EnableRouter event) throws RouterException {
        return enable();
    }

    public boolean disable(@Observes @Default DisableRouter event) throws RouterException {
        return disable();
    }

    @Override // org.fourthline.cling.transport.Router
    public UpnpServiceConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.transport.Router
    public ProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

    @Override // org.fourthline.cling.transport.Router
    public boolean enable() throws RouterException {
        lock(this.writeLock);
        try {
            if (!this.enabled) {
                try {
                    log.fine("Starting networking services...");
                    this.networkAddressFactory = getConfiguration().createNetworkAddressFactory();
                    startInterfaceBasedTransports(this.networkAddressFactory.getNetworkInterfaces());
                    startAddressBasedTransports(this.networkAddressFactory.getBindAddresses());
                    if (!this.networkAddressFactory.hasUsableNetwork()) {
                        throw new NoNetworkException("No usable network interface and/or addresses available, check the log for errors.");
                    }
                    this.streamClient = getConfiguration().createStreamClient();
                    this.enabled = true;
                    return true;
                } catch (InitializationException ex) {
                    handleStartFailure(ex);
                }
            }
            return false;
        } finally {
            unlock(this.writeLock);
        }
    }

    @Override // org.fourthline.cling.transport.Router
    public boolean disable() throws RouterException {
        lock(this.writeLock);
        try {
            if (this.enabled) {
                Log.i("RouterImpl", "Disabling network services...");
                if (this.streamClient != null) {
                    log.fine("Stopping stream client connection management/pool");
                    this.streamClient.stop();
                    this.streamClient = null;
                }
                for (Map.Entry<InetAddress, StreamServer> entry : this.streamServers.entrySet()) {
                    Logger logger = log;
                    logger.fine("Stopping stream server on address: " + entry.getKey());
                    entry.getValue().stop();
                }
                this.streamServers.clear();
                for (Map.Entry<NetworkInterface, MulticastReceiver> entry2 : this.multicastReceivers.entrySet()) {
                    Logger logger2 = log;
                    logger2.fine("Stopping multicast receiver on interface: " + entry2.getKey().getDisplayName());
                    entry2.getValue().stop();
                }
                this.multicastReceivers.clear();
                for (Map.Entry<InetAddress, DatagramIO> entry3 : this.datagramIOs.entrySet()) {
                    Logger logger3 = log;
                    logger3.fine("Stopping datagram I/O on address: " + entry3.getKey());
                    entry3.getValue().stop();
                }
                this.datagramIOs.clear();
                this.networkAddressFactory = null;
                this.enabled = false;
                return true;
            }
            return false;
        } finally {
            unlock(this.writeLock);
        }
    }

    @Override // org.fourthline.cling.transport.Router
    public void shutdown() throws RouterException {
        disable();
    }

    @Override // org.fourthline.cling.transport.Router
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override // org.fourthline.cling.transport.Router
    public void handleStartFailure(InitializationException ex) throws InitializationException {
        if (ex instanceof NoNetworkException) {
            log.info("Unable to initialize network router, no network found.");
            return;
        }
        Logger logger = log;
        logger.severe("Unable to initialize network router: " + ex);
        Logger logger2 = log;
        logger2.severe("Cause: " + Exceptions.unwrap(ex));
    }

    @Override // org.fourthline.cling.transport.Router
    public List<NetworkAddress> getActiveStreamServers(InetAddress preferredAddress) throws RouterException {
        StreamServer preferredServer;
        lock(this.readLock);
        try {
            if (!this.enabled || this.streamServers.size() <= 0) {
                return Collections.EMPTY_LIST;
            }
            List<NetworkAddress> streamServerAddresses = new ArrayList<>();
            if (preferredAddress != null && (preferredServer = this.streamServers.get(preferredAddress)) != null) {
                streamServerAddresses.add(new NetworkAddress(preferredAddress, preferredServer.getPort(), this.networkAddressFactory.getHardwareAddress(preferredAddress)));
                return streamServerAddresses;
            }
            for (Map.Entry<InetAddress, StreamServer> entry : this.streamServers.entrySet()) {
                byte[] hardwareAddress = this.networkAddressFactory.getHardwareAddress(entry.getKey());
                streamServerAddresses.add(new NetworkAddress(entry.getKey(), entry.getValue().getPort(), hardwareAddress));
            }
            return streamServerAddresses;
        } finally {
            unlock(this.readLock);
        }
    }

    @Override // org.fourthline.cling.transport.Router
    public void received(IncomingDatagramMessage msg) {
        if (!this.enabled) {
            Logger logger = log;
            logger.fine("Router disabled, ignoring incoming message: " + msg);
            return;
        }
        try {
            ReceivingAsync protocol = getProtocolFactory().createReceivingAsync(msg);
            if (protocol == null) {
                if (log.isLoggable(Level.FINEST)) {
                    Logger logger2 = log;
                    logger2.finest("No protocol, ignoring received message: " + msg);
                    return;
                }
                return;
            }
            if (log.isLoggable(Level.FINE)) {
                Logger logger3 = log;
                logger3.fine("Received asynchronous message: " + msg);
            }
            getConfiguration().getAsyncProtocolExecutor().execute(protocol);
        } catch (ProtocolCreationException ex) {
            Logger logger4 = log;
            logger4.warning("Handling received datagram failed - " + Exceptions.unwrap(ex).toString());
        }
    }

    @Override // org.fourthline.cling.transport.Router
    public void received(UpnpStream stream) {
        if (!this.enabled) {
            Logger logger = log;
            logger.fine("Router disabled, ignoring incoming: " + stream);
            return;
        }
        Logger logger2 = log;
        logger2.fine("Received synchronous stream: " + stream);
        getConfiguration().getSyncProtocolExecutorService().execute(stream);
    }

    @Override // org.fourthline.cling.transport.Router
    public void send(OutgoingDatagramMessage msg) throws RouterException {
        lock(this.readLock);
        try {
            if (this.enabled) {
                for (DatagramIO datagramIO : this.datagramIOs.values()) {
                    datagramIO.send(msg);
                }
            } else {
                Logger logger = log;
                logger.fine("Router disabled, not sending datagram: " + msg);
            }
        } finally {
            unlock(this.readLock);
        }
    }

    @Override // org.fourthline.cling.transport.Router
    public StreamResponseMessage send(StreamRequestMessage msg) throws RouterException {
        lock(this.readLock);
        try {
            if (!this.enabled) {
                Logger logger = log;
                logger.fine("Router disabled, not sending stream request: " + msg);
                return null;
            } else if (this.streamClient == null) {
                Logger logger2 = log;
                logger2.fine("No StreamClient available, not sending: " + msg);
                return null;
            } else {
                Logger logger3 = log;
                logger3.fine("Sending via TCP unicast stream: " + msg);
                try {
                    return this.streamClient.sendRequest(msg);
                } catch (InterruptedException ex) {
                    throw new RouterException("Sending stream request was interrupted", ex);
                }
            }
        } finally {
            unlock(this.readLock);
        }
    }

    @Override // org.fourthline.cling.transport.Router
    public void broadcast(byte[] bytes) throws RouterException {
        lock(this.readLock);
        try {
            if (this.enabled) {
                for (Map.Entry<InetAddress, DatagramIO> entry : this.datagramIOs.entrySet()) {
                    InetAddress broadcast = this.networkAddressFactory.getBroadcastAddress(entry.getKey());
                    if (broadcast != null) {
                        Log.i("RouterImpl", "Sending UDP datagram to broadcast address: " + broadcast.getHostAddress());
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
                        entry.getValue().send(packet);
                    }
                }
            } else {
                Logger logger = log;
                logger.fine("Router disabled, not broadcasting bytes: " + bytes.length);
            }
        } finally {
            unlock(this.readLock);
        }
    }

    protected void startInterfaceBasedTransports(Iterator<NetworkInterface> interfaces) throws InitializationException {
        while (interfaces.hasNext()) {
            NetworkInterface networkInterface = interfaces.next();
            MulticastReceiver multicastReceiver = getConfiguration().createMulticastReceiver(this.networkAddressFactory);
            if (multicastReceiver == null) {
                Logger logger = log;
                logger.info("Configuration did not create a MulticastReceiver for: " + networkInterface);
            } else {
                try {
                    if (log.isLoggable(Level.FINE)) {
                        Logger logger2 = log;
                        logger2.fine("Init multicast receiver on interface: " + networkInterface.getDisplayName());
                    }
                    multicastReceiver.init(networkInterface, this, this.networkAddressFactory, getConfiguration().getDatagramProcessor());
                    this.multicastReceivers.put(networkInterface, multicastReceiver);
                } catch (InitializationException ex) {
                    throw ex;
                }
            }
        }
        for (Map.Entry<NetworkInterface, MulticastReceiver> entry : this.multicastReceivers.entrySet()) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger3 = log;
                logger3.fine("Starting multicast receiver on interface: " + entry.getKey().getDisplayName());
            }
            getConfiguration().getMulticastReceiverExecutor().execute(entry.getValue());
        }
    }

    protected void startAddressBasedTransports(Iterator<InetAddress> addresses) throws InitializationException {
        while (addresses.hasNext()) {
            InetAddress address = addresses.next();
            StreamServer streamServer = getConfiguration().createStreamServer(this.networkAddressFactory);
            if (streamServer == null) {
                Logger logger = log;
                logger.info("Configuration did not create a StreamServer for: " + address);
            } else {
                try {
                    if (log.isLoggable(Level.FINE)) {
                        Logger logger2 = log;
                        logger2.fine("Init stream server on address: " + address);
                    }
                    streamServer.init(address, this);
                    this.streamServers.put(address, streamServer);
                } catch (InitializationException ex) {
                    Throwable cause = Exceptions.unwrap(ex);
                    if (cause instanceof BindException) {
                        Logger logger3 = log;
                        logger3.warning("Failed to init StreamServer: " + cause);
                        if (log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "Initialization exception root cause", cause);
                        }
                        Logger logger4 = log;
                        logger4.warning("Removing unusable address: " + address);
                        addresses.remove();
                    } else {
                        throw ex;
                    }
                }
            }
            DatagramIO datagramIO = getConfiguration().createDatagramIO(this.networkAddressFactory);
            if (datagramIO == null) {
                Logger logger5 = log;
                logger5.info("Configuration did not create a StreamServer for: " + address);
            } else {
                try {
                    if (log.isLoggable(Level.FINE)) {
                        Logger logger6 = log;
                        logger6.fine("Init datagram I/O on address: " + address);
                    }
                    datagramIO.init(address, this, getConfiguration().getDatagramProcessor());
                    this.datagramIOs.put(address, datagramIO);
                } catch (InitializationException ex2) {
                    throw ex2;
                }
            }
        }
        for (Map.Entry<InetAddress, StreamServer> entry : this.streamServers.entrySet()) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger7 = log;
                logger7.fine("Starting stream server on address: " + entry.getKey());
            }
            getConfiguration().getStreamServerExecutorService().execute(entry.getValue());
        }
    }

    protected void lock(Lock lock, int timeoutMilliseconds) throws RouterException {
        try {
            Logger logger = log;
            logger.finest("Trying to obtain lock with timeout milliseconds '" + timeoutMilliseconds + "': " + lock.getClass().getSimpleName());
            if (lock.tryLock(timeoutMilliseconds, TimeUnit.MILLISECONDS)) {
                Logger logger2 = log;
                logger2.finest("Acquired router lock: " + lock.getClass().getSimpleName());
                return;
            }
            throw new RouterException("Router wasn't available exclusively after waiting " + timeoutMilliseconds + "ms, lock failed: " + lock.getClass().getSimpleName());
        } catch (InterruptedException ex) {
            throw new RouterException("Interruption while waiting for exclusive access: " + lock.getClass().getSimpleName(), ex);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void lock(Lock lock) throws RouterException {
        lock(lock, getLockTimeoutMillis());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void unlock(Lock lock) {
        Logger logger = log;
        logger.finest("Releasing router lock: " + lock.getClass().getSimpleName());
        lock.unlock();
    }

    protected int getLockTimeoutMillis() {
        return 6000;
    }
}
