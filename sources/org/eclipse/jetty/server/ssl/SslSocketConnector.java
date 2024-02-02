package org.eclipse.jetty.server.ssl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.io.bio.SocketEndPoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
/* loaded from: classes.dex */
public class SslSocketConnector extends SocketConnector implements SslConnector {
    private static final Logger LOG = Log.getLogger(SslSocketConnector.class);
    private int _handshakeTimeout;
    private final SslContextFactory _sslContextFactory;

    public SslSocketConnector() {
        this(new SslContextFactory(SslContextFactory.DEFAULT_KEYSTORE_PATH));
        setSoLingerTime(30000);
    }

    public SslSocketConnector(SslContextFactory sslContextFactory) {
        this._handshakeTimeout = 0;
        this._sslContextFactory = sslContextFactory;
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    public boolean isAllowRenegotiate() {
        return this._sslContextFactory.isAllowRenegotiate();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    public void setAllowRenegotiate(boolean allowRenegotiate) {
        this._sslContextFactory.setAllowRenegotiate(allowRenegotiate);
    }

    @Override // org.eclipse.jetty.server.bio.SocketConnector, org.eclipse.jetty.server.AbstractConnector
    public void accept(int acceptorID) throws IOException, InterruptedException {
        Socket socket = this._serverSocket.accept();
        configure(socket);
        SocketConnector.ConnectorEndPoint connection = new SslConnectorEndPoint(socket);
        connection.dispatch();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.AbstractConnector
    public void configure(Socket socket) throws IOException {
        super.configure(socket);
    }

    @Override // org.eclipse.jetty.server.bio.SocketConnector, org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.server.Connector
    public void customize(EndPoint endpoint, Request request) throws IOException {
        super.customize(endpoint, request);
        request.setScheme("https");
        SocketEndPoint socket_end_point = (SocketEndPoint) endpoint;
        SSLSocket sslSocket = (SSLSocket) socket_end_point.getTransport();
        SSLSession sslSession = sslSocket.getSession();
        SslCertificates.customize(sslSession, endpoint, request);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String[] getExcludeCipherSuites() {
        return this._sslContextFactory.getExcludeCipherSuites();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String[] getIncludeCipherSuites() {
        return this._sslContextFactory.getIncludeCipherSuites();
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
    public String getProtocol() {
        return this._sslContextFactory.getProtocol();
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
    public SslContextFactory getSslContextFactory() {
        return this._sslContextFactory;
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public String getTruststoreType() {
        return this._sslContextFactory.getTrustStoreType();
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public boolean getWantClientAuth() {
        return this._sslContextFactory.getWantClientAuth();
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

    @Override // org.eclipse.jetty.server.bio.SocketConnector, org.eclipse.jetty.server.Connector
    public void open() throws IOException {
        this._sslContextFactory.checkKeyStore();
        try {
            this._sslContextFactory.start();
            super.open();
        } catch (Exception e) {
            throw new RuntimeIOException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.bio.SocketConnector, org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._sslContextFactory.checkKeyStore();
        this._sslContextFactory.start();
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.bio.SocketConnector, org.eclipse.jetty.server.AbstractConnector, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._sslContextFactory.stop();
        super.doStop();
    }

    @Override // org.eclipse.jetty.server.bio.SocketConnector
    protected ServerSocket newServerSocket(String host, int port, int backlog) throws IOException {
        return this._sslContextFactory.newSslServerSocket(host, port, backlog);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setExcludeCipherSuites(String[] cipherSuites) {
        this._sslContextFactory.setExcludeCipherSuites(cipherSuites);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setIncludeCipherSuites(String[] cipherSuites) {
        this._sslContextFactory.setIncludeCipherSuites(cipherSuites);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setKeyPassword(String password) {
        this._sslContextFactory.setKeyManagerPassword(password);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setKeystore(String keystore) {
        this._sslContextFactory.setKeyStorePath(keystore);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setKeystoreType(String keystoreType) {
        this._sslContextFactory.setKeyStoreType(keystoreType);
    }

    @Override // org.eclipse.jetty.server.ssl.SslConnector
    @Deprecated
    public void setNeedClientAuth(boolean needClientAuth) {
        this._sslContextFactory.setNeedClientAuth(needClientAuth);
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
    public void setProtocol(String protocol) {
        this._sslContextFactory.setProtocol(protocol);
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
    @Deprecated
    public void setWantClientAuth(boolean wantClientAuth) {
        this._sslContextFactory.setWantClientAuth(wantClientAuth);
    }

    public void setHandshakeTimeout(int msec) {
        this._handshakeTimeout = msec;
    }

    public int getHandshakeTimeout() {
        return this._handshakeTimeout;
    }

    /* loaded from: classes.dex */
    public class SslConnectorEndPoint extends SocketConnector.ConnectorEndPoint {
        @Override // org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint, org.eclipse.jetty.io.bio.SocketEndPoint, org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
        public /* bridge */ /* synthetic */ void close() throws IOException {
            super.close();
        }

        @Override // org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint
        public /* bridge */ /* synthetic */ void dispatch() throws IOException {
            super.dispatch();
        }

        @Override // org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint, org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
        public /* bridge */ /* synthetic */ int fill(Buffer x0) throws IOException {
            return super.fill(x0);
        }

        @Override // org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint, org.eclipse.jetty.io.ConnectedEndPoint
        public /* bridge */ /* synthetic */ Connection getConnection() {
            return super.getConnection();
        }

        @Override // org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint, org.eclipse.jetty.io.ConnectedEndPoint
        public /* bridge */ /* synthetic */ void setConnection(Connection x0) {
            super.setConnection(x0);
        }

        public SslConnectorEndPoint(Socket socket) throws IOException {
            super(socket);
        }

        @Override // org.eclipse.jetty.io.bio.SocketEndPoint, org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
        public void shutdownOutput() throws IOException {
            close();
        }

        @Override // org.eclipse.jetty.io.bio.SocketEndPoint, org.eclipse.jetty.io.bio.StreamEndPoint, org.eclipse.jetty.io.EndPoint
        public void shutdownInput() throws IOException {
            close();
        }

        @Override // org.eclipse.jetty.server.bio.SocketConnector.ConnectorEndPoint, java.lang.Runnable
        public void run() {
            try {
                int handshakeTimeout = SslSocketConnector.this.getHandshakeTimeout();
                int oldTimeout = this._socket.getSoTimeout();
                if (handshakeTimeout > 0) {
                    this._socket.setSoTimeout(handshakeTimeout);
                }
                final SSLSocket ssl = (SSLSocket) this._socket;
                ssl.addHandshakeCompletedListener(new HandshakeCompletedListener() { // from class: org.eclipse.jetty.server.ssl.SslSocketConnector.SslConnectorEndPoint.1
                    boolean handshook = false;

                    @Override // javax.net.ssl.HandshakeCompletedListener
                    public void handshakeCompleted(HandshakeCompletedEvent event) {
                        if (this.handshook) {
                            if (!SslSocketConnector.this._sslContextFactory.isAllowRenegotiate()) {
                                Logger logger = SslSocketConnector.LOG;
                                logger.warn("SSL renegotiate denied: " + ssl, new Object[0]);
                                try {
                                    ssl.close();
                                    return;
                                } catch (IOException e) {
                                    SslSocketConnector.LOG.warn(e);
                                    return;
                                }
                            }
                            return;
                        }
                        this.handshook = true;
                    }
                });
                ssl.startHandshake();
                if (handshakeTimeout > 0) {
                    this._socket.setSoTimeout(oldTimeout);
                }
                super.run();
            } catch (SSLException e) {
                SslSocketConnector.LOG.debug(e);
                try {
                    close();
                } catch (IOException e2) {
                    SslSocketConnector.LOG.ignore(e2);
                }
            } catch (IOException e3) {
                SslSocketConnector.LOG.debug(e3);
                try {
                    close();
                } catch (IOException e22) {
                    SslSocketConnector.LOG.ignore(e22);
                }
            }
        }
    }

    @Deprecated
    public String getAlgorithm() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setAlgorithm(String algorithm) {
        throw new UnsupportedOperationException();
    }
}
