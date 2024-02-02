package com.sun.net.httpserver;

import com.sun.net.httpserver.Authenticator;
import org.eclipse.jetty.http.HttpHeaders;
/* loaded from: classes.dex */
public abstract class BasicAuthenticator extends Authenticator {
    protected String realm;

    public abstract boolean checkCredentials(String str, String str2);

    public BasicAuthenticator(String str) {
        this.realm = str;
    }

    public String getRealm() {
        return this.realm;
    }

    @Override // com.sun.net.httpserver.Authenticator
    public Authenticator.Result authenticate(HttpExchange httpExchange) {
        httpExchange.getHttpContext();
        String first = httpExchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (first == null) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + this.realm + "\"");
            return new Authenticator.Retry(401);
        }
        int indexOf = first.indexOf(32);
        if (indexOf == -1 || !first.substring(0, indexOf).equals("Basic")) {
            return new Authenticator.Failure(401);
        }
        String str = new String(Base64.base64ToByteArray(first.substring(indexOf + 1)));
        int indexOf2 = str.indexOf(58);
        String substring = str.substring(0, indexOf2);
        if (checkCredentials(substring, str.substring(indexOf2 + 1))) {
            return new Authenticator.Success(new HttpPrincipal(substring, this.realm));
        }
        Headers responseHeaders2 = httpExchange.getResponseHeaders();
        responseHeaders2.set(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + this.realm + "\"");
        return new Authenticator.Failure(401);
    }
}
