package org.fourthline.cling.registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class RegistryItems<D extends Device, S extends GENASubscription> {
    protected final RegistryImpl registry;
    protected final Set<RegistryItem<UDN, D>> deviceItems = new HashSet();
    protected final Set<RegistryItem<String, S>> subscriptionItems = new HashSet();

    abstract void add(D d);

    abstract void maintain();

    abstract boolean remove(D d);

    abstract void removeAll();

    abstract void shutdown();

    /* JADX INFO: Access modifiers changed from: package-private */
    public RegistryItems(RegistryImpl registry) {
        this.registry = registry;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Set<RegistryItem<UDN, D>> getDeviceItems() {
        return this.deviceItems;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Set<RegistryItem<String, S>> getSubscriptionItems() {
        return this.subscriptionItems;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public D get(UDN udn, boolean rootOnly) {
        D foundDevice;
        for (RegistryItem<UDN, D> item : this.deviceItems) {
            D device = item.getItem();
            if (device.getIdentity().getUdn().equals(udn)) {
                return device;
            }
            if (!rootOnly && (foundDevice = (D) item.getItem().findDevice(udn)) != null) {
                return foundDevice;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Collection<D> get(DeviceType deviceType) {
        Collection<D> devices = new HashSet<>();
        for (RegistryItem<UDN, D> item : this.deviceItems) {
            Device[] findDevices = item.getItem().findDevices(deviceType);
            if (findDevices != null) {
                devices.addAll(Arrays.asList(findDevices));
            }
        }
        return devices;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Collection<D> get(ServiceType serviceType) {
        Collection<D> devices = new HashSet<>();
        for (RegistryItem<UDN, D> item : this.deviceItems) {
            Device[] findDevices = item.getItem().findDevices(serviceType);
            if (findDevices != null) {
                devices.addAll(Arrays.asList(findDevices));
            }
        }
        return devices;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Collection<D> get() {
        Collection<D> devices = new HashSet<>();
        for (RegistryItem<UDN, D> item : this.deviceItems) {
            devices.add(item.getItem());
        }
        return devices;
    }

    boolean contains(D device) {
        return contains(device.getIdentity().getUdn());
    }

    boolean contains(UDN udn) {
        return this.deviceItems.contains(new RegistryItem(udn));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addSubscription(S subscription) {
        RegistryItem<String, S> subscriptionItem = new RegistryItem<>(subscription.getSubscriptionId(), subscription, subscription.getActualDurationSeconds());
        this.subscriptionItems.add(subscriptionItem);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean updateSubscription(S subscription) {
        if (removeSubscription(subscription)) {
            addSubscription(subscription);
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean removeSubscription(S subscription) {
        return this.subscriptionItems.remove(new RegistryItem(subscription.getSubscriptionId()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public S getSubscription(String subscriptionId) {
        for (RegistryItem<String, S> registryItem : this.subscriptionItems) {
            if (registryItem.getKey().equals(subscriptionId)) {
                return registryItem.getItem();
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Resource[] getResources(Device device) throws RegistrationException {
        try {
            return this.registry.getConfiguration().getNamespace().getResources(device);
        } catch (ValidationException ex) {
            throw new RegistrationException("Resource discover error: " + ex.toString(), ex);
        }
    }
}
