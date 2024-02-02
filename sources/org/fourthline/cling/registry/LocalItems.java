package org.fourthline.cling.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.SendingAsync;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class LocalItems extends RegistryItems<LocalDevice, LocalGENASubscription> {
    private static Logger log = Logger.getLogger(Registry.class.getName());
    protected Map<UDN, DiscoveryOptions> discoveryOptions;
    protected long lastAliveIntervalTimestamp;
    protected Map<LocalDevice, SendingNotificationAlive> notificationAlives;
    protected Random randomGenerator;

    /* JADX INFO: Access modifiers changed from: package-private */
    public LocalItems(RegistryImpl registry) {
        super(registry);
        this.discoveryOptions = new HashMap();
        this.lastAliveIntervalTimestamp = 0L;
        this.notificationAlives = new HashMap();
        this.randomGenerator = new Random();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setDiscoveryOptions(UDN udn, DiscoveryOptions options) {
        if (options != null) {
            this.discoveryOptions.put(udn, options);
        } else {
            this.discoveryOptions.remove(udn);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public DiscoveryOptions getDiscoveryOptions(UDN udn) {
        return this.discoveryOptions.get(udn);
    }

    protected boolean isAdvertised(UDN udn) {
        return getDiscoveryOptions(udn) == null || getDiscoveryOptions(udn).isAdvertised();
    }

    protected boolean isByeByeBeforeFirstAlive(UDN udn) {
        return getDiscoveryOptions(udn) != null && getDiscoveryOptions(udn).isByeByeBeforeFirstAlive();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void add(LocalDevice localDevice) throws RegistrationException {
        add(localDevice, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void add(final LocalDevice localDevice, DiscoveryOptions options) throws RegistrationException {
        Resource[] resources;
        this.notificationAlives.put(localDevice, this.registry.getProtocolFactory().createSendingNotificationAlive(localDevice));
        setDiscoveryOptions(localDevice.getIdentity().getUdn(), options);
        if (this.registry.getDevice(localDevice.getIdentity().getUdn(), false) != null) {
            log.fine("Ignoring addition, device already registered: " + localDevice);
            return;
        }
        log.fine("Adding local device to registry: " + localDevice);
        for (Resource deviceResource : getResources(localDevice)) {
            if (this.registry.getResource(deviceResource.getPathQuery()) != null) {
                throw new RegistrationException("URI namespace conflict with already registered resource: " + deviceResource);
            }
            this.registry.addResource(deviceResource);
            log.fine("Registered resource: " + deviceResource);
        }
        log.fine("Adding item to registry with expiration in seconds: " + localDevice.getIdentity().getMaxAgeSeconds());
        RegistryItem<UDN, LocalDevice> localItem = new RegistryItem<>(localDevice.getIdentity().getUdn(), localDevice, localDevice.getIdentity().getMaxAgeSeconds().intValue());
        getDeviceItems().add(localItem);
        log.fine("Registered local device: " + localItem);
        if (isByeByeBeforeFirstAlive(localItem.getKey())) {
            advertiseByebye(localDevice, true);
        }
        if (isAdvertised(localItem.getKey())) {
            advertiseAlive(localDevice);
        }
        for (final RegistryListener listener : this.registry.getListeners()) {
            this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.LocalItems.1
                @Override // java.lang.Runnable
                public void run() {
                    listener.localDeviceAdded(LocalItems.this.registry, localDevice);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public Collection<LocalDevice> get() {
        Set<LocalDevice> c = new HashSet<>();
        for (RegistryItem<UDN, LocalDevice> item : getDeviceItems()) {
            c.add(item.getItem());
        }
        return Collections.unmodifiableCollection(c);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public boolean remove(LocalDevice localDevice) throws RegistrationException {
        return remove(localDevice, false);
    }

    boolean remove(final LocalDevice localDevice, boolean shuttingDown) throws RegistrationException {
        Resource[] resources;
        this.notificationAlives.remove(localDevice);
        LocalDevice registeredDevice = get(localDevice.getIdentity().getUdn(), true);
        if (registeredDevice == null) {
            return false;
        }
        log.fine("Removing local device from registry: " + localDevice);
        setDiscoveryOptions(localDevice.getIdentity().getUdn(), null);
        getDeviceItems().remove(new RegistryItem(localDevice.getIdentity().getUdn()));
        for (Resource deviceResource : getResources(localDevice)) {
            if (this.registry.removeResource(deviceResource)) {
                log.fine("Unregistered resource: " + deviceResource);
            }
        }
        Iterator<RegistryItem<String, LocalGENASubscription>> it = getSubscriptionItems().iterator();
        while (it.hasNext()) {
            final RegistryItem<String, LocalGENASubscription> incomingSubscription = it.next();
            UDN subscriptionForUDN = incomingSubscription.getItem().getService().getDevice().getIdentity().getUdn();
            if (subscriptionForUDN.equals(registeredDevice.getIdentity().getUdn())) {
                log.fine("Removing incoming subscription: " + incomingSubscription.getKey());
                it.remove();
                if (!shuttingDown) {
                    this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.LocalItems.2
                        @Override // java.lang.Runnable
                        public void run() {
                            ((LocalGENASubscription) incomingSubscription.getItem()).end(CancelReason.DEVICE_WAS_REMOVED);
                        }
                    });
                }
            }
        }
        if (isAdvertised(localDevice.getIdentity().getUdn())) {
            advertiseByebye(localDevice, !shuttingDown);
        }
        if (!shuttingDown) {
            for (final RegistryListener listener : this.registry.getListeners()) {
                this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.LocalItems.3
                    @Override // java.lang.Runnable
                    public void run() {
                        listener.localDeviceRemoved(LocalItems.this.registry, localDevice);
                    }
                });
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void removeAll() {
        removeAll(false);
    }

    void removeAll(boolean shuttingDown) {
        LocalDevice[] allDevices = (LocalDevice[]) get().toArray(new LocalDevice[get().size()]);
        for (LocalDevice device : allDevices) {
            remove(device, shuttingDown);
        }
    }

    public void advertiseLocalDevices() {
        Iterator it = this.deviceItems.iterator();
        while (it.hasNext()) {
            RegistryItem<UDN, LocalDevice> localItem = (RegistryItem) it.next();
            if (isAdvertised(localItem.getKey())) {
                advertiseAlive(localItem.getItem());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void maintain() {
        if (getDeviceItems().isEmpty()) {
            return;
        }
        Set<RegistryItem<UDN, LocalDevice>> expiredLocalItems = new HashSet<>();
        int aliveIntervalMillis = this.registry.getConfiguration().getAliveIntervalMillis();
        if (aliveIntervalMillis > 0) {
            long now = System.currentTimeMillis();
            if (now - this.lastAliveIntervalTimestamp > aliveIntervalMillis) {
                this.lastAliveIntervalTimestamp = now;
                for (RegistryItem<UDN, LocalDevice> localItem : getDeviceItems()) {
                    if (isAdvertised(localItem.getKey())) {
                        Logger logger = log;
                        logger.finer("Flooding advertisement of local item: " + localItem);
                        expiredLocalItems.add(localItem);
                    }
                }
            }
        } else {
            this.lastAliveIntervalTimestamp = 0L;
            for (RegistryItem<UDN, LocalDevice> localItem2 : getDeviceItems()) {
                if (isAdvertised(localItem2.getKey()) && localItem2.getExpirationDetails().hasExpired(true)) {
                    Logger logger2 = log;
                    logger2.finer("Local item has expired: " + localItem2);
                    expiredLocalItems.add(localItem2);
                }
            }
        }
        for (RegistryItem<UDN, LocalDevice> expiredLocalItem : expiredLocalItems) {
            Logger logger3 = log;
            logger3.fine("Refreshing local device advertisement: " + expiredLocalItem.getItem());
            advertiseAlive(expiredLocalItem.getItem());
            expiredLocalItem.getExpirationDetails().stampLastRefresh();
        }
        Set<RegistryItem<String, LocalGENASubscription>> expiredIncomingSubscriptions = new HashSet<>();
        for (RegistryItem<String, LocalGENASubscription> item : getSubscriptionItems()) {
            if (item.getExpirationDetails().hasExpired(false)) {
                expiredIncomingSubscriptions.add(item);
            }
        }
        for (RegistryItem<String, LocalGENASubscription> subscription : expiredIncomingSubscriptions) {
            Logger logger4 = log;
            logger4.fine("Removing expired: " + subscription);
            removeSubscription(subscription.getItem());
            subscription.getItem().end(CancelReason.EXPIRED);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void shutdown() {
        log.fine("Clearing all registered subscriptions to local devices during shutdown");
        getSubscriptionItems().clear();
        log.fine("Removing all local devices from registry during shutdown");
        removeAll(true);
    }

    protected void advertiseAlive(final LocalDevice localDevice) {
        this.registry.executeAsyncProtocol(new Runnable() { // from class: org.fourthline.cling.registry.LocalItems.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    LocalItems.log.finer("Sleeping some milliseconds to avoid flooding the network with ALIVE msgs");
                    Thread.sleep(LocalItems.this.randomGenerator.nextInt(100));
                } catch (InterruptedException ex) {
                    Logger logger = LocalItems.log;
                    logger.severe("Background execution interrupted: " + ex.getMessage());
                }
                SendingNotificationAlive alive = LocalItems.this.notificationAlives.get(localDevice);
                if (alive != null) {
                    alive.run();
                } else {
                    LocalItems.this.registry.getProtocolFactory().createSendingNotificationAlive(localDevice).run();
                }
            }
        });
    }

    protected void advertiseByebye(LocalDevice localDevice, boolean asynchronous) {
        SendingAsync prot = this.registry.getProtocolFactory().createSendingNotificationByebye(localDevice);
        if (asynchronous) {
            this.registry.executeAsyncProtocol(prot);
        } else {
            prot.run();
        }
    }
}
