package org.eclipse.jetty.client.security;

import java.io.IOException;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class BasicAuthentication implements Authentication {
    private Buffer _authorization;

    public BasicAuthentication(Realm realm) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Basic ");
        sb.append(B64Code.encode(realm.getPrincipal() + ":" + realm.getCredentials(), StringUtil.__ISO_8859_1));
        String authenticationString = sb.toString();
        this._authorization = new ByteArrayBuffer(authenticationString);
    }

    @Override // org.eclipse.jetty.client.security.Authentication
    public void setCredentials(HttpExchange exchange) throws IOException {
        exchange.setRequestHeader(HttpHeaders.AUTHORIZATION_BUFFER, this._authorization);
    }
}
