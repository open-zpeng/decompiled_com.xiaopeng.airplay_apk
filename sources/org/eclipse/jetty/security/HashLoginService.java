package org.eclipse.jetty.security;

import java.io.IOException;
import org.eclipse.jetty.security.PropertyUserStore;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Credential;
/* loaded from: classes.dex */
public class HashLoginService extends MappedLoginService implements PropertyUserStore.UserListener {
    private static final Logger LOG = Log.getLogger(HashLoginService.class);
    private String _config;
    private Resource _configResource;
    private PropertyUserStore _propertyUserStore;
    private int _refreshInterval = 0;
    private Scanner _scanner;

    public HashLoginService() {
    }

    public HashLoginService(String name) {
        setName(name);
    }

    public HashLoginService(String name, String config) {
        setName(name);
        setConfig(config);
    }

    public String getConfig() {
        return this._config;
    }

    public void getConfig(String config) {
        this._config = config;
    }

    public Resource getConfigResource() {
        return this._configResource;
    }

    public void setConfig(String config) {
        this._config = config;
    }

    public void setRefreshInterval(int msec) {
        this._refreshInterval = msec;
    }

    public int getRefreshInterval() {
        return this._refreshInterval;
    }

    @Override // org.eclipse.jetty.security.MappedLoginService
    protected UserIdentity loadUser(String username) {
        return null;
    }

    @Override // org.eclipse.jetty.security.MappedLoginService
    public void loadUsers() throws IOException {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.security.MappedLoginService, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        if (this._propertyUserStore == null) {
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("doStart: Starting new PropertyUserStore. PropertiesFile: " + this._config + " refreshInterval: " + this._refreshInterval, new Object[0]);
            }
            this._propertyUserStore = new PropertyUserStore();
            this._propertyUserStore.setRefreshInterval(this._refreshInterval);
            this._propertyUserStore.setConfig(this._config);
            this._propertyUserStore.registerUserListener(this);
            this._propertyUserStore.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.security.MappedLoginService, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        if (this._scanner != null) {
            this._scanner.stop();
        }
        this._scanner = null;
    }

    @Override // org.eclipse.jetty.security.PropertyUserStore.UserListener
    public void update(String userName, Credential credential, String[] roleArray) {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("update: " + userName + " Roles: " + roleArray.length, new Object[0]);
        }
        putUser(userName, credential, roleArray);
    }

    @Override // org.eclipse.jetty.security.PropertyUserStore.UserListener
    public void remove(String userName) {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("remove: " + userName, new Object[0]);
        }
        removeUser(userName);
    }
}
