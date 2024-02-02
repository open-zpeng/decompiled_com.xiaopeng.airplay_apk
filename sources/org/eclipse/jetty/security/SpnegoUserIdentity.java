package org.eclipse.jetty.security;

import java.security.Principal;
import java.util.List;
import javax.security.auth.Subject;
import org.eclipse.jetty.server.UserIdentity;
/* loaded from: classes.dex */
public class SpnegoUserIdentity implements UserIdentity {
    private Principal _principal;
    private List<String> _roles;
    private Subject _subject;

    public SpnegoUserIdentity(Subject subject, Principal principal, List<String> roles) {
        this._subject = subject;
        this._principal = principal;
        this._roles = roles;
    }

    @Override // org.eclipse.jetty.server.UserIdentity
    public Subject getSubject() {
        return this._subject;
    }

    @Override // org.eclipse.jetty.server.UserIdentity
    public Principal getUserPrincipal() {
        return this._principal;
    }

    @Override // org.eclipse.jetty.server.UserIdentity
    public boolean isUserInRole(String role, UserIdentity.Scope scope) {
        return this._roles.contains(role);
    }
}
