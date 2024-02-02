package org.fourthline.cling.transport.impl.jetty;

import java.util.concurrent.ExecutorService;
import org.fourthline.cling.transport.spi.AbstractStreamClientConfiguration;
/* loaded from: classes.dex */
public class StreamClientConfigurationImpl extends AbstractStreamClientConfiguration {
    public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService) {
        super(timeoutExecutorService);
    }

    public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds) {
        super(timeoutExecutorService, timeoutSeconds);
    }

    public int getRequestRetryCount() {
        return 0;
    }
}
