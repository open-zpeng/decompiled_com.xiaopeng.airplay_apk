package org.fourthline.cling.model.message.gena;

import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
/* loaded from: classes.dex */
public class OutgoingUnsubscribeRequestMessage extends StreamRequestMessage {
    public OutgoingUnsubscribeRequestMessage(RemoteGENASubscription subscription, UpnpHeaders extraHeaders) {
        super(UpnpRequest.Method.UNSUBSCRIBE, subscription.getEventSubscriptionURL());
        getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscription.getSubscriptionId()));
        if (extraHeaders != null) {
            getHeaders().putAll(extraHeaders);
        }
    }
}
