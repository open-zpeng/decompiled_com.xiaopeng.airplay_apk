package org.fourthline.cling.transport.spi;

import java.util.concurrent.ExecutorService;
/* loaded from: classes.dex */
public interface StreamClientConfiguration {
    int getLogWarningSeconds();

    ExecutorService getRequestExecutorService();

    int getTimeoutSeconds();

    String getUserAgentValue(int i, int i2);
}
