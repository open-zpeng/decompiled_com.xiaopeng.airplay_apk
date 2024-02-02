package org.fourthline.cling;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryImpl;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.RouterImpl;
import org.seamless.util.Exceptions;
@Alternative
/* loaded from: classes.dex */
public class UpnpServiceImpl implements UpnpService {
    private static Logger log = Logger.getLogger(UpnpServiceImpl.class.getName());
    protected final UpnpServiceConfiguration configuration;
    protected final ControlPoint controlPoint;
    protected final ProtocolFactory protocolFactory;
    protected final Registry registry;
    protected final Router router;

    public UpnpServiceImpl() {
        this(new DefaultUpnpServiceConfiguration(), new RegistryListener[0]);
    }

    public UpnpServiceImpl(RegistryListener... registryListeners) {
        this(new DefaultUpnpServiceConfiguration(), registryListeners);
    }

    public UpnpServiceImpl(UpnpServiceConfiguration configuration, RegistryListener... registryListeners) {
        this.configuration = configuration;
        log.info(">>> Starting UPnP service...");
        log.info("Using configuration: " + getConfiguration().getClass().getName());
        this.protocolFactory = createProtocolFactory();
        this.registry = createRegistry(this.protocolFactory);
        for (RegistryListener registryListener : registryListeners) {
            this.registry.addListener(registryListener);
        }
        this.router = createRouter(this.protocolFactory, this.registry);
        try {
            this.router.enable();
            this.controlPoint = createControlPoint(this.protocolFactory, this.registry);
            log.info("<<< UPnP service started successfully");
        } catch (RouterException ex) {
            throw new RuntimeException("Enabling network router failed: " + ex, ex);
        }
    }

    protected ProtocolFactory createProtocolFactory() {
        return new ProtocolFactoryImpl(this);
    }

    protected Registry createRegistry(ProtocolFactory protocolFactory) {
        return new RegistryImpl(this);
    }

    protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
        return new RouterImpl(getConfiguration(), protocolFactory);
    }

    protected ControlPoint createControlPoint(ProtocolFactory protocolFactory, Registry registry) {
        return new ControlPointImpl(getConfiguration(), protocolFactory, registry);
    }

    @Override // org.fourthline.cling.UpnpService
    public UpnpServiceConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.UpnpService
    public ControlPoint getControlPoint() {
        return this.controlPoint;
    }

    @Override // org.fourthline.cling.UpnpService
    public ProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

    @Override // org.fourthline.cling.UpnpService
    public Registry getRegistry() {
        return this.registry;
    }

    @Override // org.fourthline.cling.UpnpService
    public Router getRouter() {
        return this.router;
    }

    @Override // org.fourthline.cling.UpnpService
    public synchronized void shutdown() {
        shutdown(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void shutdown(boolean separateThread) {
        Runnable shutdown = new Runnable() { // from class: org.fourthline.cling.UpnpServiceImpl.1
            @Override // java.lang.Runnable
            public void run() {
                UpnpServiceImpl.log.info(">>> Shutting down UPnP service...");
                UpnpServiceImpl.this.shutdownRegistry();
                UpnpServiceImpl.this.shutdownRouter();
                UpnpServiceImpl.this.shutdownConfiguration();
                UpnpServiceImpl.log.info("<<< UPnP service shutdown completed");
            }
        };
        if (separateThread) {
            new Thread(shutdown).start();
        } else {
            shutdown.run();
        }
    }

    protected void shutdownRegistry() {
        getRegistry().shutdown();
    }

    protected void shutdownRouter() {
        try {
            getRouter().shutdown();
        } catch (RouterException ex) {
            Throwable cause = Exceptions.unwrap(ex);
            if (cause instanceof InterruptedException) {
                Logger logger = log;
                Level level = Level.INFO;
                logger.log(level, "Router shutdown was interrupted: " + ex, cause);
                return;
            }
            Logger logger2 = log;
            Level level2 = Level.SEVERE;
            logger2.log(level2, "Router error on shutdown: " + ex, cause);
        }
    }

    protected void shutdownConfiguration() {
        getConfiguration().shutdown();
    }
}
