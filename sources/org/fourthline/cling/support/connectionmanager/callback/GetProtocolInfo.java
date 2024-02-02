package org.fourthline.cling.support.connectionmanager.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.model.ProtocolInfos;
/* loaded from: classes.dex */
public abstract class GetProtocolInfo extends ActionCallback {
    public abstract void received(ActionInvocation actionInvocation, ProtocolInfos protocolInfos, ProtocolInfos protocolInfos2);

    public GetProtocolInfo(Service service) {
        this(service, null);
    }

    protected GetProtocolInfo(Service service, ControlPoint controlPoint) {
        super(new ActionInvocation(service.getAction("GetProtocolInfo")), controlPoint);
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        try {
            ActionArgumentValue sink = invocation.getOutput("Sink");
            ActionArgumentValue source = invocation.getOutput("Source");
            received(invocation, sink != null ? new ProtocolInfos(sink.toString()) : null, source != null ? new ProtocolInfos(source.toString()) : null);
        } catch (Exception ex) {
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            invocation.setFailure(new ActionException(errorCode, "Can't parse ProtocolInfo response: " + ex, ex));
            failure(invocation, null);
        }
    }
}
