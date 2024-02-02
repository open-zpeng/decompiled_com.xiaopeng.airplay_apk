package org.fourthline.cling.mock;

import javax.enterprise.inject.Alternative;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;
import org.fourthline.cling.protocol.async.SendingSearch;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryImpl;
import org.fourthline.cling.registry.RegistryMaintainer;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
@Alternative
/* loaded from: classes.dex */
public class MockUpnpService implements UpnpService {
    protected final UpnpServiceConfiguration configuration;
    protected final ControlPoint controlPoint;
    protected final NetworkAddressFactory networkAddressFactory;
    protected final ProtocolFactory protocolFactory;
    protected final Registry registry;
    protected final MockRouter router;

    public MockUpnpService() {
        this(false, new MockUpnpServiceConfiguration(false, false));
    }

    public MockUpnpService(MockUpnpServiceConfiguration configuration) {
        this(false, configuration);
    }

    public MockUpnpService(boolean sendsAlive, boolean maintainsRegistry) {
        this(sendsAlive, new MockUpnpServiceConfiguration(maintainsRegistry, false));
    }

    public MockUpnpService(boolean sendsAlive, boolean maintainsRegistry, boolean multiThreaded) {
        this(sendsAlive, new MockUpnpServiceConfiguration(maintainsRegistry, multiThreaded));
    }

    public MockUpnpService(boolean sendsAlive, final MockUpnpServiceConfiguration configuration) {
        this.configuration = configuration;
        this.protocolFactory = createProtocolFactory(this, sendsAlive);
        this.registry = new RegistryImpl(this) { // from class: org.fourthline.cling.mock.MockUpnpService.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // org.fourthline.cling.registry.RegistryImpl
            public RegistryMaintainer createRegistryMaintainer() {
                if (configuration.isMaintainsRegistry()) {
                    return super.createRegistryMaintainer();
                }
                return null;
            }
        };
        this.networkAddressFactory = this.configuration.createNetworkAddressFactory();
        this.router = createRouter();
        this.controlPoint = new ControlPointImpl(configuration, this.protocolFactory, this.registry);
    }

    protected ProtocolFactory createProtocolFactory(UpnpService service, boolean sendsAlive) {
        return new MockProtocolFactory(service, sendsAlive);
    }

    protected MockRouter createRouter() {
        return new MockRouter(getConfiguration(), getProtocolFactory());
    }

    /* loaded from: classes.dex */
    public static class MockProtocolFactory extends ProtocolFactoryImpl {
        private boolean sendsAlive;

        public MockProtocolFactory(UpnpService upnpService, boolean sendsAlive) {
            super(upnpService);
            this.sendsAlive = sendsAlive;
        }

        @Override // org.fourthline.cling.protocol.ProtocolFactoryImpl, org.fourthline.cling.protocol.ProtocolFactory
        public SendingNotificationAlive createSendingNotificationAlive(LocalDevice localDevice) {
            return new SendingNotificationAlive(getUpnpService(), localDevice) { // from class: org.fourthline.cling.mock.MockUpnpService.MockProtocolFactory.1
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // org.fourthline.cling.protocol.async.SendingNotificationAlive, org.fourthline.cling.protocol.async.SendingNotification, org.fourthline.cling.protocol.SendingAsync
                public void execute() throws RouterException {
                    if (MockProtocolFactory.this.sendsAlive) {
                        super.execute();
                    }
                }
            };
        }

        @Override // org.fourthline.cling.protocol.ProtocolFactoryImpl, org.fourthline.cling.protocol.ProtocolFactory
        public SendingSearch createSendingSearch(UpnpHeader searchTarget, int mxSeconds) {
            return new SendingSearch(getUpnpService(), searchTarget, mxSeconds) { // from class: org.fourthline.cling.mock.MockUpnpService.MockProtocolFactory.2
                @Override // org.fourthline.cling.protocol.async.SendingSearch
                public int getBulkIntervalMilliseconds() {
                    return 0;
                }
            };
        }
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
    public MockRouter getRouter() {
        return this.router;
    }

    @Override // org.fourthline.cling.UpnpService
    public void shutdown() {
        getRegistry().shutdown();
        getConfiguration().shutdown();
    }
}
