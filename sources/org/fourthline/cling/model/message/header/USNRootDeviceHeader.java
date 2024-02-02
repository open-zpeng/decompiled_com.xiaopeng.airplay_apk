package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class USNRootDeviceHeader extends UpnpHeader<UDN> {
    public static final String ROOT_DEVICE_SUFFIX = "::upnp:rootdevice";

    public USNRootDeviceHeader() {
    }

    public USNRootDeviceHeader(UDN udn) {
        setValue(udn);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (!s.startsWith("uuid:") || !s.endsWith(ROOT_DEVICE_SUFFIX)) {
            throw new InvalidHeaderException("Invalid root device USN header value, must start with 'uuid:' and end with '::upnp:rootdevice' but is '" + s + "'");
        }
        UDN udn = new UDN(s.substring("uuid:".length(), s.length() - ROOT_DEVICE_SUFFIX.length()));
        setValue(udn);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString() + ROOT_DEVICE_SUFFIX;
    }
}
