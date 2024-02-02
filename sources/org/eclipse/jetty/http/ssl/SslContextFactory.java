package org.eclipse.jetty.http.ssl;
/* loaded from: classes.dex */
public class SslContextFactory extends org.eclipse.jetty.util.ssl.SslContextFactory {
    public SslContextFactory() {
    }

    public SslContextFactory(boolean trustAll) {
        super(trustAll);
    }

    public SslContextFactory(String keyStorePath) {
        super(keyStorePath);
    }
}
