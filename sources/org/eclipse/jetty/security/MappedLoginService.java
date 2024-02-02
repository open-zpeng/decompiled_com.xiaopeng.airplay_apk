package org.eclipse.jetty.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.security.auth.Subject;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Credential;
/* loaded from: classes.dex */
public abstract class MappedLoginService extends AbstractLifeCycle implements LoginService {
    private static final Logger LOG = Log.getLogger(MappedLoginService.class);
    protected String _name;
    protected IdentityService _identityService = new DefaultIdentityService();
    protected final ConcurrentMap<String, UserIdentity> _users = new ConcurrentHashMap();

    /* loaded from: classes.dex */
    public interface UserPrincipal extends Principal, Serializable {
        boolean authenticate(Object obj);

        boolean isAuthenticated();
    }

    protected abstract UserIdentity loadUser(String str);

    protected abstract void loadUsers() throws IOException;

    @Override // org.eclipse.jetty.security.LoginService
    public String getName() {
        return this._name;
    }

    @Override // org.eclipse.jetty.security.LoginService
    public IdentityService getIdentityService() {
        return this._identityService;
    }

    public ConcurrentMap<String, UserIdentity> getUsers() {
        return this._users;
    }

    @Override // org.eclipse.jetty.security.LoginService
    public void setIdentityService(IdentityService identityService) {
        if (isRunning()) {
            throw new IllegalStateException("Running");
        }
        this._identityService = identityService;
    }

    public void setName(String name) {
        if (isRunning()) {
            throw new IllegalStateException("Running");
        }
        this._name = name;
    }

    public void setUsers(Map<String, UserIdentity> users) {
        if (isRunning()) {
            throw new IllegalStateException("Running");
        }
        this._users.clear();
        this._users.putAll(users);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        loadUsers();
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
    }

    @Override // org.eclipse.jetty.security.LoginService
    public void logout(UserIdentity identity) {
        LOG.debug("logout {}", identity);
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + this._name + "]";
    }

    protected synchronized UserIdentity putUser(String userName, Object info) {
        UserIdentity identity;
        if (info instanceof UserIdentity) {
            identity = (UserIdentity) info;
        } else {
            Credential credential = info instanceof Credential ? (Credential) info : Credential.getCredential(info.toString());
            Principal userPrincipal = new KnownUser(userName, credential);
            Subject subject = new Subject();
            subject.getPrincipals().add(userPrincipal);
            subject.getPrivateCredentials().add(credential);
            subject.setReadOnly();
            identity = this._identityService.newUserIdentity(subject, userPrincipal, IdentityService.NO_ROLES);
        }
        this._users.put(userName, identity);
        return identity;
    }

    public synchronized UserIdentity putUser(String userName, Credential credential, String[] roles) {
        UserIdentity identity;
        Principal userPrincipal = new KnownUser(userName, credential);
        Subject subject = new Subject();
        subject.getPrincipals().add(userPrincipal);
        subject.getPrivateCredentials().add(credential);
        if (roles != null) {
            for (String role : roles) {
                subject.getPrincipals().add(new RolePrincipal(role));
            }
        }
        subject.setReadOnly();
        identity = this._identityService.newUserIdentity(subject, userPrincipal, roles);
        this._users.put(userName, identity);
        return identity;
    }

    public void removeUser(String username) {
        this._users.remove(username);
    }

    public UserIdentity login(String username, Object credentials) {
        UserIdentity user = this._users.get(username);
        if (user == null) {
            user = loadUser(username);
        }
        if (user != null) {
            UserPrincipal principal = (UserPrincipal) user.getUserPrincipal();
            if (principal.authenticate(credentials)) {
                return user;
            }
            return null;
        }
        return null;
    }

    @Override // org.eclipse.jetty.security.LoginService
    public boolean validate(UserIdentity user) {
        return this._users.containsKey(user.getUserPrincipal().getName()) || loadUser(user.getUserPrincipal().getName()) != null;
    }

    /* loaded from: classes.dex */
    public static class RolePrincipal implements Principal, Serializable {
        private static final long serialVersionUID = 2998397924051854402L;
        private final String _roleName;

        public RolePrincipal(String name) {
            this._roleName = name;
        }

        @Override // java.security.Principal
        public String getName() {
            return this._roleName;
        }
    }

    /* loaded from: classes.dex */
    public static class Anonymous implements UserPrincipal, Serializable {
        private static final long serialVersionUID = 1097640442553284845L;

        @Override // org.eclipse.jetty.security.MappedLoginService.UserPrincipal
        public boolean isAuthenticated() {
            return false;
        }

        @Override // java.security.Principal
        public String getName() {
            return "Anonymous";
        }

        @Override // org.eclipse.jetty.security.MappedLoginService.UserPrincipal
        public boolean authenticate(Object credentials) {
            return false;
        }
    }

    /* loaded from: classes.dex */
    public static class KnownUser implements UserPrincipal, Serializable {
        private static final long serialVersionUID = -6226920753748399662L;
        private final Credential _credential;
        private final String _name;

        public KnownUser(String name, Credential credential) {
            this._name = name;
            this._credential = credential;
        }

        @Override // org.eclipse.jetty.security.MappedLoginService.UserPrincipal
        public boolean authenticate(Object credentials) {
            return this._credential != null && this._credential.check(credentials);
        }

        @Override // java.security.Principal
        public String getName() {
            return this._name;
        }

        @Override // org.eclipse.jetty.security.MappedLoginService.UserPrincipal
        public boolean isAuthenticated() {
            return true;
        }

        @Override // java.security.Principal
        public String toString() {
            return this._name;
        }
    }
}
