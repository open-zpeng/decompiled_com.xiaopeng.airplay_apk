package org.eclipse.jetty.server.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class AbstractSession implements AbstractSessionManager.SessionIf {
    static final Logger LOG = SessionHandler.LOG;
    private long _accessed;
    private final Map<String, Object> _attributes;
    private final String _clusterId;
    private long _cookieSet;
    private final long _created;
    private boolean _doInvalidate;
    private boolean _idChanged;
    private boolean _invalid;
    private long _lastAccessed;
    private final AbstractSessionManager _manager;
    private long _maxIdleMs;
    private boolean _newSession;
    private final String _nodeId;
    private int _requests;

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractSession(AbstractSessionManager abstractSessionManager, HttpServletRequest request) {
        this._attributes = new HashMap();
        this._manager = abstractSessionManager;
        this._newSession = true;
        this._created = System.currentTimeMillis();
        this._clusterId = this._manager._sessionIdManager.newSessionId(request, this._created);
        this._nodeId = this._manager._sessionIdManager.getNodeId(this._clusterId, request);
        this._accessed = this._created;
        this._lastAccessed = this._created;
        this._requests = 1;
        this._maxIdleMs = this._manager._dftMaxIdleSecs > 0 ? this._manager._dftMaxIdleSecs * 1000 : -1L;
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("new session & id " + this._nodeId + " " + this._clusterId, new Object[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractSession(AbstractSessionManager abstractSessionManager, long created, long accessed, String clusterId) {
        this._attributes = new HashMap();
        this._manager = abstractSessionManager;
        this._created = created;
        this._clusterId = clusterId;
        this._nodeId = this._manager._sessionIdManager.getNodeId(this._clusterId, null);
        this._accessed = accessed;
        this._lastAccessed = accessed;
        this._requests = 1;
        this._maxIdleMs = this._manager._dftMaxIdleSecs > 0 ? this._manager._dftMaxIdleSecs * 1000 : -1L;
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("new session " + this._nodeId + " " + this._clusterId, new Object[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void checkValid() throws IllegalStateException {
        if (this._invalid) {
            throw new IllegalStateException();
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager.SessionIf
    public AbstractSession getSession() {
        return this;
    }

    public long getAccessed() {
        long j;
        synchronized (this) {
            j = this._accessed;
        }
        return j;
    }

    @Override // javax.servlet.http.HttpSession
    public Object getAttribute(String name) {
        Object obj;
        synchronized (this) {
            checkValid();
            obj = this._attributes.get(name);
        }
        return obj;
    }

    public int getAttributes() {
        int size;
        synchronized (this) {
            checkValid();
            size = this._attributes.size();
        }
        return size;
    }

    @Override // javax.servlet.http.HttpSession
    public Enumeration<String> getAttributeNames() {
        Enumeration<String> enumeration;
        synchronized (this) {
            checkValid();
            List<String> names = this._attributes == null ? Collections.EMPTY_LIST : new ArrayList<>(this._attributes.keySet());
            enumeration = Collections.enumeration(names);
        }
        return enumeration;
    }

    public Set<String> getNames() {
        HashSet hashSet;
        synchronized (this) {
            hashSet = new HashSet(this._attributes.keySet());
        }
        return hashSet;
    }

    public long getCookieSetTime() {
        return this._cookieSet;
    }

    @Override // javax.servlet.http.HttpSession
    public long getCreationTime() throws IllegalStateException {
        return this._created;
    }

    @Override // javax.servlet.http.HttpSession
    public String getId() throws IllegalStateException {
        return this._manager._nodeIdInSessionId ? this._nodeId : this._clusterId;
    }

    public String getNodeId() {
        return this._nodeId;
    }

    public String getClusterId() {
        return this._clusterId;
    }

    @Override // javax.servlet.http.HttpSession
    public long getLastAccessedTime() throws IllegalStateException {
        checkValid();
        return this._lastAccessed;
    }

    public void setLastAccessedTime(long time) {
        this._lastAccessed = time;
    }

    @Override // javax.servlet.http.HttpSession
    public int getMaxInactiveInterval() {
        return (int) (this._maxIdleMs / 1000);
    }

    @Override // javax.servlet.http.HttpSession
    public ServletContext getServletContext() {
        return this._manager._context;
    }

    @Override // javax.servlet.http.HttpSession
    @Deprecated
    public HttpSessionContext getSessionContext() throws IllegalStateException {
        checkValid();
        return AbstractSessionManager.__nullSessionContext;
    }

    @Override // javax.servlet.http.HttpSession
    @Deprecated
    public Object getValue(String name) throws IllegalStateException {
        return getAttribute(name);
    }

    @Override // javax.servlet.http.HttpSession
    @Deprecated
    public String[] getValueNames() throws IllegalStateException {
        synchronized (this) {
            checkValid();
            if (this._attributes == null) {
                return new String[0];
            }
            String[] a = new String[this._attributes.size()];
            return (String[]) this._attributes.keySet().toArray(a);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Map<String, Object> getAttributeMap() {
        return this._attributes;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void addAttributes(Map<String, Object> map) {
        this._attributes.putAll(map);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean access(long time) {
        synchronized (this) {
            if (this._invalid) {
                return false;
            }
            this._newSession = false;
            this._lastAccessed = this._accessed;
            this._accessed = time;
            if (this._maxIdleMs > 0 && this._lastAccessed > 0 && this._lastAccessed + this._maxIdleMs < time) {
                invalidate();
                return false;
            }
            this._requests++;
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void complete() {
        synchronized (this) {
            this._requests--;
            if (this._doInvalidate && this._requests <= 0) {
                doInvalidate();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void timeout() throws IllegalStateException {
        this._manager.removeSession(this, true);
        boolean do_invalidate = false;
        synchronized (this) {
            if (!this._invalid) {
                if (this._requests <= 0) {
                    do_invalidate = true;
                } else {
                    this._doInvalidate = true;
                }
            }
        }
        if (do_invalidate) {
            doInvalidate();
        }
    }

    @Override // javax.servlet.http.HttpSession
    public void invalidate() throws IllegalStateException {
        this._manager.removeSession(this, true);
        doInvalidate();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void doInvalidate() throws IllegalStateException {
        try {
            LOG.debug("invalidate {}", this._clusterId);
            if (isValid()) {
                clearAttributes();
            }
            synchronized (this) {
                this._invalid = true;
            }
        } catch (Throwable th) {
            synchronized (this) {
                this._invalid = true;
                throw th;
            }
        }
    }

    public void clearAttributes() {
        ArrayList<String> keys;
        Object value;
        while (this._attributes != null && this._attributes.size() > 0) {
            synchronized (this) {
                keys = new ArrayList<>(this._attributes.keySet());
            }
            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                synchronized (this) {
                    value = doPutOrRemove(key, null);
                }
                unbindValue(key, value);
                this._manager.doSessionAttributeListeners(this, key, value, null);
            }
        }
        if (this._attributes != null) {
            this._attributes.clear();
        }
    }

    public boolean isIdChanged() {
        return this._idChanged;
    }

    @Override // javax.servlet.http.HttpSession
    public boolean isNew() throws IllegalStateException {
        checkValid();
        return this._newSession;
    }

    @Override // javax.servlet.http.HttpSession
    @Deprecated
    public void putValue(String name, Object value) throws IllegalStateException {
        setAttribute(name, value);
    }

    @Override // javax.servlet.http.HttpSession
    public void removeAttribute(String name) {
        setAttribute(name, null);
    }

    @Override // javax.servlet.http.HttpSession
    @Deprecated
    public void removeValue(String name) throws IllegalStateException {
        removeAttribute(name);
    }

    protected Object doPutOrRemove(String name, Object value) {
        return value == null ? this._attributes.remove(name) : this._attributes.put(name, value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Object doGet(String name) {
        return this._attributes.get(name);
    }

    @Override // javax.servlet.http.HttpSession
    public void setAttribute(String name, Object value) {
        Object old;
        synchronized (this) {
            checkValid();
            old = doPutOrRemove(name, value);
        }
        if (value == null || !value.equals(old)) {
            if (old != null) {
                unbindValue(name, old);
            }
            if (value != null) {
                bindValue(name, value);
            }
            this._manager.doSessionAttributeListeners(this, name, old, value);
        }
    }

    public void setIdChanged(boolean changed) {
        this._idChanged = changed;
    }

    @Override // javax.servlet.http.HttpSession
    public void setMaxInactiveInterval(int secs) {
        this._maxIdleMs = secs * 1000;
    }

    public String toString() {
        return getClass().getName() + ":" + getId() + "@" + hashCode();
    }

    public void bindValue(String name, Object value) {
        if (value != null && (value instanceof HttpSessionBindingListener)) {
            ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name));
        }
    }

    public boolean isValid() {
        return !this._invalid;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void cookieSet() {
        synchronized (this) {
            this._cookieSet = this._accessed;
        }
    }

    public int getRequests() {
        int i;
        synchronized (this) {
            i = this._requests;
        }
        return i;
    }

    public void setRequests(int requests) {
        synchronized (this) {
            this._requests = requests;
        }
    }

    public void unbindValue(String name, Object value) {
        if (value != null && (value instanceof HttpSessionBindingListener)) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name));
        }
    }

    public void willPassivate() {
        synchronized (this) {
            HttpSessionEvent event = new HttpSessionEvent(this);
            for (Object value : this._attributes.values()) {
                if (value instanceof HttpSessionActivationListener) {
                    HttpSessionActivationListener listener = (HttpSessionActivationListener) value;
                    listener.sessionWillPassivate(event);
                }
            }
        }
    }

    public void didActivate() {
        synchronized (this) {
            HttpSessionEvent event = new HttpSessionEvent(this);
            for (Object value : this._attributes.values()) {
                if (value instanceof HttpSessionActivationListener) {
                    HttpSessionActivationListener listener = (HttpSessionActivationListener) value;
                    listener.sessionDidActivate(event);
                }
            }
        }
    }
}
