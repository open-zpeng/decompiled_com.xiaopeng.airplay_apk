package org.eclipse.jetty.security;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Credential;
/* loaded from: classes.dex */
public class JDBCLoginService extends MappedLoginService {
    private static final Logger LOG = Log.getLogger(JDBCLoginService.class);
    private int _cacheTime;
    private Connection _con;
    private String _config;
    private String _jdbcDriver;
    private long _lastHashPurge;
    private String _password;
    private String _roleSql;
    private String _roleTableRoleField;
    private String _url;
    private String _userName;
    private String _userSql;
    private String _userTableKey;
    private String _userTablePasswordField;

    public JDBCLoginService() throws IOException {
    }

    public JDBCLoginService(String name) throws IOException {
        setName(name);
    }

    public JDBCLoginService(String name, String config) throws IOException {
        setName(name);
        setConfig(config);
    }

    public JDBCLoginService(String name, IdentityService identityService, String config) throws IOException {
        setName(name);
        setIdentityService(identityService);
        setConfig(config);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.security.MappedLoginService, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        Properties properties = new Properties();
        Resource resource = Resource.newResource(this._config);
        properties.load(resource.getInputStream());
        this._jdbcDriver = properties.getProperty("jdbcdriver");
        this._url = properties.getProperty("url");
        this._userName = properties.getProperty("username");
        this._password = properties.getProperty("password");
        String _userTable = properties.getProperty("usertable");
        this._userTableKey = properties.getProperty("usertablekey");
        String _userTableUserField = properties.getProperty("usertableuserfield");
        this._userTablePasswordField = properties.getProperty("usertablepasswordfield");
        String _roleTable = properties.getProperty("roletable");
        String _roleTableKey = properties.getProperty("roletablekey");
        this._roleTableRoleField = properties.getProperty("roletablerolefield");
        String _userRoleTable = properties.getProperty("userroletable");
        String _userRoleTableUserKey = properties.getProperty("userroletableuserkey");
        String _userRoleTableRoleKey = properties.getProperty("userroletablerolekey");
        this._cacheTime = new Integer(properties.getProperty("cachetime")).intValue();
        if (this._jdbcDriver == null || this._jdbcDriver.equals("") || this._url == null || this._url.equals("") || this._userName == null || this._userName.equals("") || this._password == null || this._cacheTime < 0) {
            LOG.warn("UserRealm " + getName() + " has not been properly configured", new Object[0]);
        }
        this._cacheTime *= 1000;
        this._lastHashPurge = 0L;
        this._userSql = "select " + this._userTableKey + "," + this._userTablePasswordField + " from " + _userTable + " where " + _userTableUserField + " = ?";
        this._roleSql = "select r." + this._roleTableRoleField + " from " + _roleTable + " r, " + _userRoleTable + " u where u." + _userRoleTableUserKey + " = ? and r." + _roleTableKey + " = u." + _userRoleTableRoleKey;
        Loader.loadClass(getClass(), this._jdbcDriver).newInstance();
        super.doStart();
    }

    public String getConfig() {
        return this._config;
    }

    public void setConfig(String config) {
        if (isRunning()) {
            throw new IllegalStateException("Running");
        }
        this._config = config;
    }

    public void connectDatabase() {
        try {
            Class.forName(this._jdbcDriver);
            this._con = DriverManager.getConnection(this._url, this._userName, this._password);
        } catch (ClassNotFoundException e) {
            Logger logger = LOG;
            logger.warn("UserRealm " + getName() + " could not connect to database; will try later", e);
        } catch (SQLException e2) {
            Logger logger2 = LOG;
            logger2.warn("UserRealm " + getName() + " could not connect to database; will try later", e2);
        }
    }

    @Override // org.eclipse.jetty.security.MappedLoginService, org.eclipse.jetty.security.LoginService
    public UserIdentity login(String username, Object credentials) {
        long now = System.currentTimeMillis();
        if (now - this._lastHashPurge > this._cacheTime || this._cacheTime == 0) {
            this._users.clear();
            this._lastHashPurge = now;
            closeConnection();
        }
        return super.login(username, credentials);
    }

    @Override // org.eclipse.jetty.security.MappedLoginService
    protected void loadUsers() {
    }

    @Override // org.eclipse.jetty.security.MappedLoginService
    protected UserIdentity loadUser(String username) {
        try {
            if (this._con == null) {
                connectDatabase();
            }
            if (this._con == null) {
                throw new SQLException("Can't connect to database");
            }
            PreparedStatement stat = this._con.prepareStatement(this._userSql);
            stat.setObject(1, username);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                int key = rs.getInt(this._userTableKey);
                String credentials = rs.getString(this._userTablePasswordField);
                stat.close();
                PreparedStatement stat2 = this._con.prepareStatement(this._roleSql);
                stat2.setInt(1, key);
                ResultSet rs2 = stat2.executeQuery();
                List<String> roles = new ArrayList<>();
                while (rs2.next()) {
                    roles.add(rs2.getString(this._roleTableRoleField));
                }
                stat2.close();
                return putUser(username, Credential.getCredential(credentials), (String[]) roles.toArray(new String[roles.size()]));
            }
            return null;
        } catch (SQLException e) {
            Logger logger = LOG;
            logger.warn("UserRealm " + getName() + " could not load user information from database", e);
            closeConnection();
            return null;
        }
    }

    private void closeConnection() {
        if (this._con != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Closing db connection for JDBCUserRealm", new Object[0]);
            }
            try {
                this._con.close();
            } catch (Exception e) {
                LOG.ignore(e);
            }
        }
        this._con = null;
    }
}
