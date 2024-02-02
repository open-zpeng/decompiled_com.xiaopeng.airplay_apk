package org.eclipse.jetty.security;

import java.security.Principal;
import javax.security.auth.Subject;
import org.eclipse.jetty.server.UserIdentity;
/* loaded from: classes.dex */
public class DefaultUserIdentity implements UserIdentity {
    private final String[] _roles;
    private final Subject _subject;
    private final Principal _userPrincipal;

    public DefaultUserIdentity(Subject subject, Principal userPrincipal, String[] roles) {
        this._subject = subject;
        this._userPrincipal = userPrincipal;
        this._roles = roles;
    }

    @Override // org.eclipse.jetty.server.UserIdentity
    public Subject getSubject() {
        return this._subject;
    }

    @Override // org.eclipse.jetty.server.UserIdentity
    public Principal getUserPrincipal() {
        return this._userPrincipal;
    }

    @Override // org.eclipse.jetty.server.UserIdentity
    public boolean isUserInRole(String role, UserIdentity.Scope scope) {
        if (scope != null && scope.getRoleRefMap() != null) {
            role = scope.getRoleRefMap().get(role);
        }
        String[] arr$ = this._roles;
        for (String r : arr$) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return DefaultUserIdentity.class.getSimpleName() + "('" + this._userPrincipal + "')";
    }
}
