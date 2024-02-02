package org.fourthline.cling.model.types;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.logging.Logger;
import org.eclipse.jetty.util.StringUtil;
import org.fourthline.cling.model.ModelUtil;
/* loaded from: classes.dex */
public class UDN {
    public static final String PREFIX = "uuid:";
    private static final Logger log = Logger.getLogger(UDN.class.getName());
    private String identifierString;

    public UDN(String identifierString) {
        this.identifierString = identifierString;
    }

    public UDN(UUID uuid) {
        this.identifierString = uuid.toString();
    }

    public boolean isUDA11Compliant() {
        try {
            UUID.fromString(this.identifierString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getIdentifierString() {
        return this.identifierString;
    }

    public static UDN valueOf(String udnString) {
        return new UDN(udnString.startsWith("uuid:") ? udnString.substring("uuid:".length()) : udnString);
    }

    public static UDN uniqueSystemIdentifier(String salt) {
        StringBuilder systemSalt = new StringBuilder();
        if (!ModelUtil.ANDROID_RUNTIME) {
            try {
                systemSalt.append(new String(ModelUtil.getFirstNetworkInterfaceHardwareAddress(), StringUtil.__UTF8));
                try {
                    byte[] hash = MessageDigest.getInstance("MD5").digest(systemSalt.toString().getBytes(StringUtil.__UTF8));
                    return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } catch (UnsupportedEncodingException ex2) {
                throw new RuntimeException(ex2);
            }
        }
        throw new RuntimeException("This method does not create a unique identifier on Android, see the Javadoc and use new UDN(UUID) instead!");
    }

    public String toString() {
        return "uuid:" + getIdentifierString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof UDN)) {
            return false;
        }
        UDN udn = (UDN) o;
        return this.identifierString.equals(udn.identifierString);
    }

    public int hashCode() {
        return this.identifierString.hashCode();
    }
}
