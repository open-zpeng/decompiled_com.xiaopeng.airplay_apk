package org.eclipse.jetty.util.security;

import com.xpeng.airplay.service.NsdConstants;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class CertificateValidator {
    private static final Logger LOG = Log.getLogger(CertificateValidator.class);
    private static AtomicLong __aliasCount = new AtomicLong();
    private Collection<? extends CRL> _crls;
    private String _ocspResponderURL;
    private KeyStore _trustStore;
    private int _maxCertPathLength = -1;
    private boolean _enableCRLDP = false;
    private boolean _enableOCSP = false;

    public CertificateValidator(KeyStore trustStore, Collection<? extends CRL> crls) {
        if (trustStore == null) {
            throw new InvalidParameterException("TrustStore must be specified for CertificateValidator.");
        }
        this._trustStore = trustStore;
        this._crls = crls;
    }

    public void validate(KeyStore keyStore) throws CertificateException {
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                validate(keyStore, alias);
            }
        } catch (KeyStoreException kse) {
            throw new CertificateException("Unable to retrieve aliases from keystore", kse);
        }
    }

    public String validate(KeyStore keyStore, String keyAlias) throws CertificateException {
        if (keyAlias == null) {
            return null;
        }
        try {
            validate(keyStore, keyStore.getCertificate(keyAlias));
            return keyAlias;
        } catch (KeyStoreException kse) {
            LOG.debug(kse);
            throw new CertificateException("Unable to validate certificate for alias [" + keyAlias + "]: " + kse.getMessage(), kse);
        }
    }

    public void validate(KeyStore keyStore, Certificate cert) throws CertificateException {
        String str;
        if (cert != null && (cert instanceof X509Certificate)) {
            ((X509Certificate) cert).checkValidity();
            try {
                if (keyStore == null) {
                    throw new InvalidParameterException("Keystore cannot be null");
                }
                String certAlias = keyStore.getCertificateAlias((X509Certificate) cert);
                if (certAlias == null) {
                    certAlias = "JETTY" + String.format("%016X", Long.valueOf(__aliasCount.incrementAndGet()));
                    keyStore.setCertificateEntry(certAlias, cert);
                }
                Certificate[] certChain = keyStore.getCertificateChain(certAlias);
                if (certChain == null || certChain.length == 0) {
                    throw new IllegalStateException("Unable to retrieve certificate chain");
                }
                validate(certChain);
            } catch (KeyStoreException kse) {
                LOG.debug(kse);
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to validate certificate");
                if (0 == 0) {
                    str = "";
                } else {
                    str = " for alias [" + ((String) null) + "]";
                }
                sb.append(str);
                sb.append(": ");
                sb.append(kse.getMessage());
                throw new CertificateException(sb.toString(), kse);
            }
        }
    }

    public void validate(Certificate[] certChain) throws CertificateException {
        try {
            ArrayList<X509Certificate> certList = new ArrayList<>();
            for (Certificate item : certChain) {
                if (item != null) {
                    if (!(item instanceof X509Certificate)) {
                        throw new IllegalStateException("Invalid certificate type in chain");
                    }
                    certList.add((X509Certificate) item);
                }
            }
            if (certList.isEmpty()) {
                throw new IllegalStateException("Invalid certificate chain");
            }
            X509CertSelector certSelect = new X509CertSelector();
            certSelect.setCertificate(certList.get(0));
            PKIXBuilderParameters pbParams = new PKIXBuilderParameters(this._trustStore, certSelect);
            pbParams.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList)));
            pbParams.setMaxPathLength(this._maxCertPathLength);
            pbParams.setRevocationEnabled(true);
            if (this._crls != null && !this._crls.isEmpty()) {
                pbParams.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(this._crls)));
            }
            if (this._enableOCSP) {
                Security.setProperty("ocsp.enable", NsdConstants.AIRPLAY_TXT_VALUE_DA);
            }
            if (this._enableCRLDP) {
                System.setProperty("com.sun.security.enableCRLDP", NsdConstants.AIRPLAY_TXT_VALUE_DA);
            }
            CertPathBuilderResult buildResult = CertPathBuilder.getInstance("PKIX").build(pbParams);
            CertPathValidator.getInstance("PKIX").validate(buildResult.getCertPath(), pbParams);
        } catch (GeneralSecurityException gse) {
            LOG.debug(gse);
            throw new CertificateException("Unable to validate certificate: " + gse.getMessage(), gse);
        }
    }

    public KeyStore getTrustStore() {
        return this._trustStore;
    }

    public Collection<? extends CRL> getCrls() {
        return this._crls;
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
