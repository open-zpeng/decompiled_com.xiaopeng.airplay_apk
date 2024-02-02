package org.fourthline.cling.support.igd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.igd.callback.PortMappingAdd;
import org.fourthline.cling.support.igd.callback.PortMappingDelete;
import org.fourthline.cling.support.model.PortMapping;
/* loaded from: classes.dex */
public class PortMappingListener extends DefaultRegistryListener {
    protected Map<Service, List<PortMapping>> activePortMappings;
    protected PortMapping[] portMappings;
    private static final Logger log = Logger.getLogger(PortMappingListener.class.getName());
    public static final DeviceType IGD_DEVICE_TYPE = new UDADeviceType("InternetGatewayDevice", 1);
    public static final DeviceType CONNECTION_DEVICE_TYPE = new UDADeviceType("WANConnectionDevice", 1);
    public static final ServiceType IP_SERVICE_TYPE = new UDAServiceType("WANIPConnection", 1);
    public static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);

    public PortMappingListener(PortMapping portMapping) {
        this(new PortMapping[]{portMapping});
    }

    public PortMappingListener(PortMapping[] portMappings) {
        this.activePortMappings = new HashMap();
        this.portMappings = portMappings;
    }

    @Override // org.fourthline.cling.registry.DefaultRegistryListener
    public synchronized void deviceAdded(Registry registry, Device device) {
        PortMapping[] portMappingArr;
        Service connectionService = discoverConnectionService(device);
        if (connectionService == null) {
            return;
        }
        log.fine("Activating port mappings on: " + connectionService);
        final List<PortMapping> activeForService = new ArrayList<>();
        for (final PortMapping pm : this.portMappings) {
            new PortMappingAdd(connectionService, registry.getUpnpService().getControlPoint(), pm) { // from class: org.fourthline.cling.support.igd.PortMappingListener.1
                @Override // org.fourthline.cling.controlpoint.ActionCallback
                public void success(ActionInvocation invocation) {
                    Logger logger = PortMappingListener.log;
                    logger.fine("Port mapping added: " + pm);
                    activeForService.add(pm);
                }

                @Override // org.fourthline.cling.controlpoint.ActionCallback
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    PortMappingListener portMappingListener = PortMappingListener.this;
                    portMappingListener.handleFailureMessage("Failed to add port mapping: " + pm);
                    PortMappingListener portMappingListener2 = PortMappingListener.this;
                    portMappingListener2.handleFailureMessage("Reason: " + defaultMsg);
                }
            }.run();
        }
        this.activePortMappings.put(connectionService, activeForService);
    }

    @Override // org.fourthline.cling.registry.DefaultRegistryListener
    public synchronized void deviceRemoved(Registry registry, Device device) {
        Service[] findServices;
        for (Service service : device.findServices()) {
            Iterator<Map.Entry<Service, List<PortMapping>>> it = this.activePortMappings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Service, List<PortMapping>> activeEntry = it.next();
                if (activeEntry.getKey().equals(service)) {
                    if (activeEntry.getValue().size() > 0) {
                        handleFailureMessage("Device disappeared, couldn't delete port mappings: " + activeEntry.getValue().size());
                    }
                    it.remove();
                }
            }
        }
    }

    @Override // org.fourthline.cling.registry.DefaultRegistryListener, org.fourthline.cling.registry.RegistryListener
    public synchronized void beforeShutdown(Registry registry) {
        for (Map.Entry<Service, List<PortMapping>> activeEntry : this.activePortMappings.entrySet()) {
            final Iterator<PortMapping> it = activeEntry.getValue().iterator();
            while (it.hasNext()) {
                final PortMapping pm = it.next();
                Logger logger = log;
                logger.fine("Trying to delete port mapping on IGD: " + pm);
                new PortMappingDelete(activeEntry.getKey(), registry.getUpnpService().getControlPoint(), pm) { // from class: org.fourthline.cling.support.igd.PortMappingListener.2
                    @Override // org.fourthline.cling.controlpoint.ActionCallback
                    public void success(ActionInvocation invocation) {
                        Logger logger2 = PortMappingListener.log;
                        logger2.fine("Port mapping deleted: " + pm);
                        it.remove();
                    }

                    @Override // org.fourthline.cling.controlpoint.ActionCallback
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        PortMappingListener portMappingListener = PortMappingListener.this;
                        portMappingListener.handleFailureMessage("Failed to delete port mapping: " + pm);
                        PortMappingListener portMappingListener2 = PortMappingListener.this;
                        portMappingListener2.handleFailureMessage("Reason: " + defaultMsg);
                    }
                }.run();
            }
        }
    }

    protected Service discoverConnectionService(Device device) {
        if (device.getType().equals(IGD_DEVICE_TYPE)) {
            Device[] connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE);
            if (connectionDevices.length == 0) {
                Logger logger = log;
                logger.fine("IGD doesn't support '" + CONNECTION_DEVICE_TYPE + "': " + device);
                return null;
            }
            Device connectionDevice = connectionDevices[0];
            Logger logger2 = log;
            logger2.fine("Using first discovered WAN connection device: " + connectionDevice);
            Service ipConnectionService = connectionDevice.findService(IP_SERVICE_TYPE);
            Service pppConnectionService = connectionDevice.findService(PPP_SERVICE_TYPE);
            if (ipConnectionService == null && pppConnectionService == null) {
                Logger logger3 = log;
                logger3.fine("IGD doesn't support IP or PPP WAN connection service: " + device);
            }
            return ipConnectionService != null ? ipConnectionService : pppConnectionService;
        }
        return null;
    }

    protected void handleFailureMessage(String s) {
        log.warning(s);
    }
}
