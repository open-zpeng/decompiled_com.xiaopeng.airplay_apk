package org.fourthline.cling.transport.impl;

import org.fourthline.cling.transport.spi.DatagramIOConfiguration;
/* loaded from: classes.dex */
public class DatagramIOConfigurationImpl implements DatagramIOConfiguration {
    private int maxDatagramBytes;
    private int timeToLive;

    public DatagramIOConfigurationImpl() {
        this.timeToLive = 4;
        this.maxDatagramBytes = 640;
    }

    public DatagramIOConfigurationImpl(int timeToLive, int maxDatagramBytes) {
        this.timeToLive = 4;
        this.maxDatagramBytes = 640;
        this.timeToLive = timeToLive;
        this.maxDatagramBytes = maxDatagramBytes;
    }

    @Override // org.fourthline.cling.transport.spi.DatagramIOConfiguration
    public int getTimeToLive() {
        return this.timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override // org.fourthline.cling.transport.spi.DatagramIOConfiguration
    public int getMaxDatagramBytes() {
        return this.maxDatagramBytes;
    }

    public void setMaxDatagramBytes(int maxDatagramBytes) {
        this.maxDatagramBytes = maxDatagramBytes;
    }
}
