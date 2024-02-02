package org.eclipse.jetty.server.session;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class HashSessionManager extends AbstractSessionManager {
    private static int __id;
    static final Logger __log = SessionHandler.LOG;
    private TimerTask _saveTask;
    File _storeDir;
    private TimerTask _task;
    private Timer _timer;
    protected final ConcurrentMap<String, HashedSession> _sessions = new ConcurrentHashMap();
    private boolean _timerStop = false;
    long _scavengePeriodMs = 30000;
    long _savePeriodMs = 0;
    long _idleSavePeriodMs = 0;
    private boolean _lazyLoad = false;
    private volatile boolean _sessionsLoaded = false;
    private boolean _deleteUnrestorableSessions = false;

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        this._timerStop = false;
        ServletContext context = ContextHandler.getCurrentContext();
        if (context != null) {
            this._timer = (Timer) context.getAttribute("org.eclipse.jetty.server.session.timer");
        }
        if (this._timer == null) {
            this._timerStop = true;
            StringBuilder sb = new StringBuilder();
            sb.append("HashSessionScavenger-");
            int i = __id;
            __id = i + 1;
            sb.append(i);
            this._timer = new Timer(sb.toString(), true);
        }
        setScavengePeriod(getScavengePeriod());
        if (this._storeDir != null) {
            if (!this._storeDir.exists()) {
                this._storeDir.mkdirs();
            }
            if (!this._lazyLoad) {
                restoreSessions();
            }
        }
        setSavePeriod(getSavePeriod());
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        synchronized (this) {
            if (this._saveTask != null) {
                this._saveTask.cancel();
            }
            this._saveTask = null;
            if (this._task != null) {
                this._task.cancel();
            }
            this._task = null;
            if (this._timer != null && this._timerStop) {
                this._timer.cancel();
            }
            this._timer = null;
        }
        super.doStop();
        this._sessions.clear();
    }

    public int getScavengePeriod() {
        return (int) (this._scavengePeriodMs / 1000);
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    public int getSessions() {
        int sessions = super.getSessions();
        if (__log.isDebugEnabled() && this._sessions.size() != sessions) {
            Logger logger = __log;
            logger.warn("sessions: " + this._sessions.size() + "!=" + sessions, new Object[0]);
        }
        return sessions;
    }

    public int getIdleSavePeriod() {
        if (this._idleSavePeriodMs <= 0) {
            return 0;
        }
        return (int) (this._idleSavePeriodMs / 1000);
    }

    public void setIdleSavePeriod(int seconds) {
        this._idleSavePeriodMs = seconds * 1000;
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager, org.eclipse.jetty.server.SessionManager
    public void setMaxInactiveInterval(int seconds) {
        super.setMaxInactiveInterval(seconds);
        if (this._dftMaxIdleSecs > 0 && this._scavengePeriodMs > this._dftMaxIdleSecs * 1000) {
            setScavengePeriod((this._dftMaxIdleSecs + 9) / 10);
        }
    }

    public void setSavePeriod(int seconds) {
        long period = seconds * 1000;
        if (period < 0) {
            period = 0;
        }
        this._savePeriodMs = period;
        if (this._timer != null) {
            synchronized (this) {
                if (this._saveTask != null) {
                    this._saveTask.cancel();
                }
                if (this._savePeriodMs > 0 && this._storeDir != null) {
                    this._saveTask = new TimerTask() { // from class: org.eclipse.jetty.server.session.HashSessionManager.1
                        @Override // java.util.TimerTask, java.lang.Runnable
                        public void run() {
                            try {
                                HashSessionManager.this.saveSessions(true);
                            } catch (Exception e) {
                                HashSessionManager.__log.warn(e);
                            }
                        }
                    };
                    this._timer.schedule(this._saveTask, this._savePeriodMs, this._savePeriodMs);
                }
            }
        }
    }

    public int getSavePeriod() {
        if (this._savePeriodMs <= 0) {
            return 0;
        }
        return (int) (this._savePeriodMs / 1000);
    }

    public void setScavengePeriod(int seconds) {
        if (seconds == 0) {
            seconds = 60;
        }
        long old_period = this._scavengePeriodMs;
        long period = seconds * 1000;
        if (period > 60000) {
            period = 60000;
        }
        if (period < 1000) {
            period = 1000;
        }
        this._scavengePeriodMs = period;
        if (this._timer != null) {
            if (period != old_period || this._task == null) {
                synchronized (this) {
                    if (this._task != null) {
                        this._task.cancel();
                    }
                    this._task = new TimerTask() { // from class: org.eclipse.jetty.server.session.HashSessionManager.2
                        @Override // java.util.TimerTask, java.lang.Runnable
                        public void run() {
                            HashSessionManager.this.scavenge();
                        }
                    };
                    this._timer.schedule(this._task, this._scavengePeriodMs, this._scavengePeriodMs);
                }
            }
        }
    }

    protected void scavenge() {
        long now;
        if (isStopping() || isStopped()) {
            return;
        }
        Thread thread = Thread.currentThread();
        ClassLoader old_loader = thread.getContextClassLoader();
        try {
            if (this._loader != null) {
                thread.setContextClassLoader(this._loader);
            }
            now = System.currentTimeMillis();
        } finally {
            thread.setContextClassLoader(old_loader);
        }
        for (HashedSession session : this._sessions.values()) {
            long idleTime = session.getMaxInactiveInterval() * 1000;
            if (idleTime <= 0 || session.getAccessed() + idleTime >= now) {
                if (this._idleSavePeriodMs > 0 && session.getAccessed() + this._idleSavePeriodMs < now) {
                    try {
                        session.idle();
                    } catch (Exception e) {
                        Logger logger = __log;
                        logger.warn("Problem idling session " + session.getId(), e);
                    }
                }
            } else {
                try {
                    session.timeout();
                } catch (Exception e2) {
                    __log.warn("Problem scavenging sessions", e2);
                }
            }
            thread.setContextClassLoader(old_loader);
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected void addSession(AbstractSession session) {
        if (isRunning()) {
            this._sessions.put(session.getClusterId(), (HashedSession) session);
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    public AbstractSession getSession(String idInCluster) {
        if (this._lazyLoad && !this._sessionsLoaded) {
            try {
                restoreSessions();
            } catch (Exception e) {
                __log.warn(e);
            }
        }
        Map<String, HashedSession> sessions = this._sessions;
        if (sessions == null) {
            return null;
        }
        HashedSession session = sessions.get(idInCluster);
        if (session == null && this._lazyLoad) {
            session = restoreSession(idInCluster);
        }
        if (session == null) {
            return null;
        }
        if (this._idleSavePeriodMs != 0) {
            session.deIdle();
        }
        return session;
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected void invalidateSessions() throws Exception {
        ArrayList<HashedSession> sessions = new ArrayList<>(this._sessions.values());
        int loop = 100;
        while (sessions.size() > 0) {
            int loop2 = loop - 1;
            if (loop <= 0) {
                return;
            }
            if (isStopping() && this._storeDir != null && this._storeDir.exists() && this._storeDir.canWrite()) {
                Iterator i$ = sessions.iterator();
                while (i$.hasNext()) {
                    HashedSession session = i$.next();
                    session.save(false);
                    removeSession((AbstractSession) session, false);
                }
            } else {
                Iterator i$2 = sessions.iterator();
                while (i$2.hasNext()) {
                    i$2.next().invalidate();
                }
            }
            sessions = new ArrayList<>(this._sessions.values());
            loop = loop2;
        }
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected AbstractSession newSession(HttpServletRequest request) {
        return new HashedSession(this, request);
    }

    protected AbstractSession newSession(long created, long accessed, String clusterId) {
        return new HashedSession(this, created, accessed, clusterId);
    }

    @Override // org.eclipse.jetty.server.session.AbstractSessionManager
    protected boolean removeSession(String clusterId) {
        return this._sessions.remove(clusterId) != null;
    }

    public void setStoreDirectory(File dir) throws IOException {
        this._storeDir = dir.getCanonicalFile();
    }

    public File getStoreDirectory() {
        return this._storeDir;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this._lazyLoad = lazyLoad;
    }

    public boolean isLazyLoad() {
        return this._lazyLoad;
    }

    public boolean isDeleteUnrestorableSessions() {
        return this._deleteUnrestorableSessions;
    }

    public void setDeleteUnrestorableSessions(boolean deleteUnrestorableSessions) {
        this._deleteUnrestorableSessions = deleteUnrestorableSessions;
    }

    public void restoreSessions() throws Exception {
        this._sessionsLoaded = true;
        if (this._storeDir == null || !this._storeDir.exists()) {
            return;
        }
        if (!this._storeDir.canRead()) {
            Logger logger = __log;
            logger.warn("Unable to restore Sessions: Cannot read from Session storage directory " + this._storeDir.getAbsolutePath(), new Object[0]);
            return;
        }
        String[] files = this._storeDir.list();
        for (int i = 0; files != null && i < files.length; i++) {
            restoreSession(files[i]);
        }
    }

    protected synchronized HashedSession restoreSession(String idInCuster) {
        Logger logger;
        String str;
        Logger logger2;
        String str2;
        File file = new File(this._storeDir, idInCuster);
        FileInputStream in = null;
        Exception error = null;
        try {
        } catch (Exception e) {
            error = e;
            if (in != null) {
                IO.close((InputStream) in);
            }
            if (error != null) {
                if (isDeleteUnrestorableSessions() && file.exists() && file.getParentFile().equals(this._storeDir)) {
                    file.delete();
                    logger2 = __log;
                    str2 = "Deleting file for unrestorable session " + idInCuster;
                } else {
                    logger = __log;
                    str = "Problem restoring session " + idInCuster;
                }
            }
        }
        if (file.exists()) {
            in = new FileInputStream(file);
            HashedSession session = restoreSession(in, null);
            addSession(session, false);
            session.didActivate();
            IO.close((InputStream) in);
            if (0 == 0) {
                file.delete();
            } else if (isDeleteUnrestorableSessions() && file.exists() && file.getParentFile().equals(this._storeDir)) {
                file.delete();
                __log.warn("Deleting file for unrestorable session " + idInCuster, (Throwable) null);
            } else {
                __log.warn("Problem restoring session " + idInCuster, (Throwable) null);
            }
            return session;
        }
        if (0 != 0) {
            IO.close((InputStream) null);
        }
        if (0 != 0) {
            if (!isDeleteUnrestorableSessions() || !file.exists() || !file.getParentFile().equals(this._storeDir)) {
                logger = __log;
                str = "Problem restoring session " + idInCuster;
                logger.warn(str, error);
                return null;
            }
            file.delete();
            logger2 = __log;
            str2 = "Deleting file for unrestorable session " + idInCuster;
            logger2.warn(str2, error);
            return null;
        }
        file.delete();
        return null;
    }

    public void saveSessions(boolean reactivate) throws Exception {
        if (this._storeDir == null || !this._storeDir.exists()) {
            return;
        }
        if (!this._storeDir.canWrite()) {
            Logger logger = __log;
            logger.warn("Unable to save Sessions: Session persistence storage directory " + this._storeDir.getAbsolutePath() + " is not writeable", new Object[0]);
            return;
        }
        for (HashedSession session : this._sessions.values()) {
            session.save(true);
        }
    }

    public HashedSession restoreSession(InputStream is, HashedSession session) throws Exception {
        DataInputStream in = new DataInputStream(is);
        try {
            String clusterId = in.readUTF();
            in.readUTF();
            long created = in.readLong();
            long accessed = in.readLong();
            int requests = in.readInt();
            if (session == null) {
                session = (HashedSession) newSession(created, accessed, clusterId);
            }
            session.setRequests(requests);
            int size = in.readInt();
            if (size > 0) {
                ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(in);
                for (int i = 0; i < size; i++) {
                    String key = ois.readUTF();
                    Object value = ois.readObject();
                    session.setAttribute(key, value);
                }
                IO.close((InputStream) ois);
            }
            return session;
        } finally {
            IO.close((InputStream) in);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public class ClassLoadingObjectInputStream extends ObjectInputStream {
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
}
