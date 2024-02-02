package org.eclipse.jetty.security;

import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
/* loaded from: classes.dex */
public class UserAuthentication implements Authentication.User {
    private final String _method;
    private final UserIdentity _userIdentity;

    public UserAuthentication(String method, UserIdentity userIdentity) {
        this._method = method;
        this._userIdentity = userIdentity;
    }

    @Override // org.eclipse.jetty.server.Authentication.User
    public String getAuthMethod() {
        return this._method;
    }

    @Override // org.eclipse.jetty.server.Authentication.User
    public UserIdentity getUserIdentity() {
        return this._userIdentity;
    }

    @Override // org.eclipse.jetty.server.Authentication.User
    public boolean isUserInRole(UserIdentity.Scope scope, String role) {
        return this._userIdentity.isUserInRole(role, scope);
    }

    public String toString() {
        return "{User," + getAuthMethod() + "," + this._userIdentity + "}";
    }

    @Override // org.eclipse.jetty.server.Authentication.User
    public void logout() {
        SecurityHandler security = SecurityHandler.getCurrentSecurityHandler();
        if (security != null) {
            security.logout(this);
        }
    }
}
