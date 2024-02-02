package org.eclipse.jetty.security.authentication;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.session.AbstractSessionManager;
/* loaded from: classes.dex */
public abstract class LoginAuthenticator implements Authenticator {
    protected IdentityService _identityService;
    protected LoginService _loginService;
    private boolean _renewSession;

    public UserIdentity login(String username, Object password, ServletRequest request) {
        UserIdentity user = this._loginService.login(username, password);
        if (user != null) {
            renewSession((HttpServletRequest) request, null);
            return user;
        }
        return null;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public void setConfiguration(Authenticator.AuthConfiguration configuration) {
        this._loginService = configuration.getLoginService();
        if (this._loginService == null) {
            throw new IllegalStateException("No LoginService for " + this + " in " + configuration);
        }
        this._identityService = configuration.getIdentityService();
        if (this._identityService == null) {
            throw new IllegalStateException("No IdentityService for " + this + " in " + configuration);
        }
        this._renewSession = configuration.isSessionRenewedOnAuthentication();
    }

    public LoginService getLoginService() {
        return this._loginService;
    }

    protected HttpSession renewSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession(false);
        if (this._renewSession && httpSession != null && httpSession.getAttribute(AbstractSessionManager.SESSION_KNOWN_ONLY_TO_AUTHENTICATED) != Boolean.TRUE) {
            synchronized (this) {
                httpSession = AbstractSessionManager.renewSession(request, httpSession, true);
            }
        }
        return httpSession;
    }
}
