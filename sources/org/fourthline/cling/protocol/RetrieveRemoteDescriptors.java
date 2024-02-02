package org.fourthline.cling.protocol;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.RegistrationException;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public class RetrieveRemoteDescriptors implements Runnable {
    protected List<UDN> errorsAlreadyLogged = new ArrayList();
    private RemoteDevice rd;
    private final UpnpService upnpService;
    private static final Logger log = Logger.getLogger(RetrieveRemoteDescriptors.class.getName());
    private static final Set<URL> activeRetrievals = new CopyOnWriteArraySet();

    public RetrieveRemoteDescriptors(UpnpService upnpService, RemoteDevice rd) {
        this.upnpService = upnpService;
        this.rd = rd;
    }

    public UpnpService getUpnpService() {
        return this.upnpService;
    }

    @Override // java.lang.Runnable
    public void run() {
        URL deviceURL = this.rd.getIdentity().getDescriptorURL();
        if (activeRetrievals.contains(deviceURL)) {
            Logger logger = log;
            logger.finer("Exiting early, active retrieval for URL already in progress: " + deviceURL);
        } else if (getUpnpService().getRegistry().getRemoteDevice(this.rd.getIdentity().getUdn(), true) != null) {
            Logger logger2 = log;
            logger2.finer("Exiting early, already discovered: " + deviceURL);
        } else {
            try {
                try {
                    activeRetrievals.add(deviceURL);
                    describe();
                } catch (RouterException ex) {
                    Logger logger3 = log;
                    Level level = Level.WARNING;
                    logger3.log(level, "Descriptor retrieval failed: " + deviceURL, (Throwable) ex);
                }
            } finally {
                activeRetrievals.remove(deviceURL);
            }
        }
    }

    protected void describe() throws RouterException {
        if (getUpnpService().getRouter() == null) {
            log.warning("Router not yet initialized");
            return;
        }
        try {
            StreamRequestMessage deviceDescRetrievalMsg = new StreamRequestMessage(UpnpRequest.Method.GET, this.rd.getIdentity().getDescriptorURL());
            UpnpHeaders headers = getUpnpService().getConfiguration().getDescriptorRetrievalHeaders(this.rd.getIdentity());
            if (headers != null) {
                deviceDescRetrievalMsg.getHeaders().putAll(headers);
            }
            Logger logger = log;
            logger.fine("Sending device descriptor retrieval message: " + deviceDescRetrievalMsg);
            StreamResponseMessage deviceDescMsg = getUpnpService().getRouter().send(deviceDescRetrievalMsg);
            if (deviceDescMsg == null) {
                Logger logger2 = log;
                logger2.warning("Device descriptor retrieval failed, no response: " + this.rd.getIdentity().getDescriptorURL());
            } else if (deviceDescMsg.getOperation().isFailed()) {
                Logger logger3 = log;
                logger3.warning("Device descriptor retrieval failed: " + this.rd.getIdentity().getDescriptorURL() + ", " + deviceDescMsg.getOperation().getResponseDetails());
            } else {
                if (!deviceDescMsg.isContentTypeTextUDA()) {
                    Logger logger4 = log;
                    logger4.fine("Received device descriptor without or with invalid Content-Type: " + this.rd.getIdentity().getDescriptorURL());
                }
                String descriptorContent = deviceDescMsg.getBodyString();
                if (descriptorContent == null || descriptorContent.length() == 0) {
                    Logger logger5 = log;
                    logger5.warning("Received empty device descriptor:" + this.rd.getIdentity().getDescriptorURL());
                    return;
                }
                Logger logger6 = log;
                logger6.fine("Received root device descriptor: " + deviceDescMsg);
                describe(descriptorContent);
            }
        } catch (IllegalArgumentException ex) {
            Logger logger7 = log;
            logger7.warning("Device descriptor retrieval failed: " + this.rd.getIdentity().getDescriptorURL() + ", possibly invalid URL: " + ex);
        }
    }

    protected void describe(String descriptorXML) throws RouterException {
        try {
            DeviceDescriptorBinder deviceDescriptorBinder = getUpnpService().getConfiguration().getDeviceDescriptorBinderUDA10();
            RemoteDevice describedDevice = (RemoteDevice) deviceDescriptorBinder.describe((DeviceDescriptorBinder) this.rd, descriptorXML);
            Logger logger = log;
            logger.fine("Remote device described (without services) notifying listeners: " + describedDevice);
            boolean notifiedStart = getUpnpService().getRegistry().notifyDiscoveryStart(describedDevice);
            Logger logger2 = log;
            logger2.fine("Hydrating described device's services: " + describedDevice);
            RemoteDevice hydratedDevice = describeServices(describedDevice);
            if (hydratedDevice == null) {
                if (!this.errorsAlreadyLogged.contains(this.rd.getIdentity().getUdn())) {
                    this.errorsAlreadyLogged.add(this.rd.getIdentity().getUdn());
                    Logger logger3 = log;
                    logger3.warning("Device service description failed: " + this.rd);
                }
                if (notifiedStart) {
                    Registry registry = getUpnpService().getRegistry();
                    registry.notifyDiscoveryFailure(describedDevice, new DescriptorBindingException("Device service description failed: " + this.rd));
                    return;
                }
                return;
            }
            Logger logger4 = log;
            logger4.fine("Adding fully hydrated remote device to registry: " + hydratedDevice);
            getUpnpService().getRegistry().addDevice(hydratedDevice);
        } catch (DescriptorBindingException ex) {
            Logger logger5 = log;
            logger5.warning("Could not hydrate device or its services from descriptor: " + this.rd);
            Logger logger6 = log;
            logger6.warning("Cause was: " + Exceptions.unwrap(ex));
            if (0 != 0 && 0 != 0) {
                getUpnpService().getRegistry().notifyDiscoveryFailure(null, ex);
            }
        } catch (ValidationException ex2) {
            if (!this.errorsAlreadyLogged.contains(this.rd.getIdentity().getUdn())) {
                this.errorsAlreadyLogged.add(this.rd.getIdentity().getUdn());
                Logger logger7 = log;
                logger7.warning("Could not validate device model: " + this.rd);
                for (ValidationError validationError : ex2.getErrors()) {
                    log.warning(validationError.toString());
                }
                if (0 != 0 && 0 != 0) {
                    getUpnpService().getRegistry().notifyDiscoveryFailure(null, ex2);
                }
            }
        } catch (RegistrationException ex3) {
            Logger logger8 = log;
            logger8.warning("Adding hydrated device to registry failed: " + this.rd);
            Logger logger9 = log;
            logger9.warning("Cause was: " + ex3.toString());
            if (0 != 0 && 0 != 0) {
                getUpnpService().getRegistry().notifyDiscoveryFailure(null, ex3);
            }
        }
    }

    protected RemoteDevice describeServices(RemoteDevice currentDevice) throws RouterException, DescriptorBindingException, ValidationException {
        RemoteDevice[] embeddedDevices;
        RemoteDevice describedEmbeddedDevice;
        List<RemoteService> describedServices = new ArrayList<>();
        if (currentDevice.hasServices()) {
            List<RemoteService> filteredServices = filterExclusiveServices(currentDevice.getServices());
            for (RemoteService service : filteredServices) {
                RemoteService svc = describeService(service);
                if (svc != null) {
                    describedServices.add(svc);
                } else {
                    log.warning("Skipping invalid service '" + service + "' of: " + currentDevice);
                }
            }
        }
        List<RemoteDevice> describedEmbeddedDevices = new ArrayList<>();
        int i = 0;
        if (currentDevice.hasEmbeddedDevices()) {
            for (RemoteDevice embeddedDevice : currentDevice.getEmbeddedDevices()) {
                if (embeddedDevice != null && (describedEmbeddedDevice = describeServices(embeddedDevice)) != null) {
                    describedEmbeddedDevices.add(describedEmbeddedDevice);
                }
            }
        }
        Icon[] iconDupes = new Icon[currentDevice.getIcons().length];
        while (true) {
            int i2 = i;
            if (i2 < currentDevice.getIcons().length) {
                Icon icon = currentDevice.getIcons()[i2];
                iconDupes[i2] = icon.deepCopy();
                i = i2 + 1;
            } else {
                return currentDevice.newInstance(((RemoteDeviceIdentity) currentDevice.getIdentity()).getUdn(), currentDevice.getVersion(), currentDevice.getType(), currentDevice.getDetails(), iconDupes, currentDevice.toServiceArray((Collection<RemoteService>) describedServices), describedEmbeddedDevices);
            }
        }
    }

    protected RemoteService describeService(RemoteService service) throws RouterException, DescriptorBindingException, ValidationException {
        try {
            URL descriptorURL = service.getDevice().normalizeURI(service.getDescriptorURI());
            StreamRequestMessage serviceDescRetrievalMsg = new StreamRequestMessage(UpnpRequest.Method.GET, descriptorURL);
            UpnpHeaders headers = getUpnpService().getConfiguration().getDescriptorRetrievalHeaders(service.getDevice().getIdentity());
            if (headers != null) {
                serviceDescRetrievalMsg.getHeaders().putAll(headers);
            }
            Logger logger = log;
            logger.fine("Sending service descriptor retrieval message: " + serviceDescRetrievalMsg);
            StreamResponseMessage serviceDescMsg = getUpnpService().getRouter().send(serviceDescRetrievalMsg);
            if (serviceDescMsg == null) {
                Logger logger2 = log;
                logger2.warning("Could not retrieve service descriptor, no response: " + service);
                return null;
            } else if (serviceDescMsg.getOperation().isFailed()) {
                Logger logger3 = log;
                logger3.warning("Service descriptor retrieval failed: " + descriptorURL + ", " + serviceDescMsg.getOperation().getResponseDetails());
                return null;
            } else {
                if (!serviceDescMsg.isContentTypeTextUDA()) {
                    Logger logger4 = log;
                    logger4.fine("Received service descriptor without or with invalid Content-Type: " + descriptorURL);
                }
                String descriptorContent = serviceDescMsg.getBodyString();
                if (descriptorContent == null || descriptorContent.length() == 0) {
                    Logger logger5 = log;
                    logger5.warning("Received empty service descriptor:" + descriptorURL);
                    return null;
                }
                Logger logger6 = log;
                logger6.fine("Received service descriptor, hydrating service model: " + serviceDescMsg);
                ServiceDescriptorBinder serviceDescriptorBinder = getUpnpService().getConfiguration().getServiceDescriptorBinderUDA10();
                return (RemoteService) serviceDescriptorBinder.describe((ServiceDescriptorBinder) service, descriptorContent);
            }
        } catch (IllegalArgumentException e) {
            Logger logger7 = log;
            logger7.warning("Could not normalize service descriptor URL: " + service.getDescriptorURI());
            return null;
        }
    }

    protected List<RemoteService> filterExclusiveServices(RemoteService[] services) {
        ServiceType[] exclusiveTypes = getUpnpService().getConfiguration().getExclusiveServiceTypes();
        if (exclusiveTypes == null || exclusiveTypes.length == 0) {
            return Arrays.asList(services);
        }
        List<RemoteService> exclusiveServices = new ArrayList<>();
        for (RemoteService discoveredService : services) {
            for (ServiceType exclusiveType : exclusiveTypes) {
                if (discoveredService.getServiceType().implementsVersion(exclusiveType)) {
                    log.fine("Including exclusive service: " + discoveredService);
                    exclusiveServices.add(discoveredService);
                } else {
                    log.fine("Excluding unwanted service: " + exclusiveType);
                }
            }
        }
        return exclusiveServices;
    }
}
