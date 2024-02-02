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
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class BasicAuthenticator extends LoginAuthenticator {
    @Override // org.eclipse.jetty.security.Authenticator
    public String getAuthMethod() {
        return "BASIC";
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
        int space;
        String credentials;
        int i;
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String credentials2 = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (!mandatory) {
                return new DeferredAuthentication(this);
            }
            if (credentials2 != null && (space = credentials2.indexOf(32)) > 0) {
                String method = credentials2.substring(0, space);
                if ("basic".equalsIgnoreCase(method) && (i = (credentials = B64Code.decode(credentials2.substring(space + 1), StringUtil.__ISO_8859_1)).indexOf(58)) > 0) {
                    String username = credentials.substring(0, i);
                    String password = credentials.substring(i + 1);
                    UserIdentity user = login(username, password, request);
                    if (user != null) {
                        return new UserAuthentication(getAuthMethod(), user);
                    }
                }
            }
            if (DeferredAuthentication.isDeferred(response)) {
                return Authentication.UNAUTHENTICATED;
            }
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "basic realm=\"" + this._loginService.getName() + '\"');
            response.sendError(401);
            return Authentication.SEND_CONTINUE;
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
        return true;
    }
}
