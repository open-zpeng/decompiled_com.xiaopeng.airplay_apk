package org.fourthline.cling.transport.spi;

import java.util.logging.Logger;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.protocol.ProtocolCreationException;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ReceivingSync;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class UpnpStream implements Runnable {
    private static Logger log = Logger.getLogger(UpnpStream.class.getName());
    protected final ProtocolFactory protocolFactory;
    protected ReceivingSync syncProtocol;

    /* JADX INFO: Access modifiers changed from: protected */
    public UpnpStream(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    public ProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

    public StreamResponseMessage process(StreamRequestMessage requestMsg) {
        Logger logger = log;
        logger.fine("Processing stream request message: " + requestMsg);
        try {
            this.syncProtocol = getProtocolFactory().createReceivingSync(requestMsg);
            Logger logger2 = log;
            logger2.fine("Running protocol for synchronous message processing: " + this.syncProtocol);
            this.syncProtocol.run();
            StreamResponseMessage responseMsg = this.syncProtocol.getOutputMessage();
            if (responseMsg == null) {
                log.finer("Protocol did not return any response message");
                return null;
            }
            Logger logger3 = log;
            logger3.finer("Protocol returned response: " + responseMsg);
            return responseMsg;
        } catch (ProtocolCreationException ex) {
            Logger logger4 = log;
            logger4.warning("Processing stream request failed - " + Exceptions.unwrap(ex).toString());
            return new StreamResponseMessage(UpnpResponse.Status.NOT_IMPLEMENTED);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void responseSent(StreamResponseMessage responseMessage) {
        if (this.syncProtocol != null) {
            this.syncProtocol.responseSent(responseMessage);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void responseException(Throwable t) {
        if (this.syncProtocol != null) {
            this.syncProtocol.responseException(t);
        }
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ")";
    }
}
