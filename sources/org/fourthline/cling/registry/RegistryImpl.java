package org.fourthline.cling.registry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ProtocolFactory;
@ApplicationScoped
/* loaded from: classes.dex */
public class RegistryImpl implements Registry {
    private static Logger log = Logger.getLogger(Registry.class.getName());
    protected RegistryMaintainer registryMaintainer;
    protected UpnpService upnpService;
    protected final Set<RemoteGENASubscription> pendingSubscriptionsLock = new HashSet();
    private final Executor aliveExecutor = Executors.newSingleThreadExecutor();
    protected final Set<RegistryListener> registryListeners = new HashSet();
    protected final Set<RegistryItem<URI, Resource>> resourceItems = new HashSet();
    protected final List<Runnable> pendingExecutions = new ArrayList();
    protected final RemoteItems remoteItems = new RemoteItems(this);
    protected final LocalItems localItems = new LocalItems(this);

    public RegistryImpl() {
    }

    @Inject
    public RegistryImpl(UpnpService upnpService) {
        Logger logger = log;
        logger.fine("Creating Registry: " + getClass().getName());
        this.upnpService = upnpService;
        log.fine("Starting registry background maintenance...");
        this.registryMaintainer = createRegistryMaintainer();
        if (this.registryMaintainer != null) {
            getConfiguration().getRegistryMaintainerExecutor().execute(this.registryMaintainer);
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public UpnpService getUpnpService() {
        return this.upnpService;
    }

    @Override // org.fourthline.cling.registry.Registry
    public UpnpServiceConfiguration getConfiguration() {
        return getUpnpService().getConfiguration();
    }

    @Override // org.fourthline.cling.registry.Registry
    public ProtocolFactory getProtocolFactory() {
        return getUpnpService().getProtocolFactory();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public RegistryMaintainer createRegistryMaintainer() {
        return new RegistryMaintainer(this, getConfiguration().getRegistryMaintenanceIntervalMillis());
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addListener(RegistryListener listener) {
        this.registryListeners.add(listener);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void removeListener(RegistryListener listener) {
        this.registryListeners.remove(listener);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<RegistryListener> getListeners() {
        return Collections.unmodifiableCollection(this.registryListeners);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean notifyDiscoveryStart(final RemoteDevice device) {
        if (getUpnpService().getRegistry().getRemoteDevice(device.getIdentity().getUdn(), true) != null) {
            Logger logger = log;
            logger.finer("Not notifying listeners, already registered: " + device);
            return false;
        }
        for (final RegistryListener listener : getListeners()) {
            getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.RegistryImpl.1
                @Override // java.lang.Runnable
                public void run() {
                    listener.remoteDeviceDiscoveryStarted(RegistryImpl.this, device);
                }
            });
        }
        return true;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void notifyDiscoveryFailure(final RemoteDevice device, final Exception ex) {
        for (final RegistryListener listener : getListeners()) {
            getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.registry.RegistryImpl.2
                @Override // java.lang.Runnable
                public void run() {
                    listener.remoteDeviceDiscoveryFailed(RegistryImpl.this, device, ex);
                }
            });
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addDevice(LocalDevice localDevice) {
        this.localItems.add(localDevice);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addDevice(LocalDevice localDevice, DiscoveryOptions options) {
        this.localItems.add(localDevice, options);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void setDiscoveryOptions(UDN udn, DiscoveryOptions options) {
        this.localItems.setDiscoveryOptions(udn, options);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized DiscoveryOptions getDiscoveryOptions(UDN udn) {
        return this.localItems.getDiscoveryOptions(udn);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addDevice(RemoteDevice remoteDevice) {
        this.remoteItems.add(remoteDevice);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean update(RemoteDeviceIdentity rdIdentity) {
        return this.remoteItems.update(rdIdentity);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean removeDevice(LocalDevice localDevice) {
        return this.localItems.remove(localDevice);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean removeDevice(RemoteDevice remoteDevice) {
        return this.remoteItems.remove(remoteDevice);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void removeAllLocalDevices() {
        this.localItems.removeAll();
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void removeAllRemoteDevices() {
        this.remoteItems.removeAll();
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean removeDevice(UDN udn) {
        Device device = getDevice(udn, true);
        if (device != null && (device instanceof LocalDevice)) {
            return removeDevice((LocalDevice) device);
        } else if (device != null && (device instanceof RemoteDevice)) {
            return removeDevice((RemoteDevice) device);
        } else {
            return false;
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Device getDevice(UDN udn, boolean rootOnly) {
        Device device = this.localItems.get(udn, rootOnly);
        if (device != null) {
            return device;
        }
        Device device2 = this.remoteItems.get(udn, rootOnly);
        if (device2 != null) {
            return device2;
        }
        return null;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized LocalDevice getLocalDevice(UDN udn, boolean rootOnly) {
        return this.localItems.get(udn, rootOnly);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized RemoteDevice getRemoteDevice(UDN udn, boolean rootOnly) {
        return this.remoteItems.get(udn, rootOnly);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<LocalDevice> getLocalDevices() {
        return Collections.unmodifiableCollection(this.localItems.get());
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<RemoteDevice> getRemoteDevices() {
        return Collections.unmodifiableCollection(this.remoteItems.get());
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<Device> getDevices() {
        Set all;
        all = new HashSet();
        all.addAll(this.localItems.get());
        all.addAll(this.remoteItems.get());
        return Collections.unmodifiableCollection(all);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<Device> getDevices(DeviceType deviceType) {
        Collection<Device> devices;
        devices = new HashSet<>();
        devices.addAll(this.localItems.get(deviceType));
        devices.addAll(this.remoteItems.get(deviceType));
        return Collections.unmodifiableCollection(devices);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<Device> getDevices(ServiceType serviceType) {
        Collection<Device> devices;
        devices = new HashSet<>();
        devices.addAll(this.localItems.get(serviceType));
        devices.addAll(this.remoteItems.get(serviceType));
        return Collections.unmodifiableCollection(devices);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Service getService(ServiceReference serviceReference) {
        Device device = getDevice(serviceReference.getUdn(), false);
        if (device != null) {
            return device.findService(serviceReference.getServiceId());
        }
        return null;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Resource getResource(URI pathQuery) throws IllegalArgumentException {
        if (pathQuery.isAbsolute()) {
            throw new IllegalArgumentException("Resource URI can not be absolute, only path and query:" + pathQuery);
        }
        for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
            Resource resource = resourceItem.getItem();
            if (resource.matches(pathQuery)) {
                return resource;
            }
        }
        if (pathQuery.getPath().endsWith("/")) {
            URI pathQueryWithoutSlash = URI.create(pathQuery.toString().substring(0, pathQuery.toString().length() - 1));
            for (RegistryItem<URI, Resource> resourceItem2 : this.resourceItems) {
                Resource resource2 = resourceItem2.getItem();
                if (resource2.matches(pathQueryWithoutSlash)) {
                    return resource2;
                }
            }
        }
        return null;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized <T extends Resource> T getResource(Class<T> resourceType, URI pathQuery) throws IllegalArgumentException {
        T t = (T) getResource(pathQuery);
        if (t != null) {
            if (resourceType.isAssignableFrom(t.getClass())) {
                return t;
            }
        }
        return null;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized Collection<Resource> getResources() {
        Collection<Resource> s;
        s = new HashSet<>();
        for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
            s.add(resourceItem.getItem());
        }
        return s;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized <T extends Resource> Collection<T> getResources(Class<T> resourceType) {
        HashSet hashSet;
        hashSet = new HashSet();
        for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
            if (resourceType.isAssignableFrom(resourceItem.getItem().getClass())) {
                hashSet.add(resourceItem.getItem());
            }
        }
        return hashSet;
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addResource(Resource resource) {
        addResource(resource, 0);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addResource(Resource resource, int maxAgeSeconds) {
        RegistryItem resourceItem = new RegistryItem(resource.getPathQuery(), resource, maxAgeSeconds);
        this.resourceItems.remove(resourceItem);
        this.resourceItems.add(resourceItem);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean removeResource(Resource resource) {
        return this.resourceItems.remove(new RegistryItem(resource.getPathQuery()));
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addLocalSubscription(LocalGENASubscription subscription) {
        this.localItems.addSubscription(subscription);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized LocalGENASubscription getLocalSubscription(String subscriptionId) {
        return this.localItems.getSubscription(subscriptionId);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean updateLocalSubscription(LocalGENASubscription subscription) {
        return this.localItems.updateSubscription(subscription);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean removeLocalSubscription(LocalGENASubscription subscription) {
        return this.localItems.removeSubscription(subscription);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void addRemoteSubscription(RemoteGENASubscription subscription) {
        this.remoteItems.addSubscription(subscription);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized RemoteGENASubscription getRemoteSubscription(String subscriptionId) {
        return this.remoteItems.getSubscription(subscriptionId);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void updateRemoteSubscription(RemoteGENASubscription subscription) {
        this.remoteItems.updateSubscription(subscription);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void removeRemoteSubscription(RemoteGENASubscription subscription) {
        this.remoteItems.removeSubscription(subscription);
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void advertiseLocalDevices() {
        this.localItems.advertiseLocalDevices();
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void shutdown() {
        log.fine("Shutting down registry...");
        if (this.registryMaintainer != null) {
            this.registryMaintainer.stop();
        }
        log.finest("Executing final pending operations on shutdown: " + this.pendingExecutions.size());
        runPendingExecutions(false);
        for (RegistryListener listener : this.registryListeners) {
            listener.beforeShutdown(this);
        }
        RegistryItem<URI, Resource>[] resources = (RegistryItem[]) this.resourceItems.toArray(new RegistryItem[this.resourceItems.size()]);
        for (RegistryItem<URI, Resource> resourceItem : resources) {
            resourceItem.getItem().shutdown();
        }
        this.remoteItems.shutdown();
        this.localItems.shutdown();
        for (RegistryListener listener2 : this.registryListeners) {
            listener2.afterShutdown();
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void pause() {
        if (this.registryMaintainer != null) {
            log.fine("Pausing registry maintenance");
            runPendingExecutions(true);
            this.registryMaintainer.stop();
            this.registryMaintainer = null;
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized void resume() {
        if (this.registryMaintainer == null) {
            log.fine("Resuming registry maintenance");
            this.remoteItems.resume();
            this.registryMaintainer = createRegistryMaintainer();
            if (this.registryMaintainer != null) {
                getConfiguration().getRegistryMaintainerExecutor().execute(this.registryMaintainer);
            }
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public synchronized boolean isPaused() {
        return this.registryMaintainer == null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void maintain() {
        if (log.isLoggable(Level.FINEST)) {
            log.finest("Maintaining registry...");
        }
        Iterator<RegistryItem<URI, Resource>> it = this.resourceItems.iterator();
        while (it.hasNext()) {
            RegistryItem<URI, Resource> item = it.next();
            if (item.getExpirationDetails().hasExpired()) {
                if (log.isLoggable(Level.FINER)) {
                    Logger logger = log;
                    logger.finer("Removing expired resource: " + item);
                }
                it.remove();
            }
        }
        for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
            resourceItem.getItem().maintain(this.pendingExecutions, resourceItem.getExpirationDetails());
        }
        this.remoteItems.maintain();
        this.localItems.maintain();
        runPendingExecutions(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void executeAsyncProtocol(Runnable runnable) {
        this.pendingExecutions.add(runnable);
    }

    synchronized void runPendingExecutions(boolean async) {
        if (log.isLoggable(Level.FINEST)) {
            Logger logger = log;
            logger.finest("Executing pending operations: " + this.pendingExecutions.size());
        }
        for (Runnable pendingExecution : this.pendingExecutions) {
            if (async) {
                this.aliveExecutor.execute(pendingExecution);
            } else {
                pendingExecution.run();
            }
        }
        if (this.pendingExecutions.size() > 0) {
            this.pendingExecutions.clear();
        }
    }

    public void printDebugLog() {
        if (log.isLoggable(Level.FINE)) {
            log.fine("====================================    REMOTE   ================================================");
            for (RemoteDevice remoteDevice : this.remoteItems.get()) {
                log.fine(remoteDevice.toString());
            }
            log.fine("====================================    LOCAL    ================================================");
            for (LocalDevice localDevice : this.localItems.get()) {
                log.fine(localDevice.toString());
            }
            log.fine("====================================  RESOURCES  ================================================");
            for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
                log.fine(resourceItem.toString());
            }
            log.fine("=================================================================================================");
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public void registerPendingRemoteSubscription(RemoteGENASubscription subscription) {
        synchronized (this.pendingSubscriptionsLock) {
            this.pendingSubscriptionsLock.add(subscription);
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public void unregisterPendingRemoteSubscription(RemoteGENASubscription subscription) {
        synchronized (this.pendingSubscriptionsLock) {
            if (this.pendingSubscriptionsLock.remove(subscription)) {
                this.pendingSubscriptionsLock.notifyAll();
            }
        }
    }

    @Override // org.fourthline.cling.registry.Registry
    public RemoteGENASubscription getWaitRemoteSubscription(String subscriptionId) {
        RemoteGENASubscription subscription;
        synchronized (this.pendingSubscriptionsLock) {
            subscription = getRemoteSubscription(subscriptionId);
            while (subscription == null && !this.pendingSubscriptionsLock.isEmpty()) {
                try {
                    log.finest("Subscription not found, waiting for pending subscription procedure to terminate.");
                    this.pendingSubscriptionsLock.wait();
                } catch (InterruptedException e) {
                }
                subscription = getRemoteSubscription(subscriptionId);
            }
        }
        return subscription;
    }
}
