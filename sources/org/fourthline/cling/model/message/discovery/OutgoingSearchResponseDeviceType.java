package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.DeviceUSNHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
/* loaded from: classes.dex */
public class OutgoingSearchResponseDeviceType extends OutgoingSearchResponse {
    public OutgoingSearchResponseDeviceType(IncomingDatagramMessage request, Location location, LocalDevice device) {
        super(request, location, device);
        getHeaders().add(UpnpHeader.Type.ST, new DeviceTypeHeader(device.getType()));
        getHeaders().add(UpnpHeader.Type.USN, new DeviceUSNHeader(device.getIdentity().getUdn(), device.getType()));
    }
}
