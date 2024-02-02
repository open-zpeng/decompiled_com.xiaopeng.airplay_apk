package org.fourthline.cling.model.message.gena;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.header.EventSequenceHeader;
import org.fourthline.cling.model.message.header.NTEventHeader;
import org.fourthline.cling.model.message.header.NTSHeader;
import org.fourthline.cling.model.message.header.SubscriptionIdHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class IncomingEventRequestMessage extends StreamRequestMessage {
    private final RemoteService service;
    private final List<StateVariableValue> stateVariableValues;

    public IncomingEventRequestMessage(StreamRequestMessage source, RemoteService service) {
        super(source);
        this.stateVariableValues = new ArrayList();
        this.service = service;
    }

    public RemoteService getService() {
        return this.service;
    }

    public List<StateVariableValue> getStateVariableValues() {
        return this.stateVariableValues;
    }

    public String getSubscrptionId() {
        SubscriptionIdHeader header = (SubscriptionIdHeader) getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public UnsignedIntegerFourBytes getSequence() {
        EventSequenceHeader header = (EventSequenceHeader) getHeaders().getFirstHeader(UpnpHeader.Type.SEQ, EventSequenceHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public boolean hasNotificationHeaders() {
        UpnpHeader ntHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NT);
        UpnpHeader ntsHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NTS);
        return (ntHeader == null || ntHeader.getValue() == null || ntsHeader == null || ntsHeader.getValue() == null) ? false : true;
    }

    public boolean hasValidNotificationHeaders() {
        NTEventHeader ntHeader = (NTEventHeader) getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class);
        NTSHeader ntsHeader = (NTSHeader) getHeaders().getFirstHeader(UpnpHeader.Type.NTS, NTSHeader.class);
        return (ntHeader == null || ntHeader.getValue() == null || ntsHeader == null || !ntsHeader.getValue().equals(NotificationSubtype.PROPCHANGE)) ? false : true;
    }

    @Override // org.fourthline.cling.model.message.UpnpMessage
    public String toString() {
        return super.toString() + " SEQUENCE: " + getSequence().getValue();
    }
}
