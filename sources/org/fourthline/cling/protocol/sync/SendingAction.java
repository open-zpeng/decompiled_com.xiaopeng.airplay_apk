package org.fourthline.cling.protocol.sync;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionCancelledException;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.control.IncomingActionResponseMessage;
import org.fourthline.cling.model.message.control.OutgoingActionRequestMessage;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.protocol.SendingSync;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public class SendingAction extends SendingSync<OutgoingActionRequestMessage, IncomingActionResponseMessage> {
    private static final Logger log = Logger.getLogger(SendingAction.class.getName());
    protected final ActionInvocation actionInvocation;

    public SendingAction(UpnpService upnpService, ActionInvocation actionInvocation, URL controlURL) {
        super(upnpService, new OutgoingActionRequestMessage(actionInvocation, controlURL));
        this.actionInvocation = actionInvocation;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.protocol.SendingSync
    public IncomingActionResponseMessage executeSync() throws RouterException {
        return invokeRemote(getInputMessage());
    }

    protected IncomingActionResponseMessage invokeRemote(OutgoingActionRequestMessage requestMessage) throws RouterException {
        Device device = this.actionInvocation.getAction().getService().getDevice();
        Logger logger = log;
        logger.fine("Sending outgoing action call '" + this.actionInvocation.getAction().getName() + "' to remote service of: " + device);
        IncomingActionResponseMessage responseMessage = null;
        try {
            StreamResponseMessage streamResponse = sendRemoteRequest(requestMessage);
            if (streamResponse == null) {
                log.fine("No connection or no no response received, returning null");
                this.actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Connection error or no response received"));
                return null;
            }
            IncomingActionResponseMessage responseMessage2 = new IncomingActionResponseMessage(streamResponse);
            if (responseMessage2.isFailedNonRecoverable()) {
                Logger logger2 = log;
                logger2.fine("Response was a non-recoverable failure: " + responseMessage2);
                ErrorCode errorCode = ErrorCode.ACTION_FAILED;
                throw new ActionException(errorCode, "Non-recoverable remote execution failure: " + responseMessage2.getOperation().getResponseDetails());
            }
            if (responseMessage2.isFailedRecoverable()) {
                handleResponseFailure(responseMessage2);
            } else {
                handleResponse(responseMessage2);
            }
            return responseMessage2;
        } catch (ActionException ex) {
            Logger logger3 = log;
            logger3.fine("Remote action invocation failed, returning Internal Server Error message: " + ex.getMessage());
            this.actionInvocation.setFailure(ex);
            if (0 != 0 && ((UpnpResponse) responseMessage.getOperation()).isFailed()) {
                return null;
            }
            return new IncomingActionResponseMessage(new UpnpResponse(UpnpResponse.Status.INTERNAL_SERVER_ERROR));
        }
    }

    protected StreamResponseMessage sendRemoteRequest(OutgoingActionRequestMessage requestMessage) throws ActionException, RouterException {
        try {
            Logger logger = log;
            logger.fine("Writing SOAP request body of: " + requestMessage);
            getUpnpService().getConfiguration().getSoapActionProcessor().writeBody(requestMessage, this.actionInvocation);
            log.fine("Sending SOAP body of message as stream to remote device");
            return getUpnpService().getRouter().send(requestMessage);
        } catch (UnsupportedDataException ex) {
            if (log.isLoggable(Level.FINE)) {
                Logger logger2 = log;
                logger2.fine("Error writing SOAP body: " + ex);
                log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
            }
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            throw new ActionException(errorCode, "Error writing request message. " + ex.getMessage());
        } catch (RouterException ex2) {
            Throwable cause = Exceptions.unwrap(ex2);
            if (cause instanceof InterruptedException) {
                if (log.isLoggable(Level.FINE)) {
                    Logger logger3 = log;
                    logger3.fine("Sending action request message was interrupted: " + cause);
                }
                throw new ActionCancelledException((InterruptedException) cause);
            }
            throw ex2;
        }
    }

    protected void handleResponse(IncomingActionResponseMessage responseMsg) throws ActionException {
        try {
            Logger logger = log;
            logger.fine("Received response for outgoing call, reading SOAP response body: " + responseMsg);
            getUpnpService().getConfiguration().getSoapActionProcessor().readBody(responseMsg, this.actionInvocation);
        } catch (UnsupportedDataException ex) {
            Logger logger2 = log;
            logger2.fine("Error reading SOAP body: " + ex);
            log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            throw new ActionException(errorCode, "Error reading SOAP response message. " + ex.getMessage(), false);
        }
    }

    protected void handleResponseFailure(IncomingActionResponseMessage responseMsg) throws ActionException {
        try {
            log.fine("Received response with Internal Server Error, reading SOAP failure message");
            getUpnpService().getConfiguration().getSoapActionProcessor().readBody(responseMsg, this.actionInvocation);
        } catch (UnsupportedDataException ex) {
            Logger logger = log;
            logger.fine("Error reading SOAP body: " + ex);
            log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            throw new ActionException(errorCode, "Error reading SOAP response failure message. " + ex.getMessage(), false);
        }
    }
}
