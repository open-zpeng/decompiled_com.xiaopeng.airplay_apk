package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
/* loaded from: classes.dex */
public class PeerManagerHeader extends DLNAHeader<ServiceReference> {
    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            try {
                ServiceReference serviceReference = new ServiceReference(s);
                if (serviceReference.getUdn() != null && serviceReference.getServiceId() != null) {
                    setValue(serviceReference);
                    return;
                }
            } catch (Exception e) {
            }
        }
        throw new InvalidHeaderException("Invalid PeerManager header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
