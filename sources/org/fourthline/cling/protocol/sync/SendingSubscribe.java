package org.fourthline.cling.protocol.sync;

import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.gena.IncomingSubscribeResponseMessage;
import org.fourthline.cling.model.message.gena.OutgoingSubscribeRequestMessage;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public class SendingSubscribe extends SendingSync<OutgoingSubscribeRequestMessage, IncomingSubscribeResponseMessage> {
    private static final Logger log = Logger.getLogger(SendingSubscribe.class.getName());
    protected final RemoteGENASubscription subscription;

    public SendingSubscribe(UpnpService upnpService, RemoteGENASubscription subscription, List<NetworkAddress> activeStreamServers) {
        super(upnpService, new OutgoingSubscribeRequestMessage(subscription, subscription.getEventCallbackURLs(activeStreamServers, upnpService.getConfiguration().getNamespace()), upnpService.getConfiguration().getEventSubscriptionHeaders(subscription.getService())));
        this.subscription = subscription;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.protocol.SendingSync
    public IncomingSubscribeResponseMessage executeSync() throws RouterException {
        if (!getInputMessage().hasCallbackURLs()) {
            log.fine("Subscription failed, no active local callback URLs available (network disabled?)");
            getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingSubscribe.1
                @Override // java.lang.Runnable
                public void run() {
                    SendingSubscribe.this.subscription.fail(null);
                }
            });
            return null;
        }
        Logger logger = log;
        logger.fine("Sending subscription request: " + getInputMessage());
        try {
            getUpnpService().getRegistry().registerPendingRemoteSubscription(this.subscription);
            StreamResponseMessage response = getUpnpService().getRouter().send(getInputMessage());
            if (response == null) {
                onSubscriptionFailure();
                return null;
            }
            final IncomingSubscribeResponseMessage responseMessage = new IncomingSubscribeResponseMessage(response);
            if (response.getOperation().isFailed()) {
                Logger logger2 = log;
                logger2.fine("Subscription failed, response was: " + responseMessage);
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingSubscribe.2
                    @Override // java.lang.Runnable
                    public void run() {
                        SendingSubscribe.this.subscription.fail(responseMessage.getOperation());
                    }
                });
            } else if (responseMessage.isValidHeaders()) {
                Logger logger3 = log;
                logger3.fine("Subscription established, adding to registry, response was: " + response);
                this.subscription.setSubscriptionId(responseMessage.getSubscriptionId());
                this.subscription.setActualSubscriptionDurationSeconds(responseMessage.getSubscriptionDurationSeconds());
                getUpnpService().getRegistry().addRemoteSubscription(this.subscription);
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingSubscribe.4
                    @Override // java.lang.Runnable
                    public void run() {
                        SendingSubscribe.this.subscription.establish();
                    }
                });
            } else {
                log.severe("Subscription failed, invalid or missing (SID, Timeout) response headers");
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingSubscribe.3
                    @Override // java.lang.Runnable
                    public void run() {
                        SendingSubscribe.this.subscription.fail(responseMessage.getOperation());
                    }
                });
            }
            return responseMessage;
        } catch (RouterException e) {
            onSubscriptionFailure();
            return null;
        } finally {
            getUpnpService().getRegistry().unregisterPendingRemoteSubscription(this.subscription);
        }
    }

    protected void onSubscriptionFailure() {
        log.fine("Subscription failed");
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingSubscribe.5
            @Override // java.lang.Runnable
            public void run() {
                SendingSubscribe.this.subscription.fail(null);
            }
        });
    }
}
