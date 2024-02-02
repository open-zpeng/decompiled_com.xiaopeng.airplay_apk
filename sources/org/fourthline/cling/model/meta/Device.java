package org.fourthline.cling.model.meta;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public abstract class Device<DI extends DeviceIdentity, D extends Device, S extends Service> implements Validatable {
    private static final Logger log = Logger.getLogger(Device.class.getName());
    private final DeviceDetails details;
    protected final D[] embeddedDevices;
    private final Icon[] icons;
    private final DI identity;
    private D parentDevice;
    protected final S[] services;
    private final DeviceType type;
    private final UDAVersion version;

    public abstract Resource[] discoverResources(Namespace namespace);

    public abstract D findDevice(UDN udn);

    public abstract D[] getEmbeddedDevices();

    public abstract D getRoot();

    public abstract S[] getServices();

    public abstract D newInstance(UDN udn, UDAVersion uDAVersion, DeviceType deviceType, DeviceDetails deviceDetails, Icon[] iconArr, S[] sArr, List<D> list) throws ValidationException;

    public abstract S newInstance(ServiceType serviceType, ServiceId serviceId, URI uri, URI uri2, URI uri3, Action<S>[] actionArr, StateVariable<S>[] stateVariableArr) throws ValidationException;

    public abstract S[] newServiceArray(int i);

    public abstract D[] toDeviceArray(Collection<D> collection);

    public abstract S[] toServiceArray(Collection<S> collection);

    public Device(DI identity) throws ValidationException {
        this(identity, null, null, null, null, null);
    }

    public Device(DI identity, DeviceType type, DeviceDetails details, Icon[] icons, S[] services) throws ValidationException {
        this(identity, null, type, details, icons, services, null);
    }

    public Device(DI identity, DeviceType type, DeviceDetails details, Icon[] icons, S[] services, D[] embeddedDevices) throws ValidationException {
        this(identity, null, type, details, icons, services, embeddedDevices);
    }

    public Device(DI identity, UDAVersion version, DeviceType type, DeviceDetails details, Icon[] icons, S[] services, D[] embeddedDevices) throws ValidationException {
        this.identity = identity;
        this.version = version == null ? new UDAVersion() : version;
        this.type = type;
        this.details = details;
        List<Icon> validIcons = new ArrayList<>();
        if (icons != null) {
            for (Icon icon : icons) {
                if (icon != null) {
                    icon.setDevice(this);
                    List<ValidationError> iconErrors = icon.validate();
                    if (iconErrors.isEmpty()) {
                        validIcons.add(icon);
                    } else {
                        log.warning("Discarding invalid '" + icon + "': " + iconErrors);
                    }
                }
            }
        }
        this.icons = (Icon[]) validIcons.toArray(new Icon[validIcons.size()]);
        boolean allNullServices = true;
        if (services != null) {
            boolean allNullServices2 = true;
            for (S service : services) {
                if (service != null) {
                    allNullServices2 = false;
                    service.setDevice(this);
                }
            }
            allNullServices = allNullServices2;
        }
        D[] dArr = null;
        this.services = (services == null || allNullServices) ? null : services;
        boolean allNullEmbedded = true;
        if (embeddedDevices != null) {
            boolean allNullEmbedded2 = true;
            for (D embeddedDevice : embeddedDevices) {
                if (embeddedDevice != null) {
                    allNullEmbedded2 = false;
                    embeddedDevice.setParentDevice(this);
                }
            }
            allNullEmbedded = allNullEmbedded2;
        }
        if (embeddedDevices != null && !allNullEmbedded) {
            dArr = embeddedDevices;
        }
        this.embeddedDevices = dArr;
        List<ValidationError> errors = validate();
        if (errors.size() > 0) {
            if (log.isLoggable(Level.FINEST)) {
                for (ValidationError error : errors) {
                    log.finest(error.toString());
                }
            }
            throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
        }
    }

    public DI getIdentity() {
        return this.identity;
    }

    public UDAVersion getVersion() {
        return this.version;
    }

    public DeviceType getType() {
        return this.type;
    }

    public DeviceDetails getDetails() {
        return this.details;
    }

    public DeviceDetails getDetails(RemoteClientInfo info) {
        return getDetails();
    }

    public Icon[] getIcons() {
        return this.icons;
    }

    public boolean hasIcons() {
        return getIcons() != null && getIcons().length > 0;
    }

    public boolean hasServices() {
        return getServices() != null && getServices().length > 0;
    }

    public boolean hasEmbeddedDevices() {
        return getEmbeddedDevices() != null && getEmbeddedDevices().length > 0;
    }

    public D getParentDevice() {
        return this.parentDevice;
    }

    void setParentDevice(D parentDevice) {
        if (this.parentDevice != null) {
            throw new IllegalStateException("Final value has been set already, model is immutable");
        }
        this.parentDevice = parentDevice;
    }

    public boolean isRoot() {
        return getParentDevice() == null;
    }

    public D[] findEmbeddedDevices() {
        return toDeviceArray(findEmbeddedDevices(this));
    }

    public D[] findDevices(DeviceType deviceType) {
        return toDeviceArray(find(deviceType, (DeviceType) this));
    }

    public D[] findDevices(ServiceType serviceType) {
        return toDeviceArray(find(serviceType, (ServiceType) this));
    }

    public Icon[] findIcons() {
        List<Icon> icons = new ArrayList<>();
        if (hasIcons()) {
            icons.addAll(Arrays.asList(getIcons()));
        }
        D[] embeddedDevices = findEmbeddedDevices();
        for (D embeddedDevice : embeddedDevices) {
            if (embeddedDevice.hasIcons()) {
                icons.addAll(Arrays.asList(embeddedDevice.getIcons()));
            }
        }
        return (Icon[]) icons.toArray(new Icon[icons.size()]);
    }

    public S[] findServices() {
        return toServiceArray(findServices(null, null, this));
    }

    public S[] findServices(ServiceType serviceType) {
        return toServiceArray(findServices(serviceType, null, this));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Multi-variable type inference failed */
    public D find(UDN udn, D current) {
        if (current.getIdentity() == null || current.getIdentity().getUdn() == null || !current.getIdentity().getUdn().equals(udn)) {
            if (current.hasEmbeddedDevices()) {
                for (Device device : current.getEmbeddedDevices()) {
                    D match = (D) find(udn, (UDN) device);
                    if (match != null) {
                        return match;
                    }
                }
                return null;
            }
            return null;
        }
        return current;
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected Collection<D> findEmbeddedDevices(D current) {
        Collection<D> devices = new HashSet<>();
        if (!current.isRoot() && current.getIdentity().getUdn() != null) {
            devices.add(current);
        }
        if (current.hasEmbeddedDevices()) {
            for (Device device : current.getEmbeddedDevices()) {
                devices.addAll(findEmbeddedDevices(device));
            }
        }
        return devices;
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected Collection<D> find(DeviceType deviceType, D current) {
        Collection<D> devices = new HashSet<>();
        if (current.getType() != null && current.getType().implementsVersion(deviceType)) {
            devices.add(current);
        }
        if (current.hasEmbeddedDevices()) {
            for (Device device : current.getEmbeddedDevices()) {
                devices.addAll(find(deviceType, (DeviceType) device));
            }
        }
        return devices;
    }

    protected Collection<D> find(ServiceType serviceType, D current) {
        Collection<S> services = findServices(serviceType, null, current);
        HashSet hashSet = new HashSet();
        for (Service service : services) {
            hashSet.add(service.getDevice());
        }
        return hashSet;
    }

    protected Collection<S> findServices(ServiceType serviceType, ServiceId serviceId, D current) {
        Service[] services;
        Service[] services2;
        Collection services3 = new HashSet();
        if (current.hasServices()) {
            for (Service service : current.getServices()) {
                if (isMatch(service, serviceType, serviceId)) {
                    services3.add(service);
                }
            }
        }
        Collection<D> embeddedDevices = findEmbeddedDevices(current);
        if (embeddedDevices != null) {
            for (D embeddedDevice : embeddedDevices) {
                if (embeddedDevice.hasServices()) {
                    for (Service service2 : embeddedDevice.getServices()) {
                        if (isMatch(service2, serviceType, serviceId)) {
                            services3.add(service2);
                        }
                    }
                }
            }
        }
        return services3;
    }

    public S findService(ServiceId serviceId) {
        Collection<S> services = findServices(null, serviceId, this);
        if (services.size() == 1) {
            return services.iterator().next();
        }
        return null;
    }

    public S findService(ServiceType serviceType) {
        Collection<S> services = findServices(serviceType, null, this);
        if (services.size() > 0) {
            return services.iterator().next();
        }
        return null;
    }

    public ServiceType[] findServiceTypes() {
        Collection<S> services = findServices(null, null, this);
        Collection<ServiceType> col = new HashSet<>();
        for (S service : services) {
            col.add(service.getServiceType());
        }
        return (ServiceType[]) col.toArray(new ServiceType[col.size()]);
    }

    private boolean isMatch(Service s, ServiceType serviceType, ServiceId serviceId) {
        boolean matchesType = serviceType == null || s.getServiceType().implementsVersion(serviceType);
        boolean matchesId = serviceId == null || s.getServiceId().equals(serviceId);
        return matchesType && matchesId;
    }

    public boolean isFullyHydrated() {
        S[] services = findServices();
        for (S service : services) {
            if (service.hasStateVariables()) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayString() {
        String str;
        String str2;
        String trim;
        String str3;
        String modelName;
        String cleanModelName = null;
        String cleanModelNumber = null;
        if (getDetails() != null && getDetails().getModelDetails() != null) {
            ModelDetails modelDetails = getDetails().getModelDetails();
            if (modelDetails.getModelName() != null) {
                if (modelDetails.getModelNumber() != null && modelDetails.getModelName().endsWith(modelDetails.getModelNumber())) {
                    modelName = modelDetails.getModelName().substring(0, modelDetails.getModelName().length() - modelDetails.getModelNumber().length());
                } else {
                    modelName = modelDetails.getModelName();
                }
                cleanModelName = modelName;
            }
            if (cleanModelName != null) {
                if (modelDetails.getModelNumber() != null && !cleanModelName.startsWith(modelDetails.getModelNumber())) {
                    str3 = modelDetails.getModelNumber();
                } else {
                    str3 = "";
                }
                cleanModelNumber = str3;
            } else {
                cleanModelNumber = modelDetails.getModelNumber();
            }
        }
        StringBuilder sb = new StringBuilder();
        if (getDetails() != null && getDetails().getManufacturerDetails() != null) {
            if (cleanModelName != null && getDetails().getManufacturerDetails().getManufacturer() != null) {
                if (cleanModelName.startsWith(getDetails().getManufacturerDetails().getManufacturer())) {
                    trim = cleanModelName.substring(getDetails().getManufacturerDetails().getManufacturer().length()).trim();
                } else {
                    trim = cleanModelName.trim();
                }
                cleanModelName = trim;
            }
            if (getDetails().getManufacturerDetails().getManufacturer() != null) {
                sb.append(getDetails().getManufacturerDetails().getManufacturer());
            }
        }
        if (cleanModelName == null || cleanModelName.length() <= 0) {
            str = "";
        } else {
            str = " " + cleanModelName;
        }
        sb.append(str);
        if (cleanModelNumber == null || cleanModelNumber.length() <= 0) {
            str2 = "";
        } else {
            str2 = " " + cleanModelNumber.trim();
        }
        sb.append(str2);
        return sb.toString();
    }

    @Override // org.fourthline.cling.model.Validatable
    public List<ValidationError> validate() {
        Device[] embeddedDevices;
        Service[] services;
        List<ValidationError> errors = new ArrayList<>();
        if (getType() != null) {
            errors.addAll(getVersion().validate());
            if (getIdentity() != null) {
                errors.addAll(getIdentity().validate());
            }
            if (getDetails() != null) {
                errors.addAll(getDetails().validate());
            }
            if (hasServices()) {
                for (Service service : getServices()) {
                    if (service != null) {
                        errors.addAll(service.validate());
                    }
                }
            }
            if (hasEmbeddedDevices()) {
                for (Device embeddedDevice : getEmbeddedDevices()) {
                    if (embeddedDevice != null) {
                        errors.addAll(embeddedDevice.validate());
                    }
                }
            }
        }
        return errors;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device device = (Device) o;
        if (this.identity.equals(device.identity)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.identity.hashCode();
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") Identity: " + getIdentity().toString() + ", Root: " + isRoot();
    }
}
