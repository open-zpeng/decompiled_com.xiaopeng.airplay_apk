package org.eclipse.jetty.server.session;

import java.io.IOException;
import java.util.EnumSet;
import java.util.EventListener;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ScopedHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class SessionHandler extends ScopedHandler {
    private SessionManager _sessionManager;
    static final Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");
    public static final EnumSet<SessionTrackingMode> DEFAULT_TRACKING = EnumSet.of(SessionTrackingMode.COOKIE, SessionTrackingMode.URL);

    public SessionHandler() {
        this(new HashSessionManager());
    }

    public SessionHandler(SessionManager manager) {
        setSessionManager(manager);
    }

    public SessionManager getSessionManager() {
        return this._sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        if (isStarted()) {
            throw new IllegalStateException();
        }
        SessionManager old_session_manager = this._sessionManager;
        if (getServer() != null) {
            getServer().getContainer().update((Object) this, (Object) old_session_manager, (Object) sessionManager, "sessionManager", true);
        }
        if (sessionManager != null) {
            sessionManager.setSessionHandler(this);
        }
        this._sessionManager = sessionManager;
        if (old_session_manager != null) {
            old_session_manager.setSessionHandler(null);
        }
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.server.Handler
    public void setServer(Server server) {
        Server old_server = getServer();
        if (old_server != null && old_server != server) {
            old_server.getContainer().update((Object) this, (Object) this._sessionManager, (Object) null, "sessionManager", true);
        }
        super.setServer(server);
        if (server != null && server != old_server) {
            server.getContainer().update((Object) this, (Object) null, (Object) this._sessionManager, "sessionManager", true);
        }
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler, org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    protected void doStart() throws Exception {
        this._sessionManager.start();
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._sessionManager.stop();
        super.doStop();
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        SessionManager old_session_manager = null;
        HttpSession old_session = null;
        HttpSession access = null;
        try {
            old_session_manager = baseRequest.getSessionManager();
            old_session = baseRequest.getSession(false);
            if (old_session_manager != this._sessionManager) {
                baseRequest.setSessionManager(this._sessionManager);
                baseRequest.setSession(null);
                checkRequestedSessionId(baseRequest, request);
            }
            HttpSession session = null;
            if (this._sessionManager != null) {
                session = baseRequest.getSession(false);
                if (session != null) {
                    if (session != old_session) {
                        access = session;
                        HttpCookie cookie = this._sessionManager.access(session, request.isSecure());
                        if (cookie != null) {
                            baseRequest.getResponse().addCookie(cookie);
                        }
                    }
                } else {
                    session = baseRequest.recoverNewSession(this._sessionManager);
                    if (session != null) {
                        baseRequest.setSession(session);
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("sessionManager=" + this._sessionManager, new Object[0]);
                LOG.debug("session=" + session, new Object[0]);
            }
            if (this._nextScope != null) {
                this._nextScope.doScope(target, baseRequest, request, response);
            } else if (this._outerScope != null) {
                this._outerScope.doHandle(target, baseRequest, request, response);
            } else {
                doHandle(target, baseRequest, request, response);
            }
        } finally {
            if (0 != 0) {
                this._sessionManager.complete(null);
            }
            HttpSession session2 = baseRequest.getSession(false);
            if (session2 != null && old_session == null && session2 != null) {
                this._sessionManager.complete(session2);
            }
            if (old_session_manager != null && old_session_manager != this._sessionManager) {
                baseRequest.setSessionManager(old_session_manager);
                baseRequest.setSession(old_session);
            }
        }
    }

    @Override // org.eclipse.jetty.server.handler.ScopedHandler
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (never()) {
            nextHandle(target, baseRequest, request, response);
        } else if (this._nextScope != null && this._nextScope == this._handler) {
            this._nextScope.doHandle(target, baseRequest, request, response);
        } else if (this._handler != null) {
            this._handler.handle(target, baseRequest, request, response);
        }
    }

    protected void checkRequestedSessionId(Request baseRequest, HttpServletRequest request) {
        HttpSession session;
        boolean requested_session_id_from_cookie;
        String requested_session_id;
        int s;
        char c;
        Cookie[] cookies;
        String requested_session_id2 = request.getRequestedSessionId();
        SessionManager sessionManager = getSessionManager();
        if (requested_session_id2 != null && sessionManager != null) {
            HttpSession session2 = sessionManager.getHttpSession(requested_session_id2);
            if (session2 != null && sessionManager.isValid(session2)) {
                baseRequest.setSession(session2);
            }
        } else if (DispatcherType.REQUEST.equals(baseRequest.getDispatcherType())) {
            boolean z = true;
            if (this._sessionManager.isUsingCookies() && (cookies = request.getCookies()) != null && cookies.length > 0) {
                String sessionCookie = sessionManager.getSessionCookieConfig().getName();
                session = null;
                requested_session_id_from_cookie = false;
                requested_session_id = requested_session_id2;
                for (int i = 0; i < cookies.length; i++) {
                    if (sessionCookie.equalsIgnoreCase(cookies[i].getName())) {
                        requested_session_id = cookies[i].getValue();
                        requested_session_id_from_cookie = true;
                        LOG.debug("Got Session ID {} from cookie", requested_session_id);
                        if (requested_session_id != null) {
                            session = sessionManager.getHttpSession(requested_session_id);
                            if (session != null && sessionManager.isValid(session)) {
                                break;
                            }
                        } else {
                            LOG.warn("null session id from cookie", new Object[0]);
                        }
                    }
                }
            } else {
                session = null;
                requested_session_id_from_cookie = false;
                requested_session_id = requested_session_id2;
            }
            if (requested_session_id == null || session == null) {
                String uri = request.getRequestURI();
                String prefix = sessionManager.getSessionIdPathParameterNamePrefix();
                if (prefix != null && (s = uri.indexOf(prefix)) >= 0) {
                    int s2 = s + prefix.length();
                    int i2 = s2;
                    while (i2 < uri.length() && (c = uri.charAt(i2)) != ';' && c != '#' && c != '?' && c != '/') {
                        i2++;
                    }
                    requested_session_id = uri.substring(s2, i2);
                    requested_session_id_from_cookie = false;
                    session = sessionManager.getHttpSession(requested_session_id);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Got Session ID {} from URL", requested_session_id);
                    }
                }
            }
            baseRequest.setRequestedSessionId(requested_session_id);
            baseRequest.setRequestedSessionIdFromCookie((requested_session_id == null || !requested_session_id_from_cookie) ? false : false);
            if (session != null && sessionManager.isValid(session)) {
                baseRequest.setSession(session);
            }
        }
    }

    public void addEventListener(EventListener listener) {
        if (this._sessionManager != null) {
            this._sessionManager.addEventListener(listener);
        }
    }

    public void clearEventListeners() {
        if (this._sessionManager != null) {
            this._sessionManager.clearEventListeners();
        }
    }
}
