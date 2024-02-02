package org.eclipse.jetty.server.ssl;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.BuffersFactory;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.io.nio.AsyncConnection;
import org.eclipse.jetty.io.nio.SslConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
/* loaded from: classes.dex */
public class SslSelectChannelConnector extends SelectChannelConnector implements SslConnector {
    private Buffers _sslBuffers;
    private final SslContextFactory _sslContextFactory;

    public SslSelectChannelConnector() {
        this(new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH));
        setSoLingerTime(30000);
    }

    public SslSelectChannelConnector(SslContextFactory sslContextFactory) {
        this._sslContextFactory = sslContextFactory;
        addBean(this._sslContextFactory);
        setUseDirectBuffers(false);
        setSoLingerTime(30000);
    }

    @Override // org.eclipse.jetty.server.nio.SelectChannelConnector, org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void customize(EndPoint endpoint, Request request) throws IOException {
        request.setScheme("https");
        super.customize(endpoint, request);
        SslConnection.SslEndPoint sslEndpoint = (SslConnection.SslEndPoint) endpoint;
        SSLEngine sslEngine = sslEndpoint.getSslEngine();
        SSLSession sslSession = sslEngine.getSession();
        SslCertificates.customize(sslSession, endpoint, request);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public boolean isAllowRenegotiate() {
        return this._sslContextFactory.isAllowRenegotiate();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setAllowRenegotiate(boolean allowRenegotiate) {
        this._sslContextFactory.setAllowRenegotiate(allowRenegotiate);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String[] getExcludeCipherSuites() {
        return this._sslContextFactory.getExcludeCipherSuites();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setExcludeCipherSuites(String[] cipherSuites) {
        this._sslContextFactory.setExcludeCipherSuites(cipherSuites);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String[] getIncludeCipherSuites() {
        return this._sslContextFactory.getIncludeCipherSuites();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setIncludeCipherSuites(String[] cipherSuites) {
        this._sslContextFactory.setIncludeCipherSuites(cipherSuites);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setPassword(String password) {
        this._sslContextFactory.setKeyStorePassword(password);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setTrustPassword(String password) {
        this._sslContextFactory.setTrustStorePassword(password);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setKeyPassword(String password) {
        this._sslContextFactory.setKeyManagerPassword(password);
    }

    @Deprecated
    public String getAlgorithm() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setAlgorithm(String algorithm) {
        throw new UnsupportedOperationException();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getProtocol() {
        return this._sslContextFactory.getProtocol();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setProtocol(String protocol) {
        this._sslContextFactory.setProtocol(protocol);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setKeystore(String keystore) {
        this._sslContextFactory.setKeyStorePath(keystore);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getKeystore() {
        return this._sslContextFactory.getKeyStorePath();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getKeystoreType() {
        return this._sslContextFactory.getKeyStoreType();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public boolean getNeedClientAuth() {
        return this._sslContextFactory.getNeedClientAuth();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public boolean getWantClientAuth() {
        return this._sslContextFactory.getWantClientAuth();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setNeedClientAuth(boolean needClientAuth) {
        this._sslContextFactory.setNeedClientAuth(needClientAuth);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setWantClientAuth(boolean wantClientAuth) {
        this._sslContextFactory.setWantClientAuth(wantClientAuth);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setKeystoreType(String keystoreType) {
        this._sslContextFactory.setKeyStoreType(keystoreType);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getProvider() {
        return this._sslContextFactory.getProvider();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getSecureRandomAlgorithm() {
        return this._sslContextFactory.getSecureRandomAlgorithm();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getSslKeyManagerFactoryAlgorithm() {
        return this._sslContextFactory.getSslKeyManagerFactoryAlgorithm();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getSslTrustManagerFactoryAlgorithm() {
        return this._sslContextFactory.getTrustManagerFactoryAlgorithm();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getTruststore() {
        return this._sslContextFactory.getTrustStore();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getTruststoreType() {
        return this._sslContextFactory.getTrustStoreType();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setProvider(String provider) {
        this._sslContextFactory.setProvider(provider);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setSecureRandomAlgorithm(String algorithm) {
        this._sslContextFactory.setSecureRandomAlgorithm(algorithm);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setSslKeyManagerFactoryAlgorithm(String algorithm) {
        this._sslContextFactory.setSslKeyManagerFactoryAlgorithm(algorithm);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setSslTrustManagerFactoryAlgorithm(String algorithm) {
        this._sslContextFactory.setTrustManagerFactoryAlgorithm(algorithm);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setTruststore(String truststore) {
        this._sslContextFactory.setTrustStore(truststore);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setTruststoreType(String truststoreType) {
        this._sslContextFactory.setTrustStoreType(truststoreType);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setSslContext(SSLContext sslContext) {
        this._sslContextFactory.setSslContext(sslContext);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public SSLContext getSslContext() {
        return this._sslContextFactory.getSslContext();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    public SslContextFactory getSslContextFactory() {
        return this._sslContextFactory;
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public boolean isConfidential(Request request) {
        int confidentialPort = getConfidentialPort();
        return confidentialPort == 0 || confidentialPort == request.getServerPort();
    }

    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public boolean isIntegral(Request request) {
        int integralPort = getIntegralPort();
        return integralPort == 0 || integralPort == request.getServerPort();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.nio.SelectChannelConnector
    public AsyncConnection newConnection(SocketChannel channel, AsyncEndPoint endpoint) {
        try {
            SSLEngine engine = createSSLEngine(channel);
            SslConnection connection = newSslConnection(endpoint, engine);
            AsyncConnection delegate = newPlainConnection(channel, connection.getSslEndPoint());
            connection.getSslEndPoint().setConnection(delegate);
            connection.setAllowRenegotiate(this._sslContextFactory.isAllowRenegotiate());
            return connection;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    protected AsyncConnection newPlainConnection(SocketChannel channel, AsyncEndPoint endPoint) {
        return super.newConnection(channel, endPoint);
    }

    protected SslConnection newSslConnection(AsyncEndPoint endpoint, SSLEngine engine) {
        return new SslConnection(engine, endpoint);
    }

    protected SSLEngine createSSLEngine(SocketChannel channel) throws IOException {
        SSLEngine engine;
        if (channel != null) {
            String peerHost = channel.socket().getInetAddress().getHostAddress();
            int peerPort = channel.socket().getPort();
            engine = this._sslContextFactory.newSslEngine(peerHost, peerPort);
        } else {
            engine = this._sslContextFactory.newSslEngine();
        }
        engine.setUseClientMode(false);
        return engine;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.nio.SelectChannelConnector, org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._sslContextFactory.checkKeyStore();
        this._sslContextFactory.start();
        SSLEngine sslEngine = this._sslContextFactory.newSslEngine();
        sslEngine.setUseClientMode(false);
        SSLSession sslSession = sslEngine.getSession();
        this._sslBuffers = BuffersFactory.newBuffers(getUseDirectBuffers() ? Buffers.Type.DIRECT : Buffers.Type.INDIRECT, sslSession.getApplicationBufferSize(), getUseDirectBuffers() ? Buffers.Type.DIRECT : Buffers.Type.INDIRECT, sslSession.getApplicationBufferSize(), getUseDirectBuffers() ? Buffers.Type.DIRECT : Buffers.Type.INDIRECT, getMaxBuffers());
        if (getRequestHeaderSize() < sslSession.getApplicationBufferSize()) {
            setRequestHeaderSize(sslSession.getApplicationBufferSize());
        }
        if (getRequestBufferSize() < sslSession.getApplicationBufferSize()) {
            setRequestBufferSize(sslSession.getApplicationBufferSize());
        }
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._sslBuffers = null;
        super.doStop();
    }

    public Buffers getSslBuffers() {
        return this._sslBuffers;
    }
}
