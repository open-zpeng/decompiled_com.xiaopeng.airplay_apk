package org.eclipse.jetty.util.security;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class CertificateUtils {
    public static KeyStore getKeyStore(InputStream storeStream, String storePath, String storeType, String storeProvider, String storePassword) throws Exception {
        KeyStore keystore = null;
        if (storeStream != null || storePath != null) {
            InputStream inStream = storeStream;
            if (inStream == null) {
                try {
                    inStream = Resource.newResource(storePath).getInputStream();
                } catch (Throwable th) {
                    if (inStream != null) {
                        inStream.close();
                    }
                    throw th;
                }
            }
            if (storeProvider != null) {
                keystore = KeyStore.getInstance(storeType, storeProvider);
            } else {
                keystore = KeyStore.getInstance(storeType);
            }
            keystore.load(inStream, storePassword == null ? null : storePassword.toCharArray());
            if (inStream != null) {
                inStream.close();
            }
        }
        return keystore;
    }

    public static Collection<? extends CRL> loadCRL(String crlPath) throws Exception {
        Collection<? extends CRL> crlList = null;
        if (crlPath != null) {
            InputStream in = null;
            try {
                in = Resource.newResource(crlPath).getInputStream();
                crlList = CertificateFactory.getInstance("X.509").generateCRLs(in);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return crlList;
    }
}
