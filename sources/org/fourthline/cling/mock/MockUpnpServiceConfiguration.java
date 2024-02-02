package org.fourthline.cling.mock;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
@Alternative
/* loaded from: classes.dex */
public class MockUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {
    protected final boolean maintainsRegistry;
    protected final boolean multiThreaded;

    public MockUpnpServiceConfiguration() {
        this(false, false);
    }

    public MockUpnpServiceConfiguration(boolean maintainsRegistry) {
        this(maintainsRegistry, false);
    }

    public MockUpnpServiceConfiguration(boolean maintainsRegistry, boolean multiThreaded) {
        super(false);
        this.maintainsRegistry = maintainsRegistry;
        this.multiThreaded = multiThreaded;
    }

    public boolean isMaintainsRegistry() {
        return this.maintainsRegistry;
    }

    public boolean isMultiThreaded() {
        return this.multiThreaded;
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort) {
        return new NetworkAddressFactoryImpl(streamListenPort) { // from class: org.fourthline.cling.mock.MockUpnpServiceConfiguration.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl
            public boolean isUsableNetworkInterface(NetworkInterface iface) throws Exception {
                return iface.isLoopback();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl
            public boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) {
                return address.isLoopbackAddress() && (address instanceof Inet4Address);
            }
        };
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getRegistryMaintainerExecutor() {
        return getDefaultExecutorService();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    public ExecutorService getDefaultExecutorService() {
        if (isMultiThreaded()) {
            return super.getDefaultExecutorService();
        }
        return new AbstractExecutorService() { // from class: org.fourthline.cling.mock.MockUpnpServiceConfiguration.2
            boolean terminated;

            @Override // java.util.concurrent.ExecutorService
            public void shutdown() {
                this.terminated = true;
            }

            @Override // java.util.concurrent.ExecutorService
            public List<Runnable> shutdownNow() {
                shutdown();
                return null;
            }

            @Override // java.util.concurrent.ExecutorService
            public boolean isShutdown() {
                return this.terminated;
            }

            @Override // java.util.concurrent.ExecutorService
            public boolean isTerminated() {
                return this.terminated;
            }

            @Override // java.util.concurrent.ExecutorService
            public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
                shutdown();
                return this.terminated;
            }

            @Override // java.util.concurrent.Executor
            public void execute(Runnable runnable) {
                runnable.run();
            }
        };
    }
}
