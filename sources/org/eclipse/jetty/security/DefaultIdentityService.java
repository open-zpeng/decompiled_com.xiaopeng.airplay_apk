package org.eclipse.jetty.security;

import java.security.Principal;
import javax.security.auth.Subject;
import org.eclipse.jetty.server.UserIdentity;
/* loaded from: classes.dex */
public class DefaultIdentityService implements IdentityService {
    @Override // org.eclipse.jetty.security.IdentityService
    public Object associate(UserIdentity user) {
        return null;
    }

    @Override // org.eclipse.jetty.security.IdentityService
    public void disassociate(Object previous) {
    }

    @Override // org.eclipse.jetty.security.IdentityService
    public Object setRunAs(UserIdentity user, RunAsToken token) {
        return token;
    }

    @Override // org.eclipse.jetty.security.IdentityService
    public void unsetRunAs(Object lastToken) {
    }

    @Override // org.eclipse.jetty.security.IdentityService
    public RunAsToken newRunAsToken(String runAsName) {
        return new RoleRunAsToken(runAsName);
    }

    @Override // org.eclipse.jetty.security.IdentityService
    public UserIdentity getSystemUserIdentity() {
        return null;
    }

    @Override // org.eclipse.jetty.security.IdentityService
    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles) {
        return new DefaultUserIdentity(subject, userPrincipal, roles);
    }
}
