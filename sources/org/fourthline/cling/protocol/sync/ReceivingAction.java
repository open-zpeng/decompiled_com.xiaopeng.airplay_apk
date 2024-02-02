package org.fourthline.cling.protocol.sync;

import android.util.Log;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionCancelledException;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.RemoteActionInvocation;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.control.IncomingActionRequestMessage;
import org.fourthline.cling.model.message.control.OutgoingActionResponseMessage;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.resource.ServiceControlResource;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.protocol.ReceivingSync;
import org.fourthline.cling.transport.RouterException;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public class ReceivingAction extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {
    private static final Logger log = Logger.getLogger(ReceivingAction.class.getName());
    private final String TAG;

    public ReceivingAction(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
        this.TAG = "ReceivingAction";
    }

    @Override // org.fourthline.cling.protocol.ReceivingSync
    protected StreamResponseMessage executeSync() throws RouterException {
        RemoteActionInvocation invocation;
        OutgoingActionResponseMessage responseMessage;
        ActionException actionException;
        ContentTypeHeader contentTypeHeader = (ContentTypeHeader) ((StreamRequestMessage) getInputMessage()).getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class);
        if (contentTypeHeader != null && !contentTypeHeader.isUDACompliantXML()) {
            Log.w("ReceivingAction", "Received invalid Content-Type '" + contentTypeHeader + "': " + getInputMessage());
            return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.UNSUPPORTED_MEDIA_TYPE));
        }
        if (contentTypeHeader == null) {
            Log.w("ReceivingAction", "Received without Content-Type: " + getInputMessage());
        }
        ServiceControlResource resource = (ServiceControlResource) getUpnpService().getRegistry().getResource(ServiceControlResource.class, ((StreamRequestMessage) getInputMessage()).getUri());
        if (resource == null) {
            Log.i("ReceivingAction", "No local resource found: " + getInputMessage());
            return null;
        }
        Log.d("ReceivingAction", "Found local action resource matching relative request URI: " + ((StreamRequestMessage) getInputMessage()).getUri());
        try {
            IncomingActionRequestMessage requestMessage = new IncomingActionRequestMessage((StreamRequestMessage) getInputMessage(), resource.getModel());
            Log.i("ReceivingAction", "Created incoming action request message: " + requestMessage.getAction().getName());
            invocation = new RemoteActionInvocation(requestMessage.getAction(), getRemoteClientInfo());
            getUpnpService().getConfiguration().getSoapActionProcessor().readBody(requestMessage, invocation);
            Log.d("ReceivingAction", "Executing on local service: " + invocation);
            resource.getModel().getExecutor(invocation.getAction()).execute(invocation);
            if (invocation.getFailure() == null) {
                responseMessage = new OutgoingActionResponseMessage(invocation.getAction());
            } else if (invocation.getFailure() instanceof ActionCancelledException) {
                Log.i("ReceivingAction", "Action execution was cancelled, returning 404 to client");
                return null;
            } else {
                responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR, invocation.getAction());
            }
        } catch (UnsupportedDataException ex) {
            Logger logger = log;
            Level level = Level.WARNING;
            logger.log(level, "Error reading action request XML body: " + ex.toString(), Exceptions.unwrap(ex));
            if (Exceptions.unwrap(ex) instanceof ActionException) {
                actionException = (ActionException) Exceptions.unwrap(ex);
            } else {
                actionException = new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage());
            }
            invocation = new RemoteActionInvocation(actionException, getRemoteClientInfo());
            responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        } catch (ActionException ex2) {
            Logger logger2 = log;
            logger2.finer("Error executing local action: " + ex2);
            invocation = new RemoteActionInvocation(ex2, getRemoteClientInfo());
            responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        }
        try {
            getUpnpService().getConfiguration().getSoapActionProcessor().writeBody(responseMessage, invocation);
            Log.d("ReceivingAction", "Returning finished response message: " + responseMessage);
            return responseMessage;
        } catch (UnsupportedDataException ex3) {
            log.warning("Failure writing body of response message, sending '500 Internal Server Error' without body");
            log.log(Level.WARNING, "Exception root cause: ", Exceptions.unwrap(ex3));
            return new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
