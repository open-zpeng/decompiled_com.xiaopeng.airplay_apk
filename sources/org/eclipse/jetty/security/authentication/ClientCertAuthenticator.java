package org.eclipse.jetty.security.authentication;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.security.CertificateUtils;
import org.eclipse.jetty.util.security.CertificateValidator;
import org.eclipse.jetty.util.security.Password;
/* loaded from: classes.dex */
public class ClientCertAuthenticator extends LoginAuthenticator {
    private static final String PASSWORD_PROPERTY = "org.eclipse.jetty.ssl.password";
    private String _crlPath;
    private String _ocspResponderURL;
    private transient Password _trustStorePassword;
    private String _trustStorePath;
    private String _trustStoreProvider;
    private boolean _validateCerts;
    private String _trustStoreType = "JKS";
    private int _maxCertPathLength = -1;
    private boolean _enableCRLDP = false;
    private boolean _enableOCSP = false;

    @Override // org.eclipse.jetty.security.Authenticator
    public String getAuthMethod() {
        return "CLIENT_CERT";
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
        X509Certificate[] arr$;
        if (!mandatory) {
            return new DeferredAuthentication(this);
        }
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (certs != null) {
            try {
                if (certs.length > 0) {
                    if (this._validateCerts) {
                        KeyStore trustStore = getKeyStore(null, this._trustStorePath, this._trustStoreType, this._trustStoreProvider, this._trustStorePassword == null ? null : this._trustStorePassword.toString());
                        Collection<? extends CRL> crls = loadCRL(this._crlPath);
                        CertificateValidator validator = new CertificateValidator(trustStore, crls);
                        validator.validate(certs);
                    }
                    X509Certificate[] arr$2 = certs;
                    int len$ = arr$2.length;
                    int i$ = 0;
                    while (i$ < len$) {
                        X509Certificate cert = arr$2[i$];
                        if (cert == null) {
                            arr$ = arr$2;
                        } else {
                            Principal principal = cert.getSubjectDN();
                            if (principal == null) {
                                principal = cert.getIssuerDN();
                            }
                            String username = principal == null ? "clientcert" : principal.getName();
                            char[] credential = B64Code.encode(cert.getSignature());
                            UserIdentity user = login(username, credential, req);
                            if (user == null) {
                                arr$ = arr$2;
                            } else {
                                return new UserAuthentication(getAuthMethod(), user);
                            }
                        }
                        i$++;
                        arr$2 = arr$;
                    }
                }
            } catch (Exception e) {
                throw new ServerAuthException(e.getMessage());
            }
        }
        if (!DeferredAuthentication.isDeferred(response)) {
            response.sendError(403);
            return Authentication.SEND_FAILURE;
        }
        return Authentication.UNAUTHENTICATED;
    }

    protected KeyStore getKeyStore(InputStream storeStream, String storePath, String storeType, String storeProvider, String storePassword) throws Exception {
        return CertificateUtils.getKeyStore(storeStream, storePath, storeType, storeProvider, storePassword);
    }

    protected Collection<? extends CRL> loadCRL(String crlPath) throws Exception {
        return CertificateUtils.loadCRL(crlPath);
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
        return true;
    }

    public boolean isValidateCerts() {
        return this._validateCerts;
    }

    public void setValidateCerts(boolean validateCerts) {
        this._validateCerts = validateCerts;
    }

    public String getTrustStore() {
        return this._trustStorePath;
    }

    public void setTrustStore(String trustStorePath) {
        this._trustStorePath = trustStorePath;
    }

    public String getTrustStoreProvider() {
        return this._trustStoreProvider;
    }

    public void setTrustStoreProvider(String trustStoreProvider) {
        this._trustStoreProvider = trustStoreProvider;
    }

    public String getTrustStoreType() {
        return this._trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this._trustStoreType = trustStoreType;
    }

    public void setTrustStorePassword(String password) {
        this._trustStorePassword = Password.getPassword("org.eclipse.jetty.ssl.password", password, null);
    }

    public String getCrlPath() {
        return this._crlPath;
    }

    public void setCrlPath(String crlPath) {
        this._crlPath = crlPath;
    }

    public int getMaxCertPathLength() {
        return this._maxCertPathLength;
    }

    public void setMaxCertPathLength(int maxCertPathLength) {
        this._maxCertPathLength = maxCertPathLength;
    }

    public boolean isEnableCRLDP() {
        return this._enableCRLDP;
    }

    public void setEnableCRLDP(boolean enableCRLDP) {
        this._enableCRLDP = enableCRLDP;
    }

    public boolean isEnableOCSP() {
        return this._enableOCSP;
    }

    public void setEnableOCSP(boolean enableOCSP) {
        this._enableOCSP = enableOCSP;
    }

    public String getOcspResponderURL() {
        return this._ocspResponderURL;
    }

    public void setOcspResponderURL(String ocspResponderURL) {
        this._ocspResponderURL = ocspResponderURL;
    }
}
