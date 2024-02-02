package org.eclipse.jetty.security;

import javax.servlet.ServletContext;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.ClientCertAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.security.authentication.SpnegoAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;
/* loaded from: classes.dex */
public class DefaultAuthenticatorFactory implements Authenticator.Factory {
    LoginService _loginService;

    @Override // org.eclipse.jetty.security.Authenticator.Factory
    public Authenticator getAuthenticator(Server server, ServletContext context, Authenticator.AuthConfiguration configuration, IdentityService identityService, LoginService loginService) {
        String auth = configuration.getAuthMethod();
        Authenticator authenticator = null;
        if (auth == null || "BASIC".equalsIgnoreCase(auth)) {
            authenticator = new BasicAuthenticator();
        } else if ("DIGEST".equalsIgnoreCase(auth)) {
            authenticator = new DigestAuthenticator();
        } else if ("FORM".equalsIgnoreCase(auth)) {
            authenticator = new FormAuthenticator();
        } else if (Constraint.__SPNEGO_AUTH.equalsIgnoreCase(auth)) {
            authenticator = new SpnegoAuthenticator();
        } else if (Constraint.__NEGOTIATE_AUTH.equalsIgnoreCase(auth)) {
            authenticator = new SpnegoAuthenticator(Constraint.__NEGOTIATE_AUTH);
        }
        if ("CLIENT_CERT".equalsIgnoreCase(auth) || Constraint.__CERT_AUTH2.equalsIgnoreCase(auth)) {
            Authenticator authenticator2 = new ClientCertAuthenticator();
            return authenticator2;
        }
        return authenticator;
    }

    public LoginService getLoginService() {
        return this._loginService;
    }

    public void setLoginService(LoginService loginService) {
        this._loginService = loginService;
    }
}
