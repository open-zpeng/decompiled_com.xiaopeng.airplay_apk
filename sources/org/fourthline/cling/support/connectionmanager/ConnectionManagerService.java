package org.fourthline.cling.support.connectionmanager;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.model.types.csv.CSVUnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;
@UpnpService(serviceId = @UpnpServiceId("ConnectionManager"), serviceType = @UpnpServiceType(value = "ConnectionManager", version = 1), stringConvertibleTypes = {ProtocolInfo.class, ProtocolInfos.class, ServiceReference.class})
@UpnpStateVariables({@UpnpStateVariable(datatype = "string", name = "SourceProtocolInfo"), @UpnpStateVariable(datatype = "string", name = "SinkProtocolInfo"), @UpnpStateVariable(datatype = "string", name = "CurrentConnectionIDs"), @UpnpStateVariable(allowedValuesEnum = ConnectionInfo.Status.class, name = "A_ARG_TYPE_ConnectionStatus", sendEvents = false), @UpnpStateVariable(datatype = "string", name = "A_ARG_TYPE_ConnectionManager", sendEvents = false), @UpnpStateVariable(allowedValuesEnum = ConnectionInfo.Direction.class, name = "A_ARG_TYPE_Direction", sendEvents = false), @UpnpStateVariable(datatype = "string", name = "A_ARG_TYPE_ProtocolInfo", sendEvents = false), @UpnpStateVariable(datatype = "i4", name = "A_ARG_TYPE_ConnectionID", sendEvents = false), @UpnpStateVariable(datatype = "i4", name = "A_ARG_TYPE_AVTransportID", sendEvents = false), @UpnpStateVariable(datatype = "i4", name = "A_ARG_TYPE_RcsID", sendEvents = false)})
/* loaded from: classes.dex */
public class ConnectionManagerService {
    private static final Logger log = Logger.getLogger(ConnectionManagerService.class.getName());
    protected final Map<Integer, ConnectionInfo> activeConnections;
    protected final PropertyChangeSupport propertyChangeSupport;
    protected final ProtocolInfos sinkProtocolInfo;
    protected final ProtocolInfos sourceProtocolInfo;

    public ConnectionManagerService() {
        this(new ConnectionInfo());
    }

    public ConnectionManagerService(ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo) {
        this(sourceProtocolInfo, sinkProtocolInfo, new ConnectionInfo());
    }

    public ConnectionManagerService(ConnectionInfo... activeConnections) {
        this(null, new ProtocolInfos(new ProtocolInfo[0]), new ProtocolInfos(new ProtocolInfo[0]), activeConnections);
    }

    public ConnectionManagerService(ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections) {
        this(null, sourceProtocolInfo, sinkProtocolInfo, activeConnections);
    }

    public ConnectionManagerService(PropertyChangeSupport propertyChangeSupport, ProtocolInfos sourceProtocolInfo, ProtocolInfos sinkProtocolInfo, ConnectionInfo... activeConnections) {
        this.activeConnections = new ConcurrentHashMap();
        this.propertyChangeSupport = propertyChangeSupport == null ? new PropertyChangeSupport(this) : propertyChangeSupport;
        this.sourceProtocolInfo = sourceProtocolInfo;
        this.sinkProtocolInfo = sinkProtocolInfo;
        for (ConnectionInfo activeConnection : activeConnections) {
            this.activeConnections.put(Integer.valueOf(activeConnection.getConnectionID()), activeConnection);
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return this.propertyChangeSupport;
    }

    @UpnpAction(out = {@UpnpOutputArgument(getterName = "getRcsID", name = "RcsID"), @UpnpOutputArgument(getterName = "getAvTransportID", name = "AVTransportID"), @UpnpOutputArgument(getterName = "getProtocolInfo", name = "ProtocolInfo"), @UpnpOutputArgument(getterName = "getPeerConnectionManager", name = "PeerConnectionManager", stateVariable = "A_ARG_TYPE_ConnectionManager"), @UpnpOutputArgument(getterName = "getPeerConnectionID", name = "PeerConnectionID", stateVariable = "A_ARG_TYPE_ConnectionID"), @UpnpOutputArgument(getterName = "getDirection", name = "Direction"), @UpnpOutputArgument(getterName = "getConnectionStatus", name = "Status", stateVariable = "A_ARG_TYPE_ConnectionStatus")})
    public synchronized ConnectionInfo getCurrentConnectionInfo(@UpnpInputArgument(name = "ConnectionID") int connectionId) throws ActionException {
        ConnectionInfo info;
        Logger logger = log;
        logger.fine("Getting connection information of connection ID: " + connectionId);
        info = this.activeConnections.get(Integer.valueOf(connectionId));
        if (info == null) {
            ConnectionManagerErrorCode connectionManagerErrorCode = ConnectionManagerErrorCode.INVALID_CONNECTION_REFERENCE;
            throw new ConnectionManagerException(connectionManagerErrorCode, "Non-active connection ID: " + connectionId);
        }
        return info;
    }

    @UpnpAction(out = {@UpnpOutputArgument(name = "ConnectionIDs")})
    public synchronized CSV<UnsignedIntegerFourBytes> getCurrentConnectionIDs() {
        CSV<UnsignedIntegerFourBytes> csv;
        csv = new CSVUnsignedIntegerFourBytes();
        for (Integer connectionID : this.activeConnections.keySet()) {
            csv.add(new UnsignedIntegerFourBytes(connectionID.intValue()));
        }
        Logger logger = log;
        logger.fine("Returning current connection IDs: " + csv.size());
        return csv;
    }

    @UpnpAction(out = {@UpnpOutputArgument(getterName = "getSourceProtocolInfo", name = "Source", stateVariable = "SourceProtocolInfo"), @UpnpOutputArgument(getterName = "getSinkProtocolInfo", name = "Sink", stateVariable = "SinkProtocolInfo")})
    public synchronized void getProtocolInfo() throws ActionException {
    }

    public synchronized ProtocolInfos getSourceProtocolInfo() {
        return this.sourceProtocolInfo;
    }

    public synchronized ProtocolInfos getSinkProtocolInfo() {
        return this.sinkProtocolInfo;
    }
}
