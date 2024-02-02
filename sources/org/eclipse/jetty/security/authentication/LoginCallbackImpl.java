package org.eclipse.jetty.security.authentication;

import java.security.Principal;
import javax.security.auth.Subject;
import org.eclipse.jetty.security.IdentityService;
/* loaded from: classes.dex */
public class LoginCallbackImpl implements LoginCallback {
    private Object credential;
    private String[] roles = IdentityService.NO_ROLES;
    private final Subject subject;
    private boolean success;
    private final String userName;
    private Principal userPrincipal;

    public LoginCallbackImpl(Subject subject, String userName, Object credential) {
        this.subject = subject;
        this.userName = userName;
        this.credential = credential;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public Subject getSubject() {
        return this.subject;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public String getUserName() {
        return this.userName;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public Object getCredential() {
        return this.credential;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public boolean isSuccess() {
        return this.success;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public String[] getRoles() {
        return this.roles;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public void setRoles(String[] groups) {
        this.roles = groups;
    }

    @Override // org.eclipse.jetty.security.authentication.LoginCallback
    public void clearPassword() {
        if (this.credential != null) {
            this.credential = null;
        }
    }
}
