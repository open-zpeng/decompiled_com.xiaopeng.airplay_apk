package org.fourthline.cling.support.connectionmanager;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.support.connectionmanager.callback.ConnectionComplete;
import org.fourthline.cling.support.connectionmanager.callback.PrepareForConnection;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;
/* loaded from: classes.dex */
public abstract class AbstractPeeringConnectionManagerService extends ConnectionManagerService {
    private static final Logger log = Logger.getLogger(AbstractPeeringConnectionManagerService.class.getName());

    protected abstract void closeConnection(ConnectionInfo connectionInfo);

    protected abstract ConnectionInfo createConnection(int i, int i2, ServiceReference serviceReference, ConnectionInfo.Direction direction, ProtocolInfo protocolInfo) throws ActionException;

    protected abstract void peerFailure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String str);

    protected AbstractPeeringConnectionManagerService(ConnectionInfo... activeConnections) {
        super(activeConnections);
    }

    protected AbstractPeeringConnectionManagerService(ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections) {
        super(sourceProtocolInfo, sinkProtocolInfo, activeConnections);
    }

    protected AbstractPeeringConnectionManagerService(PropertyChangeSupport propertyChangeSupport, ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections) {
        super(propertyChangeSupport, sourceProtocolInfo, sinkProtocolInfo, activeConnections);
    }

    protected synchronized int getNewConnectionId() {
        int currentHighestID;
        currentHighestID = -1;
        for (Integer key : this.activeConnections.keySet()) {
            if (key.intValue() > currentHighestID) {
                currentHighestID = key.intValue();
            }
        }
        return currentHighestID + 1;
    }

    protected synchronized void storeConnection(ConnectionInfo info) {
        CSV<UnsignedIntegerFourBytes> oldConnectionIDs = getCurrentConnectionIDs();
        this.activeConnections.put(Integer.valueOf(info.getConnectionID()), info);
        Logger logger = log;
        logger.fine("Connection stored, firing event: " + info.getConnectionID());
        CSV<UnsignedIntegerFourBytes> newConnectionIDs = getCurrentConnectionIDs();
        getPropertyChangeSupport().firePropertyChange("CurrentConnectionIDs", oldConnectionIDs, newConnectionIDs);
    }

    protected synchronized void removeConnection(int connectionID) {
        CSV<UnsignedIntegerFourBytes> oldConnectionIDs = getCurrentConnectionIDs();
        this.activeConnections.remove(Integer.valueOf(connectionID));
        Logger logger = log;
        logger.fine("Connection removed, firing event: " + connectionID);
        CSV<UnsignedIntegerFourBytes> newConnectionIDs = getCurrentConnectionIDs();
        getPropertyChangeSupport().firePropertyChange("CurrentConnectionIDs", oldConnectionIDs, newConnectionIDs);
    }

    @UpnpAction(out = {@UpnpOutputArgument(getterName = "getConnectionID", name = "ConnectionID", stateVariable = "A_ARG_TYPE_ConnectionID"), @UpnpOutputArgument(getterName = "getAvTransportID", name = "AVTransportID", stateVariable = "A_ARG_TYPE_AVTransportID"), @UpnpOutputArgument(getterName = "getRcsID", name = "RcsID", stateVariable = "A_ARG_TYPE_RcsID")})
    public synchronized ConnectionInfo prepareForConnection(@UpnpInputArgument(name = "RemoteProtocolInfo", stateVariable = "A_ARG_TYPE_ProtocolInfo") ProtocolInfo remoteProtocolInfo, @UpnpInputArgument(name = "PeerConnectionManager", stateVariable = "A_ARG_TYPE_ConnectionManager") ServiceReference peerConnectionManager, @UpnpInputArgument(name = "PeerConnectionID", stateVariable = "A_ARG_TYPE_ConnectionID") int peerConnectionId, @UpnpInputArgument(name = "Direction", stateVariable = "A_ARG_TYPE_Direction") String direction) throws ActionException {
        ConnectionInfo newConnectionInfo;
        int connectionId = getNewConnectionId();
        try {
            ConnectionInfo.Direction dir = ConnectionInfo.Direction.valueOf(direction);
            Logger logger = log;
            logger.fine("Preparing for connection with local new ID " + connectionId + " and peer connection ID: " + peerConnectionId);
            newConnectionInfo = createConnection(connectionId, peerConnectionId, peerConnectionManager, dir, remoteProtocolInfo);
            storeConnection(newConnectionInfo);
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.ARGUMENT_VALUE_INVALID;
            throw new ConnectionManagerException(errorCode, "Unsupported direction: " + direction);
        }
        return newConnectionInfo;
    }

    @UpnpAction
    public synchronized void connectionComplete(@UpnpInputArgument(name = "ConnectionID", stateVariable = "A_ARG_TYPE_ConnectionID") int connectionID) throws ActionException {
        ConnectionInfo info = getCurrentConnectionInfo(connectionID);
        Logger logger = log;
        logger.fine("Closing connection ID " + connectionID);
        closeConnection(info);
        removeConnection(connectionID);
    }

    public synchronized int createConnectionWithPeer(ServiceReference localServiceReference, ControlPoint controlPoint, final Service peerService, final ProtocolInfo protInfo, final ConnectionInfo.Direction direction) {
        final int localConnectionID;
        final boolean[] failed;
        localConnectionID = getNewConnectionId();
        Logger logger = log;
        logger.fine("Creating new connection ID " + localConnectionID + " with peer: " + peerService);
        failed = new boolean[1];
        new PrepareForConnection(peerService, controlPoint, protInfo, localServiceReference, localConnectionID, direction) { // from class: org.fourthline.cling.support.connectionmanager.AbstractPeeringConnectionManagerService.1
            @Override // org.fourthline.cling.support.connectionmanager.callback.PrepareForConnection
            public void received(ActionInvocation invocation, int peerConnectionID, int rcsID, int avTransportID) {
                ConnectionInfo info = new ConnectionInfo(localConnectionID, rcsID, avTransportID, protInfo, peerService.getReference(), peerConnectionID, direction.getOpposite(), ConnectionInfo.Status.OK);
                AbstractPeeringConnectionManagerService.this.storeConnection(info);
            }

            @Override // org.fourthline.cling.controlpoint.ActionCallback
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                AbstractPeeringConnectionManagerService.this.peerFailure(invocation, operation, defaultMsg);
                failed[0] = true;
            }
        }.run();
        return failed[0] ? -1 : localConnectionID;
    }

    public synchronized void closeConnectionWithPeer(ControlPoint controlPoint, Service peerService, int connectionID) throws ActionException {
        closeConnectionWithPeer(controlPoint, peerService, getCurrentConnectionInfo(connectionID));
    }

    public synchronized void closeConnectionWithPeer(ControlPoint controlPoint, Service peerService, final ConnectionInfo connectionInfo) throws ActionException {
        Logger logger = log;
        logger.fine("Closing connection ID " + connectionInfo.getConnectionID() + " with peer: " + peerService);
        new ConnectionComplete(peerService, controlPoint, connectionInfo.getPeerConnectionID()) { // from class: org.fourthline.cling.support.connectionmanager.AbstractPeeringConnectionManagerService.2
            @Override // org.fourthline.cling.controlpoint.ActionCallback
            public void success(ActionInvocation invocation) {
                AbstractPeeringConnectionManagerService.this.removeConnection(connectionInfo.getConnectionID());
            }

            @Override // org.fourthline.cling.controlpoint.ActionCallback
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                AbstractPeeringConnectionManagerService.this.peerFailure(invocation, operation, defaultMsg);
            }
        }.run();
    }
}
