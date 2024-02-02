package org.eclipse.jetty.server.session;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.util.statistic.SampleStatistic;
/* loaded from: classes.dex */
public abstract class AbstractSessionManager extends AbstractLifeCycle implements SessionManager {
    public static final String SESSION_KNOWN_ONLY_TO_AUTHENTICATED = "org.eclipse.jetty.security.sessionKnownOnlytoAuthenticated";
    public static final int __distantFuture = 628992000;
    static final Logger __log = SessionHandler.LOG;
    static final HttpSessionContext __nullSessionContext = new HttpSessionContext() { // from class: org.eclipse.jetty.server.session.AbstractSessionManager.1
        @Override // javax.servlet.http.HttpSessionContext
        public HttpSession getSession(String sessionId) {
            return null;
        }

        @Override // javax.servlet.http.HttpSessionContext
        public Enumeration getIds() {
            return Collections.enumeration(Collections.EMPTY_LIST);
        }
    };
    protected boolean _checkingRemoteSessionIdEncoding;
    protected ContextHandler.Context _context;
    protected ClassLoader _loader;
    protected boolean _nodeIdInSessionId;
    protected int _refreshCookieAge;
    protected String _sessionComment;
    protected String _sessionDomain;
    protected SessionHandler _sessionHandler;
    protected SessionIdManager _sessionIdManager;
    protected String _sessionPath;
    public Set<SessionTrackingMode> _sessionTrackingModes;
    private boolean _usingURLs;
    public Set<SessionTrackingMode> __defaultSessionTrackingModes = Collections.unmodifiableSet(new HashSet(Arrays.asList(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)));
    private boolean _usingCookies = true;
    protected int _dftMaxIdleSecs = -1;
    protected boolean _httpOnly = false;
    protected boolean _secureCookies = false;
    protected boolean _secureRequestOnly = true;
    protected final List<HttpSessionAttributeListener> _sessionAttributeListeners = new CopyOnWriteArrayList();
    protected final List<HttpSessionListener> _sessionListeners = new CopyOnWriteArrayList();
    protected String _sessionCookie = SessionManager.__DefaultSessionCookie;
    protected String _sessionIdPathParameterName = SessionManager.__DefaultSessionIdPathParameterName;
    protected String _sessionIdPathParameterNamePrefix = ";" + this._sessionIdPathParameterName + "=";
    protected int _maxCookieAge = -1;
    protected final CounterStatistic _sessionsStats = new CounterStatistic();
    protected final SampleStatistic _sessionTimeStats = new SampleStatistic();
    private SessionCookieConfig _cookieConfig = new SessionCookieConfig() { // from class: org.eclipse.jetty.server.session.AbstractSessionManager.2
        @Override // javax.servlet.SessionCookieConfig
        public String getComment() {
            return AbstractSessionManager.this._sessionComment;
        }

        @Override // javax.servlet.SessionCookieConfig
        public String getDomain() {
            return AbstractSessionManager.this._sessionDomain;
        }

        @Override // javax.servlet.SessionCookieConfig
        public int getMaxAge() {
            return AbstractSessionManager.this._maxCookieAge;
        }

        @Override // javax.servlet.SessionCookieConfig
        public String getName() {
            return AbstractSessionManager.this._sessionCookie;
        }

        @Override // javax.servlet.SessionCookieConfig
        public String getPath() {
            return AbstractSessionManager.this._sessionPath;
        }

        @Override // javax.servlet.SessionCookieConfig
        public boolean isHttpOnly() {
            return AbstractSessionManager.this._httpOnly;
        }

        @Override // javax.servlet.SessionCookieConfig
        public boolean isSecure() {
            return AbstractSessionManager.this._secureCookies;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setComment(String comment) {
            AbstractSessionManager.this._sessionComment = comment;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setDomain(String domain) {
            AbstractSessionManager.this._sessionDomain = domain;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setHttpOnly(boolean httpOnly) {
            AbstractSessionManager.this._httpOnly = httpOnly;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setMaxAge(int maxAge) {
            AbstractSessionManager.this._maxCookieAge = maxAge;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setName(String name) {
            AbstractSessionManager.this._sessionCookie = name;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setPath(String path) {
            AbstractSessionManager.this._sessionPath = path;
        }

        @Override // javax.servlet.SessionCookieConfig
        public void setSecure(boolean secure) {
            AbstractSessionManager.this._secureCookies = secure;
        }
    };

    /* loaded from: classes.dex */
    public interface SessionIf extends HttpSession {
        AbstractSession getSession();
    }

    protected abstract void addSession(AbstractSession abstractSession);

    public abstract AbstractSession getSession(String str);

    protected abstract void invalidateSessions() throws Exception;

    protected abstract AbstractSession newSession(HttpServletRequest httpServletRequest);

    protected abstract boolean removeSession(String str);

    public static HttpSession renewSession(HttpServletRequest request, HttpSession httpSession, boolean authenticated) {
        Map<String, Object> attributes = new HashMap<>();
        Enumeration<String> e = httpSession.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            attributes.put(name, httpSession.getAttribute(name));
            httpSession.removeAttribute(name);
        }
        httpSession.invalidate();
        HttpSession httpSession2 = request.getSession(true);
        if (authenticated) {
            httpSession2.setAttribute(SESSION_KNOWN_ONLY_TO_AUTHENTICATED, Boolean.TRUE);
        }
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            httpSession2.setAttribute(entry.getKey(), entry.getValue());
        }
        return httpSession2;
    }

    public AbstractSessionManager() {
        setSessionTrackingModes(this.__defaultSessionTrackingModes);
    }

    public ContextHandler.Context getContext() {
        return this._context;
    }

    public ContextHandler getContextHandler() {
        return this._context.getContextHandler();
    }

    public String getSessionPath() {
        return this._sessionPath;
    }

    public int getMaxCookieAge() {
        return this._maxCookieAge;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public HttpCookie access(HttpSession session, boolean secure) {
        long now = System.currentTimeMillis();
        AbstractSession s = ((SessionIf) session).getSession();
        if (s.access(now) && isUsingCookies()) {
            if (s.isIdChanged() || (getSessionCookieConfig().getMaxAge() > 0 && getRefreshCookieAge() > 0 && (now - s.getCookieSetTime()) / 1000 > getRefreshCookieAge())) {
                HttpCookie cookie = getSessionCookie(session, this._context == null ? "/" : this._context.getContextPath(), secure);
                s.cookieSet();
                s.setIdChanged(false);
                return cookie;
            }
            return null;
        }
        return null;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void addEventListener(EventListener listener) {
        if (listener instanceof HttpSessionAttributeListener) {
            this._sessionAttributeListeners.add((HttpSessionAttributeListener) listener);
        }
        if (listener instanceof HttpSessionListener) {
            this._sessionListeners.add((HttpSessionListener) listener);
        }
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void clearEventListeners() {
        this._sessionAttributeListeners.clear();
        this._sessionListeners.clear();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void complete(HttpSession session) {
        AbstractSession s = ((SessionIf) session).getSession();
        s.complete();
    }

    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        String tmp;
        this._context = ContextHandler.getCurrentContext();
        this._loader = Thread.currentThread().getContextClassLoader();
        if (this._sessionIdManager == null) {
            Server server = getSessionHandler().getServer();
            synchronized (server) {
                this._sessionIdManager = server.getSessionIdManager();
                if (this._sessionIdManager == null) {
                    this._sessionIdManager = new HashSessionIdManager();
                    server.setSessionIdManager(this._sessionIdManager);
                }
            }
        }
        if (!this._sessionIdManager.isStarted()) {
            this._sessionIdManager.start();
        }
        if (this._context != null) {
            String tmp2 = this._context.getInitParameter(SessionManager.__SessionCookieProperty);
            if (tmp2 != null) {
                this._sessionCookie = tmp2;
            }
            String tmp3 = this._context.getInitParameter(SessionManager.__SessionIdPathParameterNameProperty);
            if (tmp3 != null) {
                setSessionIdPathParameterName(tmp3);
            }
            if (this._maxCookieAge == -1 && (tmp = this._context.getInitParameter(SessionManager.__MaxAgeProperty)) != null) {
                this._maxCookieAge = Integer.parseInt(tmp.trim());
            }
            if (this._sessionDomain == null) {
                this._sessionDomain = this._context.getInitParameter(SessionManager.__SessionDomainProperty);
            }
            if (this._sessionPath == null) {
                this._sessionPath = this._context.getInitParameter(SessionManager.__SessionPathProperty);
            }
            String tmp4 = this._context.getInitParameter(SessionManager.__CheckRemoteSessionEncoding);
            if (tmp4 != null) {
                this._checkingRemoteSessionIdEncoding = Boolean.parseBoolean(tmp4);
            }
        }
        super.doStart();
    }

    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        invalidateSessions();
        this._loader = null;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public boolean getHttpOnly() {
        return this._httpOnly;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public HttpSession getHttpSession(String nodeId) {
        String cluster_id = getSessionIdManager().getClusterId(nodeId);
        AbstractSession session = getSession(cluster_id);
        if (session != null && !session.getNodeId().equals(nodeId)) {
            session.setIdChanged(true);
        }
        return session;
    }

    public SessionIdManager getIdManager() {
        return getSessionIdManager();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public SessionIdManager getSessionIdManager() {
        return this._sessionIdManager;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public int getMaxInactiveInterval() {
        return this._dftMaxIdleSecs;
    }

    @Deprecated
    public int getMaxSessions() {
        return getSessionsMax();
    }

    public int getSessionsMax() {
        return (int) this._sessionsStats.getMax();
    }

    public int getSessionsTotal() {
        return (int) this._sessionsStats.getTotal();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    @Deprecated
    public SessionIdManager getMetaManager() {
        return getSessionIdManager();
    }

    @Deprecated
    public int getMinSessions() {
        return 0;
    }

    public int getRefreshCookieAge() {
        return this._refreshCookieAge;
    }

    public boolean getSecureCookies() {
        return this._secureCookies;
    }

    public boolean isSecureRequestOnly() {
        return this._secureRequestOnly;
    }

    public void setSecureRequestOnly(boolean secureRequestOnly) {
        this._secureRequestOnly = secureRequestOnly;
    }

    public String getSessionCookie() {
        return this._sessionCookie;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public HttpCookie getSessionCookie(HttpSession session, String contextPath, boolean requestIsSecure) {
        if (isUsingCookies()) {
            String sessionPath = this._sessionPath == null ? contextPath : this._sessionPath;
            String sessionPath2 = (sessionPath == null || sessionPath.length() == 0) ? "/" : sessionPath;
            String id = getNodeId(session);
            if (this._sessionComment == null) {
                HttpCookie cookie = new HttpCookie(this._sessionCookie, id, this._sessionDomain, sessionPath2, this._cookieConfig.getMaxAge(), this._cookieConfig.isHttpOnly(), this._cookieConfig.isSecure() || (isSecureRequestOnly() && requestIsSecure));
                return cookie;
            }
            HttpCookie cookie2 = new HttpCookie(this._sessionCookie, id, this._sessionDomain, sessionPath2, this._cookieConfig.getMaxAge(), this._cookieConfig.isHttpOnly(), this._cookieConfig.isSecure() || (isSecureRequestOnly() && requestIsSecure), this._sessionComment, 1);
            return cookie2;
        }
        return null;
    }

    public String getSessionDomain() {
        return this._sessionDomain;
    }

    public SessionHandler getSessionHandler() {
        return this._sessionHandler;
    }

    public Map getSessionMap() {
        throw new UnsupportedOperationException();
    }

    public int getSessions() {
        return (int) this._sessionsStats.getCurrent();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public String getSessionIdPathParameterName() {
        return this._sessionIdPathParameterName;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public String getSessionIdPathParameterNamePrefix() {
        return this._sessionIdPathParameterNamePrefix;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public boolean isUsingCookies() {
        return this._usingCookies;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public boolean isValid(HttpSession session) {
        AbstractSession s = ((SessionIf) session).getSession();
        return s.isValid();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public String getClusterId(HttpSession session) {
        AbstractSession s = ((SessionIf) session).getSession();
        return s.getClusterId();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public String getNodeId(HttpSession session) {
        AbstractSession s = ((SessionIf) session).getSession();
        return s.getNodeId();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public HttpSession newHttpSession(HttpServletRequest request) {
        AbstractSession session = newSession(request);
        session.setMaxInactiveInterval(this._dftMaxIdleSecs);
        addSession(session, true);
        return session;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void removeEventListener(EventListener listener) {
        if (listener instanceof HttpSessionAttributeListener) {
            this._sessionAttributeListeners.remove(listener);
        }
        if (listener instanceof HttpSessionListener) {
            this._sessionListeners.remove(listener);
        }
    }

    @Deprecated
    public void resetStats() {
        statsReset();
    }

    public void statsReset() {
        this._sessionsStats.reset(getSessions());
        this._sessionTimeStats.reset();
    }

    public void setHttpOnly(boolean httpOnly) {
        this._httpOnly = httpOnly;
    }

    public void setIdManager(SessionIdManager metaManager) {
        setSessionIdManager(metaManager);
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void setSessionIdManager(SessionIdManager metaManager) {
        this._sessionIdManager = metaManager;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void setMaxInactiveInterval(int seconds) {
        this._dftMaxIdleSecs = seconds;
    }

    public void setRefreshCookieAge(int ageInSeconds) {
        this._refreshCookieAge = ageInSeconds;
    }

    public void setSessionCookie(String cookieName) {
        this._sessionCookie = cookieName;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void setSessionHandler(SessionHandler sessionHandler) {
        this._sessionHandler = sessionHandler;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void setSessionIdPathParameterName(String param) {
        String str = null;
        this._sessionIdPathParameterName = (param == null || "none".equals(param)) ? null : param;
        if (param != null && !"none".equals(param)) {
            str = ";" + this._sessionIdPathParameterName + "=";
        }
        this._sessionIdPathParameterNamePrefix = str;
    }

    public void setUsingCookies(boolean usingCookies) {
        this._usingCookies = usingCookies;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void addSession(AbstractSession session, boolean created) {
        synchronized (this._sessionIdManager) {
            this._sessionIdManager.addSession(session);
            addSession(session);
        }
        if (created) {
            this._sessionsStats.increment();
            if (this._sessionListeners != null) {
                HttpSessionEvent event = new HttpSessionEvent(session);
                for (HttpSessionListener listener : this._sessionListeners) {
                    listener.sessionCreated(event);
                }
            }
        }
    }

    public boolean isNodeIdInSessionId() {
        return this._nodeIdInSessionId;
    }

    public void setNodeIdInSessionId(boolean nodeIdInSessionId) {
        this._nodeIdInSessionId = nodeIdInSessionId;
    }

    public void removeSession(HttpSession session, boolean invalidate) {
        AbstractSession s = ((SessionIf) session).getSession();
        removeSession(s, invalidate);
    }

    public void removeSession(AbstractSession session, boolean invalidate) {
        boolean removed = removeSession(session.getClusterId());
        if (removed) {
            this._sessionsStats.decrement();
            this._sessionTimeStats.set(Math.round((System.currentTimeMillis() - session.getCreationTime()) / 1000.0d));
            this._sessionIdManager.removeSession(session);
            if (invalidate) {
                this._sessionIdManager.invalidateAll(session.getClusterId());
            }
            if (invalidate && this._sessionListeners != null) {
                HttpSessionEvent event = new HttpSessionEvent(session);
                for (HttpSessionListener listener : this._sessionListeners) {
                    listener.sessionDestroyed(event);
                }
            }
        }
    }

    public long getSessionTimeMax() {
        return this._sessionTimeStats.getMax();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return this.__defaultSessionTrackingModes;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return Collections.unmodifiableSet(this._sessionTrackingModes);
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        this._sessionTrackingModes = new HashSet(sessionTrackingModes);
        this._usingCookies = this._sessionTrackingModes.contains(SessionTrackingMode.COOKIE);
        this._usingURLs = this._sessionTrackingModes.contains(SessionTrackingMode.URL);
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public boolean isUsingURLs() {
        return this._usingURLs;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public SessionCookieConfig getSessionCookieConfig() {
        return this._cookieConfig;
    }

    public long getSessionTimeTotal() {
        return this._sessionTimeStats.getTotal();
    }

    public double getSessionTimeMean() {
        return this._sessionTimeStats.getMean();
    }

    public double getSessionTimeStdDev() {
        return this._sessionTimeStats.getStdDev();
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public boolean isCheckingRemoteSessionIdEncoding() {
        return this._checkingRemoteSessionIdEncoding;
    }

    @Override // org.eclipse.jetty.server.SessionManager
    public void setCheckingRemoteSessionIdEncoding(boolean remote) {
        this._checkingRemoteSessionIdEncoding = remote;
    }

    public void doSessionAttributeListeners(AbstractSession session, String name, Object old, Object value) {
        if (!this._sessionAttributeListeners.isEmpty()) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, old == null ? value : old);
            for (HttpSessionAttributeListener l : this._sessionAttributeListeners) {
                if (old == null) {
                    l.attributeAdded(event);
                } else if (value == null) {
                    l.attributeRemoved(event);
                } else {
                    l.attributeReplaced(event);
                }
            }
        }
    }
}
