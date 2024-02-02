package org.fourthline.cling.transport.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.fourthline.cling.transport.spi.MulticastReceiverConfiguration;
/* loaded from: classes.dex */
public class MulticastReceiverConfigurationImpl implements MulticastReceiverConfiguration {
    private InetAddress group;
    private int maxDatagramBytes;
    private int port;

    public MulticastReceiverConfigurationImpl(InetAddress group, int port, int maxDatagramBytes) {
        this.group = group;
        this.port = port;
        this.maxDatagramBytes = maxDatagramBytes;
    }

    public MulticastReceiverConfigurationImpl(InetAddress group, int port) {
        this(group, port, 640);
    }

    public MulticastReceiverConfigurationImpl(String group, int port, int maxDatagramBytes) throws UnknownHostException {
        this(InetAddress.getByName(group), port, maxDatagramBytes);
    }

    public MulticastReceiverConfigurationImpl(String group, int port) throws UnknownHostException {
        this(InetAddress.getByName(group), port, 640);
    }

    @Override // org.fourthline.cling.transport.spi.MulticastReceiverConfiguration
    public InetAddress getGroup() {
        return this.group;
    }

    public void setGroup(InetAddress group) {
        this.group = group;
    }

    @Override // org.fourthline.cling.transport.spi.MulticastReceiverConfiguration
    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override // org.fourthline.cling.transport.spi.MulticastReceiverConfiguration
    public int getMaxDatagramBytes() {
        return this.maxDatagramBytes;
    }

    public void setMaxDatagramBytes(int maxDatagramBytes) {
        this.maxDatagramBytes = maxDatagramBytes;
    }
}
