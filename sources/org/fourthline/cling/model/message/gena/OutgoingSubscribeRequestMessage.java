package org.fourthline.cling.model.message.gena;

import java.net.URL;
import java.util.List;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.header.CallbackHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
/* loaded from: classes.dex */
public class OutgoingSubscribeRequestMessage extends StreamRequestMessage {
    public OutgoingSubscribeRequestMessage(RemoteGENASubscription subscription, List<URL> callbackURLs, UpnpHeaders extraHeaders) {
        super(UpnpRequest.Method.SUBSCRIBE, subscription.getEventSubscriptionURL());
        getHeaders().add(UpnpHeader.Type.CALLBACK, new CallbackHeader(callbackURLs));
        getHeaders().add(UpnpHeader.Type.NT, new NTEventHeader());
        getHeaders().add(UpnpHeader.Type.TIMEOUT, new TimeoutHeader(subscription.getRequestedDurationSeconds()));
        if (extraHeaders != null) {
            getHeaders().putAll(extraHeaders);
        }
    }

    public boolean hasCallbackURLs() {
        CallbackHeader callbackHeader = (CallbackHeader) getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class);
        return callbackHeader.getValue().size() > 0;
    }
}
