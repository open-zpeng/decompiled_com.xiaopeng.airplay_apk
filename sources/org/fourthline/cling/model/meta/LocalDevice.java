package org.fourthline.cling.model.meta;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.profile.DeviceDetailsProvider;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.resource.DeviceDescriptorResource;
import org.fourthline.cling.model.resource.IconResource;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.resource.ServiceControlResource;
import org.fourthline.cling.model.resource.ServiceDescriptorResource;
import org.fourthline.cling.model.resource.ServiceEventSubscriptionResource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class LocalDevice extends Device<DeviceIdentity, LocalDevice, LocalService> {
    private final DeviceDetailsProvider deviceDetailsProvider;

    public LocalDevice(DeviceIdentity identity) throws ValidationException {
        super(identity);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, LocalService service) throws ValidationException {
        super(identity, type, details, null, new LocalService[]{service});
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetailsProvider deviceDetailsProvider, LocalService service) throws ValidationException {
        super(identity, type, null, null, new LocalService[]{service});
        this.deviceDetailsProvider = deviceDetailsProvider;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetailsProvider deviceDetailsProvider, LocalService service, LocalDevice embeddedDevice) throws ValidationException {
        super(identity, type, null, null, new LocalService[]{service}, new LocalDevice[]{embeddedDevice});
        this.deviceDetailsProvider = deviceDetailsProvider;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, LocalService service, LocalDevice embeddedDevice) throws ValidationException {
        super(identity, type, details, null, new LocalService[]{service}, new LocalDevice[]{embeddedDevice});
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, LocalService[] services) throws ValidationException {
        super(identity, type, details, null, services);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, LocalService[] services, LocalDevice[] embeddedDevices) throws ValidationException {
        super(identity, type, details, null, services, embeddedDevices);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, LocalService service) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, new LocalService[]{service});
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, LocalService service, LocalDevice embeddedDevice) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, new LocalService[]{service}, new LocalDevice[]{embeddedDevice});
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, LocalService[] services) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, services);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetailsProvider deviceDetailsProvider, Icon icon, LocalService[] services) throws ValidationException {
        super(identity, type, null, new Icon[]{icon}, services);
        this.deviceDetailsProvider = deviceDetailsProvider;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon icon, LocalService[] services, LocalDevice[] embeddedDevices) throws ValidationException {
        super(identity, type, details, new Icon[]{icon}, services, embeddedDevices);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, LocalService service) throws ValidationException {
        super(identity, type, details, icons, new LocalService[]{service});
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, LocalService service, LocalDevice embeddedDevice) throws ValidationException {
        super(identity, type, details, icons, new LocalService[]{service}, new LocalDevice[]{embeddedDevice});
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetailsProvider deviceDetailsProvider, Icon[] icons, LocalService service, LocalDevice embeddedDevice) throws ValidationException {
        super(identity, type, null, icons, new LocalService[]{service}, new LocalDevice[]{embeddedDevice});
        this.deviceDetailsProvider = deviceDetailsProvider;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, LocalService[] services) throws ValidationException {
        super(identity, type, details, icons, services);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, DeviceType type, DeviceDetails details, Icon[] icons, LocalService[] services, LocalDevice[] embeddedDevices) throws ValidationException {
        super(identity, type, details, icons, services, embeddedDevices);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, LocalService[] services, LocalDevice[] embeddedDevices) throws ValidationException {
        super(identity, version, type, details, icons, services, embeddedDevices);
        this.deviceDetailsProvider = null;
    }

    public LocalDevice(DeviceIdentity identity, UDAVersion version, DeviceType type, DeviceDetailsProvider deviceDetailsProvider, Icon[] icons, LocalService[] services, LocalDevice[] embeddedDevices) throws ValidationException {
        super(identity, version, type, null, icons, services, embeddedDevices);
        this.deviceDetailsProvider = deviceDetailsProvider;
    }

    public DeviceDetailsProvider getDeviceDetailsProvider() {
        return this.deviceDetailsProvider;
    }

    @Override // org.fourthline.cling.model.meta.Device
    public DeviceDetails getDetails(RemoteClientInfo info) {
        if (getDeviceDetailsProvider() != null) {
            return getDeviceDetailsProvider().provide(info);
        }
        return getDetails();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public LocalService[] getServices() {
        return this.services != 0 ? (LocalService[]) this.services : new LocalService[0];
    }

    @Override // org.fourthline.cling.model.meta.Device
    public LocalDevice[] getEmbeddedDevices() {
        return this.embeddedDevices != 0 ? (LocalDevice[]) this.embeddedDevices : new LocalDevice[0];
    }

    @Override // org.fourthline.cling.model.meta.Device
    public LocalDevice newInstance(UDN udn, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, LocalService[] services, List<LocalDevice> embeddedDevices) throws ValidationException {
        return new LocalDevice(new DeviceIdentity(udn, getIdentity().getMaxAgeSeconds()), version, type, details, icons, services, embeddedDevices.size() > 0 ? (LocalDevice[]) embeddedDevices.toArray(new LocalDevice[embeddedDevices.size()]) : null);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public LocalService newInstance(ServiceType serviceType, ServiceId serviceId, URI descriptorURI, URI controlURI, URI eventSubscriptionURI, Action<LocalService>[] actions, StateVariable<LocalService>[] stateVariables) throws ValidationException {
        return new LocalService(serviceType, serviceId, actions, stateVariables);
    }

    @Override // org.fourthline.cling.model.meta.Device
    public LocalDevice[] toDeviceArray(Collection<LocalDevice> col) {
        return (LocalDevice[]) col.toArray(new LocalDevice[col.size()]);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public LocalService[] newServiceArray(int size) {
        return new LocalService[size];
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.model.meta.Device
    public LocalService[] toServiceArray(Collection<LocalService> col) {
        return (LocalService[]) col.toArray(new LocalService[col.size()]);
    }

    @Override // org.fourthline.cling.model.meta.Device, org.fourthline.cling.model.Validatable
    public List<ValidationError> validate() {
        Icon[] icons;
        List<ValidationError> errors = new ArrayList<>();
        errors.addAll(super.validate());
        if (hasIcons()) {
            for (Icon icon : getIcons()) {
                if (icon.getUri().isAbsolute()) {
                    errors.add(new ValidationError(getClass(), "icons", "Local icon URI can not be absolute: " + icon.getUri()));
                }
                if (icon.getUri().toString().contains("../")) {
                    errors.add(new ValidationError(getClass(), "icons", "Local icon URI must not contain '../': " + icon.getUri()));
                }
                if (icon.getUri().toString().startsWith("/")) {
                    errors.add(new ValidationError(getClass(), "icons", "Local icon URI must not start with '/': " + icon.getUri()));
                }
            }
        }
        return errors;
    }

    @Override // org.fourthline.cling.model.meta.Device
    public Resource[] discoverResources(Namespace namespace) {
        LocalService[] services;
        Icon[] icons;
        Device[] embeddedDevices;
        List<Resource> discovered = new ArrayList<>();
        if (isRoot()) {
            discovered.add(new DeviceDescriptorResource(namespace.getDescriptorPath(this), this));
        }
        for (LocalService service : getServices()) {
            discovered.add(new ServiceDescriptorResource(namespace.getDescriptorPath(service), service));
            discovered.add(new ServiceControlResource(namespace.getControlPath(service), service));
            discovered.add(new ServiceEventSubscriptionResource(namespace.getEventSubscriptionPath(service), service));
        }
        for (Icon icon : getIcons()) {
            discovered.add(new IconResource(namespace.prefixIfRelative(this, icon.getUri()), icon));
        }
        if (hasEmbeddedDevices()) {
            for (Device embeddedDevice : getEmbeddedDevices()) {
                discovered.addAll(Arrays.asList(embeddedDevice.discoverResources(namespace)));
            }
        }
        return (Resource[]) discovered.toArray(new Resource[discovered.size()]);
    }

    @Override // org.fourthline.cling.model.meta.Device
    public LocalDevice getRoot() {
        if (isRoot()) {
            return this;
        }
        LocalDevice current = this;
        while (current.getParentDevice() != null) {
            current = current.getParentDevice();
        }
        return current;
    }

    @Override // org.fourthline.cling.model.meta.Device
    public LocalDevice findDevice(UDN udn) {
        return find(udn, (UDN) this);
    }
}
