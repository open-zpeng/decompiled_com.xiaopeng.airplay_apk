package org.eclipse.jetty.util.component;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class AbstractLifeCycle implements LifeCycle {
    public static final String FAILED = "FAILED";
    private static final Logger LOG = Log.getLogger(AbstractLifeCycle.class);
    public static final String RUNNING = "RUNNING";
    public static final String STARTED = "STARTED";
    public static final String STARTING = "STARTING";
    public static final String STOPPED = "STOPPED";
    public static final String STOPPING = "STOPPING";
    private final Object _lock = new Object();
    private final int __FAILED = -1;
    private final int __STOPPED = 0;
    private final int __STARTING = 1;
    private final int __STARTED = 2;
    private final int __STOPPING = 3;
    private volatile int _state = 0;
    protected final CopyOnWriteArrayList<LifeCycle.Listener> _listeners = new CopyOnWriteArrayList<>();

    /* JADX INFO: Access modifiers changed from: protected */
    public void doStart() throws Exception {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void doStop() throws Exception {
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public final void start() throws Exception {
        synchronized (this._lock) {
            try {
                try {
                    if (this._state != 2 && this._state != 1) {
                        setStarting();
                        doStart();
                        setStarted();
                    }
                } catch (Error e) {
                    setFailed(e);
                    throw e;
                } catch (Exception e2) {
                    setFailed(e2);
                    throw e2;
                }
            } finally {
            }
        }
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public final void stop() throws Exception {
        synchronized (this._lock) {
            try {
                try {
                    if (this._state != 3 && this._state != 0) {
                        setStopping();
                        doStop();
                        setStopped();
                    }
                } catch (Error e) {
                    setFailed(e);
                    throw e;
                } catch (Exception e2) {
                    setFailed(e2);
                    throw e2;
                }
            } finally {
            }
        }
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public boolean isRunning() {
        int state = this._state;
        return state == 2 || state == 1;
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public boolean isStarted() {
        return this._state == 2;
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public boolean isStarting() {
        return this._state == 1;
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public boolean isStopping() {
        return this._state == 3;
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public boolean isStopped() {
        return this._state == 0;
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public boolean isFailed() {
        return this._state == -1;
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public void addLifeCycleListener(LifeCycle.Listener listener) {
        this._listeners.add(listener);
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle
    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        this._listeners.remove(listener);
    }

    public String getState() {
        switch (this._state) {
            case -1:
                return FAILED;
            case 0:
                return STOPPED;
            case 1:
                return STARTING;
            case 2:
                return STARTED;
            case 3:
                return STOPPING;
            default:
                return null;
        }
    }

    public static String getState(LifeCycle lc) {
        return lc.isStarting() ? STARTING : lc.isStarted() ? STARTED : lc.isStopping() ? STOPPING : lc.isStopped() ? STOPPED : FAILED;
    }

    private void setStarted() {
        this._state = 2;
        LOG.debug("STARTED {}", this);
        Iterator i$ = this._listeners.iterator();
        while (i$.hasNext()) {
            LifeCycle.Listener listener = i$.next();
            listener.lifeCycleStarted(this);
        }
    }

    private void setStarting() {
        LOG.debug("starting {}", this);
        this._state = 1;
        Iterator i$ = this._listeners.iterator();
        while (i$.hasNext()) {
            LifeCycle.Listener listener = i$.next();
            listener.lifeCycleStarting(this);
        }
    }

    private void setStopping() {
        LOG.debug("stopping {}", this);
        this._state = 3;
        Iterator i$ = this._listeners.iterator();
        while (i$.hasNext()) {
            LifeCycle.Listener listener = i$.next();
            listener.lifeCycleStopping(this);
        }
    }

    private void setStopped() {
        this._state = 0;
        LOG.debug("{} {}", STOPPED, this);
        Iterator i$ = this._listeners.iterator();
        while (i$.hasNext()) {
            LifeCycle.Listener listener = i$.next();
            listener.lifeCycleStopped(this);
        }
    }

    private void setFailed(Throwable th) {
        this._state = -1;
        Logger logger = LOG;
        logger.warn("FAILED " + this + ": " + th, th);
        Iterator i$ = this._listeners.iterator();
        while (i$.hasNext()) {
            LifeCycle.Listener listener = i$.next();
            listener.lifeCycleFailure(this, th);
        }
    }

    /* loaded from: classes.dex */
    public static abstract class AbstractLifeCycleListener implements LifeCycle.Listener {
        @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
        public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        }

        @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
        public void lifeCycleStarted(LifeCycle event) {
        }

        @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
        public void lifeCycleStarting(LifeCycle event) {
        }

        @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
        public void lifeCycleStopped(LifeCycle event) {
        }

        @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
        public void lifeCycleStopping(LifeCycle event) {
        }
    }
}
