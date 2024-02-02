package org.fourthline.cling.protocol.async;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequest;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestDeviceType;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestRootDevice;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestServiceType;
import org.fourthline.cling.model.message.discovery.OutgoingNotificationRequestUDN;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.protocol.SendingAsync;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public abstract class SendingNotification extends SendingAsync {
    private static final Logger log = Logger.getLogger(SendingNotification.class.getName());
    private List<NetworkAddress> activeStreamServers;
    private List<Location> descriptorLocations;
    private LocalDevice device;
    private List<OutgoingNotificationRequest> rootDeviceMsgs;
    private List<OutgoingNotificationRequest> serviceTypeMsgs;

    protected abstract NotificationSubtype getNotificationSubtype();

    public SendingNotification(UpnpService upnpService, LocalDevice device) {
        super(upnpService);
        this.descriptorLocations = new ArrayList();
        this.rootDeviceMsgs = new ArrayList();
        this.serviceTypeMsgs = new ArrayList();
        this.activeStreamServers = new ArrayList();
        this.device = device;
    }

    private void updateDeviceLocations() {
        for (NetworkAddress activeStreamServer : this.activeStreamServers) {
            this.descriptorLocations.add(new Location(activeStreamServer, getUpnpService().getConfiguration().getNamespace().getDescriptorPathString(getDevice())));
        }
    }

    public LocalDevice getDevice() {
        return this.device;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.protocol.SendingAsync
    public void execute() throws RouterException {
        if (this.activeStreamServers.size() == 0) {
            log.fine("no active stream servers found (network disabled?)");
            try {
                this.activeStreamServers.addAll(getUpnpService().getRouter().getActiveStreamServers(null));
            } catch (RouterException e) {
                log.warning("fail to get active stream servers");
            }
        }
        if (this.descriptorLocations.size() == 0) {
            updateDeviceLocations();
        }
        for (int i = 0; i < getBulkRepeat(); i++) {
            try {
                for (Location descriptorLocation : this.descriptorLocations) {
                    sendMessages(descriptorLocation);
                }
                Logger logger = log;
                logger.finer("Sleeping " + getBulkIntervalMilliseconds() + " milliseconds");
                Thread.sleep((long) getBulkIntervalMilliseconds());
            } catch (InterruptedException ex) {
                Logger logger2 = log;
                logger2.warning("Advertisement thread was interrupted: " + ex);
            } catch (RouterException re) {
                Logger logger3 = log;
                logger3.warning("fail to send message: " + re);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getBulkRepeat() {
        return 3;
    }

    protected int getBulkIntervalMilliseconds() {
        return 60;
    }

    public void sendMessages(Location descriptorLocation) throws RouterException {
        LocalDevice[] findEmbeddedDevices;
        log.finer("Sending root device messages: " + getDevice());
        if (this.rootDeviceMsgs.size() == 0) {
            this.rootDeviceMsgs.addAll(createDeviceMessages(getDevice(), descriptorLocation));
        }
        for (OutgoingNotificationRequest upnpMessage : this.rootDeviceMsgs) {
            getUpnpService().getRouter().send(upnpMessage);
        }
        if (getDevice().hasEmbeddedDevices()) {
            for (LocalDevice embeddedDevice : getDevice().findEmbeddedDevices()) {
                log.finer("Sending embedded device messages: " + embeddedDevice);
                List<OutgoingNotificationRequest> embeddedDeviceMsgs = createDeviceMessages(embeddedDevice, descriptorLocation);
                for (OutgoingNotificationRequest upnpMessage2 : embeddedDeviceMsgs) {
                    getUpnpService().getRouter().send(upnpMessage2);
                }
            }
        }
        if (this.serviceTypeMsgs.size() == 0) {
            this.serviceTypeMsgs.addAll(createServiceTypeMessages(getDevice(), descriptorLocation));
        }
        if (this.serviceTypeMsgs.size() > 0) {
            log.finer("Sending service type messages");
            for (OutgoingNotificationRequest upnpMessage3 : this.serviceTypeMsgs) {
                getUpnpService().getRouter().send(upnpMessage3);
            }
        }
    }

    protected List<OutgoingNotificationRequest> createDeviceMessages(LocalDevice device, Location descriptorLocation) {
        List<OutgoingNotificationRequest> msgs = new ArrayList<>();
        if (device.isRoot()) {
            msgs.add(new OutgoingNotificationRequestRootDevice(descriptorLocation, device, getNotificationSubtype()));
        }
        msgs.add(new OutgoingNotificationRequestUDN(descriptorLocation, device, getNotificationSubtype()));
        msgs.add(new OutgoingNotificationRequestDeviceType(descriptorLocation, device, getNotificationSubtype()));
        return msgs;
    }

    protected List<OutgoingNotificationRequest> createServiceTypeMessages(LocalDevice device, Location descriptorLocation) {
        ServiceType[] findServiceTypes;
        List<OutgoingNotificationRequest> msgs = new ArrayList<>();
        for (ServiceType serviceType : device.findServiceTypes()) {
            msgs.add(new OutgoingNotificationRequestServiceType(descriptorLocation, device, getNotificationSubtype(), serviceType));
        }
        return msgs;
    }
}
