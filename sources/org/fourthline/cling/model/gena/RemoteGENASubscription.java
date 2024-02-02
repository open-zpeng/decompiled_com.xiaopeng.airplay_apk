package org.fourthline.cling.model.gena;

import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.fourthline.cling.model.Location;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public abstract class RemoteGENASubscription extends GENASubscription<RemoteService> {
    protected PropertyChangeSupport propertyChangeSupport;

    public abstract void ended(CancelReason cancelReason, UpnpResponse upnpResponse);

    public abstract void eventsMissed(int i);

    public abstract void failed(UpnpResponse upnpResponse);

    public abstract void invalidMessage(UnsupportedDataException unsupportedDataException);

    /* JADX INFO: Access modifiers changed from: protected */
    public RemoteGENASubscription(RemoteService service, int requestedDurationSeconds) {
        super(service, requestedDurationSeconds);
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public synchronized URL getEventSubscriptionURL() {
        return getService().getDevice().normalizeURI(getService().getEventSubscriptionURI());
    }

    public synchronized List<URL> getEventCallbackURLs(List<NetworkAddress> activeStreamServers, Namespace namespace) {
        List<URL> callbackURLs;
        callbackURLs = new ArrayList<>();
        for (NetworkAddress activeStreamServer : activeStreamServers) {
            callbackURLs.add(new Location(activeStreamServer, namespace.getEventCallbackPathString(getService())).getURL());
        }
        return callbackURLs;
    }

    public synchronized void establish() {
        established();
    }

    public synchronized void fail(UpnpResponse responseStatus) {
        failed(responseStatus);
    }

    public synchronized void end(CancelReason reason, UpnpResponse response) {
        ended(reason, response);
    }

    public synchronized void receive(UnsignedIntegerFourBytes sequence, Collection<StateVariableValue> newValues) {
        if (this.currentSequence != null) {
            if (this.currentSequence.getValue().equals(Long.valueOf(this.currentSequence.getBits().getMaxValue())) && sequence.getValue().longValue() == 1) {
                System.err.println("TODO: HANDLE ROLLOVER");
                return;
            } else if (this.currentSequence.getValue().longValue() >= sequence.getValue().longValue()) {
                return;
            } else {
                long expectedValue = this.currentSequence.getValue().longValue() + 1;
                int difference = (int) (sequence.getValue().longValue() - expectedValue);
                if (difference != 0) {
                    eventsMissed(difference);
                }
            }
        }
        this.currentSequence = sequence;
        for (StateVariableValue newValue : newValues) {
            this.currentValues.put(newValue.getStateVariable().getName(), newValue);
        }
        eventReceived();
    }

    @Override // org.fourthline.cling.model.gena.GENASubscription
    public String toString() {
        return "(SID: " + getSubscriptionId() + ") " + getService();
    }
}
