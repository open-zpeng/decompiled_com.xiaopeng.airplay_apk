package com.sun.net.httpserver;

import java.net.InetSocketAddress;
/* loaded from: classes.dex */
public abstract class HttpsParameters {
    private String[] cipherSuites;
    private boolean needClientAuth;
    private String[] protocols;
    private boolean wantClientAuth;

    public abstract InetSocketAddress getClientAddress();

    public abstract HttpsConfigurator getHttpsConfigurator();

    public String[] getCipherSuites() {
        return this.cipherSuites;
    }

    public void setCipherSuites(String[] strArr) {
        this.cipherSuites = strArr;
    }

    public String[] getProtocols() {
        return this.protocols;
    }

    public void setProtocols(String[] strArr) {
        this.protocols = strArr;
    }

    public boolean getWantClientAuth() {
        return this.wantClientAuth;
    }

    public void setWantClientAuth(boolean z) {
        this.wantClientAuth = z;
    }

    public boolean getNeedClientAuth() {
        return this.needClientAuth;
    }

    public void setNeedClientAuth(boolean z) {
        this.needClientAuth = z;
    }
}
