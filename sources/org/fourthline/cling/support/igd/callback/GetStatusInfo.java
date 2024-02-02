package org.fourthline.cling.support.igd.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Connection;
/* loaded from: classes.dex */
public abstract class GetStatusInfo extends ActionCallback {
    protected abstract void success(Connection.StatusInfo statusInfo);

    public GetStatusInfo(Service service) {
        super(new ActionInvocation(service.getAction("GetStatusInfo")));
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        try {
            Connection.Status status = Connection.Status.valueOf(invocation.getOutput("NewConnectionStatus").getValue().toString());
            Connection.Error lastError = Connection.Error.valueOf(invocation.getOutput("NewLastConnectionError").getValue().toString());
            success(new Connection.StatusInfo(status, (UnsignedIntegerFourBytes) invocation.getOutput("NewUptime").getValue(), lastError));
        } catch (Exception ex) {
            ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
            invocation.setFailure(new ActionException(errorCode, "Invalid status or last error string: " + ex, ex));
            failure(invocation, null);
        }
    }
}
