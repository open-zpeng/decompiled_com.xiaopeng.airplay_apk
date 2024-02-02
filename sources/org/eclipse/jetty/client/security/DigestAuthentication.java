package org.eclipse.jetty.client.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
/* loaded from: classes.dex */
public class DigestAuthentication implements Authentication {
    private static final String NC = "00000001";
    Map details;
    Realm securityRealm;

    public DigestAuthentication(Realm realm, Map details) {
        this.securityRealm = realm;
        this.details = details;
    }

    @Override // org.eclipse.jetty.client.security.Authentication
    public void setCredentials(HttpExchange exchange) throws IOException {
        StringBuilder buffer = new StringBuilder().append("Digest");
        buffer.append(" ");
        buffer.append("username");
        buffer.append('=');
        buffer.append('\"');
        buffer.append(this.securityRealm.getPrincipal());
        buffer.append('\"');
        buffer.append(", ");
        buffer.append("realm");
        buffer.append('=');
        buffer.append('\"');
        buffer.append(String.valueOf(this.details.get("realm")));
        buffer.append('\"');
        buffer.append(", ");
        buffer.append("nonce");
        buffer.append('=');
        buffer.append('\"');
        buffer.append(String.valueOf(this.details.get("nonce")));
        buffer.append('\"');
        buffer.append(", ");
        buffer.append("uri");
        buffer.append('=');
        buffer.append('\"');
        buffer.append(exchange.getURI());
        buffer.append('\"');
        buffer.append(", ");
        buffer.append("algorithm");
        buffer.append('=');
        buffer.append(String.valueOf(this.details.get("algorithm")));
        String cnonce = newCnonce(exchange, this.securityRealm, this.details);
        buffer.append(", ");
        buffer.append("response");
        buffer.append('=');
        buffer.append('\"');
        buffer.append(newResponse(cnonce, exchange, this.securityRealm, this.details));
        buffer.append('\"');
        buffer.append(", ");
        buffer.append("qop");
        buffer.append('=');
        buffer.append(String.valueOf(this.details.get("qop")));
        buffer.append(", ");
        buffer.append("nc");
        buffer.append('=');
        buffer.append(NC);
        buffer.append(", ");
        buffer.append("cnonce");
        buffer.append('=');
        buffer.append('\"');
        buffer.append(cnonce);
        buffer.append('\"');
        exchange.setRequestHeader(HttpHeaders.AUTHORIZATION, new String(buffer.toString().getBytes(StringUtil.__ISO_8859_1)));
    }

    protected String newResponse(String cnonce, HttpExchange exchange, Realm securityRealm, Map details) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(securityRealm.getPrincipal().getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(String.valueOf(details.get("realm")).getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(securityRealm.getCredentials().getBytes(StringUtil.__ISO_8859_1));
            byte[] ha1 = md.digest();
            md.reset();
            md.update(exchange.getMethod().getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(exchange.getURI().getBytes(StringUtil.__ISO_8859_1));
            byte[] ha2 = md.digest();
            md.update(TypeUtil.toString(ha1, 16).getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(String.valueOf(details.get("nonce")).getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(NC.getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(cnonce.getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(String.valueOf(details.get("qop")).getBytes(StringUtil.__ISO_8859_1));
            md.update(HttpTokens.COLON);
            md.update(TypeUtil.toString(ha2, 16).getBytes(StringUtil.__ISO_8859_1));
            byte[] digest = md.digest();
            return encode(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String newCnonce(HttpExchange exchange, Realm securityRealm, Map details) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(String.valueOf(System.currentTimeMillis()).getBytes(StringUtil.__ISO_8859_1));
            return encode(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String encode(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            buffer.append(Integer.toHexString((data[i] & 240) >>> 4));
            buffer.append(Integer.toHexString(data[i] & 15));
        }
        return buffer.toString();
    }
}
