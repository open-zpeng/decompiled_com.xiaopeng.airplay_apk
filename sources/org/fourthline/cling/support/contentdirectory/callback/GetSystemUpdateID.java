package org.fourthline.cling.support.contentdirectory.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
/* loaded from: classes.dex */
public abstract class GetSystemUpdateID extends ActionCallback {
    public abstract void received(ActionInvocation actionInvocation, long j);

    public GetSystemUpdateID(Service service) {
        super(new ActionInvocation(service.getAction("GetSystemUpdateID")));
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        boolean ok = true;
        long id = 0;
        try {
            id = Long.valueOf(invocation.getOutput("Id").getValue().toString()).longValue();
        } catch (Exception ex) {
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            invocation.setFailure(new ActionException(errorCode, "Can't parse GetSystemUpdateID response: " + ex, ex));
            failure(invocation, null);
            ok = false;
        }
        if (ok) {
            received(invocation, id);
        }
    }
}
