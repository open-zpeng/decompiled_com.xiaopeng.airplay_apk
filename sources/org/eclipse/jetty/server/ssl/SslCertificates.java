package org.eclipse.jetty.server.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class SslCertificates {
    private static final Logger LOG = Log.getLogger(SslCertificates.class);
    static final String CACHED_INFO_ATTR = CachedInfo.class.getName();

    public static X509Certificate[] getCertChain(SSLSession sslSession) {
        try {
            javax.security.cert.X509Certificate[] javaxCerts = sslSession.getPeerCertificateChain();
            if (javaxCerts != null && javaxCerts.length != 0) {
                int length = javaxCerts.length;
                X509Certificate[] javaCerts = new X509Certificate[length];
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                for (int i = 0; i < length; i++) {
                    byte[] bytes = javaxCerts[i].getEncoded();
                    ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                    javaCerts[i] = (X509Certificate) cf.generateCertificate(stream);
                }
                return javaCerts;
            }
            return null;
        } catch (SSLPeerUnverifiedException e) {
            return null;
        } catch (Exception e2) {
            LOG.warn(Log.EXCEPTION, e2);
            return null;
        }
    }

    public static void customize(SSLSession sslSession, EndPoint endpoint, Request request) throws IOException {
        Integer keySize;
        X509Certificate[] certs;
        String idStr;
        request.setScheme("https");
        try {
            String cipherSuite = sslSession.getCipherSuite();
            CachedInfo cachedInfo = (CachedInfo) sslSession.getValue(CACHED_INFO_ATTR);
            if (cachedInfo != null) {
                keySize = cachedInfo.getKeySize();
                certs = cachedInfo.getCerts();
                idStr = cachedInfo.getIdStr();
            } else {
                keySize = new Integer(ServletSSL.deduceKeyLength(cipherSuite));
                certs = getCertChain(sslSession);
                byte[] bytes = sslSession.getId();
                String idStr2 = TypeUtil.toHexString(bytes);
                sslSession.putValue(CACHED_INFO_ATTR, new CachedInfo(keySize, certs, idStr2));
                idStr = idStr2;
            }
            if (certs != null) {
                request.setAttribute("javax.servlet.request.X509Certificate", certs);
            }
            request.setAttribute("javax.servlet.request.cipher_suite", cipherSuite);
            request.setAttribute("javax.servlet.request.key_size", keySize);
            request.setAttribute("javax.servlet.request.ssl_session_id", idStr);
        } catch (Exception e) {
            LOG.warn(Log.EXCEPTION, e);
        }
    }

    /* loaded from: classes.dex */
    private static class CachedInfo {
        private final X509Certificate[] _certs;
        private final String _idStr;
        private final Integer _keySize;

        CachedInfo(Integer keySize, X509Certificate[] certs, String idStr) {
            this._keySize = keySize;
            this._certs = certs;
            this._idStr = idStr;
        }

        X509Certificate[] getCerts() {
            return this._certs;
        }

        Integer getKeySize() {
            return this._keySize;
        }

        String getIdStr() {
            return this._idStr;
        }
    }
}
