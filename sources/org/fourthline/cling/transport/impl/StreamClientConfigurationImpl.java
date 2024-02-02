package org.fourthline.cling.transport.impl;

import java.util.concurrent.ExecutorService;
import org.fourthline.cling.transport.spi.AbstractStreamClientConfiguration;
/* loaded from: classes.dex */
public class StreamClientConfigurationImpl extends AbstractStreamClientConfiguration {
    private boolean usePersistentConnections;

    public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService) {
        super(timeoutExecutorService);
        this.usePersistentConnections = false;
    }

    public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds) {
        super(timeoutExecutorService, timeoutSeconds);
        this.usePersistentConnections = false;
    }

    public boolean isUsePersistentConnections() {
        return this.usePersistentConnections;
    }

    public void setUsePersistentConnections(boolean usePersistentConnections) {
        this.usePersistentConnections = usePersistentConnections;
    }
}
