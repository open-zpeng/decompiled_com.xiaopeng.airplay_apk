package org.fourthline.cling.protocol.sync;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.gena.IncomingSubscribeResponseMessage;
import org.fourthline.cling.model.message.gena.OutgoingRenewalRequestMessage;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public class SendingRenewal extends SendingSync<OutgoingRenewalRequestMessage, IncomingSubscribeResponseMessage> {
    private static final Logger log = Logger.getLogger(SendingRenewal.class.getName());
    protected final RemoteGENASubscription subscription;

    public SendingRenewal(UpnpService upnpService, RemoteGENASubscription subscription) {
        super(upnpService, new OutgoingRenewalRequestMessage(subscription, upnpService.getConfiguration().getEventSubscriptionHeaders(subscription.getService())));
        this.subscription = subscription;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.protocol.SendingSync
    public IncomingSubscribeResponseMessage executeSync() throws RouterException {
        Logger logger = log;
        logger.fine("Sending subscription renewal request: " + getInputMessage());
        try {
            StreamResponseMessage response = getUpnpService().getRouter().send(getInputMessage());
            if (response == null) {
                onRenewalFailure();
                return null;
            }
            final IncomingSubscribeResponseMessage responseMessage = new IncomingSubscribeResponseMessage(response);
            if (response.getOperation().isFailed()) {
                Logger logger2 = log;
                logger2.fine("Subscription renewal failed, response was: " + response);
                getUpnpService().getRegistry().removeRemoteSubscription(this.subscription);
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingRenewal.1
                    @Override // java.lang.Runnable
                    public void run() {
                        SendingRenewal.this.subscription.end(CancelReason.RENEWAL_FAILED, responseMessage.getOperation());
                    }
                });
            } else if (!responseMessage.isValidHeaders()) {
                log.severe("Subscription renewal failed, invalid or missing (SID, Timeout) response headers");
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingRenewal.2
                    @Override // java.lang.Runnable
                    public void run() {
                        SendingRenewal.this.subscription.end(CancelReason.RENEWAL_FAILED, responseMessage.getOperation());
                    }
                });
            } else {
                Logger logger3 = log;
                logger3.fine("Subscription renewed, updating in registry, response was: " + response);
                this.subscription.setActualSubscriptionDurationSeconds(responseMessage.getSubscriptionDurationSeconds());
                getUpnpService().getRegistry().updateRemoteSubscription(this.subscription);
            }
            return responseMessage;
        } catch (RouterException ex) {
            onRenewalFailure();
            throw ex;
        }
    }

    protected void onRenewalFailure() {
        log.fine("Subscription renewal failed, removing subscription from registry");
        getUpnpService().getRegistry().removeRemoteSubscription(this.subscription);
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingRenewal.3
            @Override // java.lang.Runnable
            public void run() {
                SendingRenewal.this.subscription.end(CancelReason.RENEWAL_FAILED, null);
            }
        });
    }
}
