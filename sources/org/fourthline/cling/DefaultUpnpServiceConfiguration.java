package org.fourthline.cling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.transport.impl.DatagramIOConfigurationImpl;
import org.fourthline.cling.transport.impl.DatagramIOImpl;
import org.fourthline.cling.transport.impl.DatagramProcessorImpl;
import org.fourthline.cling.transport.impl.GENAEventProcessorImpl;
import org.fourthline.cling.transport.impl.MulticastReceiverConfigurationImpl;
import org.fourthline.cling.transport.impl.MulticastReceiverImpl;
import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.impl.SOAPActionProcessorImpl;
import org.fourthline.cling.transport.impl.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.StreamClientImpl;
import org.fourthline.cling.transport.impl.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.StreamServerImpl;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.seamless.util.Exceptions;
@Alternative
/* loaded from: classes.dex */
public class DefaultUpnpServiceConfiguration implements UpnpServiceConfiguration {
    private static Logger log = Logger.getLogger(DefaultUpnpServiceConfiguration.class.getName());
    private final DatagramProcessor datagramProcessor;
    private final ExecutorService defaultExecutorService;
    private final DeviceDescriptorBinder deviceDescriptorBinderUDA10;
    private final GENAEventProcessor genaEventProcessor;
    private final Namespace namespace;
    private final ServiceDescriptorBinder serviceDescriptorBinderUDA10;
    private final SOAPActionProcessor soapActionProcessor;
    private final int streamListenPort;
    private final ArrayBlockingQueue<Runnable> upnpStreamQueue;

    public DefaultUpnpServiceConfiguration() {
        this(0);
    }

    public DefaultUpnpServiceConfiguration(int streamListenPort) {
        this(streamListenPort, true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public DefaultUpnpServiceConfiguration(boolean checkRuntime) {
        this(0, checkRuntime);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public DefaultUpnpServiceConfiguration(int streamListenPort, boolean checkRuntime) {
        if (checkRuntime && ModelUtil.ANDROID_RUNTIME) {
            throw new Error("Unsupported runtime environment, use org.fourthline.cling.android.AndroidUpnpServiceConfiguration");
        }
        this.streamListenPort = streamListenPort;
        this.defaultExecutorService = createDefaultExecutorService();
        this.datagramProcessor = createDatagramProcessor();
        this.soapActionProcessor = createSOAPActionProcessor();
        this.genaEventProcessor = createGENAEventProcessor();
        this.deviceDescriptorBinderUDA10 = createDeviceDescriptorBinderUDA10();
        this.serviceDescriptorBinderUDA10 = createServiceDescriptorBinderUDA10();
        this.namespace = createNamespace();
        this.upnpStreamQueue = new ArrayBlockingQueue<>(5);
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public DatagramProcessor getDatagramProcessor() {
        return this.datagramProcessor;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public SOAPActionProcessor getSoapActionProcessor() {
        return this.soapActionProcessor;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public GENAEventProcessor getGenaEventProcessor() {
        return this.genaEventProcessor;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public StreamClient createStreamClient() {
        return new StreamClientImpl(new StreamClientConfigurationImpl(getSyncProtocolExecutorService()));
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public MulticastReceiver createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
        return new MulticastReceiverImpl(new MulticastReceiverConfigurationImpl(networkAddressFactory.getMulticastGroup(), networkAddressFactory.getMulticastPort()));
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public DatagramIO createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return new DatagramIOImpl(new DatagramIOConfigurationImpl());
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new StreamServerImpl(new StreamServerConfigurationImpl(networkAddressFactory.getStreamListenPort()));
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getMulticastReceiverExecutor() {
        return getDefaultExecutorService();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getDatagramIOExecutor() {
        return getDefaultExecutorService();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getStreamServerExecutorService() {
        return getDefaultExecutorService();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
        return this.deviceDescriptorBinderUDA10;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
        return this.serviceDescriptorBinderUDA10;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ServiceType[] getExclusiveServiceTypes() {
        return new ServiceType[0];
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public boolean isReceivedSubscriptionTimeoutIgnored() {
        return false;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity) {
        return null;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public UpnpHeaders getEventSubscriptionHeaders(RemoteService service) {
        return null;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public int getRegistryMaintenanceIntervalMillis() {
        return 1000;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public int getAliveIntervalMillis() {
        return 0;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public Integer getRemoteDeviceMaxAgeSeconds() {
        return null;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getAsyncProtocolExecutor() {
        return getDefaultExecutorService();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getSyncProtocolExecutorService() {
        return new SyncProtocolExecutor(this.upnpStreamQueue);
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public Namespace getNamespace() {
        return this.namespace;
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getRegistryMaintainerExecutor() {
        return getDefaultExecutorService();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ExecutorService getRegistryListenerExecutor() {
        return getDefaultExecutorService();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public NetworkAddressFactory createNetworkAddressFactory() {
        return createNetworkAddressFactory(this.streamListenPort);
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public void shutdown() {
        log.fine("Shutting down default executor service");
        getDefaultExecutorService().shutdownNow();
    }

    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort) {
        return new NetworkAddressFactoryImpl(streamListenPort);
    }

    protected DatagramProcessor createDatagramProcessor() {
        return new DatagramProcessorImpl();
    }

    protected SOAPActionProcessor createSOAPActionProcessor() {
        return new SOAPActionProcessorImpl();
    }

    protected GENAEventProcessor createGENAEventProcessor() {
        return new GENAEventProcessorImpl();
    }

    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new UDA10DeviceDescriptorBinderImpl();
    }

    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new UDA10ServiceDescriptorBinderImpl();
    }

    protected Namespace createNamespace() {
        return new Namespace();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public ExecutorService getDefaultExecutorService() {
        return this.defaultExecutorService;
    }

    protected ExecutorService createDefaultExecutorService() {
        return new ClingExecutor();
    }

    @Override // org.fourthline.cling.UpnpServiceConfiguration
    public ArrayBlockingQueue<Runnable> getUpnpStreamQueue() {
        return this.upnpStreamQueue;
    }

    /* loaded from: classes.dex */
    public static class SyncProtocolExecutor extends ThreadPoolExecutor {
        private ArrayBlockingQueue<Runnable> upnpRequestQue;

        public SyncProtocolExecutor(ArrayBlockingQueue<Runnable> requestQue) {
            this(new ClingThreadFactory("SyncProto-"), new ThreadPoolExecutor.DiscardPolicy() { // from class: org.fourthline.cling.DefaultUpnpServiceConfiguration.SyncProtocolExecutor.1
                @Override // java.util.concurrent.ThreadPoolExecutor.DiscardPolicy, java.util.concurrent.RejectedExecutionHandler
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                    Logger logger = DefaultUpnpServiceConfiguration.log;
                    logger.info("Thread pool rejected execution of " + runnable.getClass());
                    super.rejectedExecution(runnable, threadPoolExecutor);
                }
            });
            this.upnpRequestQue = requestQue;
        }

        public SyncProtocolExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedHandler) {
            super(0, Runtime.getRuntime().availableProcessors() * 2, 60L, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory, rejectedHandler);
        }

        @Override // java.util.concurrent.ThreadPoolExecutor
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            super.afterExecute(runnable, throwable);
            if (this.upnpRequestQue != null && (runnable instanceof UpnpStream)) {
                try {
                    this.upnpRequestQue.offer(runnable, 300L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public static class ClingExecutor extends ThreadPoolExecutor {
        public ClingExecutor() {
            this(new ClingThreadFactory(), new ThreadPoolExecutor.DiscardPolicy() { // from class: org.fourthline.cling.DefaultUpnpServiceConfiguration.ClingExecutor.1
                @Override // java.util.concurrent.ThreadPoolExecutor.DiscardPolicy, java.util.concurrent.RejectedExecutionHandler
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                    Logger logger = DefaultUpnpServiceConfiguration.log;
                    logger.info("Thread pool rejected execution of " + runnable.getClass());
                    super.rejectedExecution(runnable, threadPoolExecutor);
                }
            });
        }

        public ClingExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedHandler) {
            super(0, (Runtime.getRuntime().availableProcessors() * 2) + 4, 60L, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory, rejectedHandler);
        }

        @Override // java.util.concurrent.ThreadPoolExecutor
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            super.afterExecute(runnable, throwable);
            if (throwable != null) {
                Throwable cause = Exceptions.unwrap(throwable);
                if (!(cause instanceof InterruptedException)) {
                    Logger logger = DefaultUpnpServiceConfiguration.log;
                    logger.warning("Thread terminated " + runnable + " abruptly with exception: " + throwable);
                    Logger logger2 = DefaultUpnpServiceConfiguration.log;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Root cause: ");
                    sb.append(cause);
                    logger2.warning(sb.toString());
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public static class ClingThreadFactory implements ThreadFactory {
        protected final ThreadGroup group;
        protected String namePrefix;
        protected final AtomicInteger threadNumber;

        public ClingThreadFactory(String namePrefix) {
            this.threadNumber = new AtomicInteger(1);
            this.namePrefix = "cling-";
            this.namePrefix = namePrefix;
            SecurityManager s = System.getSecurityManager();
            this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        public ClingThreadFactory() {
            this.threadNumber = new AtomicInteger(1);
            this.namePrefix = "cling-";
            SecurityManager s = System.getSecurityManager();
            this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable r) {
            ThreadGroup threadGroup = this.group;
            Thread t = new Thread(threadGroup, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != 5) {
                t.setPriority(5);
            }
            return t;
        }
    }
}
