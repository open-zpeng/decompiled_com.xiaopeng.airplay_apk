package org.fourthline.cling.protocol.sync;

import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.gena.IncomingSubscribeRequestMessage;
import org.fourthline.cling.model.message.gena.OutgoingSubscribeResponseMessage;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.ServiceEventSubscriptionResource;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public class ReceivingSubscribe extends ReceivingSync<StreamRequestMessage, OutgoingSubscribeResponseMessage> {
    private static final Logger log = Logger.getLogger(ReceivingSubscribe.class.getName());
    protected LocalGENASubscription subscription;

    public ReceivingSubscribe(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.protocol.ReceivingSync
    public OutgoingSubscribeResponseMessage executeSync() throws RouterException {
        ServiceEventSubscriptionResource resource = (ServiceEventSubscriptionResource) getUpnpService().getRegistry().getResource(ServiceEventSubscriptionResource.class, ((StreamRequestMessage) getInputMessage()).getUri());
        if (resource == null) {
            Logger logger = log;
            logger.fine("No local resource found: " + getInputMessage());
            return null;
        }
        Logger logger2 = log;
        logger2.fine("Found local event subscription matching relative request URI: " + ((StreamRequestMessage) getInputMessage()).getUri());
        IncomingSubscribeRequestMessage requestMessage = new IncomingSubscribeRequestMessage((StreamRequestMessage) getInputMessage(), resource.getModel());
        if (requestMessage.getSubscriptionId() != null && (requestMessage.hasNotificationHeader() || requestMessage.getCallbackURLs() != null)) {
            Logger logger3 = log;
            logger3.fine("Subscription ID and NT or Callback in subscribe request: " + getInputMessage());
            return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.BAD_REQUEST);
        } else if (requestMessage.getSubscriptionId() != null) {
            return processRenewal(resource.getModel(), requestMessage);
        } else {
            if (requestMessage.hasNotificationHeader() && requestMessage.getCallbackURLs() != null) {
                return processNewSubscription(resource.getModel(), requestMessage);
            }
            Logger logger4 = log;
            logger4.fine("No subscription ID, no NT or Callback, neither subscription or renewal: " + getInputMessage());
            return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
        }
    }

    protected OutgoingSubscribeResponseMessage processRenewal(LocalService service, IncomingSubscribeRequestMessage requestMessage) {
        this.subscription = getUpnpService().getRegistry().getLocalSubscription(requestMessage.getSubscriptionId());
        if (this.subscription == null) {
            Logger logger = log;
            logger.fine("Invalid subscription ID for renewal request: " + getInputMessage());
            return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
        }
        Logger logger2 = log;
        logger2.fine("Renewing subscription: " + this.subscription);
        this.subscription.setSubscriptionDuration(requestMessage.getRequestedTimeoutSeconds());
        if (getUpnpService().getRegistry().updateLocalSubscription(this.subscription)) {
            return new OutgoingSubscribeResponseMessage(this.subscription);
        }
        Logger logger3 = log;
        logger3.fine("Subscription went away before it could be renewed: " + getInputMessage());
        return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
    }

    protected OutgoingSubscribeResponseMessage processNewSubscription(LocalService service, IncomingSubscribeRequestMessage requestMessage) {
        Integer timeoutSeconds;
        List<URL> callbackURLs = requestMessage.getCallbackURLs();
        if (callbackURLs == null || callbackURLs.size() == 0) {
            Logger logger = log;
            logger.fine("Missing or invalid Callback URLs in subscribe request: " + getInputMessage());
            return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
        } else if (!requestMessage.hasNotificationHeader()) {
            Logger logger2 = log;
            logger2.fine("Missing or invalid NT header in subscribe request: " + getInputMessage());
            return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
        } else {
            if (getUpnpService().getConfiguration().isReceivedSubscriptionTimeoutIgnored()) {
                timeoutSeconds = null;
            } else {
                timeoutSeconds = requestMessage.getRequestedTimeoutSeconds();
            }
            try {
                this.subscription = new LocalGENASubscription(service, timeoutSeconds, callbackURLs) { // from class: org.fourthline.cling.protocol.sync.ReceivingSubscribe.1
                    @Override // org.fourthline.cling.model.gena.GENASubscription
                    public void established() {
                    }

                    @Override // org.fourthline.cling.model.gena.LocalGENASubscription
                    public void ended(CancelReason reason) {
                    }

                    @Override // org.fourthline.cling.model.gena.GENASubscription
                    public void eventReceived() {
                        ReceivingSubscribe.this.getUpnpService().getConfiguration().getSyncProtocolExecutorService().execute(ReceivingSubscribe.this.getUpnpService().getProtocolFactory().createSendingEvent(this));
                    }
                };
                Logger logger3 = log;
                logger3.info("Adding subscription to registry: " + this.subscription);
                getUpnpService().getRegistry().addLocalSubscription(this.subscription);
                log.fine("Returning subscription response, waiting to send initial event");
                return new OutgoingSubscribeResponseMessage(this.subscription);
            } catch (Exception ex) {
                Logger logger4 = log;
                logger4.warning("Couldn't create local subscription to service: " + Exceptions.unwrap(ex));
                ex.printStackTrace();
                return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override // org.fourthline.cling.protocol.ReceivingSync
    public void responseSent(StreamResponseMessage responseMessage) {
        if (this.subscription == null) {
            return;
        }
        if (responseMessage == null || responseMessage.getOperation().isFailed() || this.subscription.getCurrentSequence().getValue().longValue() != 0) {
            if (this.subscription.getCurrentSequence().getValue().longValue() == 0) {
                log.fine("Subscription request's response aborted, not sending initial event");
                if (responseMessage == null) {
                    log.fine("Reason: No response at all from subscriber");
                } else {
                    Logger logger = log;
                    logger.fine("Reason: " + responseMessage.getOperation());
                }
                Logger logger2 = log;
                logger2.info("Removing subscription from registry: " + this.subscription);
                getUpnpService().getRegistry().removeLocalSubscription(this.subscription);
                return;
            }
            return;
        }
        log.fine("Establishing subscription");
        this.subscription.registerOnService();
        this.subscription.establish();
        log.info("Response to subscription sent successfully, now sending initial event asynchronously");
        getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(getUpnpService().getProtocolFactory().createSendingEvent(this.subscription));
    }

    @Override // org.fourthline.cling.protocol.ReceivingSync
    public void responseException(Throwable t) {
        if (this.subscription == null) {
            return;
        }
        Logger logger = log;
        logger.fine("Response could not be send to subscriber, removing local GENA subscription: " + this.subscription);
        getUpnpService().getRegistry().removeLocalSubscription(this.subscription);
    }
}
