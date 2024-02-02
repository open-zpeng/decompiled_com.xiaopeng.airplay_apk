package org.eclipse.jetty.security.authentication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class SessionAuthentication implements Authentication.User, Serializable, HttpSessionActivationListener, HttpSessionBindingListener {
    private static final Logger LOG = Log.getLogger(SessionAuthentication.class);
    public static final String __J_AUTHENTICATED = "org.eclipse.jetty.security.UserIdentity";
    private static final long serialVersionUID = -4643200685888258706L;
    private final Object _credentials;
    private final String _method;
    private final String _name;
    private transient HttpSession _session;
    private transient UserIdentity _userIdentity;

    public SessionAuthentication(String method, UserIdentity userIdentity, Object credentials) {
        this._method = method;
        this._userIdentity = userIdentity;
        this._name = this._userIdentity.getUserPrincipal().getName();
        this._credentials = credentials;
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

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        SecurityHandler security = SecurityHandler.getCurrentSecurityHandler();
        if (security == null) {
            throw new IllegalStateException("!SecurityHandler");
        }
        LoginService login_service = security.getLoginService();
        if (login_service == null) {
            throw new IllegalStateException("!LoginService");
        }
        this._userIdentity = login_service.login(this._name, this._credentials);
        LOG.debug("Deserialized and relogged in {}", this);
    }

    @Override // org.eclipse.jetty.server.Authentication.User
    public void logout() {
        if (this._session != null && this._session.getAttribute(__J_AUTHENTICATED) != null) {
            this._session.removeAttribute(__J_AUTHENTICATED);
        }
        doLogout();
    }

    private void doLogout() {
        SecurityHandler security = SecurityHandler.getCurrentSecurityHandler();
        if (security != null) {
            security.logout(this);
        }
        if (this._session != null) {
            this._session.removeAttribute(AbstractSessionManager.SESSION_KNOWN_ONLY_TO_AUTHENTICATED);
        }
    }

    public String toString() {
        return "Session" + super.toString();
    }

    @Override // javax.servlet.http.HttpSessionActivationListener
    public void sessionWillPassivate(HttpSessionEvent se) {
    }

    @Override // javax.servlet.http.HttpSessionActivationListener
    public void sessionDidActivate(HttpSessionEvent se) {
        if (this._session == null) {
            this._session = se.getSession();
        }
    }

    @Override // javax.servlet.http.HttpSessionBindingListener
    public void valueBound(HttpSessionBindingEvent event) {
        if (this._session == null) {
            this._session = event.getSession();
        }
    }

    @Override // javax.servlet.http.HttpSessionBindingListener
    public void valueUnbound(HttpSessionBindingEvent event) {
        doLogout();
    }
}
