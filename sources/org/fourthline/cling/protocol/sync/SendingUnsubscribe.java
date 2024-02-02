package org.fourthline.cling.protocol.sync;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.gena.OutgoingUnsubscribeRequestMessage;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public class SendingUnsubscribe extends SendingSync<OutgoingUnsubscribeRequestMessage, StreamResponseMessage> {
    private static final Logger log = Logger.getLogger(SendingUnsubscribe.class.getName());
    protected final RemoteGENASubscription subscription;

    public SendingUnsubscribe(UpnpService upnpService, RemoteGENASubscription subscription) {
        super(upnpService, new OutgoingUnsubscribeRequestMessage(subscription, upnpService.getConfiguration().getEventSubscriptionHeaders(subscription.getService())));
        this.subscription = subscription;
    }

    @Override // org.fourthline.cling.protocol.SendingSync
    protected StreamResponseMessage executeSync() throws RouterException {
        Logger logger = log;
        logger.fine("Sending unsubscribe request: " + getInputMessage());
        StreamResponseMessage response = null;
        try {
            response = getUpnpService().getRouter().send(getInputMessage());
            return response;
        } finally {
            onUnsubscribe(response);
        }
    }

    protected void onUnsubscribe(final StreamResponseMessage response) {
        getUpnpService().getRegistry().removeRemoteSubscription(this.subscription);
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(new Runnable() { // from class: org.fourthline.cling.protocol.sync.SendingUnsubscribe.1
            @Override // java.lang.Runnable
            public void run() {
                if (response == null) {
                    SendingUnsubscribe.log.fine("Unsubscribe failed, no response received");
                    SendingUnsubscribe.this.subscription.end(CancelReason.UNSUBSCRIBE_FAILED, null);
                } else if (response.getOperation().isFailed()) {
                    Logger logger = SendingUnsubscribe.log;
                    logger.fine("Unsubscribe failed, response was: " + response);
                    SendingUnsubscribe.this.subscription.end(CancelReason.UNSUBSCRIBE_FAILED, response.getOperation());
                } else {
                    Logger logger2 = SendingUnsubscribe.log;
                    logger2.fine("Unsubscribe successful, response was: " + response);
                    SendingUnsubscribe.this.subscription.end(null, response.getOperation());
                }
            }
        });
    }
}
