package org.fourthline.cling.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.UDN;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class RemoteItems extends RegistryItems<RemoteDevice, RemoteGENASubscription> {
    private static Logger log = Logger.getLogger(Registry.class.getName());

    /* JADX INFO: Access modifiers changed from: package-private */
    public RemoteItems(RegistryImpl registry) {
        super(registry);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void add(final RemoteDevice device) {
        Integer maxAgeSeconds;
        if (update(device.getIdentity())) {
            log.fine("Ignoring addition, device already registered: " + device);
            return;
        }
        Resource[] resources = getResources(device);
        for (Resource deviceResource : resources) {
            log.fine("Validating remote device resource; " + deviceResource);
            if (this.registry.getResource(deviceResource.getPathQuery()) != null) {
                throw new RegistrationException("URI namespace conflict with already registered resource: " + deviceResource);
            }
        }
        for (Resource validatedResource : resources) {
            this.registry.addResource(validatedResource);
            log.fine("Added remote device resource: " + validatedResource);
        }
        UDN udn = device.getIdentity().getUdn();
        if (this.registry.getConfiguration().getRemoteDeviceMaxAgeSeconds() != null) {
            maxAgeSeconds = this.registry.getConfiguration().getRemoteDeviceMaxAgeSeconds();
        } else {
            maxAgeSeconds = device.getIdentity().getMaxAgeSeconds();
        }
        RegistryItem item = new RegistryItem(udn, device, maxAgeSeconds.intValue());
        log.fine("Adding hydrated remote device to registry with " + item.getExpirationDetails().getMaxAgeSeconds() + " seconds expiration: " + device);
        getDeviceItems().add(item);
        if (log.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append("-------------------------- START Registry Namespace -----------------------------------\n");
            for (Resource resource : this.registry.getResources()) {
                sb.append(resource);
                sb.append("\n");
            }
            sb.append("-------------------------- END Registry Namespace -----------------------------------");
            log.finest(sb.toString());
        }
        log.fine("Completely hydrated remote device graph available, calling listeners: " + device);
        for (final RegistryListener listener : this.registry.getListeners()) {
            this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.RemoteItems.1
                @Override // java.lang.Runnable
                public void run() {
                    listener.remoteDeviceAdded(RemoteItems.this.registry, device);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean update(RemoteDeviceIdentity rdIdentity) {
        Integer maxAgeSeconds;
        for (LocalDevice localDevice : this.registry.getLocalDevices()) {
            if (localDevice.findDevice(rdIdentity.getUdn()) != null) {
                log.fine("Ignoring update, a local device graph contains UDN");
                return true;
            }
        }
        RemoteDevice registeredRemoteDevice = get(rdIdentity.getUdn(), false);
        if (registeredRemoteDevice != null) {
            if (!registeredRemoteDevice.isRoot()) {
                Logger logger = log;
                logger.fine("Updating root device of embedded: " + registeredRemoteDevice);
                registeredRemoteDevice = registeredRemoteDevice.getRoot();
            }
            UDN udn = registeredRemoteDevice.getIdentity().getUdn();
            if (this.registry.getConfiguration().getRemoteDeviceMaxAgeSeconds() != null) {
                maxAgeSeconds = this.registry.getConfiguration().getRemoteDeviceMaxAgeSeconds();
            } else {
                maxAgeSeconds = rdIdentity.getMaxAgeSeconds();
            }
            final RegistryItem<UDN, RemoteDevice> item = new RegistryItem<>(udn, registeredRemoteDevice, maxAgeSeconds.intValue());
            Logger logger2 = log;
            logger2.fine("Updating expiration of: " + registeredRemoteDevice);
            getDeviceItems().remove(item);
            getDeviceItems().add(item);
            Logger logger3 = log;
            logger3.fine("Remote device updated, calling listeners: " + registeredRemoteDevice);
            for (final RegistryListener listener : this.registry.getListeners()) {
                this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.RemoteItems.2
                    @Override // java.lang.Runnable
                    public void run() {
                        listener.remoteDeviceUpdated(RemoteItems.this.registry, (RemoteDevice) item.getItem());
                    }
                });
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public boolean remove(RemoteDevice remoteDevice) {
        return remove(remoteDevice, false);
    }

    boolean remove(RemoteDevice remoteDevice, boolean shuttingDown) throws RegistrationException {
        Resource[] resources;
        final RemoteDevice registeredDevice = (RemoteDevice) get(remoteDevice.getIdentity().getUdn(), true);
        if (registeredDevice == null) {
            return false;
        }
        log.fine("Removing remote device from registry: " + remoteDevice);
        for (Resource deviceResource : getResources(registeredDevice)) {
            if (this.registry.removeResource(deviceResource)) {
                log.fine("Unregistered resource: " + deviceResource);
            }
        }
        Iterator<RegistryItem<String, RemoteGENASubscription>> it = getSubscriptionItems().iterator();
        while (it.hasNext()) {
            final RegistryItem<String, RemoteGENASubscription> outgoingSubscription = it.next();
            UDN subscriptionForUDN = outgoingSubscription.getItem().getService().getDevice().getIdentity().getUdn();
            if (subscriptionForUDN.equals(registeredDevice.getIdentity().getUdn())) {
                log.fine("Removing outgoing subscription: " + outgoingSubscription.getKey());
                it.remove();
                if (!shuttingDown) {
                    this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.RemoteItems.3
                        @Override // java.lang.Runnable
                        public void run() {
                            ((RemoteGENASubscription) outgoingSubscription.getItem()).end(CancelReason.DEVICE_WAS_REMOVED, null);
                        }
                    });
                }
            }
        }
        if (!shuttingDown) {
            for (final RegistryListener listener : this.registry.getListeners()) {
                this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.RemoteItems.4
                    @Override // java.lang.Runnable
                    public void run() {
                        listener.remoteDeviceRemoved(RemoteItems.this.registry, registeredDevice);
                    }
                });
            }
        }
        getDeviceItems().remove(new RegistryItem(registeredDevice.getIdentity().getUdn()));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void removeAll() {
        removeAll(false);
    }

    void removeAll(boolean shuttingDown) {
        RemoteDevice[] allDevices = (RemoteDevice[]) get().toArray(new RemoteDevice[get().size()]);
        for (RemoteDevice device : allDevices) {
            remove(device, shuttingDown);
        }
    }

    void start() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void maintain() {
        if (getDeviceItems().isEmpty()) {
            return;
        }
        Map<UDN, RemoteDevice> expiredRemoteDevices = new HashMap<>();
        for (RegistryItem<UDN, RemoteDevice> remoteItem : getDeviceItems()) {
            if (log.isLoggable(Level.FINEST)) {
                Logger logger = log;
                logger.finest("Device '" + remoteItem.getItem() + "' expires in seconds: " + remoteItem.getExpirationDetails().getSecondsUntilExpiration());
            }
            if (remoteItem.getExpirationDetails().hasExpired(false)) {
                expiredRemoteDevices.put(remoteItem.getKey(), remoteItem.getItem());
            }
        }
        for (RemoteDevice remoteDevice : expiredRemoteDevices.values()) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger2 = log;
                logger2.fine("Removing expired: " + remoteDevice);
            }
            remove(remoteDevice);
        }
        Set<RemoteGENASubscription> expiredOutgoingSubscriptions = new HashSet<>();
        for (RegistryItem<String, RemoteGENASubscription> item : getSubscriptionItems()) {
            if (item.getExpirationDetails().hasExpired(true)) {
                expiredOutgoingSubscriptions.add(item.getItem());
            }
        }
        for (RemoteGENASubscription subscription : expiredOutgoingSubscriptions) {
            if (log.isLoggable(Level.FINEST)) {
                Logger logger3 = log;
                logger3.fine("Renewing outgoing subscription: " + subscription);
            }
            renewOutgoingSubscription(subscription);
        }
    }

    public void resume() {
        log.fine("Updating remote device expiration timestamps on resume");
        List<RemoteDeviceIdentity> toUpdate = new ArrayList<>();
        for (RegistryItem<UDN, RemoteDevice> remoteItem : getDeviceItems()) {
            toUpdate.add(remoteItem.getItem().getIdentity());
        }
        for (RemoteDeviceIdentity identity : toUpdate) {
            update(identity);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // org.fourthline.cling.registry.RegistryItems
    public void shutdown() {
        log.fine("Cancelling all outgoing subscriptions to remote devices during shutdown");
        List<RemoteGENASubscription> remoteSubscriptions = new ArrayList<>();
        for (RegistryItem<String, RemoteGENASubscription> item : getSubscriptionItems()) {
            remoteSubscriptions.add(item.getItem());
        }
        for (RemoteGENASubscription remoteSubscription : remoteSubscriptions) {
            this.registry.getProtocolFactory().createSendingUnsubscribe(remoteSubscription).run();
        }
        log.fine("Removing all remote devices from registry during shutdown");
        removeAll(true);
    }

    protected void renewOutgoingSubscription(RemoteGENASubscription subscription) {
        this.registry.executeAsyncProtocol(this.registry.getProtocolFactory().createSendingRenewal(subscription));
    }
}
