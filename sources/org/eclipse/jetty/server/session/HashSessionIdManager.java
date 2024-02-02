package org.eclipse.jetty.server.session;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
/* loaded from: classes.dex */
public class HashSessionIdManager extends AbstractSessionIdManager {
    private final Map<String, Set<WeakReference<HttpSession>>> _sessions;

    public HashSessionIdManager() {
        this._sessions = new HashMap();
    }

    public HashSessionIdManager(Random random) {
        super(random);
        this._sessions = new HashMap();
    }

    public Collection<String> getSessions() {
        return Collections.unmodifiableCollection(this._sessions.keySet());
    }

    public Collection<HttpSession> getSession(String id) {
        ArrayList<HttpSession> sessions = new ArrayList<>();
        Set<WeakReference<HttpSession>> refs = this._sessions.get(id);
        if (refs != null) {
            for (WeakReference<HttpSession> ref : refs) {
                HttpSession session = ref.get();
                if (session != null) {
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public String getNodeId(String clusterId, HttpServletRequest request) {
        String worker = request == null ? null : (String) request.getAttribute("org.eclipse.jetty.ajp.JVMRoute");
        if (worker != null) {
            return clusterId + '.' + worker;
        } else if (this._workerName != null) {
            return clusterId + '.' + this._workerName;
        } else {
            return clusterId;
        }
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public String getClusterId(String nodeId) {
        int dot = nodeId.lastIndexOf(46);
        return dot > 0 ? nodeId.substring(0, dot) : nodeId;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.session.AbstractSessionIdManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.session.AbstractSessionIdManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._sessions.clear();
        super.doStop();
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public boolean idInUse(String id) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this._sessions.containsKey(id);
        }
        return containsKey;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public void addSession(HttpSession session) {
        String id = getClusterId(session.getId());
        WeakReference<HttpSession> ref = new WeakReference<>(session);
        synchronized (this) {
            Set<WeakReference<HttpSession>> sessions = this._sessions.get(id);
            if (sessions == null) {
                sessions = new HashSet();
                this._sessions.put(id, sessions);
            }
            sessions.add(ref);
        }
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public void removeSession(HttpSession session) {
        String id = getClusterId(session.getId());
        synchronized (this) {
            Collection<WeakReference<HttpSession>> sessions = this._sessions.get(id);
            if (sessions != null) {
                Iterator<WeakReference<HttpSession>> iter = sessions.iterator();
                while (true) {
                    if (!iter.hasNext()) {
                        break;
                    }
                    WeakReference<HttpSession> ref = iter.next();
                    HttpSession s = ref.get();
                    if (s == null) {
                        iter.remove();
                    } else if (s == session) {
                        iter.remove();
                        break;
                    }
                }
                if (sessions.isEmpty()) {
                    this._sessions.remove(id);
                }
            }
        }
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public void invalidateAll(String id) {
        Collection<WeakReference<HttpSession>> sessions;
        synchronized (this) {
            sessions = this._sessions.remove(id);
        }
        if (sessions != null) {
            for (WeakReference<HttpSession> ref : sessions) {
                AbstractSession session = (AbstractSession) ref.get();
                if (session != null && session.isValid()) {
                    session.invalidate();
                }
            }
            sessions.clear();
        }
    }
}
