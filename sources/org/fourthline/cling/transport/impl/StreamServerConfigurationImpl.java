package org.fourthline.cling.transport.impl;

import org.fourthline.cling.transport.spi.StreamServerConfiguration;
/* loaded from: classes.dex */
public class StreamServerConfigurationImpl implements StreamServerConfiguration {
    private int listenPort;
    private int tcpConnectionBacklog;

    public StreamServerConfigurationImpl() {
    }

    public StreamServerConfigurationImpl(int listenPort) {
        this.listenPort = listenPort;
    }

    @Override // org.fourthline.cling.transport.spi.StreamServerConfiguration
    public int getListenPort() {
        return this.listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getTcpConnectionBacklog() {
        return this.tcpConnectionBacklog;
    }

    public void setTcpConnectionBacklog(int tcpConnectionBacklog) {
        this.tcpConnectionBacklog = tcpConnectionBacklog;
    }
}
