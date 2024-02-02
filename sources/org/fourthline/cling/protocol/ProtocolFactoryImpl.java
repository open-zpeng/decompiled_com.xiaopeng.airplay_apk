package org.fourthline.cling.protocol;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.NamedServiceType;
import org.fourthline.cling.model.types.NotificationSubtype;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.protocol.async.ReceivingNotification;
import org.fourthline.cling.protocol.async.ReceivingSearch;
import org.fourthline.cling.protocol.async.ReceivingSearchResponse;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;
import org.fourthline.cling.protocol.async.SendingNotificationByebye;
import org.fourthline.cling.protocol.async.SendingSearch;
import org.fourthline.cling.protocol.sync.ReceivingAction;
import org.fourthline.cling.protocol.sync.ReceivingEvent;
import org.fourthline.cling.protocol.sync.ReceivingRetrieval;
import org.fourthline.cling.protocol.sync.ReceivingSubscribe;
import org.fourthline.cling.protocol.sync.ReceivingUnsubscribe;
import org.fourthline.cling.protocol.sync.SendingAction;
import org.fourthline.cling.protocol.sync.SendingEvent;
import org.fourthline.cling.protocol.sync.SendingRenewal;
import org.fourthline.cling.protocol.sync.SendingSubscribe;
import org.fourthline.cling.protocol.sync.SendingUnsubscribe;
import org.fourthline.cling.transport.RouterException;
@ApplicationScoped
/* loaded from: classes.dex */
public class ProtocolFactoryImpl implements ProtocolFactory {
    private static final Logger log = Logger.getLogger(ProtocolFactory.class.getName());
    protected final UpnpService upnpService;

    protected ProtocolFactoryImpl() {
        this.upnpService = null;
    }

    @Inject
    public ProtocolFactoryImpl(UpnpService upnpService) {
        Logger logger = log;
        logger.fine("Creating ProtocolFactory: " + getClass().getName());
        this.upnpService = upnpService;
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public UpnpService getUpnpService() {
        return this.upnpService;
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public ReceivingAsync createReceivingAsync(IncomingDatagramMessage message) throws ProtocolCreationException {
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Creating protocol for incoming asynchronous: " + message);
        }
        if (message.getOperation() instanceof UpnpRequest) {
            switch (((UpnpRequest) message.getOperation()).getMethod()) {
                case NOTIFY:
                    if (isByeBye(message) || isSupportedServiceAdvertisement(message)) {
                        return createReceivingNotification(message);
                    }
                    return null;
                case MSEARCH:
                    return createReceivingSearch(message);
            }
        } else if (message.getOperation() instanceof UpnpResponse) {
            if (isSupportedServiceAdvertisement(message)) {
                return createReceivingSearchResponse(message);
            }
            return null;
        }
        throw new ProtocolCreationException("Protocol for incoming datagram message not found: " + message);
    }

    protected ReceivingAsync createReceivingNotification(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
        return new ReceivingNotification(getUpnpService(), incomingRequest);
    }

    protected ReceivingAsync createReceivingSearch(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
        return new ReceivingSearch(getUpnpService(), incomingRequest);
    }

    protected ReceivingAsync createReceivingSearchResponse(IncomingDatagramMessage<UpnpResponse> incomingResponse) {
        return new ReceivingSearchResponse(getUpnpService(), incomingResponse);
    }

    protected boolean isByeBye(IncomingDatagramMessage message) {
        String ntsHeader = message.getHeaders().getFirstHeader(UpnpHeader.Type.NTS.getHttpName());
        return ntsHeader != null && ntsHeader.equals(NotificationSubtype.BYEBYE.getHeaderString());
    }

    protected boolean isSupportedServiceAdvertisement(IncomingDatagramMessage message) {
        ServiceType[] exclusiveServiceTypes = getUpnpService().getConfiguration().getExclusiveServiceTypes();
        if (exclusiveServiceTypes == null) {
            return false;
        }
        if (exclusiveServiceTypes.length == 0) {
            return true;
        }
        String usnHeader = message.getHeaders().getFirstHeader(UpnpHeader.Type.USN.getHttpName());
        if (usnHeader == null) {
            return false;
        }
        try {
            NamedServiceType nst = NamedServiceType.valueOf(usnHeader);
            for (ServiceType exclusiveServiceType : exclusiveServiceTypes) {
                if (nst.getServiceType().implementsVersion(exclusiveServiceType)) {
                    return true;
                }
            }
        } catch (InvalidValueException e) {
            log.finest("Not a named service type header value: " + usnHeader);
        }
        log.fine("Service advertisement not supported, dropping it: " + usnHeader);
        return false;
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public ReceivingSync createReceivingSync(StreamRequestMessage message) throws ProtocolCreationException {
        Logger logger = log;
        logger.fine("Creating protocol for incoming synchronous: " + message);
        if (message.getOperation().getMethod().equals(UpnpRequest.Method.GET)) {
            return createReceivingRetrieval(message);
        }
        if (getUpnpService().getConfiguration().getNamespace().isControlPath(message.getUri())) {
            if (message.getOperation().getMethod().equals(UpnpRequest.Method.POST)) {
                return createReceivingAction(message);
            }
        } else if (getUpnpService().getConfiguration().getNamespace().isEventSubscriptionPath(message.getUri())) {
            if (message.getOperation().getMethod().equals(UpnpRequest.Method.SUBSCRIBE)) {
                return createReceivingSubscribe(message);
            }
            if (message.getOperation().getMethod().equals(UpnpRequest.Method.UNSUBSCRIBE)) {
                return createReceivingUnsubscribe(message);
            }
        } else if (getUpnpService().getConfiguration().getNamespace().isEventCallbackPath(message.getUri())) {
            if (message.getOperation().getMethod().equals(UpnpRequest.Method.NOTIFY)) {
                return createReceivingEvent(message);
            }
        } else if (message.getUri().getPath().contains("/event/cb")) {
            Logger logger2 = log;
            logger2.warning("Fixing trailing garbage in event message path: " + message.getUri().getPath());
            String invalid = message.getUri().toString();
            message.setUri(URI.create(invalid.substring(0, invalid.indexOf(Namespace.CALLBACK_FILE) + Namespace.CALLBACK_FILE.length())));
            if (getUpnpService().getConfiguration().getNamespace().isEventCallbackPath(message.getUri()) && message.getOperation().getMethod().equals(UpnpRequest.Method.NOTIFY)) {
                return createReceivingEvent(message);
            }
        }
        throw new ProtocolCreationException("Protocol for message type not found: " + message);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingNotificationAlive createSendingNotificationAlive(LocalDevice localDevice) {
        return new SendingNotificationAlive(getUpnpService(), localDevice);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingNotificationByebye createSendingNotificationByebye(LocalDevice localDevice) {
        return new SendingNotificationByebye(getUpnpService(), localDevice);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingSearch createSendingSearch(UpnpHeader searchTarget, int mxSeconds) {
        return new SendingSearch(getUpnpService(), searchTarget, mxSeconds);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingAction createSendingAction(ActionInvocation actionInvocation, URL controlURL) {
        return new SendingAction(getUpnpService(), actionInvocation, controlURL);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) throws ProtocolCreationException {
        try {
            List<NetworkAddress> activeStreamServers = getUpnpService().getRouter().getActiveStreamServers(subscription.getService().getDevice().getIdentity().getDiscoveredOnLocalAddress());
            return new SendingSubscribe(getUpnpService(), subscription, activeStreamServers);
        } catch (RouterException ex) {
            throw new ProtocolCreationException("Failed to obtain local stream servers (for event callback URL creation) from router", ex);
        }
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingRenewal createSendingRenewal(RemoteGENASubscription subscription) {
        return new SendingRenewal(getUpnpService(), subscription);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription) {
        return new SendingUnsubscribe(getUpnpService(), subscription);
    }

    @Override // org.fourthline.cling.protocol.ProtocolFactory
    public SendingEvent createSendingEvent(LocalGENASubscription subscription) {
        return new SendingEvent(getUpnpService(), subscription);
    }

    protected ReceivingRetrieval createReceivingRetrieval(StreamRequestMessage message) {
        return new ReceivingRetrieval(getUpnpService(), message);
    }

    protected ReceivingAction createReceivingAction(StreamRequestMessage message) {
        return new ReceivingAction(getUpnpService(), message);
    }

    protected ReceivingSubscribe createReceivingSubscribe(StreamRequestMessage message) {
        return new ReceivingSubscribe(getUpnpService(), message);
    }

    protected ReceivingUnsubscribe createReceivingUnsubscribe(StreamRequestMessage message) {
        return new ReceivingUnsubscribe(getUpnpService(), message);
    }

    protected ReceivingEvent createReceivingEvent(StreamRequestMessage message) {
        return new ReceivingEvent(getUpnpService(), message);
    }
}
