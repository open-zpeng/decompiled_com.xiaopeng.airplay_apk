package org.fourthline.cling.support.connectionmanager.callback;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
/* loaded from: classes.dex */
public abstract class PrepareForConnection extends ActionCallback {
    public abstract void received(ActionInvocation actionInvocation, int i, int i2, int i3);

    public PrepareForConnection(Service service, ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, ConnectionInfo.Direction direction) {
        this(service, null, remoteProtocolInfo, peerConnectionManager, peerConnectionID, direction);
    }

    public PrepareForConnection(Service service, ControlPoint controlPoint, ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager, int peerConnectionID, ConnectionInfo.Direction direction) {
        super(new ActionInvocation(service.getAction("PrepareForConnection")), controlPoint);
        getActionInvocation().setInput("RemoteProtocolInfo", remoteProtocolInfo.toString());
        getActionInvocation().setInput("PeerConnectionManager", peerConnectionManager.toString());
        getActionInvocation().setInput("PeerConnectionID", Integer.valueOf(peerConnectionID));
        getActionInvocation().setInput("Direction", direction.toString());
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation invocation) {
        received(invocation, ((Integer) invocation.getOutput("ConnectionID").getValue()).intValue(), ((Integer) invocation.getOutput("RcsID").getValue()).intValue(), ((Integer) invocation.getOutput("AVTransportID").getValue()).intValue());
    }
}
