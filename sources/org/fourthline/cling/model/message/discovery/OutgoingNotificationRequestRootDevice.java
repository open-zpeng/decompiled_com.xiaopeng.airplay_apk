package org.fourthline.cling.model.message.discovery;

import com.xpeng.airplay.service.NsdConstants;
import org.fourthline.cling.model.Constants;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.message.header.InterfaceMacHeader;
import org.fourthline.cling.model.message.header.RootDeviceHeader;
import org.fourthline.cling.model.message.header.USNRootDeviceHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.NotificationSubtype;
/* loaded from: classes.dex */
public class OutgoingNotificationRequestRootDevice extends OutgoingNotificationRequest {
    public OutgoingNotificationRequestRootDevice(Location location, LocalDevice device, NotificationSubtype type) {
        super(location, device, type);
        getHeaders().add(UpnpHeader.Type.NT, new RootDeviceHeader());
        getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(device.getIdentity().getUdn()));
        if (NsdConstants.AIRPLAY_TXT_VALUE_DA.equals(System.getProperty(Constants.SYSTEM_PROPERTY_ANNOUNCE_MAC_ADDRESS)) && location.getNetworkAddress().getHardwareAddress() != null) {
            getHeaders().add(UpnpHeader.Type.EXT_IFACE_MAC, new InterfaceMacHeader(location.getNetworkAddress().getHardwareAddress()));
        }
    }
}
