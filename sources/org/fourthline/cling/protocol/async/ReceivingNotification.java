package org.fourthline.cling.protocol.async;

import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.discovery.IncomingNotificationRequest;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.protocol.RetrieveRemoteDescriptors;
import org.fourthline.cling.transport.RouterException;
/* loaded from: classes.dex */
public class ReceivingNotification extends ReceivingAsync<IncomingNotificationRequest> {
    private static final Logger log = Logger.getLogger(ReceivingNotification.class.getName());

    public ReceivingNotification(UpnpService upnpService, IncomingDatagramMessage<UpnpRequest> inputMessage) {
        super(upnpService, new IncomingNotificationRequest(inputMessage));
    }

    @Override // org.fourthline.cling.protocol.ReceivingAsync
    protected void execute() throws RouterException {
        UDN udn = getInputMessage().getUDN();
        if (udn == null) {
            Logger logger = log;
            logger.fine("Ignoring notification message without UDN: " + getInputMessage());
            return;
        }
        RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity(getInputMessage());
        Logger logger2 = log;
        logger2.fine("Received device notification: " + rdIdentity);
        try {
            RemoteDevice rd = new RemoteDevice(rdIdentity);
            if (getInputMessage().isAliveMessage()) {
                Logger logger3 = log;
                logger3.fine("Received device ALIVE advertisement, descriptor location is: " + rdIdentity.getDescriptorURL());
                if (rdIdentity.getDescriptorURL() == null) {
                    Logger logger4 = log;
                    logger4.finer("Ignoring message without location URL header: " + getInputMessage());
                } else if (rdIdentity.getMaxAgeSeconds() == null) {
                    Logger logger5 = log;
                    logger5.finer("Ignoring message without max-age header: " + getInputMessage());
                } else if (getUpnpService().getRegistry().update(rdIdentity)) {
                    Logger logger6 = log;
                    logger6.finer("Remote device was already known: " + udn);
                } else {
                    getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(new RetrieveRemoteDescriptors(getUpnpService(), rd));
                }
            } else if (getInputMessage().isByeByeMessage()) {
                log.fine("Received device BYEBYE advertisement");
                boolean removed = getUpnpService().getRegistry().removeDevice(rd);
                if (removed) {
                    Logger logger7 = log;
                    logger7.fine("Removed remote device from registry: " + rd);
                }
            } else {
                Logger logger8 = log;
                logger8.finer("Ignoring unknown notification message: " + getInputMessage());
            }
        } catch (ValidationException ex) {
            Logger logger9 = log;
            logger9.warning("Validation errors of device during discovery: " + rdIdentity);
            for (ValidationError validationError : ex.getErrors()) {
                log.warning(validationError.toString());
            }
        }
    }
}
