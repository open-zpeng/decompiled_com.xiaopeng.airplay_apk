package org.fourthline.cling.transport.spi;

import java.util.concurrent.ExecutorService;
import org.fourthline.cling.model.ServerClientTokens;
/* loaded from: classes.dex */
public abstract class AbstractStreamClientConfiguration implements StreamClientConfiguration {
    protected int logWarningSeconds;
    protected ExecutorService requestExecutorService;
    protected int timeoutSeconds;

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractStreamClientConfiguration(ExecutorService requestExecutorService) {
        this.timeoutSeconds = 15;
        this.logWarningSeconds = 5;
        this.requestExecutorService = requestExecutorService;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractStreamClientConfiguration(ExecutorService requestExecutorService, int timeoutSeconds) {
        this.timeoutSeconds = 15;
        this.logWarningSeconds = 5;
        this.requestExecutorService = requestExecutorService;
        this.timeoutSeconds = timeoutSeconds;
    }

    protected AbstractStreamClientConfiguration(ExecutorService requestExecutorService, int timeoutSeconds, int logWarningSeconds) {
        this.timeoutSeconds = 15;
        this.logWarningSeconds = 5;
        this.requestExecutorService = requestExecutorService;
        this.timeoutSeconds = timeoutSeconds;
        this.logWarningSeconds = logWarningSeconds;
    }

    @Override // org.fourthline.cling.transport.spi.StreamClientConfiguration
    public ExecutorService getRequestExecutorService() {
        return this.requestExecutorService;
    }

    public void setRequestExecutorService(ExecutorService requestExecutorService) {
        this.requestExecutorService = requestExecutorService;
    }

    @Override // org.fourthline.cling.transport.spi.StreamClientConfiguration
    public int getTimeoutSeconds() {
        return this.timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override // org.fourthline.cling.transport.spi.StreamClientConfiguration
    public int getLogWarningSeconds() {
        return this.logWarningSeconds;
    }

    public void setLogWarningSeconds(int logWarningSeconds) {
        this.logWarningSeconds = logWarningSeconds;
    }

    @Override // org.fourthline.cling.transport.spi.StreamClientConfiguration
    public String getUserAgentValue(int majorVersion, int minorVersion) {
        return new ServerClientTokens(majorVersion, minorVersion).toString();
    }
}
