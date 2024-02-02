package org.eclipse.jetty.server.ssl;

import com.apple.dnssd.DNSSD;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
/* loaded from: classes.dex */
public class ServletSSL {
    public static int deduceKeyLength(String cipherSuite) {
        if (cipherSuite == null) {
            return 0;
        }
        if (cipherSuite.indexOf("WITH_AES_256_") >= 0) {
            return CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE;
        }
        if (cipherSuite.indexOf("WITH_RC4_128_") < 0 && cipherSuite.indexOf("WITH_AES_128_") < 0) {
            if (cipherSuite.indexOf("WITH_RC4_40_") >= 0) {
                return 40;
            }
            if (cipherSuite.indexOf("WITH_3DES_EDE_CBC_") >= 0) {
                return 168;
            }
            if (cipherSuite.indexOf("WITH_IDEA_CBC_") >= 0) {
                return DNSSD.REGISTRATION_DOMAINS;
            }
            if (cipherSuite.indexOf("WITH_RC2_CBC_40_") < 0 && cipherSuite.indexOf("WITH_DES40_CBC_") < 0) {
                if (cipherSuite.indexOf("WITH_DES_CBC_") < 0) {
                    return 0;
                }
                return 56;
            }
            return 40;
        }
        return DNSSD.REGISTRATION_DOMAINS;
    }
}
