package org.eclipse.jetty.security;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Credential;
/* loaded from: classes.dex */
public class PropertyUserStore extends AbstractLifeCycle {
    private static final Logger LOG = Log.getLogger(PropertyUserStore.class);
    private String _config;
    private Resource _configResource;
    private List<UserListener> _listeners;
    private Scanner _scanner;
    private int _refreshInterval = 0;
    private IdentityService _identityService = new DefaultIdentityService();
    private boolean _firstLoad = true;
    private final List<String> _knownUsers = new ArrayList();
    private final Map<String, UserIdentity> _knownUserIdentities = new HashMap();

    /* loaded from: classes.dex */
    public interface UserListener {
        void remove(String str);

        void update(String str, Credential credential, String[] strArr);
    }

    public String getConfig() {
        return this._config;
    }

    public void setConfig(String config) {
        this._config = config;
    }

    public UserIdentity getUserIdentity(String userName) {
        return this._knownUserIdentities.get(userName);
    }

    public Resource getConfigResource() throws IOException {
        if (this._configResource == null) {
            this._configResource = Resource.newResource(this._config);
        }
        return this._configResource;
    }

    public void setRefreshInterval(int msec) {
        this._refreshInterval = msec;
    }

    public int getRefreshInterval() {
        return this._refreshInterval;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadUsers() throws IOException {
        Iterator i$;
        Properties properties;
        if (this._config == null) {
            return;
        }
        int i = 0;
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Load " + this + " from " + this._config, new Object[0]);
        }
        Properties properties2 = new Properties();
        if (getConfigResource().exists()) {
            properties2.load(getConfigResource().getInputStream());
        }
        Set<String> known = new HashSet<>();
        Iterator i$2 = properties2.entrySet().iterator();
        while (i$2.hasNext()) {
            Map.Entry<Object, Object> entry = (Map.Entry) i$2.next();
            String username = ((String) entry.getKey()).trim();
            String credentials = ((String) entry.getValue()).trim();
            String roles = null;
            int c = credentials.indexOf(44);
            if (c > 0) {
                roles = credentials.substring(c + 1).trim();
                credentials = credentials.substring(i, c).trim();
            }
            if (username != null && username.length() > 0 && credentials != null && credentials.length() > 0) {
                String[] roleArray = IdentityService.NO_ROLES;
                if (roles != null && roles.length() > 0) {
                    roleArray = roles.split(",");
                }
                known.add(username);
                Credential credential = Credential.getCredential(credentials);
                Principal userPrincipal = new MappedLoginService.KnownUser(username, credential);
                Subject subject = new Subject();
                subject.getPrincipals().add(userPrincipal);
                subject.getPrivateCredentials().add(credential);
                if (roles != null) {
                    String[] arr$ = roleArray;
                    int len$ = arr$.length;
                    int i$3 = i;
                    while (true) {
                        int i$4 = i$3;
                        if (i$4 >= len$) {
                            break;
                        }
                        Iterator i$5 = i$2;
                        String role = arr$[i$4];
                        subject.getPrincipals().add(new MappedLoginService.RolePrincipal(role));
                        i$3 = i$4 + 1;
                        i$2 = i$5;
                        properties2 = properties2;
                        entry = entry;
                    }
                }
                i$ = i$2;
                properties = properties2;
                subject.setReadOnly();
                this._knownUserIdentities.put(username, this._identityService.newUserIdentity(subject, userPrincipal, roleArray));
                notifyUpdate(username, credential, roleArray);
            } else {
                i$ = i$2;
                properties = properties2;
            }
            i$2 = i$;
            properties2 = properties;
            i = 0;
        }
        synchronized (this._knownUsers) {
            if (!this._firstLoad) {
                for (String user : this._knownUsers) {
                    if (!known.contains(user)) {
                        this._knownUserIdentities.remove(user);
                        notifyRemove(user);
                    }
                }
            }
            this._knownUsers.clear();
            this._knownUsers.addAll(known);
        }
        this._firstLoad = false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        if (getRefreshInterval() > 0) {
            this._scanner = new Scanner();
            this._scanner.setScanInterval(getRefreshInterval());
            List<File> dirList = new ArrayList<>(1);
            dirList.add(getConfigResource().getFile().getParentFile());
            this._scanner.setScanDirs(dirList);
            this._scanner.setFilenameFilter(new FilenameFilter() { // from class: org.eclipse.jetty.security.PropertyUserStore.1
                @Override // java.io.FilenameFilter
                public boolean accept(File dir, String name) {
                    File f = new File(dir, name);
                    try {
                        if (f.compareTo(PropertyUserStore.this.getConfigResource().getFile()) != 0) {
                            return false;
                        }
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                }
            });
            this._scanner.addListener(new Scanner.BulkListener() { // from class: org.eclipse.jetty.security.PropertyUserStore.2
                @Override // org.eclipse.jetty.util.Scanner.BulkListener
                public void filesChanged(List<String> filenames) throws Exception {
                    if (filenames != null && !filenames.isEmpty() && filenames.size() == 1) {
                        Resource r = Resource.newResource(filenames.get(0));
                        if (r.getFile().equals(PropertyUserStore.this._configResource.getFile())) {
                            PropertyUserStore.this.loadUsers();
                        }
                    }
                }

                public String toString() {
                    return "PropertyUserStore$Scanner";
                }
            });
            this._scanner.setReportExistingFilesOnStartup(true);
            this._scanner.setRecursive(false);
            this._scanner.start();
            return;
        }
        loadUsers();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        if (this._scanner != null) {
            this._scanner.stop();
        }
        this._scanner = null;
    }

    private void notifyUpdate(String username, Credential credential, String[] roleArray) {
        if (this._listeners != null) {
            for (UserListener userListener : this._listeners) {
                userListener.update(username, credential, roleArray);
            }
        }
    }

    private void notifyRemove(String username) {
        if (this._listeners != null) {
            for (UserListener userListener : this._listeners) {
                userListener.remove(username);
            }
        }
    }

    public void registerUserListener(UserListener listener) {
        if (this._listeners == null) {
            this._listeners = new ArrayList();
        }
        this._listeners.add(listener);
    }
}
