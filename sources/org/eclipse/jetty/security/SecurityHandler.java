package org.eclipse.jetty.security;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class SecurityHandler extends HandlerWrapper implements Authenticator.AuthConfiguration {
    private String _authMethod;
    private Authenticator _authenticator;
    private IdentityService _identityService;
    private LoginService _loginService;
    private boolean _loginServiceShared;
    private String _realmName;
    private static final Logger LOG = Log.getLogger(SecurityHandler.class);
    public static Principal __NO_USER = new Principal() { // from class: org.eclipse.jetty.security.SecurityHandler.2
        @Override // java.security.Principal
        public String getName() {
            return null;
        }

        @Override // java.security.Principal
        public String toString() {
            return "No User";
        }
    };
    public static Principal __NOBODY = new Principal() { // from class: org.eclipse.jetty.security.SecurityHandler.3
        @Override // java.security.Principal
        public String getName() {
            return "Nobody";
        }

        @Override // java.security.Principal
        public String toString() {
            return getName();
        }
    };
    private boolean _checkWelcomeFiles = false;
    private Authenticator.Factory _authenticatorFactory = new DefaultAuthenticatorFactory();
    private final Map<String, String> _initParameters = new HashMap();
    private boolean _renewSession = true;

    protected abstract boolean checkUserDataPermissions(String str, Request request, Response response, Object obj) throws IOException;

    protected abstract boolean checkWebResourcePermissions(String str, Request request, Response response, Object obj, UserIdentity userIdentity) throws IOException;

    protected abstract boolean isAuthMandatory(Request request, Response response, Object obj);

    protected abstract Object prepareConstraintInfo(String str, Request request);

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public IdentityService getIdentityService() {
        return this._identityService;
    }

    public void setIdentityService(IdentityService identityService) {
        if (isStarted()) {
            throw new IllegalStateException("Started");
        }
        this._identityService = identityService;
    }

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public LoginService getLoginService() {
        return this._loginService;
    }

    public void setLoginService(LoginService loginService) {
        if (isStarted()) {
            throw new IllegalStateException("Started");
        }
        this._loginService = loginService;
        this._loginServiceShared = false;
    }

    public Authenticator getAuthenticator() {
        return this._authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        if (isStarted()) {
            throw new IllegalStateException("Started");
        }
        this._authenticator = authenticator;
    }

    public Authenticator.Factory getAuthenticatorFactory() {
        return this._authenticatorFactory;
    }

    public void setAuthenticatorFactory(Authenticator.Factory authenticatorFactory) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        this._authenticatorFactory = authenticatorFactory;
    }

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public String getRealmName() {
        return this._realmName;
    }

    public void setRealmName(String realmName) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        this._realmName = realmName;
    }

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public String getAuthMethod() {
        return this._authMethod;
    }

    public void setAuthMethod(String authMethod) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        this._authMethod = authMethod;
    }

    public boolean isCheckWelcomeFiles() {
        return this._checkWelcomeFiles;
    }

    public void setCheckWelcomeFiles(boolean authenticateWelcomeFiles) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        this._checkWelcomeFiles = authenticateWelcomeFiles;
    }

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public String getInitParameter(String key) {
        return this._initParameters.get(key);
    }

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public Set<String> getInitParameterNames() {
        return this._initParameters.keySet();
    }

    public String setInitParameter(String key, String value) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        return this._initParameters.put(key, value);
    }

    protected LoginService findLoginService() {
        List<LoginService> list = getServer().getBeans(LoginService.class);
        String realm = getRealmName();
        if (realm != null) {
            for (LoginService service : list) {
                if (service.getName() != null && service.getName().equals(realm)) {
                    return service;
                }
            }
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            return null;
        }
    }

    protected IdentityService findIdentityService() {
        return (IdentityService) getServer().getBean(IdentityService.class);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        ContextHandler.Context context = ContextHandler.getCurrentContext();
        if (context != null) {
            Enumeration<String> names = context.getInitParameterNames();
            while (names != null && names.hasMoreElements()) {
                String name = names.nextElement();
                if (name.startsWith("org.eclipse.jetty.security.") && getInitParameter(name) == null) {
                    setInitParameter(name, context.getInitParameter(name));
                }
            }
            context.getContextHandler().addEventListener(new HttpSessionListener() { // from class: org.eclipse.jetty.security.SecurityHandler.1
                @Override // javax.servlet.http.HttpSessionListener
                public void sessionDestroyed(HttpSessionEvent se) {
                }

                @Override // javax.servlet.http.HttpSessionListener
                public void sessionCreated(HttpSessionEvent se) {
                    Request request;
                    AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
                    if (connection != null && (request = connection.getRequest()) != null && request.isSecure()) {
                        se.getSession().setAttribute(AbstractSessionManager.SESSION_KNOWN_ONLY_TO_AUTHENTICATED, Boolean.TRUE);
                    }
                }
            });
        }
        if (this._loginService == null) {
            this._loginService = findLoginService();
            if (this._loginService != null) {
                this._loginServiceShared = true;
            }
        }
        if (this._identityService == null) {
            if (this._loginService != null) {
                this._identityService = this._loginService.getIdentityService();
            }
            if (this._identityService == null) {
                this._identityService = findIdentityService();
            }
            if (this._identityService == null && this._realmName != null) {
                this._identityService = new DefaultIdentityService();
            }
        }
        if (this._loginService != null) {
            if (this._loginService.getIdentityService() == null) {
                this._loginService.setIdentityService(this._identityService);
            } else if (this._loginService.getIdentityService() != this._identityService) {
                throw new IllegalStateException("LoginService has different IdentityService to " + this);
            }
        }
        if (!this._loginServiceShared && (this._loginService instanceof LifeCycle)) {
            ((LifeCycle) this._loginService).start();
        }
        if (this._authenticator == null && this._authenticatorFactory != null && this._identityService != null) {
            this._authenticator = this._authenticatorFactory.getAuthenticator(getServer(), ContextHandler.getCurrentContext(), this, this._identityService, this._loginService);
            if (this._authenticator != null) {
                this._authMethod = this._authenticator.getAuthMethod();
            }
        }
        if (this._authenticator == null) {
            if (this._realmName != null) {
                Logger logger = LOG;
                logger.warn("No ServerAuthentication for " + this, new Object[0]);
                throw new IllegalStateException("No ServerAuthentication");
            }
        } else {
            this._authenticator.setConfiguration(this);
            if (this._authenticator instanceof LifeCycle) {
                ((LifeCycle) this._authenticator).start();
            }
        }
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        if (!this._loginServiceShared && (this._loginService instanceof LifeCycle)) {
            ((LifeCycle) this._loginService).stop();
        }
    }

    protected boolean checkSecurity(Request request) {
        switch (request.getDispatcherType()) {
            case REQUEST:
            case ASYNC:
                return true;
            case FORWARD:
                if (!this._checkWelcomeFiles || request.getAttribute("org.eclipse.jetty.server.welcome") == null) {
                    return false;
                }
                request.removeAttribute("org.eclipse.jetty.server.welcome");
                return true;
            default:
                return false;
        }
    }

    @Override // org.eclipse.jetty.security.Authenticator.AuthConfiguration
    public boolean isSessionRenewedOnAuthentication() {
        return this._renewSession;
    }

    public void setSessionRenewedOnAuthentication(boolean renew) {
        this._renewSession = renew;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(16:22|(2:23|24)|(3:144|145|(1:147)(12:148|29|(2:31|32)|49|50|51|52|(2:54|55)(2:59|(7:61|(2:63|64)(1:107)|(6:85|86|87|88|89|(3:91|92|(2:94|95)(1:96)))(1:66)|67|68|(3:70|71|72)(1:79)|73)(4:109|110|111|(6:113|114|115|116|(2:118|(1:120)(1:121))|122)(4:127|(1:129)|130|(1:132))))|56|(1:58)|40|41))|(1:27)(1:143)|28|29|(0)|49|50|51|52|(0)(0)|56|(0)|40|41) */
    /* JADX WARN: Code restructure failed: missing block: B:116:0x01dd, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:118:0x01ea, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:119:0x01eb, code lost:
        r14 = r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x00bd, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x00cb, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x00cc, code lost:
        r14 = r2;
     */
    /* JADX WARN: Removed duplicated region for block: B:133:0x021e  */
    /* JADX WARN: Removed duplicated region for block: B:158:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:160:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:38:0x0098 A[Catch: all -> 0x0074, ServerAuthException -> 0x007f, TRY_ENTER, TRY_LEAVE, TryCatch #12 {ServerAuthException -> 0x007f, all -> 0x0074, blocks: (B:23:0x006d, B:38:0x0098, B:32:0x008c), top: B:153:0x006d }] */
    /* JADX WARN: Removed duplicated region for block: B:43:0x00ad A[Catch: all -> 0x00bd, ServerAuthException -> 0x00cb, TRY_ENTER, TRY_LEAVE, TryCatch #16 {ServerAuthException -> 0x00cb, all -> 0x00bd, blocks: (B:43:0x00ad, B:53:0x00e7), top: B:147:0x00ab }] */
    /* JADX WARN: Removed duplicated region for block: B:49:0x00d9 A[Catch: all -> 0x01dd, ServerAuthException -> 0x01ea, TRY_ENTER, TryCatch #17 {ServerAuthException -> 0x01ea, all -> 0x01dd, blocks: (B:41:0x00a9, B:49:0x00d9, B:51:0x00dd), top: B:145:0x00a9 }] */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void handle(java.lang.String r22, org.eclipse.jetty.server.Request r23, javax.servlet.http.HttpServletRequest r24, javax.servlet.http.HttpServletResponse r25) throws java.io.IOException, javax.servlet.ServletException {
        /*
            Method dump skipped, instructions count: 557
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.security.SecurityHandler.handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse):void");
    }

    public static SecurityHandler getCurrentSecurityHandler() {
        ContextHandler.Context context = ContextHandler.getCurrentContext();
        if (context == null) {
            return null;
        }
        SecurityHandler security = (SecurityHandler) context.getContextHandler().getChildHandlerByClass(SecurityHandler.class);
        return security;
    }

    public void logout(Authentication.User user) {
        LOG.debug("logout {}", user);
        LoginService login_service = getLoginService();
        if (login_service != null) {
            login_service.logout(user.getUserIdentity());
        }
        IdentityService identity_service = getIdentityService();
        if (identity_service != null) {
            identity_service.disassociate(null);
        }
    }

    /* loaded from: classes.dex */
    public class NotChecked implements Principal {
        public NotChecked() {
        }

        @Override // java.security.Principal
        public String getName() {
            return null;
        }

        @Override // java.security.Principal
        public String toString() {
            return "NOT CHECKED";
        }

        public SecurityHandler getSecurityHandler() {
            return SecurityHandler.this;
        }
    }
}
