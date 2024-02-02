package org.fourthline.cling.transport.impl;

import org.fourthline.cling.transport.spi.ServletContainerAdapter;
import org.fourthline.cling.transport.spi.StreamServerConfiguration;
/* loaded from: classes.dex */
public class AsyncServletStreamServerConfigurationImpl implements StreamServerConfiguration {
    protected int asyncTimeoutSeconds;
    protected int listenPort;
    protected ServletContainerAdapter servletContainerAdapter;

    public AsyncServletStreamServerConfigurationImpl(ServletContainerAdapter servletContainerAdapter) {
        this.listenPort = 0;
        this.asyncTimeoutSeconds = 60;
        this.servletContainerAdapter = servletContainerAdapter;
    }

    public AsyncServletStreamServerConfigurationImpl(ServletContainerAdapter servletContainerAdapter, int listenPort) {
        this.listenPort = 0;
        this.asyncTimeoutSeconds = 60;
        this.servletContainerAdapter = servletContainerAdapter;
        this.listenPort = listenPort;
    }

    public AsyncServletStreamServerConfigurationImpl(ServletContainerAdapter servletContainerAdapter, int listenPort, int asyncTimeoutSeconds) {
        this.listenPort = 0;
        this.asyncTimeoutSeconds = 60;
        this.servletContainerAdapter = servletContainerAdapter;
        this.listenPort = listenPort;
        this.asyncTimeoutSeconds = asyncTimeoutSeconds;
    }

    @Override // org.fourthline.cling.transport.spi.StreamServerConfiguration
    public int getListenPort() {
        return this.listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getAsyncTimeoutSeconds() {
        return this.asyncTimeoutSeconds;
    }

    public void setAsyncTimeoutSeconds(int asyncTimeoutSeconds) {
        this.asyncTimeoutSeconds = asyncTimeoutSeconds;
    }

    public ServletContainerAdapter getServletContainerAdapter() {
        return this.servletContainerAdapter;
    }

    public void setServletContainerAdapter(ServletContainerAdapter servletContainerAdapter) {
        this.servletContainerAdapter = servletContainerAdapter;
    }
}
