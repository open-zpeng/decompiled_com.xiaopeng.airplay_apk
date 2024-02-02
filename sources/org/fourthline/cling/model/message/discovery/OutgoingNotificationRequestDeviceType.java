package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.DeviceUSNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
/* loaded from: classes.dex */
public class OutgoingNotificationRequestDeviceType extends OutgoingNotificationRequest {
    public OutgoingNotificationRequestDeviceType(Location location, LocalDevice device, NotificationSubtype type) {
        super(location, device, type);
        getHeaders().add(UpnpHeader.Type.NT, new DeviceTypeHeader(device.getType()));
        getHeaders().add(UpnpHeader.Type.USN, new DeviceUSNHeader(device.getIdentity().getUdn(), device.getType()));
    }
}
