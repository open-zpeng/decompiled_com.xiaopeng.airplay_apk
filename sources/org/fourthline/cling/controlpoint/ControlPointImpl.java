package org.fourthline.cling.controlpoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.event.ExecuteAction;
import org.fourthline.cling.controlpoint.event.Search;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;
@ApplicationScoped
/* loaded from: classes.dex */
public class ControlPointImpl implements ControlPoint {
    private static Logger log = Logger.getLogger(ControlPointImpl.class.getName());
    protected UpnpServiceConfiguration configuration;
    protected ProtocolFactory protocolFactory;
    protected Registry registry;

    protected ControlPointImpl() {
    }

    @Inject
    public ControlPointImpl(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Registry registry) {
        Logger logger = log;
        logger.fine("Creating ControlPoint: " + getClass().getName());
        this.configuration = configuration;
        this.protocolFactory = protocolFactory;
        this.registry = registry;
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public UpnpServiceConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public ProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public Registry getRegistry() {
        return this.registry;
    }

    public void search(@Observes Search search) {
        search(search.getSearchType(), search.getMxSeconds());
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public void search() {
        search(new STAllHeader(), MXHeader.DEFAULT_VALUE.intValue());
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public void search(UpnpHeader searchType) {
        search(searchType, MXHeader.DEFAULT_VALUE.intValue());
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public void search(int mxSeconds) {
        search(new STAllHeader(), mxSeconds);
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public void search(UpnpHeader searchType, int mxSeconds) {
        Logger logger = log;
        logger.fine("Sending asynchronous search for: " + searchType.getString());
        getConfiguration().getAsyncProtocolExecutor().execute(getProtocolFactory().createSendingSearch(searchType, mxSeconds));
    }

    public void execute(ExecuteAction executeAction) {
        execute(executeAction.getCallback());
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public Future execute(ActionCallback callback) {
        Logger logger = log;
        logger.fine("Invoking action in background: " + callback);
        callback.setControlPoint(this);
        ExecutorService executor = getConfiguration().getSyncProtocolExecutorService();
        return executor.submit(callback);
    }

    @Override // org.fourthline.cling.controlpoint.ControlPoint
    public void execute(SubscriptionCallback callback) {
        Logger logger = log;
        logger.fine("Invoking subscription in background: " + callback);
        callback.setControlPoint(this);
        getConfiguration().getSyncProtocolExecutorService().execute(callback);
    }
}
