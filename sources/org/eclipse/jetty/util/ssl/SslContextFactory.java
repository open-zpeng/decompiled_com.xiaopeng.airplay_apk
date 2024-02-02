package org.eclipse.jetty.util.ssl;

import com.xpeng.airplay.service.NsdConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.CertificateUtils;
import org.eclipse.jetty.util.security.CertificateValidator;
import org.eclipse.jetty.util.security.Password;
/* loaded from: classes.dex */
public class SslContextFactory extends AbstractLifeCycle {
    public static final String DEFAULT_KEYMANAGERFACTORY_ALGORITHM;
    public static final String DEFAULT_KEYSTORE_PATH;
    public static final String DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM;
    public static final String KEYPASSWORD_PROPERTY = "org.eclipse.jetty.ssl.keypassword";
    public static final String PASSWORD_PROPERTY = "org.eclipse.jetty.ssl.password";
    private boolean _allowRenegotiate;
    private String _certAlias;
    private SSLContext _context;
    private String _crlPath;
    private boolean _enableCRLDP;
    private boolean _enableOCSP;
    private final Set<String> _excludeCipherSuites;
    private final Set<String> _excludeProtocols;
    private Set<String> _includeCipherSuites;
    private Set<String> _includeProtocols;
    private String _keyManagerFactoryAlgorithm;
    private transient Password _keyManagerPassword;
    private KeyStore _keyStore;
    private InputStream _keyStoreInputStream;
    private transient Password _keyStorePassword;
    private String _keyStorePath;
    private String _keyStoreProvider;
    private String _keyStoreType;
    private int _maxCertPathLength;
    private boolean _needClientAuth;
    private String _ocspResponderURL;
    private String _secureRandomAlgorithm;
    private boolean _sessionCachingEnabled;
    private String _sslProtocol;
    private String _sslProvider;
    private int _sslSessionCacheSize;
    private int _sslSessionTimeout;
    private boolean _trustAll;
    private String _trustManagerFactoryAlgorithm;
    private KeyStore _trustStore;
    private InputStream _trustStoreInputStream;
    private transient Password _trustStorePassword;
    private String _trustStorePath;
    private String _trustStoreProvider;
    private String _trustStoreType;
    private boolean _validateCerts;
    private boolean _validatePeerCerts;
    private boolean _wantClientAuth;
    public static final TrustManager[] TRUST_ALL_CERTS = {new X509TrustManager() { // from class: org.eclipse.jetty.util.ssl.SslContextFactory.1
        @Override // javax.net.ssl.X509TrustManager
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};
    private static final Logger LOG = Log.getLogger(SslContextFactory.class);

    static {
        DEFAULT_KEYMANAGERFACTORY_ALGORITHM = Security.getProperty("ssl.KeyManagerFactory.algorithm") == null ? "SunX509" : Security.getProperty("ssl.KeyManagerFactory.algorithm");
        DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM = Security.getProperty("ssl.TrustManagerFactory.algorithm") == null ? "SunX509" : Security.getProperty("ssl.TrustManagerFactory.algorithm");
        DEFAULT_KEYSTORE_PATH = System.getProperty("user.home") + File.separator + ".keystore";
    }

    public SslContextFactory() {
        this._excludeProtocols = new LinkedHashSet();
        this._includeProtocols = new LinkedHashSet();
        this._excludeCipherSuites = new LinkedHashSet();
        this._includeCipherSuites = new LinkedHashSet();
        this._keyStoreType = "JKS";
        this._trustStoreType = "JKS";
        this._needClientAuth = false;
        this._wantClientAuth = false;
        this._allowRenegotiate = true;
        this._sslProtocol = "TLS";
        this._keyManagerFactoryAlgorithm = DEFAULT_KEYMANAGERFACTORY_ALGORITHM;
        this._trustManagerFactoryAlgorithm = DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM;
        this._maxCertPathLength = -1;
        this._enableCRLDP = false;
        this._enableOCSP = false;
        this._sessionCachingEnabled = true;
        this._trustAll = true;
    }

    public SslContextFactory(boolean trustAll) {
        this._excludeProtocols = new LinkedHashSet();
        this._includeProtocols = new LinkedHashSet();
        this._excludeCipherSuites = new LinkedHashSet();
        this._includeCipherSuites = new LinkedHashSet();
        this._keyStoreType = "JKS";
        this._trustStoreType = "JKS";
        this._needClientAuth = false;
        this._wantClientAuth = false;
        this._allowRenegotiate = true;
        this._sslProtocol = "TLS";
        this._keyManagerFactoryAlgorithm = DEFAULT_KEYMANAGERFACTORY_ALGORITHM;
        this._trustManagerFactoryAlgorithm = DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM;
        this._maxCertPathLength = -1;
        this._enableCRLDP = false;
        this._enableOCSP = false;
        this._sessionCachingEnabled = true;
        this._trustAll = trustAll;
    }

    public SslContextFactory(String keyStorePath) {
        this._excludeProtocols = new LinkedHashSet();
        this._includeProtocols = new LinkedHashSet();
        this._excludeCipherSuites = new LinkedHashSet();
        this._includeCipherSuites = new LinkedHashSet();
        this._keyStoreType = "JKS";
        this._trustStoreType = "JKS";
        this._needClientAuth = false;
        this._wantClientAuth = false;
        this._allowRenegotiate = true;
        this._sslProtocol = "TLS";
        this._keyManagerFactoryAlgorithm = DEFAULT_KEYMANAGERFACTORY_ALGORITHM;
        this._trustManagerFactoryAlgorithm = DEFAULT_TRUSTMANAGERFACTORY_ALGORITHM;
        this._maxCertPathLength = -1;
        this._enableCRLDP = false;
        this._enableOCSP = false;
        this._sessionCachingEnabled = true;
        this._keyStorePath = keyStorePath;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        String str;
        if (this._context == null) {
            if (this._keyStore == null && this._keyStoreInputStream == null && this._keyStorePath == null && this._trustStore == null && this._trustStoreInputStream == null && this._trustStorePath == null) {
                TrustManager[] trust_managers = null;
                if (this._trustAll) {
                    LOG.debug("No keystore or trust store configured.  ACCEPTING UNTRUSTED CERTIFICATES!!!!!", new Object[0]);
                    trust_managers = TRUST_ALL_CERTS;
                }
                SecureRandom secureRandom = this._secureRandomAlgorithm == null ? null : SecureRandom.getInstance(this._secureRandomAlgorithm);
                this._context = this._sslProvider == null ? SSLContext.getInstance(this._sslProtocol) : SSLContext.getInstance(this._sslProtocol, this._sslProvider);
                this._context.init(null, trust_managers, secureRandom);
                return;
            }
            checkKeyStore();
            KeyStore keyStore = loadKeyStore();
            KeyStore trustStore = loadTrustStore();
            Collection<? extends CRL> crls = loadCRL(this._crlPath);
            if (this._validateCerts && keyStore != null) {
                if (this._certAlias == null) {
                    List<String> aliases = Collections.list(keyStore.aliases());
                    this._certAlias = aliases.size() == 1 ? aliases.get(0) : null;
                }
                Certificate cert = this._certAlias == null ? null : keyStore.getCertificate(this._certAlias);
                if (cert == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("No certificate found in the keystore");
                    if (this._certAlias == null) {
                        str = "";
                    } else {
                        str = " for alias " + this._certAlias;
                    }
                    sb.append(str);
                    throw new Exception(sb.toString());
                }
                CertificateValidator validator = new CertificateValidator(trustStore, crls);
                validator.setMaxCertPathLength(this._maxCertPathLength);
                validator.setEnableCRLDP(this._enableCRLDP);
                validator.setEnableOCSP(this._enableOCSP);
                validator.setOcspResponderURL(this._ocspResponderURL);
                validator.validate(keyStore, cert);
            }
            KeyManager[] keyManagers = getKeyManagers(keyStore);
            TrustManager[] trustManagers = getTrustManagers(trustStore, crls);
            SecureRandom secureRandom2 = this._secureRandomAlgorithm != null ? SecureRandom.getInstance(this._secureRandomAlgorithm) : null;
            this._context = this._sslProvider == null ? SSLContext.getInstance(this._sslProtocol) : SSLContext.getInstance(this._sslProtocol, this._sslProvider);
            this._context.init(keyManagers, trustManagers, secureRandom2);
            SSLEngine engine = newSslEngine();
            LOG.info("Enabled Protocols {} of {}", Arrays.asList(engine.getEnabledProtocols()), Arrays.asList(engine.getSupportedProtocols()));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Enabled Ciphers   {} of {}", Arrays.asList(engine.getEnabledCipherSuites()), Arrays.asList(engine.getSupportedCipherSuites()));
            }
        }
    }

    public String[] getExcludeProtocols() {
        return (String[]) this._excludeProtocols.toArray(new String[this._excludeProtocols.size()]);
    }

    public void setExcludeProtocols(String... protocols) {
        checkNotStarted();
        this._excludeProtocols.clear();
        this._excludeProtocols.addAll(Arrays.asList(protocols));
    }

    public void addExcludeProtocols(String... protocol) {
        checkNotStarted();
        this._excludeProtocols.addAll(Arrays.asList(protocol));
    }

    public String[] getIncludeProtocols() {
        return (String[]) this._includeProtocols.toArray(new String[this._includeProtocols.size()]);
    }

    public void setIncludeProtocols(String... protocols) {
        checkNotStarted();
        this._includeProtocols.clear();
        this._includeProtocols.addAll(Arrays.asList(protocols));
    }

    public String[] getExcludeCipherSuites() {
        return (String[]) this._excludeCipherSuites.toArray(new String[this._excludeCipherSuites.size()]);
    }

    public void setExcludeCipherSuites(String... cipherSuites) {
        checkNotStarted();
        this._excludeCipherSuites.clear();
        this._excludeCipherSuites.addAll(Arrays.asList(cipherSuites));
    }

    public void addExcludeCipherSuites(String... cipher) {
        checkNotStarted();
        this._excludeCipherSuites.addAll(Arrays.asList(cipher));
    }

    public String[] getIncludeCipherSuites() {
        return (String[]) this._includeCipherSuites.toArray(new String[this._includeCipherSuites.size()]);
    }

    public void setIncludeCipherSuites(String... cipherSuites) {
        checkNotStarted();
        this._includeCipherSuites.clear();
        this._includeCipherSuites.addAll(Arrays.asList(cipherSuites));
    }

    public String getKeyStorePath() {
        return this._keyStorePath;
    }

    @Deprecated
    public String getKeyStore() {
        return this._keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        checkNotStarted();
        this._keyStorePath = keyStorePath;
    }

    @Deprecated
    public void setKeyStore(String keyStorePath) {
        checkNotStarted();
        this._keyStorePath = keyStorePath;
    }

    public String getKeyStoreProvider() {
        return this._keyStoreProvider;
    }

    public void setKeyStoreProvider(String keyStoreProvider) {
        checkNotStarted();
        this._keyStoreProvider = keyStoreProvider;
    }

    public String getKeyStoreType() {
        return this._keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        checkNotStarted();
        this._keyStoreType = keyStoreType;
    }

    @Deprecated
    public InputStream getKeyStoreInputStream() {
        checkKeyStore();
        return this._keyStoreInputStream;
    }

    @Deprecated
    public void setKeyStoreInputStream(InputStream keyStoreInputStream) {
        checkNotStarted();
        this._keyStoreInputStream = keyStoreInputStream;
    }

    public String getCertAlias() {
        return this._certAlias;
    }

    public void setCertAlias(String certAlias) {
        checkNotStarted();
        this._certAlias = certAlias;
    }

    public String getTrustStore() {
        return this._trustStorePath;
    }

    public void setTrustStore(String trustStorePath) {
        checkNotStarted();
        this._trustStorePath = trustStorePath;
    }

    public String getTrustStoreProvider() {
        return this._trustStoreProvider;
    }

    public void setTrustStoreProvider(String trustStoreProvider) {
        checkNotStarted();
        this._trustStoreProvider = trustStoreProvider;
    }

    public String getTrustStoreType() {
        return this._trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        checkNotStarted();
        this._trustStoreType = trustStoreType;
    }

    @Deprecated
    public InputStream getTrustStoreInputStream() {
        checkKeyStore();
        return this._trustStoreInputStream;
    }

    @Deprecated
    public void setTrustStoreInputStream(InputStream trustStoreInputStream) {
        checkNotStarted();
        this._trustStoreInputStream = trustStoreInputStream;
    }

    public boolean getNeedClientAuth() {
        return this._needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        checkNotStarted();
        this._needClientAuth = needClientAuth;
    }

    public boolean getWantClientAuth() {
        return this._wantClientAuth;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
        checkNotStarted();
        this._wantClientAuth = wantClientAuth;
    }

    @Deprecated
    public boolean getValidateCerts() {
        return this._validateCerts;
    }

    public boolean isValidateCerts() {
        return this._validateCerts;
    }

    public void setValidateCerts(boolean validateCerts) {
        checkNotStarted();
        this._validateCerts = validateCerts;
    }

    public boolean isValidatePeerCerts() {
        return this._validatePeerCerts;
    }

    public void setValidatePeerCerts(boolean validatePeerCerts) {
        checkNotStarted();
        this._validatePeerCerts = validatePeerCerts;
    }

    public boolean isAllowRenegotiate() {
        return this._allowRenegotiate;
    }

    public void setAllowRenegotiate(boolean allowRenegotiate) {
        checkNotStarted();
        this._allowRenegotiate = allowRenegotiate;
    }

    public void setKeyStorePassword(String password) {
        checkNotStarted();
        this._keyStorePassword = Password.getPassword("org.eclipse.jetty.ssl.password", password, null);
    }

    public void setKeyManagerPassword(String password) {
        checkNotStarted();
        this._keyManagerPassword = Password.getPassword("org.eclipse.jetty.ssl.keypassword", password, null);
    }

    public void setTrustStorePassword(String password) {
        checkNotStarted();
        this._trustStorePassword = Password.getPassword("org.eclipse.jetty.ssl.password", password, null);
    }

    public String getProvider() {
        return this._sslProvider;
    }

    public void setProvider(String provider) {
        checkNotStarted();
        this._sslProvider = provider;
    }

    public String getProtocol() {
        return this._sslProtocol;
    }

    public void setProtocol(String protocol) {
        checkNotStarted();
        this._sslProtocol = protocol;
    }

    public String getSecureRandomAlgorithm() {
        return this._secureRandomAlgorithm;
    }

    public void setSecureRandomAlgorithm(String algorithm) {
        checkNotStarted();
        this._secureRandomAlgorithm = algorithm;
    }

    public String getSslKeyManagerFactoryAlgorithm() {
        return this._keyManagerFactoryAlgorithm;
    }

    public void setSslKeyManagerFactoryAlgorithm(String algorithm) {
        checkNotStarted();
        this._keyManagerFactoryAlgorithm = algorithm;
    }

    public String getTrustManagerFactoryAlgorithm() {
        return this._trustManagerFactoryAlgorithm;
    }

    public boolean isTrustAll() {
        return this._trustAll;
    }

    public void setTrustAll(boolean trustAll) {
        this._trustAll = trustAll;
    }

    public void setTrustManagerFactoryAlgorithm(String algorithm) {
        checkNotStarted();
        this._trustManagerFactoryAlgorithm = algorithm;
    }

    public String getCrlPath() {
        return this._crlPath;
    }

    public void setCrlPath(String crlPath) {
        checkNotStarted();
        this._crlPath = crlPath;
    }

    public int getMaxCertPathLength() {
        return this._maxCertPathLength;
    }

    public void setMaxCertPathLength(int maxCertPathLength) {
        checkNotStarted();
        this._maxCertPathLength = maxCertPathLength;
    }

    public SSLContext getSslContext() {
        if (!isStarted()) {
            throw new IllegalStateException(getState());
        }
        return this._context;
    }

    public void setSslContext(SSLContext sslContext) {
        checkNotStarted();
        this._context = sslContext;
    }

    protected KeyStore loadKeyStore() throws Exception {
        if (this._keyStore != null) {
            return this._keyStore;
        }
        return getKeyStore(this._keyStoreInputStream, this._keyStorePath, this._keyStoreType, this._keyStoreProvider, this._keyStorePassword == null ? null : this._keyStorePassword.toString());
    }

    protected KeyStore loadTrustStore() throws Exception {
        if (this._trustStore != null) {
            return this._trustStore;
        }
        return getKeyStore(this._trustStoreInputStream, this._trustStorePath, this._trustStoreType, this._trustStoreProvider, this._trustStorePassword == null ? null : this._trustStorePassword.toString());
    }

    @Deprecated
    protected KeyStore getKeyStore(InputStream storeStream, String storePath, String storeType, String storeProvider, String storePassword) throws Exception {
        return CertificateUtils.getKeyStore(storeStream, storePath, storeType, storeProvider, storePassword);
    }

    protected Collection<? extends CRL> loadCRL(String crlPath) throws Exception {
        return CertificateUtils.loadCRL(crlPath);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x002b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected javax.net.ssl.KeyManager[] getKeyManagers(java.security.KeyStore r7) throws java.lang.Exception {
        /*
            r6 = this;
            r0 = 0
            if (r7 == 0) goto L45
            java.lang.String r1 = r6._keyManagerFactoryAlgorithm
            javax.net.ssl.KeyManagerFactory r1 = javax.net.ssl.KeyManagerFactory.getInstance(r1)
            org.eclipse.jetty.util.security.Password r2 = r6._keyManagerPassword
            if (r2 != 0) goto L16
            org.eclipse.jetty.util.security.Password r2 = r6._keyStorePassword
            if (r2 != 0) goto L13
            r2 = 0
            goto L20
        L13:
            org.eclipse.jetty.util.security.Password r2 = r6._keyStorePassword
            goto L18
        L16:
            org.eclipse.jetty.util.security.Password r2 = r6._keyManagerPassword
        L18:
            java.lang.String r2 = r2.toString()
            char[] r2 = r2.toCharArray()
        L20:
            r1.init(r7, r2)
            javax.net.ssl.KeyManager[] r0 = r1.getKeyManagers()
            java.lang.String r2 = r6._certAlias
            if (r2 == 0) goto L45
            r2 = 0
        L2c:
            int r3 = r0.length
            if (r2 >= r3) goto L45
            r3 = r0[r2]
            boolean r3 = r3 instanceof javax.net.ssl.X509KeyManager
            if (r3 == 0) goto L42
            org.eclipse.jetty.util.ssl.AliasedX509ExtendedKeyManager r3 = new org.eclipse.jetty.util.ssl.AliasedX509ExtendedKeyManager
            java.lang.String r4 = r6._certAlias
            r5 = r0[r2]
            javax.net.ssl.X509KeyManager r5 = (javax.net.ssl.X509KeyManager) r5
            r3.<init>(r4, r5)
            r0[r2] = r3
        L42:
            int r2 = r2 + 1
            goto L2c
        L45:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.ssl.SslContextFactory.getKeyManagers(java.security.KeyStore):javax.net.ssl.KeyManager[]");
    }

    protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception {
        if (trustStore == null) {
            return null;
        }
        if (this._validatePeerCerts && this._trustManagerFactoryAlgorithm.equalsIgnoreCase("PKIX")) {
            PKIXBuilderParameters pbParams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            pbParams.setMaxPathLength(this._maxCertPathLength);
            pbParams.setRevocationEnabled(true);
            if (crls != null && !crls.isEmpty()) {
                pbParams.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(crls)));
            }
            if (this._enableCRLDP) {
                System.setProperty("com.sun.security.enableCRLDP", NsdConstants.AIRPLAY_TXT_VALUE_DA);
            }
            if (this._enableOCSP) {
                Security.setProperty("ocsp.enable", NsdConstants.AIRPLAY_TXT_VALUE_DA);
                if (this._ocspResponderURL != null) {
                    Security.setProperty("ocsp.responderURL", this._ocspResponderURL);
                }
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(this._trustManagerFactoryAlgorithm);
            trustManagerFactory.init(new CertPathTrustManagerParameters(pbParams));
            TrustManager[] managers = trustManagerFactory.getTrustManagers();
            return managers;
        }
        TrustManagerFactory trustManagerFactory2 = TrustManagerFactory.getInstance(this._trustManagerFactoryAlgorithm);
        trustManagerFactory2.init(trustStore);
        TrustManager[] managers2 = trustManagerFactory2.getTrustManagers();
        return managers2;
    }

    public void checkKeyStore() {
        if (this._context != null) {
            return;
        }
        if (this._keyStore == null && this._keyStoreInputStream == null && this._keyStorePath == null) {
            throw new IllegalStateException("SSL doesn't have a valid keystore");
        }
        if (this._trustStore == null && this._trustStoreInputStream == null && this._trustStorePath == null) {
            this._trustStore = this._keyStore;
            this._trustStorePath = this._keyStorePath;
            this._trustStoreInputStream = this._keyStoreInputStream;
            this._trustStoreType = this._keyStoreType;
            this._trustStoreProvider = this._keyStoreProvider;
            this._trustStorePassword = this._keyStorePassword;
            this._trustManagerFactoryAlgorithm = this._keyManagerFactoryAlgorithm;
        }
        if (this._keyStoreInputStream != null && this._keyStoreInputStream == this._trustStoreInputStream) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IO.copy(this._keyStoreInputStream, baos);
                this._keyStoreInputStream.close();
                this._keyStoreInputStream = new ByteArrayInputStream(baos.toByteArray());
                this._trustStoreInputStream = new ByteArrayInputStream(baos.toByteArray());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    public String[] selectProtocols(String[] enabledProtocols, String[] supportedProtocols) {
        Set<String> selected_protocols = new LinkedHashSet<>();
        if (!this._includeProtocols.isEmpty()) {
            for (String protocol : this._includeProtocols) {
                if (Arrays.asList(supportedProtocols).contains(protocol)) {
                    selected_protocols.add(protocol);
                }
            }
        } else {
            selected_protocols.addAll(Arrays.asList(enabledProtocols));
        }
        if (this._excludeProtocols != null) {
            selected_protocols.removeAll(this._excludeProtocols);
        }
        return (String[]) selected_protocols.toArray(new String[selected_protocols.size()]);
    }

    public String[] selectCipherSuites(String[] enabledCipherSuites, String[] supportedCipherSuites) {
        Set<String> selected_ciphers = new LinkedHashSet<>();
        if (!this._includeCipherSuites.isEmpty()) {
            for (String cipherSuite : this._includeCipherSuites) {
                if (Arrays.asList(supportedCipherSuites).contains(cipherSuite)) {
                    selected_ciphers.add(cipherSuite);
                }
            }
        } else {
            selected_ciphers.addAll(Arrays.asList(enabledCipherSuites));
        }
        if (this._excludeCipherSuites != null) {
            selected_ciphers.removeAll(this._excludeCipherSuites);
        }
        return (String[]) selected_ciphers.toArray(new String[selected_ciphers.size()]);
    }

    protected void checkNotStarted() {
        if (isStarted()) {
            throw new IllegalStateException("Cannot modify configuration when " + getState());
        }
    }

    public boolean isEnableCRLDP() {
        return this._enableCRLDP;
    }

    public void setEnableCRLDP(boolean enableCRLDP) {
        checkNotStarted();
        this._enableCRLDP = enableCRLDP;
    }

    public boolean isEnableOCSP() {
        return this._enableOCSP;
    }

    public void setEnableOCSP(boolean enableOCSP) {
        checkNotStarted();
        this._enableOCSP = enableOCSP;
    }

    public String getOcspResponderURL() {
        return this._ocspResponderURL;
    }

    public void setOcspResponderURL(String ocspResponderURL) {
        checkNotStarted();
        this._ocspResponderURL = ocspResponderURL;
    }

    public void setKeyStore(KeyStore keyStore) {
        checkNotStarted();
        this._keyStore = keyStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        checkNotStarted();
        this._trustStore = trustStore;
    }

    public void setKeyStoreResource(Resource resource) {
        checkNotStarted();
        try {
            this._keyStoreInputStream = resource.getInputStream();
        } catch (IOException e) {
            throw new InvalidParameterException("Unable to get resource input stream for resource " + resource.toString());
        }
    }

    public void setTrustStoreResource(Resource resource) {
        checkNotStarted();
        try {
            this._trustStoreInputStream = resource.getInputStream();
        } catch (IOException e) {
            throw new InvalidParameterException("Unable to get resource input stream for resource " + resource.toString());
        }
    }

    public boolean isSessionCachingEnabled() {
        return this._sessionCachingEnabled;
    }

    public void setSessionCachingEnabled(boolean enableSessionCaching) {
        this._sessionCachingEnabled = enableSessionCaching;
    }

    public int getSslSessionCacheSize() {
        return this._sslSessionCacheSize;
    }

    public void setSslSessionCacheSize(int sslSessionCacheSize) {
        this._sslSessionCacheSize = sslSessionCacheSize;
    }

    public int getSslSessionTimeout() {
        return this._sslSessionTimeout;
    }

    public void setSslSessionTimeout(int sslSessionTimeout) {
        this._sslSessionTimeout = sslSessionTimeout;
    }

    public SSLServerSocket newSslServerSocket(String host, int port, int backlog) throws IOException {
        SSLServerSocketFactory factory = this._context.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) (host == null ? factory.createServerSocket(port, backlog) : factory.createServerSocket(port, backlog, InetAddress.getByName(host)));
        if (getWantClientAuth()) {
            socket.setWantClientAuth(getWantClientAuth());
        }
        if (getNeedClientAuth()) {
            socket.setNeedClientAuth(getNeedClientAuth());
        }
        socket.setEnabledCipherSuites(selectCipherSuites(socket.getEnabledCipherSuites(), socket.getSupportedCipherSuites()));
        socket.setEnabledProtocols(selectProtocols(socket.getEnabledProtocols(), socket.getSupportedProtocols()));
        return socket;
    }

    public SSLSocket newSslSocket() throws IOException {
        SSLSocketFactory factory = this._context.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket();
        if (getWantClientAuth()) {
            socket.setWantClientAuth(getWantClientAuth());
        }
        if (getNeedClientAuth()) {
            socket.setNeedClientAuth(getNeedClientAuth());
        }
        socket.setEnabledCipherSuites(selectCipherSuites(socket.getEnabledCipherSuites(), socket.getSupportedCipherSuites()));
        socket.setEnabledProtocols(selectProtocols(socket.getEnabledProtocols(), socket.getSupportedProtocols()));
        return socket;
    }

    public SSLEngine newSslEngine(String host, int port) {
        SSLEngine sslEngine = isSessionCachingEnabled() ? this._context.createSSLEngine(host, port) : this._context.createSSLEngine();
        customize(sslEngine);
        return sslEngine;
    }

    public SSLEngine newSslEngine() {
        SSLEngine sslEngine = this._context.createSSLEngine();
        customize(sslEngine);
        return sslEngine;
    }

    public void customize(SSLEngine sslEngine) {
        if (getWantClientAuth()) {
            sslEngine.setWantClientAuth(getWantClientAuth());
        }
        if (getNeedClientAuth()) {
            sslEngine.setNeedClientAuth(getNeedClientAuth());
        }
        sslEngine.setEnabledCipherSuites(selectCipherSuites(sslEngine.getEnabledCipherSuites(), sslEngine.getSupportedCipherSuites()));
        sslEngine.setEnabledProtocols(selectProtocols(sslEngine.getEnabledProtocols(), sslEngine.getSupportedProtocols()));
    }

    public String toString() {
        return String.format("%s@%x(%s,%s)", getClass().getSimpleName(), Integer.valueOf(hashCode()), this._keyStorePath, this._trustStorePath);
    }
}
