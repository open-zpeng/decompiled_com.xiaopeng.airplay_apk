package org.fourthline.cling.model.message.gena;

import java.net.URL;
import java.util.List;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.header.CallbackHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.TimeoutHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalService;
/* loaded from: classes.dex */
public class IncomingSubscribeRequestMessage extends StreamRequestMessage {
    private final LocalService service;

    public IncomingSubscribeRequestMessage(StreamRequestMessage source, LocalService service) {
        super(source);
        this.service = service;
    }

    public LocalService getService() {
        return this.service;
    }

    public List<URL> getCallbackURLs() {
        CallbackHeader header = (CallbackHeader) getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public boolean hasNotificationHeader() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class) != null;
    }

    public Integer getRequestedTimeoutSeconds() {
        TimeoutHeader timeoutHeader = (TimeoutHeader) getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class);
        if (timeoutHeader != null) {
            return timeoutHeader.getValue();
        }
        return null;
    }

    public String getSubscriptionId() {
        SubscriptionIdHeader header = (SubscriptionIdHeader) getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }
}
