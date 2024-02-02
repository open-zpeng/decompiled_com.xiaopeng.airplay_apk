package org.eclipse.jetty.server.session;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.JDBCSessionManager;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class JDBCSessionIdManager extends AbstractSessionIdManager {
    static final Logger LOG = SessionHandler.LOG;
    protected String _blobType;
    protected String _connectionUrl;
    protected String _createSessionIdTable;
    protected String _createSessionTable;
    protected DataSource _datasource;
    protected DatabaseAdaptor _dbAdaptor;
    protected String _deleteId;
    protected String _deleteOldExpiredSessions;
    protected String _deleteSession;
    protected Driver _driver;
    protected String _driverClassName;
    protected String _insertId;
    protected String _insertSession;
    protected String _jndiName;
    protected long _lastScavengeTime;
    protected String _longType;
    protected String _queryId;
    protected long _scavengeIntervalMs;
    protected String _selectBoundedExpiredSessions;
    private String _selectExpiredSessions;
    protected Server _server;
    protected String _sessionIdTable;
    protected final HashSet<String> _sessionIds;
    protected String _sessionTable;
    protected String _sessionTableRowId;
    protected TimerTask _task;
    protected Timer _timer;
    protected String _updateSession;
    protected String _updateSessionAccessTime;
    protected String _updateSessionNode;

    /* loaded from: classes.dex */
    public class DatabaseAdaptor {
        String _dbName;
        boolean _isLower;
        boolean _isUpper;

        public DatabaseAdaptor(DatabaseMetaData dbMeta) throws SQLException {
            this._dbName = dbMeta.getDatabaseProductName().toLowerCase(Locale.ENGLISH);
            JDBCSessionIdManager.LOG.debug("Using database {}", this._dbName);
            this._isLower = dbMeta.storesLowerCaseIdentifiers();
            this._isUpper = dbMeta.storesUpperCaseIdentifiers();
        }

        public String convertIdentifier(String identifier) {
            if (this._isLower) {
                return identifier.toLowerCase(Locale.ENGLISH);
            }
            if (this._isUpper) {
                return identifier.toUpperCase(Locale.ENGLISH);
            }
            return identifier;
        }

        public String getDBName() {
            return this._dbName;
        }

        public String getBlobType() {
            if (JDBCSessionIdManager.this._blobType != null) {
                return JDBCSessionIdManager.this._blobType;
            }
            if (this._dbName.startsWith("postgres")) {
                return "bytea";
            }
            return "blob";
        }

        public String getLongType() {
            if (JDBCSessionIdManager.this._longType != null) {
                return JDBCSessionIdManager.this._longType;
            }
            if (this._dbName.startsWith("oracle")) {
                return "number(20)";
            }
            return "bigint";
        }

        public InputStream getBlobInputStream(ResultSet result, String columnName) throws SQLException {
            if (this._dbName.startsWith("postgres")) {
                byte[] bytes = result.getBytes(columnName);
                return new ByteArrayInputStream(bytes);
            }
            Blob blob = result.getBlob(columnName);
            return blob.getBinaryStream();
        }

        public String getRowIdColumnName() {
            if (this._dbName != null && this._dbName.startsWith("oracle")) {
                return "srowId";
            }
            return "rowId";
        }

        public boolean isEmptyStringNull() {
            return this._dbName.startsWith("oracle");
        }

        public PreparedStatement getLoadStatement(Connection connection, String rowId, String contextPath, String virtualHosts) throws SQLException {
            if ((contextPath == null || "".equals(contextPath)) && isEmptyStringNull()) {
                PreparedStatement statement = connection.prepareStatement("select * from " + JDBCSessionIdManager.this._sessionTable + " where sessionId = ? and contextPath is null and virtualHost = ?");
                statement.setString(1, rowId);
                statement.setString(2, virtualHosts);
                return statement;
            }
            PreparedStatement statement2 = connection.prepareStatement("select * from " + JDBCSessionIdManager.this._sessionTable + " where sessionId = ? and contextPath = ? and virtualHost = ?");
            statement2.setString(1, rowId);
            statement2.setString(2, contextPath);
            statement2.setString(3, virtualHosts);
            return statement2;
        }
    }

    public JDBCSessionIdManager(Server server) {
        this._sessionIds = new HashSet<>();
        this._sessionIdTable = "JettySessionIds";
        this._sessionTable = "JettySessions";
        this._sessionTableRowId = "rowId";
        this._scavengeIntervalMs = 600000L;
        this._server = server;
    }

    public JDBCSessionIdManager(Server server, Random random) {
        super(random);
        this._sessionIds = new HashSet<>();
        this._sessionIdTable = "JettySessionIds";
        this._sessionTable = "JettySessions";
        this._sessionTableRowId = "rowId";
        this._scavengeIntervalMs = 600000L;
        this._server = server;
    }

    public void setDriverInfo(String driverClassName, String connectionUrl) {
        this._driverClassName = driverClassName;
        this._connectionUrl = connectionUrl;
    }

    public void setDriverInfo(Driver driverClass, String connectionUrl) {
        this._driver = driverClass;
        this._connectionUrl = connectionUrl;
    }

    public void setDatasource(DataSource ds) {
        this._datasource = ds;
    }

    public DataSource getDataSource() {
        return this._datasource;
    }

    public String getDriverClassName() {
        return this._driverClassName;
    }

    public String getConnectionUrl() {
        return this._connectionUrl;
    }

    public void setDatasourceName(String jndi) {
        this._jndiName = jndi;
    }

    public String getDatasourceName() {
        return this._jndiName;
    }

    public void setBlobType(String name) {
        this._blobType = name;
    }

    public String getBlobType() {
        return this._blobType;
    }

    public String getLongType() {
        return this._longType;
    }

    public void setLongType(String longType) {
        this._longType = longType;
    }

    public void setScavengeInterval(long sec) {
        long sec2;
        if (sec <= 0) {
            sec2 = 60;
        } else {
            sec2 = sec;
        }
        long old_period = this._scavengeIntervalMs;
        long period = 1000 * sec2;
        this._scavengeIntervalMs = period;
        long tenPercent = this._scavengeIntervalMs / 10;
        if (System.currentTimeMillis() % 2 == 0) {
            this._scavengeIntervalMs += tenPercent;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Scavenging every " + this._scavengeIntervalMs + " ms", new Object[0]);
        }
        if (this._timer != null) {
            if (period != old_period || this._task == null) {
                synchronized (this) {
                    if (this._task != null) {
                        this._task.cancel();
                    }
                    this._task = new TimerTask() { // from class: org.eclipse.jetty.server.session.JDBCSessionIdManager.1
                        @Override // java.util.TimerTask, java.lang.Runnable
                        public void run() {
                            JDBCSessionIdManager.this.scavenge();
                        }
                    };
                    this._timer.schedule(this._task, this._scavengeIntervalMs, this._scavengeIntervalMs);
                }
            }
        }
    }

    public long getScavengeInterval() {
        return this._scavengeIntervalMs / 1000;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public void addSession(HttpSession session) {
        if (session == null) {
            return;
        }
        synchronized (this._sessionIds) {
            String id = ((JDBCSessionManager.Session) session).getClusterId();
            try {
                insert(id);
                this._sessionIds.add(id);
            } catch (Exception e) {
                Logger logger = LOG;
                logger.warn("Problem storing session id=" + id, e);
            }
        }
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public void removeSession(HttpSession session) {
        if (session == null) {
            return;
        }
        removeSession(((JDBCSessionManager.Session) session).getClusterId());
    }

    public void removeSession(String id) {
        if (id == null) {
            return;
        }
        synchronized (this._sessionIds) {
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("Removing session id=" + id, new Object[0]);
            }
            try {
                this._sessionIds.remove(id);
                delete(id);
            } catch (Exception e) {
                Logger logger2 = LOG;
                logger2.warn("Problem removing session id=" + id, e);
            }
        }
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public String getClusterId(String nodeId) {
        int dot = nodeId.lastIndexOf(46);
        return dot > 0 ? nodeId.substring(0, dot) : nodeId;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public String getNodeId(String clusterId, HttpServletRequest request) {
        if (this._workerName != null) {
            return clusterId + '.' + this._workerName;
        }
        return clusterId;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public boolean idInUse(String id) {
        boolean inUse;
        if (id == null) {
            return false;
        }
        String clusterId = getClusterId(id);
        synchronized (this._sessionIds) {
            inUse = this._sessionIds.contains(clusterId);
        }
        if (inUse) {
            return true;
        }
        try {
            return exists(clusterId);
        } catch (Exception e) {
            Logger logger = LOG;
            logger.warn("Problem checking inUse for id=" + clusterId, e);
            return false;
        }
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public void invalidateAll(String id) {
        SessionManager manager;
        removeSession(id);
        synchronized (this._sessionIds) {
            Handler[] contexts = this._server.getChildHandlersByClass(ContextHandler.class);
            for (int i = 0; contexts != null && i < contexts.length; i++) {
                SessionHandler sessionHandler = (SessionHandler) ((ContextHandler) contexts[i]).getChildHandlerByClass(SessionHandler.class);
                if (sessionHandler != null && (manager = sessionHandler.getSessionManager()) != null && (manager instanceof JDBCSessionManager)) {
                    ((JDBCSessionManager) manager).invalidateSession(id);
                }
            }
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionIdManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        initializeDatabase();
        prepareTables();
        cleanExpiredSessions();
        super.doStart();
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Scavenging interval = " + getScavengeInterval() + " sec", new Object[0]);
        }
        this._timer = new Timer("JDBCSessionScavenger", true);
        setScavengeInterval(getScavengeInterval());
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionIdManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        synchronized (this) {
            if (this._task != null) {
                this._task.cancel();
            }
            if (this._timer != null) {
                this._timer.cancel();
            }
            this._timer = null;
        }
        this._sessionIds.clear();
        super.doStop();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Connection getConnection() throws SQLException {
        if (this._datasource != null) {
            return this._datasource.getConnection();
        }
        return DriverManager.getConnection(this._connectionUrl);
    }

    private void prepareTables() throws SQLException {
        this._createSessionIdTable = "create table " + this._sessionIdTable + " (id varchar(120), primary key(id))";
        this._selectBoundedExpiredSessions = "select * from " + this._sessionTable + " where expiryTime >= ? and expiryTime <= ?";
        this._selectExpiredSessions = "select * from " + this._sessionTable + " where expiryTime >0 and expiryTime <= ?";
        this._deleteOldExpiredSessions = "delete from " + this._sessionTable + " where expiryTime >0 and expiryTime <= ?";
        this._insertId = "insert into " + this._sessionIdTable + " (id)  values (?)";
        this._deleteId = "delete from " + this._sessionIdTable + " where id = ?";
        this._queryId = "select * from " + this._sessionIdTable + " where id = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(true);
            DatabaseMetaData metaData = connection.getMetaData();
            this._dbAdaptor = new DatabaseAdaptor(metaData);
            this._sessionTableRowId = this._dbAdaptor.getRowIdColumnName();
            ResultSet result = metaData.getTables(null, null, this._dbAdaptor.convertIdentifier(this._sessionIdTable), null);
            if (!result.next()) {
                connection.createStatement().executeUpdate(this._createSessionIdTable);
            }
            String tableName = this._dbAdaptor.convertIdentifier(this._sessionTable);
            ResultSet result2 = metaData.getTables(null, null, tableName, null);
            if (!result2.next()) {
                String blobType = this._dbAdaptor.getBlobType();
                String longType = this._dbAdaptor.getLongType();
                this._createSessionTable = "create table " + this._sessionTable + " (" + this._sessionTableRowId + " varchar(120), sessionId varchar(120),  contextPath varchar(60), virtualHost varchar(60), lastNode varchar(60), accessTime " + longType + ",  lastAccessTime " + longType + ", createTime " + longType + ", cookieTime " + longType + ",  lastSavedTime " + longType + ", expiryTime " + longType + ", map " + blobType + ", primary key(" + this._sessionTableRowId + "))";
                connection.createStatement().executeUpdate(this._createSessionTable);
            }
            String index1 = "idx_" + this._sessionTable + "_expiry";
            String index2 = "idx_" + this._sessionTable + "_session";
            ResultSet result3 = metaData.getIndexInfo(null, null, tableName, false, false);
            boolean index1Exists = false;
            boolean index2Exists = false;
            while (result3.next()) {
                String idxName = result3.getString("INDEX_NAME");
                if (index1.equalsIgnoreCase(idxName)) {
                    index1Exists = true;
                } else if (index2.equalsIgnoreCase(idxName)) {
                    index2Exists = true;
                }
            }
            if (!index1Exists || !index2Exists) {
                Statement statement = connection.createStatement();
                if (!index1Exists) {
                    statement.executeUpdate("create index " + index1 + " on " + this._sessionTable + " (expiryTime)");
                }
                if (!index2Exists) {
                    statement.executeUpdate("create index " + index2 + " on " + this._sessionTable + " (sessionId, contextPath)");
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                        LOG.warn(e);
                    }
                }
            }
            this._insertSession = "insert into " + this._sessionTable + " (" + this._sessionTableRowId + ", sessionId, contextPath, virtualHost, lastNode, accessTime, lastAccessTime, createTime, cookieTime, lastSavedTime, expiryTime, map)  values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            StringBuilder sb = new StringBuilder();
            sb.append("delete from ");
            sb.append(this._sessionTable);
            sb.append(" where ");
            sb.append(this._sessionTableRowId);
            sb.append(" = ?");
            this._deleteSession = sb.toString();
            this._updateSession = "update " + this._sessionTable + " set lastNode = ?, accessTime = ?, lastAccessTime = ?, lastSavedTime = ?, expiryTime = ?, map = ? where " + this._sessionTableRowId + " = ?";
            this._updateSessionNode = "update " + this._sessionTable + " set lastNode = ? where " + this._sessionTableRowId + " = ?";
            this._updateSessionAccessTime = "update " + this._sessionTable + " set lastNode = ?, accessTime = ?, lastAccessTime = ?, lastSavedTime = ?, expiryTime = ? where " + this._sessionTableRowId + " = ?";
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void insert(String id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement query = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(true);
            query = connection.prepareStatement(this._queryId);
            query.setString(1, id);
            ResultSet result = query.executeQuery();
            if (!result.next()) {
                statement = connection.prepareStatement(this._insertId);
                statement.setString(1, id);
                statement.executeUpdate();
            }
        } finally {
            if (query != null) {
                try {
                    query.close();
                } catch (Exception e) {
                    LOG.warn(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e2) {
                    LOG.warn(e2);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void delete(String id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._deleteId);
            statement.setString(1, id);
            statement.executeUpdate();
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

    private boolean exists(String id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(this._queryId);
            statement.setString(1, id);
            ResultSet result = statement.executeQuery();
            return result.next();
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
    public void scavenge() {
        SessionManager manager;
        Connection connection = null;
        List<String> expiredSessionIds = new ArrayList<>();
        try {
            try {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Scavenge sweep started at " + System.currentTimeMillis(), new Object[0]);
                    }
                    if (this._lastScavengeTime > 0) {
                        connection = getConnection();
                        connection.setAutoCommit(true);
                        PreparedStatement statement = connection.prepareStatement(this._selectBoundedExpiredSessions);
                        long lowerBound = this._lastScavengeTime - this._scavengeIntervalMs;
                        long upperBound = this._lastScavengeTime;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(" Searching for sessions expired between " + lowerBound + " and " + upperBound, new Object[0]);
                        }
                        statement.setLong(1, lowerBound);
                        statement.setLong(2, upperBound);
                        ResultSet result = statement.executeQuery();
                        while (result.next()) {
                            String sessionId = result.getString("sessionId");
                            expiredSessionIds.add(sessionId);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(" Found expired sessionId=" + sessionId, new Object[0]);
                            }
                        }
                        Handler[] contexts = this._server.getChildHandlersByClass(ContextHandler.class);
                        for (int i = 0; contexts != null && i < contexts.length; i++) {
                            SessionHandler sessionHandler = (SessionHandler) ((ContextHandler) contexts[i]).getChildHandlerByClass(SessionHandler.class);
                            if (sessionHandler != null && (manager = sessionHandler.getSessionManager()) != null && (manager instanceof JDBCSessionManager)) {
                                ((JDBCSessionManager) manager).expire(expiredSessionIds);
                            }
                        }
                        long upperBound2 = this._lastScavengeTime - (2 * this._scavengeIntervalMs);
                        if (upperBound2 > 0) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Deleting old expired sessions expired before " + upperBound2, new Object[0]);
                            }
                            try {
                                statement = connection.prepareStatement(this._deleteOldExpiredSessions);
                                statement.setLong(1, upperBound2);
                                int rows = statement.executeUpdate();
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Deleted " + rows + " rows of old sessions expired before " + upperBound2, new Object[0]);
                                }
                                if (statement != null) {
                                    try {
                                        statement.close();
                                    } catch (Exception e) {
                                        LOG.warn(e);
                                    }
                                }
                            } catch (Throwable e2) {
                                PreparedStatement statement2 = statement;
                                try {
                                    if (statement2 != null) {
                                        try {
                                            statement2.close();
                                        } catch (Exception e3) {
                                            try {
                                                LOG.warn(e3);
                                            } catch (Exception e4) {
                                                e = e4;
                                                if (isRunning()) {
                                                    LOG.warn("Problem selecting expired sessions", e);
                                                } else {
                                                    LOG.ignore(e);
                                                }
                                                this._lastScavengeTime = System.currentTimeMillis();
                                                if (LOG.isDebugEnabled()) {
                                                    LOG.debug("Scavenge sweep ended at " + this._lastScavengeTime, new Object[0]);
                                                }
                                                if (connection != null) {
                                                    connection.close();
                                                }
                                                return;
                                            }
                                        }
                                    }
                                    throw e2;
                                } catch (Throwable th) {
                                    th = th;
                                    Connection connection2 = connection;
                                    Throwable th2 = th;
                                    this._lastScavengeTime = System.currentTimeMillis();
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Scavenge sweep ended at " + this._lastScavengeTime, new Object[0]);
                                    }
                                    if (connection2 != null) {
                                        try {
                                            connection2.close();
                                        } catch (SQLException e5) {
                                            LOG.warn(e5);
                                        }
                                    }
                                    throw th2;
                                }
                            }
                        }
                    }
                    this._lastScavengeTime = System.currentTimeMillis();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Scavenge sweep ended at " + this._lastScavengeTime, new Object[0]);
                    }
                } catch (Exception e6) {
                    e = e6;
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Throwable th3) {
                th = th3;
            }
        } catch (SQLException e7) {
            LOG.warn(e7);
        }
    }

    private void cleanExpiredSessions() {
        Connection connection = null;
        PreparedStatement statement = null;
        Statement sessionsTableStatement = null;
        Statement sessionIdsTableStatement = null;
        List<String> expiredSessionIds = new ArrayList<>();
        try {
            try {
                try {
                    connection = getConnection();
                    connection.setTransactionIsolation(2);
                    connection.setAutoCommit(false);
                    statement = connection.prepareStatement(this._selectExpiredSessions);
                    long now = System.currentTimeMillis();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Searching for sessions expired before {}", Long.valueOf(now));
                    }
                    statement.setLong(1, now);
                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        String sessionId = result.getString("sessionId");
                        expiredSessionIds.add(sessionId);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found expired sessionId={}", sessionId);
                        }
                    }
                    sessionsTableStatement = null;
                    sessionIdsTableStatement = null;
                    if (!expiredSessionIds.isEmpty()) {
                        sessionsTableStatement = connection.createStatement();
                        sessionsTableStatement.executeUpdate(createCleanExpiredSessionsSql("delete from " + this._sessionTable + " where sessionId in ", expiredSessionIds));
                        sessionIdsTableStatement = connection.createStatement();
                        sessionIdsTableStatement.executeUpdate(createCleanExpiredSessionsSql("delete from " + this._sessionIdTable + " where id in ", expiredSessionIds));
                    }
                    connection.commit();
                    synchronized (this._sessionIds) {
                        this._sessionIds.removeAll(expiredSessionIds);
                    }
                    if (sessionIdsTableStatement != null) {
                        try {
                            sessionIdsTableStatement.close();
                        } catch (Exception e) {
                            LOG.warn(e);
                        }
                    }
                    if (sessionsTableStatement != null) {
                        try {
                            sessionsTableStatement.close();
                        } catch (Exception e2) {
                            LOG.warn(e2);
                        }
                    }
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (Exception e3) {
                            LOG.warn(e3);
                        }
                    }
                } catch (Exception e4) {
                    if (connection != null) {
                        try {
                            LOG.warn("Rolling back clean of expired sessions", e4);
                            connection.rollback();
                        } catch (Exception x) {
                            LOG.warn("Rollback of expired sessions failed", x);
                        }
                    }
                    if (sessionIdsTableStatement != null) {
                        try {
                            sessionIdsTableStatement.close();
                        } catch (Exception e5) {
                            LOG.warn(e5);
                        }
                    }
                    if (sessionsTableStatement != null) {
                        try {
                            sessionsTableStatement.close();
                        } catch (Exception e6) {
                            LOG.warn(e6);
                        }
                    }
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (Exception e7) {
                            LOG.warn(e7);
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
            } catch (Throwable th) {
                if (sessionIdsTableStatement != null) {
                    try {
                        sessionIdsTableStatement.close();
                    } catch (Exception e8) {
                        LOG.warn(e8);
                    }
                }
                if (sessionsTableStatement != null) {
                    try {
                        sessionsTableStatement.close();
                    } catch (Exception e9) {
                        LOG.warn(e9);
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e10) {
                        LOG.warn(e10);
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e11) {
                        LOG.warn(e11);
                    }
                }
                throw th;
            }
        } catch (SQLException e12) {
            LOG.warn(e12);
        }
    }

    private String createCleanExpiredSessionsSql(String sql, Collection<String> expiredSessionIds) throws Exception {
        StringBuffer buff = new StringBuffer();
        buff.append(sql);
        buff.append("(");
        Iterator<String> itor = expiredSessionIds.iterator();
        while (itor.hasNext()) {
            buff.append("'" + itor.next() + "'");
            if (itor.hasNext()) {
                buff.append(",");
            }
        }
        buff.append(")");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cleaning expired sessions with: {}", buff);
        }
        return buff.toString();
    }

    private void initializeDatabase() throws Exception {
        if (this._datasource != null) {
            return;
        }
        if (this._jndiName != null) {
            InitialContext ic = new InitialContext();
            this._datasource = (DataSource) ic.lookup(this._jndiName);
        } else if (this._driver != null && this._connectionUrl != null) {
            DriverManager.registerDriver(this._driver);
        } else if (this._driverClassName != null && this._connectionUrl != null) {
            Class.forName(this._driverClassName);
        } else {
            throw new IllegalStateException("No database configured for sessions");
        }
    }
}
