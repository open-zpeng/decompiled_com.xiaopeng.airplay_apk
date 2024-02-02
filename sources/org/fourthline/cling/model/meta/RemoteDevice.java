package org.fourthline.cling.model.meta;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.resource.ServiceEventCallbackResource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.seamless.util.URIUtil;
/* loaded from: classes.dex */
public class RemoteDevice extends Device<RemoteDeviceIdentity, RemoteDevice, RemoteService> {
    public RemoteDevice(RemoteDeviceIdentity identity) throws ValidationException {
        super(identity);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService service) throws ValidationException {
        super(identity, type, details, null, new RemoteService[]{service});
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService service, RemoteDevice embeddedDevice) throws ValidationException {
        super(identity, type, details, null, new RemoteService[]{service}, new RemoteDevice[]{embeddedDevice});
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService[] services) throws ValidationException {
        super(identity, type, details, null, services);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, RemoteService[] services, RemoteDevice[] embeddedDevices) throws ValidationException {
        super(identity, type, details, null, services, embeddedDevices);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService service) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, new RemoteService[]{service});
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService service, RemoteDevice embeddedDevice) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, new RemoteService[]{service}, new RemoteDevice[]{embeddedDevice});
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService[] services) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, services);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, RemoteService[] services, RemoteDevice[] embeddedDevices) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, services, embeddedDevices);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService service) throws ValidationException {
        super(identity, type, details, icons, new RemoteService[]{service});
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService service, RemoteDevice embeddedDevice) throws ValidationException {
        super(identity, type, details, icons, new RemoteService[]{service}, new RemoteDevice[]{embeddedDevice});
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services) throws ValidationException {
        super(identity, type, details, icons, services);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services, RemoteDevice[] embeddedDevices) throws ValidationException {
        super(identity, type, details, icons, services, embeddedDevices);
    }

    public RemoteDevice(RemoteDeviceIdentity identity, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services, RemoteDevice[] embeddedDevices) throws ValidationException {
        super(identity, version, type, details, icons, services, embeddedDevices);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public RemoteService[] getServices() {
        return this.services != 0 ? (RemoteService[]) this.services : new RemoteService[0];
    }

    @Override // org.fourthline.cling.model.meta.Device
    public RemoteDevice[] getEmbeddedDevices() {
        return this.embeddedDevices != 0 ? (RemoteDevice[]) this.embeddedDevices : new RemoteDevice[0];
    }

    public URL normalizeURI(URI relativeOrAbsoluteURI) {
        if (getDetails() != null && getDetails().getBaseURL() != null) {
            return URIUtil.createAbsoluteURL(getDetails().getBaseURL(), relativeOrAbsoluteURI);
        }
        return URIUtil.createAbsoluteURL(getIdentity().getDescriptorURL(), relativeOrAbsoluteURI);
    }

    @Override // org.fourthline.cling.model.meta.Device
    public RemoteDevice newInstance(UDN udn, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, RemoteService[] services, List<RemoteDevice> embeddedDevices) throws ValidationException {
        return new RemoteDevice(new RemoteDeviceIdentity(udn, getIdentity()), version, type, details, icons, services, embeddedDevices.size() > 0 ? (RemoteDevice[]) embeddedDevices.toArray(new RemoteDevice[embeddedDevices.size()]) : null);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public RemoteService newInstance(ServiceType serviceType, ServiceId serviceId, URI descriptorURI, URI controlURI, URI eventSubscriptionURI, Action<RemoteService>[] actions, StateVariable<RemoteService>[] stateVariables) throws ValidationException {
        return new RemoteService(serviceType, serviceId, descriptorURI, controlURI, eventSubscriptionURI, actions, stateVariables);
    }

    @Override // org.fourthline.cling.model.meta.Device
    public RemoteDevice[] toDeviceArray(Collection<RemoteDevice> col) {
        return (RemoteDevice[]) col.toArray(new RemoteDevice[col.size()]);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public RemoteService[] newServiceArray(int size) {
        return new RemoteService[size];
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public RemoteService[] toServiceArray(Collection<RemoteService> col) {
        return (RemoteService[]) col.toArray(new RemoteService[col.size()]);
    }

    @Override // org.fourthline.cling.model.meta.Device
    public Resource[] discoverResources(Namespace namespace) {
        RemoteService[] services;
        Device[] embeddedDevices;
        List<Resource> discovered = new ArrayList<>();
        for (RemoteService service : getServices()) {
            if (service != null) {
                discovered.add(new ServiceEventCallbackResource(namespace.getEventCallbackPath(service), service));
            }
        }
        if (hasEmbeddedDevices()) {
            for (Device embeddedDevice : getEmbeddedDevices()) {
                if (embeddedDevice != null) {
                    discovered.addAll(Arrays.asList(embeddedDevice.discoverResources(namespace)));
                }
            }
        }
        return (Resource[]) discovered.toArray(new Resource[discovered.size()]);
    }

    @Override // org.fourthline.cling.model.meta.Device
    public RemoteDevice getRoot() {
        if (isRoot()) {
            return this;
        }
        RemoteDevice current = this;
        while (current.getParentDevice() != null) {
            current = current.getParentDevice();
        }
        return current;
    }

    @Override // org.fourthline.cling.model.meta.Device
    public RemoteDevice findDevice(UDN udn) {
        return find(udn, (UDN) this);
    }
}
