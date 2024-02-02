package org.fourthline.cling.android;

import android.os.Build;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ServerClientTokens;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl;
import org.fourthline.cling.transport.impl.RecoveringGENAEventProcessorImpl;
import org.fourthline.cling.transport.impl.RecoveringSOAPActionProcessorImpl;
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer;
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.jetty.StreamClientImpl;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;
/* loaded from: classes.dex */
public class AndroidUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {
    private static final String TAG = "AndroidUpnpServiceConfiguration";
    private ExecutorService asyncProtocolExecutor;
    private ExecutorService multicastReceiverExecutor;
    private ExecutorService registryMaintainerExecutor;

    public AndroidUpnpServiceConfiguration() {
        this(0);
    }

    public AndroidUpnpServiceConfiguration(int streamListenPort) {
        super(streamListenPort, false);
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort) {
        return new AndroidNetworkAddressFactory(streamListenPort);
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected Namespace createNamespace() {
        return new Namespace("/upnp");
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public StreamClient createStreamClient() {
        return new StreamClientImpl(new StreamClientConfigurationImpl(getSyncProtocolExecutorService()) { // from class: org.fourthline.cling.android.AndroidUpnpServiceConfiguration.1
            @Override // org.fourthline.cling.transport.spi.AbstractStreamClientConfiguration, org.fourthline.cling.transport.spi.StreamClientConfiguration
            public String getUserAgentValue(int majorVersion, int minorVersion) {
                ServerClientTokens tokens = new ServerClientTokens(majorVersion, minorVersion);
                tokens.setOsName("Android");
                tokens.setOsVersion(Build.VERSION.RELEASE);
                return tokens.toString();
            }
        });
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new AsyncServletStreamServerImpl(new AsyncServletStreamServerConfigurationImpl(JettyServletContainer.INSTANCE, networkAddressFactory.getStreamListenPort(), 15));
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new RecoveringUDA10DeviceDescriptorBinderImpl();
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new UDA10ServiceDescriptorBinderSAXImpl();
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected SOAPActionProcessor createSOAPActionProcessor() {
        return new RecoveringSOAPActionProcessorImpl();
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration
    protected GENAEventProcessor createGENAEventProcessor() {
        return new RecoveringGENAEventProcessorImpl();
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public int getRegistryMaintenanceIntervalMillis() {
        return 3000;
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getMulticastReceiverExecutor() {
        if (this.multicastReceiverExecutor == null) {
            this.multicastReceiverExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("MulticastReceiver"));
        }
        return this.multicastReceiverExecutor;
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getDatagramIOExecutor() {
        return Executors.newSingleThreadExecutor(new SimpleThreadFactory("DatagramIo"));
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getRegistryMaintainerExecutor() {
        if (this.registryMaintainerExecutor == null) {
            this.registryMaintainerExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("RegistryMainter"));
        }
        return this.registryMaintainerExecutor;
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getAsyncProtocolExecutor() {
        if (this.asyncProtocolExecutor == null) {
            this.asyncProtocolExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("AsyncProtocol"));
        }
        return this.asyncProtocolExecutor;
    }

    @Override // org.fourthline.cling.DefaultUpnpServiceConfiguration, org.fourthline.cling.UpnpServiceConfiguration
    public void shutdown() {
        Log.i(TAG, "shutdown");
        if (this.multicastReceiverExecutor != null) {
            this.multicastReceiverExecutor.shutdownNow();
            this.multicastReceiverExecutor = null;
        }
        if (this.registryMaintainerExecutor != null) {
            this.registryMaintainerExecutor.shutdownNow();
            this.registryMaintainerExecutor = null;
        }
        if (this.asyncProtocolExecutor != null) {
            this.asyncProtocolExecutor.shutdownNow();
            this.asyncProtocolExecutor = null;
        }
    }

    /* loaded from: classes.dex */
    private static final class SimpleThreadFactory implements ThreadFactory {
        private String threadName;

        SimpleThreadFactory(String name) {
            this.threadName = name;
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable r) {
            return new Thread(r, this.threadName);
        }
    }
}
