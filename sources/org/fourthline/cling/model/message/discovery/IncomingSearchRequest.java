package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.header.MANHeader;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.types.NotificationSubtype;
/* loaded from: classes.dex */
public class IncomingSearchRequest extends IncomingDatagramMessage<UpnpRequest> {
    public IncomingSearchRequest(IncomingDatagramMessage<UpnpRequest> source) {
        super(source);
    }

    public UpnpHeader getSearchTarget() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.ST);
    }

    public Integer getMX() {
        MXHeader header = (MXHeader) getHeaders().getFirstHeader(UpnpHeader.Type.MX, MXHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public boolean isMANSSDPDiscover() {
        MANHeader header = (MANHeader) getHeaders().getFirstHeader(UpnpHeader.Type.MAN, MANHeader.class);
        return header != null && header.getValue().equals(NotificationSubtype.DISCOVER.getHeaderString());
    }
}
