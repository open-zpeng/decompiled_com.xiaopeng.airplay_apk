package org.fourthline.cling.support.connectionmanager.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
/* loaded from: classes.dex */
public abstract class GetCurrentConnectionInfo extends ActionCallback {
    public abstract void received(ActionInvocation actionInvocation, ConnectionInfo connectionInfo);

    public GetCurrentConnectionInfo(Service service, int connectionID) {
        this(service, null, connectionID);
    }

    protected GetCurrentConnectionInfo(Service service, ControlPoint controlPoint, int connectionID) {
        super(new ActionInvocation(service.getAction("GetCurrentConnectionInfo")), controlPoint);
        getActionInvocation().setInput("ConnectionID", Integer.valueOf(connectionID));
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        try {
            ConnectionInfo info = new ConnectionInfo(((Integer) invocation.getInput("ConnectionID").getValue()).intValue(), ((Integer) invocation.getOutput("RcsID").getValue()).intValue(), ((Integer) invocation.getOutput("AVTransportID").getValue()).intValue(), new ProtocolInfo(invocation.getOutput("ProtocolInfo").toString()), new ServiceReference(invocation.getOutput("PeerConnectionManager").toString()), ((Integer) invocation.getOutput("PeerConnectionID").getValue()).intValue(), ConnectionInfo.Direction.valueOf(invocation.getOutput("Direction").toString()), ConnectionInfo.Status.valueOf(invocation.getOutput("Status").toString()));
            received(invocation, info);
        } catch (Exception ex) {
            ErrorCode errorCode = ErrorCode.ACTION_FAILED;
            invocation.setFailure(new ActionException(errorCode, "Can't parse ConnectionInfo response: " + ex, ex));
            failure(invocation, null);
        }
    }
}
