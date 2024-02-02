package org.eclipse.jetty.security.authentication;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Constraint;
/* loaded from: classes.dex */
public class SpnegoAuthenticator extends LoginAuthenticator {
    private static final Logger LOG = Log.getLogger(SpnegoAuthenticator.class);
    private String _authMethod;

    public SpnegoAuthenticator() {
        this._authMethod = Constraint.__SPNEGO_AUTH;
    }

    public SpnegoAuthenticator(String authMethod) {
        this._authMethod = Constraint.__SPNEGO_AUTH;
        this._authMethod = authMethod;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public String getAuthMethod() {
        return this._authMethod;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (!mandatory) {
            return new DeferredAuthentication(this);
        }
        if (header == null) {
            try {
                if (DeferredAuthentication.isDeferred(res)) {
                    return Authentication.UNAUTHENTICATED;
                }
                LOG.debug("SpengoAuthenticator: sending challenge", new Object[0]);
                res.setHeader(HttpHeaders.WWW_AUTHENTICATE, HttpHeaders.NEGOTIATE);
                res.sendError(401);
                return Authentication.SEND_CONTINUE;
            } catch (IOException ioe) {
                throw new ServerAuthException(ioe);
            }
        }
        if (header != null && header.startsWith(HttpHeaders.NEGOTIATE)) {
            String spnegoToken = header.substring(10);
            UserIdentity user = login(null, spnegoToken, request);
            if (user != null) {
                return new UserAuthentication(getAuthMethod(), user);
            }
        }
        return Authentication.UNAUTHENTICATED;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
        return true;
    }
}
