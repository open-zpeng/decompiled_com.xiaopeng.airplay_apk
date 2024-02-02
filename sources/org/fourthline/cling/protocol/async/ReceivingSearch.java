package org.fourthline.cling.protocol.async;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.discovery.IncomingSearchRequest;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponse;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseDeviceType;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseRootDevice;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseServiceType;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponseUDN;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.RootDeviceHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.ServiceTypeHeader;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public class ReceivingSearch extends ReceivingAsync<IncomingSearchRequest> {
    private final String TAG;
    protected List<NetworkAddress> activeStreamServers;
    protected Map<LocalDevice, Location> locations;
    protected final Random randomGenerator;
    protected Map<LocalDevice, OutgoingSearchResponse> searchRespRootDevice;
    private static final Logger log = Logger.getLogger(ReceivingSearch.class.getName());
    private static final boolean LOG_ENABLED = log.isLoggable(Level.FINE);

    public ReceivingSearch(UpnpService upnpService, IncomingDatagramMessage<UpnpRequest> inputMessage) {
        super(upnpService, new IncomingSearchRequest(inputMessage));
        this.TAG = "ReceivingSearch";
        this.randomGenerator = new Random();
        this.activeStreamServers = new ArrayList();
        this.locations = new HashMap();
        this.searchRespRootDevice = new HashMap();
        try {
            this.activeStreamServers.addAll(upnpService.getRouter().getActiveStreamServers(inputMessage.getLocalAddress()));
        } catch (RouterException e) {
            log.warning("fail to get active servers");
        }
    }

    @Override // org.fourthline.cling.protocol.ReceivingAsync
    protected void execute() throws RouterException {
        if (getUpnpService().getRouter() == null) {
            Log.i("ReceivingSearch", "Router hasn't completed initialization, ignoring received search message");
        } else if (!getInputMessage().isMANSSDPDiscover()) {
            Log.i("ReceivingSearch", "Invalid search request, no or invalid MAN ssdp:discover header: " + getInputMessage());
        } else {
            UpnpHeader searchTarget = getInputMessage().getSearchTarget();
            if (searchTarget == null) {
                Log.i("ReceivingSearch", "Invalid search request, did not contain ST header: " + getInputMessage().toString());
                return;
            }
            if (this.activeStreamServers.size() == 0) {
                this.activeStreamServers = getUpnpService().getRouter().getActiveStreamServers(getInputMessage().getLocalAddress());
                Log.e("ReceivingSearch", "no active stream servers found (network disabled?)");
            }
            for (NetworkAddress activeStreamServer : this.activeStreamServers) {
                sendResponses(searchTarget, activeStreamServer);
            }
        }
    }

    @Override // org.fourthline.cling.protocol.ReceivingAsync
    protected boolean waitBeforeExecution() throws InterruptedException {
        Integer mx = getInputMessage().getMX();
        if (mx == null) {
            Log.e("ReceivingSearch", "Invalid search request, did not contain MX header: " + getInputMessage());
            return false;
        }
        return true;
    }

    private void waitForMoment() throws InterruptedException {
        Integer mx = getInputMessage().getMX();
        if (mx == null) {
            Log.e("ReceivingSearch", "Invalid search request, did not contain MX header: " + getInputMessage());
            return;
        }
        if (mx.intValue() > 120 || mx.intValue() <= 0) {
            mx = MXHeader.DEFAULT_VALUE;
        }
        if (getUpnpService().getRegistry().getLocalDevices().size() > 0) {
            int sleepTime = this.randomGenerator.nextInt(mx.intValue() * 300);
            Thread.sleep(sleepTime);
        }
    }

    protected void sendResponses(UpnpHeader searchTarget, NetworkAddress activeStreamServer) throws RouterException {
        if (searchTarget instanceof STAllHeader) {
            sendSearchResponseAll(activeStreamServer);
        } else if (searchTarget instanceof RootDeviceHeader) {
            sendSearchResponseRootDevices(activeStreamServer);
        } else if (searchTarget instanceof UDNHeader) {
            sendSearchResponseUDN((UDN) searchTarget.getValue(), activeStreamServer);
        } else if (searchTarget instanceof DeviceTypeHeader) {
            sendSearchResponseDeviceType((DeviceType) searchTarget.getValue(), activeStreamServer);
        } else if (searchTarget instanceof ServiceTypeHeader) {
            sendSearchResponseServiceType((ServiceType) searchTarget.getValue(), activeStreamServer);
        } else {
            Logger logger = log;
            logger.warning("Non-implemented search request target: " + searchTarget.getClass());
        }
    }

    protected void sendSearchResponseAll(NetworkAddress activeStreamServer) throws RouterException {
        LocalDevice[] findEmbeddedDevices;
        Log.d("ReceivingSearch", "Responding to 'all' search with advertisement messages for all local devices");
        for (LocalDevice localDevice : getUpnpService().getRegistry().getLocalDevices()) {
            if (!isAdvertisementDisabled(localDevice)) {
                if (LOG_ENABLED) {
                    log.finer("Sending root device messages: " + localDevice);
                }
                List<OutgoingSearchResponse> rootDeviceMsgs = createDeviceMessages(localDevice, activeStreamServer);
                for (OutgoingSearchResponse upnpMessage : rootDeviceMsgs) {
                    getUpnpService().getRouter().send(upnpMessage);
                }
                if (localDevice.hasEmbeddedDevices()) {
                    for (LocalDevice embeddedDevice : localDevice.findEmbeddedDevices()) {
                        if (LOG_ENABLED) {
                            log.finer("Sending embedded device messages: " + embeddedDevice);
                        }
                        List<OutgoingSearchResponse> embeddedDeviceMsgs = createDeviceMessages(embeddedDevice, activeStreamServer);
                        for (OutgoingSearchResponse upnpMessage2 : embeddedDeviceMsgs) {
                            getUpnpService().getRouter().send(upnpMessage2);
                        }
                    }
                }
                List<OutgoingSearchResponse> serviceTypeMsgs = createServiceTypeMessages(localDevice, activeStreamServer);
                if (serviceTypeMsgs.size() > 0) {
                    if (LOG_ENABLED) {
                        log.finer("Sending service type messages");
                    }
                    for (OutgoingSearchResponse upnpMessage3 : serviceTypeMsgs) {
                        getUpnpService().getRouter().send(upnpMessage3);
                    }
                }
            }
        }
    }

    protected List<OutgoingSearchResponse> createDeviceMessages(LocalDevice device, NetworkAddress activeStreamServer) {
        List<OutgoingSearchResponse> msgs = new ArrayList<>();
        if (device.isRoot()) {
            msgs.add(new OutgoingSearchResponseRootDevice(getInputMessage(), getDescriptorLocation(activeStreamServer, device), device));
        }
        msgs.add(new OutgoingSearchResponseUDN(getInputMessage(), getDescriptorLocation(activeStreamServer, device), device));
        msgs.add(new OutgoingSearchResponseDeviceType(getInputMessage(), getDescriptorLocation(activeStreamServer, device), device));
        for (OutgoingSearchResponse msg : msgs) {
            prepareOutgoingSearchResponse(msg);
        }
        return msgs;
    }

    protected List<OutgoingSearchResponse> createServiceTypeMessages(LocalDevice device, NetworkAddress activeStreamServer) {
        ServiceType[] findServiceTypes;
        List<OutgoingSearchResponse> msgs = new ArrayList<>();
        for (ServiceType serviceType : device.findServiceTypes()) {
            OutgoingSearchResponse message = new OutgoingSearchResponseServiceType(getInputMessage(), getDescriptorLocation(activeStreamServer, device), device, serviceType);
            prepareOutgoingSearchResponse(message);
            msgs.add(message);
        }
        return msgs;
    }

    protected void sendSearchResponseRootDevices(NetworkAddress activeStreamServer) throws RouterException {
        Log.d("ReceivingSearch", "Responding to root device search with advertisement messages for all local root devices");
        for (LocalDevice device : getUpnpService().getRegistry().getLocalDevices()) {
            if (!isAdvertisementDisabled(device)) {
                OutgoingSearchResponse response = this.searchRespRootDevice.get(device);
                if (response == null) {
                    response = new OutgoingSearchResponseRootDevice(getInputMessage(), getDescriptorLocation(activeStreamServer, device), device);
                    this.searchRespRootDevice.put(device, response);
                } else {
                    response.setDestinationAddress(getInputMessage().getSourceAddress());
                    response.setDestinationPort(getInputMessage().getSourcePort());
                }
                response.getHeaders().log();
                prepareOutgoingSearchResponse(response);
                getUpnpService().getRouter().send(response);
                try {
                    waitForMoment();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    protected void sendSearchResponseUDN(UDN udn, NetworkAddress activeStreamServer) throws RouterException {
        Device device = getUpnpService().getRegistry().getDevice(udn, false);
        if (device == null || !(device instanceof LocalDevice) || isAdvertisementDisabled((LocalDevice) device)) {
            return;
        }
        Log.d("ReceivingSearch", "Responding to UDN device search: " + udn);
        OutgoingSearchResponse message = new OutgoingSearchResponseUDN(getInputMessage(), getDescriptorLocation(activeStreamServer, (LocalDevice) device), (LocalDevice) device);
        prepareOutgoingSearchResponse(message);
        getUpnpService().getRouter().send(message);
    }

    protected void sendSearchResponseDeviceType(DeviceType deviceType, NetworkAddress activeStreamServer) throws RouterException {
        Log.d("ReceivingSearch", "Responding to device type search: " + deviceType);
        Collection<Device> devices = getUpnpService().getRegistry().getDevices(deviceType);
        for (Device device : devices) {
            if ((device instanceof LocalDevice) && !isAdvertisementDisabled((LocalDevice) device)) {
                Log.d("ReceivingSearch", "Sending matching device type search result for: " + device);
                OutgoingSearchResponse message = new OutgoingSearchResponseDeviceType(getInputMessage(), getDescriptorLocation(activeStreamServer, (LocalDevice) device), (LocalDevice) device);
                prepareOutgoingSearchResponse(message);
                getUpnpService().getRouter().send(message);
            }
        }
    }

    protected void sendSearchResponseServiceType(ServiceType serviceType, NetworkAddress activeStreamServer) throws RouterException {
        Log.d("ReceivingSearch", "Responding to service type search: " + serviceType);
        Collection<Device> devices = getUpnpService().getRegistry().getDevices(serviceType);
        for (Device device : devices) {
            if ((device instanceof LocalDevice) && !isAdvertisementDisabled((LocalDevice) device)) {
                Log.i("ReceivingSearch", "Sending matching service type search result: " + device);
                OutgoingSearchResponse message = new OutgoingSearchResponseServiceType(getInputMessage(), getDescriptorLocation(activeStreamServer, (LocalDevice) device), (LocalDevice) device, serviceType);
                prepareOutgoingSearchResponse(message);
                getUpnpService().getRouter().send(message);
            }
        }
    }

    protected Location getDescriptorLocation(NetworkAddress activeStreamServer, LocalDevice device) {
        Location location = this.locations.get(device);
        if (location == null) {
            Location location2 = new Location(activeStreamServer, getUpnpService().getConfiguration().getNamespace().getDescriptorPathString(device));
            this.locations.put(device, location2);
            return location2;
        }
        location.setNetworkAddress(activeStreamServer);
        return location;
    }

    protected boolean isAdvertisementDisabled(LocalDevice device) {
        DiscoveryOptions options = getUpnpService().getRegistry().getDiscoveryOptions(device.getIdentity().getUdn());
        return (options == null || options.isAdvertised()) ? false : true;
    }

    protected void prepareOutgoingSearchResponse(OutgoingSearchResponse message) {
    }
}
