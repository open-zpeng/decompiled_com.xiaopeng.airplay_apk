package org.fourthline.cling.model.meta;

import java.net.InetAddress;
import java.net.URL;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.discovery.IncomingNotificationRequest;
import org.fourthline.cling.model.message.discovery.IncomingSearchResponse;
import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class RemoteDeviceIdentity extends DeviceIdentity {
    private final URL descriptorURL;
    private final InetAddress discoveredOnLocalAddress;
    private final byte[] interfaceMacAddress;

    public RemoteDeviceIdentity(UDN udn, RemoteDeviceIdentity template) {
        this(udn, template.getMaxAgeSeconds(), template.getDescriptorURL(), template.getInterfaceMacAddress(), template.getDiscoveredOnLocalAddress());
    }

    public RemoteDeviceIdentity(UDN udn, Integer maxAgeSeconds, URL descriptorURL, byte[] interfaceMacAddress, InetAddress discoveredOnLocalAddress) {
        super(udn, maxAgeSeconds);
        this.descriptorURL = descriptorURL;
        this.interfaceMacAddress = interfaceMacAddress;
        this.discoveredOnLocalAddress = discoveredOnLocalAddress;
    }

    public RemoteDeviceIdentity(IncomingNotificationRequest notificationRequest) {
        this(notificationRequest.getUDN(), notificationRequest.getMaxAge(), notificationRequest.getLocationURL(), notificationRequest.getInterfaceMacHeader(), notificationRequest.getLocalAddress());
    }

    public RemoteDeviceIdentity(IncomingSearchResponse searchResponse) {
        this(searchResponse.getRootDeviceUDN(), searchResponse.getMaxAge(), searchResponse.getLocationURL(), searchResponse.getInterfaceMacHeader(), searchResponse.getLocalAddress());
    }

    public URL getDescriptorURL() {
        return this.descriptorURL;
    }

    public byte[] getInterfaceMacAddress() {
        return this.interfaceMacAddress;
    }

    public InetAddress getDiscoveredOnLocalAddress() {
        return this.discoveredOnLocalAddress;
    }

    public byte[] getWakeOnLANBytes() {
        if (getInterfaceMacAddress() == null) {
            return null;
        }
        int i = 6;
        byte[] bytes = new byte[(16 * getInterfaceMacAddress().length) + 6];
        for (int i2 = 0; i2 < 6; i2++) {
            bytes[i2] = -1;
        }
        while (i < bytes.length) {
            System.arraycopy(getInterfaceMacAddress(), 0, bytes, i, getInterfaceMacAddress().length);
            i += getInterfaceMacAddress().length;
        }
        return bytes;
    }

    @Override // org.fourthline.cling.model.meta.DeviceIdentity
    public String toString() {
        if (ModelUtil.ANDROID_RUNTIME) {
            return "(RemoteDeviceIdentity) UDN: " + getUdn() + ", Descriptor: " + getDescriptorURL();
        }
        return "(" + getClass().getSimpleName() + ") UDN: " + getUdn() + ", Descriptor: " + getDescriptorURL();
    }
}
