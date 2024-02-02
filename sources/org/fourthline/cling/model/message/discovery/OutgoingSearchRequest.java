package org.fourthline.cling.model.message.discovery;

import org.fourthline.cling.model.Constants;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.header.HostHeader;
import org.fourthline.cling.model.message.header.MANHeader;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.types.NotificationSubtype;
/* loaded from: classes.dex */
public class OutgoingSearchRequest extends OutgoingDatagramMessage<UpnpRequest> {
    private UpnpHeader searchTarget;

    public OutgoingSearchRequest(UpnpHeader searchTarget, int mxSeconds) {
        super(new UpnpRequest(UpnpRequest.Method.MSEARCH), ModelUtil.getInetAddressByName(Constants.IPV4_UPNP_MULTICAST_GROUP), Constants.UPNP_MULTICAST_PORT);
        this.searchTarget = searchTarget;
        getHeaders().add(UpnpHeader.Type.MAN, new MANHeader(NotificationSubtype.DISCOVER.getHeaderString()));
        getHeaders().add(UpnpHeader.Type.MX, new MXHeader(Integer.valueOf(mxSeconds)));
        getHeaders().add(UpnpHeader.Type.ST, searchTarget);
        getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
    }

    public UpnpHeader getSearchTarget() {
        return this.searchTarget;
    }
}
