package org.eclipse.jetty.server.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class JDBCSessionManager extends AbstractSessionManager {
    private static final Logger LOG = Log.getLogger(JDBCSessionManager.class);
    protected JDBCSessionIdManager _jdbcSessionIdMgr = null;
    protected long _saveIntervalSec = 60;
    private ConcurrentHashMap<String, AbstractSession> _sessions;

    /* loaded from: classes.dex */
    public class Session extends AbstractSession {
        private static final long serialVersionUID = 5208464051134226143L;
        private String _canonicalContext;
        private long _cookieSet;
        private boolean _dirty;
        private long _expiryTime;
        private String _lastNode;
        private long _lastSaved;
        private String _rowId;
        private String _virtualHost;

        protected Session(HttpServletRequest request) {
            super(JDBCSessionManager.this, request);
            this._dirty = false;
            int maxInterval = getMaxInactiveInterval();
            this._expiryTime = maxInterval <= 0 ? 0L : System.currentTimeMillis() + (maxInterval * 1000);
            this._virtualHost = JDBCSessionManager.getVirtualHost(JDBCSessionManager.this._context);
            this._canonicalContext = JDBCSessionManager.canonicalize(JDBCSessionManager.this._context.getContextPath());
            this._lastNode = JDBCSessionManager.this.getSessionIdManager().getWorkerName();
        }

        protected Session(String sessionId, String rowId, long created, long accessed) {
            super(JDBCSessionManager.this, created, accessed, sessionId);
            this._dirty = false;
            this._rowId = rowId;
        }

        protected synchronized String getRowId() {
            return this._rowId;
        }

        protected synchronized void setRowId(String rowId) {
            this._rowId = rowId;
        }

        public synchronized void setVirtualHost(String vhost) {
            this._virtualHost = vhost;
        }

        public synchronized String getVirtualHost() {
            return this._virtualHost;
        }

        public synchronized long getLastSaved() {
            return this._lastSaved;
        }

        public synchronized void setLastSaved(long time) {
            this._lastSaved = time;
        }

        public synchronized void setExpiryTime(long time) {
            this._expiryTime = time;
        }

        public synchronized long getExpiryTime() {
            return this._expiryTime;
        }

        public synchronized void setCanonicalContext(String str) {
            this._canonicalContext = str;
        }

        public synchronized String getCanonicalContext() {
            return this._canonicalContext;
        }

        public void setCookieSet(long ms) {
            this._cookieSet = ms;
        }

        public synchronized long getCookieSet() {
            return this._cookieSet;
        }

        public synchronized void setLastNode(String node) {
            this._lastNode = node;
        }

        public synchronized String getLastNode() {
            return this._lastNode;
        }

        @Override // org.eclipse.jetty.server.session.AbstractSession, javax.servlet.http.HttpSession
        public void setAttribute(String name, Object value) {
            super.setAttribute(name, value);
            this._dirty = true;
        }

        @Override // org.eclipse.jetty.server.session.AbstractSession, javax.servlet.http.HttpSession
        public void removeAttribute(String name) {
            super.removeAttribute(name);
            this._dirty = true;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.server.session.AbstractSession
        public void cookieSet() {
            this._cookieSet = getAccessed();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.server.session.AbstractSession
        public boolean access(long time) {
            synchronized (this) {
                if (super.access(time)) {
                    int maxInterval = getMaxInactiveInterval();
                    this._expiryTime = maxInterval <= 0 ? 0L : (maxInterval * 1000) + time;
                    return true;
                }
                return false;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.server.session.AbstractSession
        public void complete() {
            synchronized (this) {
                super.complete();
                try {
                    if (isValid()) {
                        if (this._dirty) {
                            willPassivate();
                            JDBCSessionManager.this.updateSession(this);
                            didActivate();
                        } else if (getAccessed() - this._lastSaved >= JDBCSessionManager.this.getSaveInterval() * 1000) {
                            JDBCSessionManager.this.updateSessionAccessTime(this);
                        }
                    }
                } catch (Exception e) {
                    Logger logger = LOG;
                    logger.warn("Problem persisting changed session data id=" + getId(), e);
                }
                this._dirty = false;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // org.eclipse.jetty.server.session.AbstractSession
        public void timeout() throws IllegalStateException {
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Timing out session id=" + getClusterId(), new Object[0]);
            }
            super.timeout();
        }

        @Override // org.eclipse.jetty.server.session.AbstractSession
        public String toString() {
            return "Session rowId=" + this._rowId + ",id=" + getId() + ",lastNode=" + this._lastNode + ",created=" + getCreationTime() + ",accessed=" + getAccessed() + ",lastAccessed=" + getLastAccessedTime() + ",cookieSet=" + this._cookieSet + ",lastSaved=" + this._lastSaved + ",expiry=" + this._expiryTime;
        }
    }

    /* loaded from: classes.dex */
    protected class ClassLoadingObjectInputStream extends ObjectInputStream {
        public ClassLoadingObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        public ClassLoadingObjectInputStream() throws IOException {
        }

        @Override // java.io.ObjectInputStream
        public Class<?> resolveClass(ObjectStreamClass cl) throws IOException, ClassNotFoundException {
            try {
                return Class.forName(cl.getName(), false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                return super.resolveClass(cl);
            }
        }
    }

    public void setSaveInterval(long sec) {
        this._saveIntervalSec = sec;
    }

    public long getSaveInterval() {
        return this._saveIntervalSec;
    }

    public void cacheInvalidate(Session session) {
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    public Session getSession(String idInCluster) {
        Session session;
        Session memSession = (Session) this._sessions.get(idInCluster);
        synchronized (this) {
            long now = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                if (memSession == null) {
                    Logger logger = LOG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("getSession(");
                    sb.append(idInCluster);
                    sb.append("): not in session map,");
                    sb.append(" now=");
                    sb.append(now);
                    sb.append(" lastSaved=");
                    sb.append(memSession == null ? 0L : memSession._lastSaved);
                    sb.append(" interval=");
                    sb.append(this._saveIntervalSec * 1000);
                    logger.debug(sb.toString(), new Object[0]);
                } else {
                    Logger logger2 = LOG;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("getSession(");
                    sb2.append(idInCluster);
                    sb2.append("): in session map, ");
                    sb2.append(" now=");
                    sb2.append(now);
                    sb2.append(" lastSaved=");
                    sb2.append(memSession == null ? 0L : memSession._lastSaved);
                    sb2.append(" interval=");
                    sb2.append(this._saveIntervalSec * 1000);
                    sb2.append(" lastNode=");
                    sb2.append(memSession._lastNode);
                    sb2.append(" thisNode=");
                    sb2.append(getSessionIdManager().getWorkerName());
                    sb2.append(" difference=");
                    sb2.append(now - memSession._lastSaved);
                    logger2.debug(sb2.toString(), new Object[0]);
                }
            }
            try {
                if (memSession != null) {
                    if (now - memSession._lastSaved >= this._saveIntervalSec * 1000) {
                        Logger logger3 = LOG;
                        logger3.debug("getSession(" + idInCluster + "): stale session. Reloading session data from db.", new Object[0]);
                        session = loadSession(idInCluster, canonicalize(this._context.getContextPath()), getVirtualHost(this._context));
                    } else {
                        Logger logger4 = LOG;
                        logger4.debug("getSession(" + idInCluster + "): session in session map", new Object[0]);
                        session = memSession;
                    }
                } else {
                    Logger logger5 = LOG;
                    logger5.debug("getSession(" + idInCluster + "): no session in session map. Reloading session data from db.", new Object[0]);
                    session = loadSession(idInCluster, canonicalize(this._context.getContextPath()), getVirtualHost(this._context));
                }
                if (session != null) {
                    if (session.getLastNode().equals(getSessionIdManager().getWorkerName()) && memSession != null) {
                        session = memSession;
                        LOG.debug("getSession({}): Session not stale {}", idInCluster, session);
                    }
                    if (session._expiryTime > 0 && session._expiryTime <= now) {
                        LOG.debug("getSession ({}): Session has expired", idInCluster);
                        session = null;
                    }
                    if (LOG.isDebugEnabled()) {
                        Logger logger6 = LOG;
                        logger6.debug("getSession(" + idInCluster + "): lastNode=" + session.getLastNode() + " thisNode=" + getSessionIdManager().getWorkerName(), new Object[0]);
                    }
                    session.setLastNode(getSessionIdManager().getWorkerName());
                    this._sessions.put(idInCluster, session);
                    try {
                        updateSessionNode(session);
                        session.didActivate();
                    } catch (Exception e) {
                        Logger logger7 = LOG;
                        logger7.warn("Unable to update freshly loaded session " + idInCluster, e);
                        return null;
                    }
                } else {
                    LOG.debug("getSession({}): No session in database matching id={}", idInCluster, idInCluster);
                }
            } catch (Exception e2) {
                Logger logger8 = LOG;
                logger8.warn("Unable to load session " + idInCluster, e2);
                return null;
            }
        }
        return session;
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    public int getSessions() {
        int size;
        synchronized (this) {
            size = this._sessions.size();
        }
        return size;
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        if (this._sessionIdManager == null) {
            throw new IllegalStateException("No session id manager defined");
        }
        this._jdbcSessionIdMgr = (JDBCSessionIdManager) this._sessionIdManager;
        this._sessions = new ConcurrentHashMap<>();
        super.doStart();
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._sessions.clear();
        this._sessions = null;
        super.doStop();
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected void invalidateSessions() {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void invalidateSession(String idInCluster) {
        Session session;
        synchronized (this) {
            session = (Session) this._sessions.get(idInCluster);
        }
        if (session != null) {
            session.invalidate();
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected boolean removeSession(String idInCluster) {
        boolean z;
        synchronized (this) {
            Session session = (Session) this._sessions.remove(idInCluster);
            if (session != null) {
                try {
                    deleteSession(session);
                } catch (Exception e) {
                    Logger logger = LOG;
                    logger.warn("Problem deleting session id=" + idInCluster, e);
                }
            }
            z = session != null;
        }
        return z;
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected void addSession(AbstractSession session) {
        if (session == null) {
            return;
        }
        synchronized (this) {
            this._sessions.put(session.getClusterId(), session);
        }
        try {
            synchronized (session) {
                session.willPassivate();
                storeSession((Session) session);
                session.didActivate();
            }
        } catch (Exception e) {
            Logger logger = LOG;
            logger.warn("Unable to store new session id=" + session.getId(), e);
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected AbstractSession newSession(HttpServletRequest request) {
        return new Session(request);
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    public void removeSession(AbstractSession session, boolean invalidate) {
        boolean removed = false;
        synchronized (this) {
            if (getSession(session.getClusterId()) != null) {
                removed = true;
                removeSession(session.getClusterId());
            }
        }
        if (removed) {
            this._sessionIdManager.removeSession(session);
            if (invalidate) {
                this._sessionIdManager.invalidateAll(session.getClusterId());
            }
            if (invalidate && !this._sessionListeners.isEmpty()) {
                HttpSessionEvent event = new HttpSessionEvent(session);
                for (HttpSessionListener l : this._sessionListeners) {
                    l.sessionDestroyed(event);
                }
            }
            if (!invalidate) {
                session.willPassivate();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void expire(List<?> sessionIds) {
        if (isStopping() || isStopped()) {
            return;
        }
        Thread thread = Thread.currentThread();
        ClassLoader old_loader = thread.getContextClassLoader();
        ListIterator<?> itor = sessionIds.listIterator();
        while (itor.hasNext()) {
            try {
                String sessionId = (String) itor.next();
                if (LOG.isDebugEnabled()) {
                    Logger logger = LOG;
                    logger.debug("Expiring session id " + sessionId, new Object[0]);
                }
                Session session = (Session) this._sessions.get(sessionId);
                if (session != null) {
                    session.timeout();
                    itor.remove();
                } else if (LOG.isDebugEnabled()) {
                    Logger logger2 = LOG;
                    logger2.debug("Unrecognized session id=" + sessionId, new Object[0]);
                }
            } finally {
                try {
                } finally {
                }
            }
        }
    }

    protected Session loadSession(final String id, final String canonicalContextPath, final String vhost) throws Exception {
        final AtomicReference<Session> _reference = new AtomicReference<>();
        final AtomicReference<Exception> _exception = new AtomicReference<>();
        Runnable load = new Runnable() { // from class: org.eclipse.jetty.server.session.JDBCSessionManager.1
            @Override // java.lang.Runnable
            public void run() {
                Session session = null;
                Connection connection = null;
                PreparedStatement statement = null;
                try {
                    try {
                        try {
                            connection = JDBCSessionManager.this.getConnection();
                            statement = JDBCSessionManager.this._jdbcSessionIdMgr._dbAdaptor.getLoadStatement(connection, id, canonicalContextPath, vhost);
                            ResultSet result = statement.executeQuery();
                            if (result.next()) {
                                session = new Session(id, result.getString(JDBCSessionManager.this._jdbcSessionIdMgr._sessionTableRowId), result.getLong("createTime"), result.getLong("accessTime"));
                                session.setCookieSet(result.getLong("cookieTime"));
                                session.setLastAccessedTime(result.getLong("lastAccessTime"));
                                session.setLastNode(result.getString("lastNode"));
                                session.setLastSaved(result.getLong("lastSavedTime"));
                                session.setExpiryTime(result.getLong("expiryTime"));
                                session.setCanonicalContext(result.getString("contextPath"));
                                session.setVirtualHost(result.getString("virtualHost"));
                                InputStream is = ((JDBCSessionIdManager) JDBCSessionManager.this.getSessionIdManager())._dbAdaptor.getBlobInputStream(result, "map");
                                ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(is);
                                Object o = ois.readObject();
                                session.addAttributes((Map) o);
                                ois.close();
                                if (JDBCSessionManager.LOG.isDebugEnabled()) {
                                    Logger logger = JDBCSessionManager.LOG;
                                    logger.debug("LOADED session " + session, new Object[0]);
                                }
                            }
                            _reference.set(session);
                            if (statement != null) {
                                try {
                                    statement.close();
                                } catch (Exception e) {
                                    JDBCSessionManager.LOG.warn(e);
                                }
                            }
                        } catch (Throwable th) {
                            if (statement != null) {
                                try {
                                    statement.close();
                                } catch (Exception e2) {
                                    JDBCSessionManager.LOG.warn(e2);
                                }
                            }
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (Exception e3) {
                                    JDBCSessionManager.LOG.warn(e3);
                                }
                            }
                            throw th;
                        }
                    } catch (Exception e4) {
                        _exception.set(e4);
                        if (statement != null) {
                            try {
                                statement.close();
                            } catch (Exception e5) {
                                JDBCSessionManager.LOG.warn(e5);
                            }
                        }
                        if (connection == null) {
                            return;
                        }
                        connection.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e6) {
                    JDBCSessionManager.LOG.warn(e6);
                }
            }
        };
        if (this._context == null) {
            load.run();
        } else {
            this._context.getContextHandler().handle(load);
        }
        if (_exception.get() != null) {
            this._jdbcSessionIdMgr.removeSession(id);
            throw _exception.get();
        }
        return _reference.get();
    }

    protected void storeSession(Session session) throws Exception {
        if (session == null) {
            return;
        }
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            String rowId = calculateRowId(session);
            long now = System.currentTimeMillis();
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._jdbcSessionIdMgr._insertSession);
            statement.setString(1, rowId);
            statement.setString(2, session.getId());
            statement.setString(3, session.getCanonicalContext());
            statement.setString(4, session.getVirtualHost());
            statement.setString(5, getSessionIdManager().getWorkerName());
            statement.setLong(6, session.getAccessed());
            statement.setLong(7, session.getLastAccessedTime());
            statement.setLong(8, session.getCreationTime());
            statement.setLong(9, session.getCookieSet());
            statement.setLong(10, now);
            statement.setLong(11, session.getExpiryTime());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(session.getAttributeMap());
            byte[] bytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            statement.setBinaryStream(12, (InputStream) bais, bytes.length);
            statement.executeUpdate();
            session.setRowId(rowId);
            session.setLastSaved(now);
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Stored session " + session, new Object[0]);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    LOG.warn(e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected void updateSession(Session data) throws Exception {
        if (data == null) {
            return;
        }
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            long now = System.currentTimeMillis();
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._jdbcSessionIdMgr._updateSession);
            statement.setString(1, getSessionIdManager().getWorkerName());
            statement.setLong(2, data.getAccessed());
            statement.setLong(3, data.getLastAccessedTime());
            statement.setLong(4, now);
            statement.setLong(5, data.getExpiryTime());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data.getAttributeMap());
            byte[] bytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            statement.setBinaryStream(6, (InputStream) bais, bytes.length);
            statement.setString(7, data.getRowId());
            statement.executeUpdate();
            data.setLastSaved(now);
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Updated session " + data, new Object[0]);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    LOG.warn(e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected void updateSessionNode(Session data) throws Exception {
        String nodeId = getSessionIdManager().getWorkerName();
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._jdbcSessionIdMgr._updateSessionNode);
            statement.setString(1, nodeId);
            statement.setString(2, data.getRowId());
            statement.executeUpdate();
            statement.close();
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Updated last node for session id=" + data.getId() + ", lastNode = " + nodeId, new Object[0]);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    LOG.warn(e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSessionAccessTime(Session data) throws Exception {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            long now = System.currentTimeMillis();
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._jdbcSessionIdMgr._updateSessionAccessTime);
            statement.setString(1, getSessionIdManager().getWorkerName());
            statement.setLong(2, data.getAccessed());
            statement.setLong(3, data.getLastAccessedTime());
            statement.setLong(4, now);
            statement.setLong(5, data.getExpiryTime());
            statement.setString(6, data.getRowId());
            statement.executeUpdate();
            data.setLastSaved(now);
            statement.close();
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Updated access time session id=" + data.getId(), new Object[0]);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    LOG.warn(e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected void deleteSession(Session data) throws Exception {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._jdbcSessionIdMgr._deleteSession);
            statement.setString(1, data.getRowId());
            statement.executeUpdate();
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Deleted Session " + data, new Object[0]);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    LOG.warn(e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Connection getConnection() throws SQLException {
        return ((JDBCSessionIdManager) getSessionIdManager()).getConnection();
    }

    private String calculateRowId(Session data) {
        String rowId = canonicalize(this._context.getContextPath());
        return (rowId + "_" + getVirtualHost(this._context)) + "_" + data.getId();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getVirtualHost(ContextHandler.Context context) {
        String[] vhosts;
        if (context == null || (vhosts = context.getContextHandler().getVirtualHosts()) == null || vhosts.length == 0 || vhosts[0] == null) {
            return StringUtil.ALL_INTERFACES;
        }
        return vhosts[0];
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String canonicalize(String path) {
        if (path == null) {
            return "";
        }
        return path.replace('/', '_').replace('.', '_').replace('\\', '_');
    }
}
