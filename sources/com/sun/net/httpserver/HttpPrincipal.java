package com.sun.net.httpserver;

import java.security.Principal;
/* loaded from: classes.dex */
public class HttpPrincipal implements Principal {
    private String realm;
    private String username;

    public HttpPrincipal(String str, String str2) {
        if (str == null || str2 == null) {
            throw new NullPointerException();
        }
        this.username = str;
        this.realm = str2;
    }

    @Override // java.security.Principal
    public boolean equals(Object obj) {
        if (obj instanceof HttpPrincipal) {
            HttpPrincipal httpPrincipal = (HttpPrincipal) obj;
            return this.username.equals(httpPrincipal.username) && this.realm.equals(httpPrincipal.realm);
        }
        return false;
    }

    @Override // java.security.Principal
    public String getName() {
        return this.username;
    }

    public String getUsername() {
        return this.username;
    }

    public String getRealm() {
        return this.realm;
    }

    @Override // java.security.Principal
    public int hashCode() {
        return (this.username + this.realm).hashCode();
    }

    @Override // java.security.Principal
    public String toString() {
        return getName();
    }
}
