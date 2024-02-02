package org.fourthline.cling.protocol.sync;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.model.message.gena.OutgoingEventResponseMessage;
import org.fourthline.cling.model.resource.ServiceEventCallbackResource;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public class ReceivingEvent extends ReceivingSync<StreamRequestMessage, OutgoingEventResponseMessage> {
    private static final Logger log = Logger.getLogger(ReceivingEvent.class.getName());

    public ReceivingEvent(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.protocol.ReceivingSync
    public OutgoingEventResponseMessage executeSync() throws RouterException {
        if (!((StreamRequestMessage) getInputMessage()).isContentTypeTextUDA()) {
            Logger logger = log;
            logger.warning("Received without or with invalid Content-Type: " + getInputMessage());
        }
        ServiceEventCallbackResource resource = (ServiceEventCallbackResource) getUpnpService().getRegistry().getResource(ServiceEventCallbackResource.class, ((StreamRequestMessage) getInputMessage()).getUri());
        if (resource == null) {
            Logger logger2 = log;
            logger2.fine("No local resource found: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.NOT_FOUND));
        }
        final IncomingEventRequestMessage requestMessage = new IncomingEventRequestMessage((StreamRequestMessage) getInputMessage(), resource.getModel());
        if (requestMessage.getSubscrptionId() == null) {
            Logger logger3 = log;
            logger3.fine("Subscription ID missing in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        } else if (!requestMessage.hasValidNotificationHeaders()) {
            Logger logger4 = log;
            logger4.fine("Missing NT and/or NTS headers in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.BAD_REQUEST));
        } else if (!requestMessage.hasValidNotificationHeaders()) {
            Logger logger5 = log;
            logger5.fine("Invalid NT and/or NTS headers in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        } else if (requestMessage.getSequence() == null) {
            Logger logger6 = log;
            logger6.fine("Sequence missing in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        } else {
            try {
                getUpnpService().getConfiguration().getGenaEventProcessor().readBody(requestMessage);
                final RemoteGENASubscription subscription = getUpnpService().getRegistry().getWaitRemoteSubscription(requestMessage.getSubscrptionId());
                if (subscription == null) {
                    Logger logger7 = log;
                    logger7.severe("Invalid subscription ID, no active subscription: " + requestMessage);
                    return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
                }
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.ReceivingEvent.2
                    @Override // java.lang.Runnable
                    public void run() {
                        ReceivingEvent.log.fine("Calling active subscription with event state variable values");
                        subscription.receive(requestMessage.getSequence(), requestMessage.getStateVariableValues());
                    }
                });
                return new OutgoingEventResponseMessage();
            } catch (UnsupportedDataException ex) {
                Logger logger8 = log;
                logger8.fine("Can't read event message request body, " + ex);
                final RemoteGENASubscription subscription2 = getUpnpService().getRegistry().getRemoteSubscription(requestMessage.getSubscrptionId());
                if (subscription2 != null) {
                    getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.ReceivingEvent.1
                        @Override // java.lang.Runnable
                        public void run() {
                            subscription2.invalidMessage(ex);
                        }
                    });
                }
                return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.INTERNAL_SERVER_ERROR));
            }
        }
    }
}
