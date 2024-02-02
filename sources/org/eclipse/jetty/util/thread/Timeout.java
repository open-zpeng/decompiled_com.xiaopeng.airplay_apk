package org.eclipse.jetty.util.thread;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Timeout {
    private static final Logger LOG = Log.getLogger(Timeout.class);
    private long _duration;
    private Task _head;
    private Object _lock;
    private volatile long _now;

    public Timeout() {
        this._now = System.currentTimeMillis();
        this._head = new Task();
        this._lock = new Object();
        this._head._timeout = this;
    }

    public Timeout(Object lock) {
        this._now = System.currentTimeMillis();
        this._head = new Task();
        this._lock = lock;
        this._head._timeout = this;
    }

    public long getDuration() {
        return this._duration;
    }

    public void setDuration(long duration) {
        this._duration = duration;
    }

    public long setNow() {
        long currentTimeMillis = System.currentTimeMillis();
        this._now = currentTimeMillis;
        return currentTimeMillis;
    }

    public long getNow() {
        return this._now;
    }

    public void setNow(long now) {
        this._now = now;
    }

    public Task expired() {
        synchronized (this._lock) {
            long _expiry = this._now - this._duration;
            if (this._head._next != this._head) {
                Task task = this._head._next;
                if (task._timestamp > _expiry) {
                    return null;
                }
                task.unlink();
                task._expired = true;
                return task;
            }
            return null;
        }
    }

    public void tick() {
        Task task;
        long expiry = this._now - this._duration;
        while (true) {
            try {
                synchronized (this._lock) {
                    task = this._head._next;
                    if (task != this._head && task._timestamp <= expiry) {
                        task.unlink();
                        task._expired = true;
                        task.expire();
                    }
                    return;
                }
                task.expired();
            } catch (Throwable th) {
                LOG.warn(Log.EXCEPTION, th);
            }
        }
    }

    public void tick(long now) {
        this._now = now;
        tick();
    }

    public void schedule(Task task) {
        schedule(task, 0L);
    }

    public void schedule(Task task, long delay) {
        synchronized (this._lock) {
            if (task._timestamp != 0) {
                task.unlink();
                task._timestamp = 0L;
            }
            task._timeout = this;
            task._expired = false;
            task._delay = delay;
            task._timestamp = this._now + delay;
            Task last = this._head._prev;
            while (last != this._head && last._timestamp > task._timestamp) {
                last = last._prev;
            }
            last.link(task);
        }
    }

    public void cancelAll() {
        synchronized (this._lock) {
            Task task = this._head;
            Task task2 = this._head;
            Task task3 = this._head;
            task2._prev = task3;
            task._next = task3;
        }
    }

    public boolean isEmpty() {
        boolean z;
        synchronized (this._lock) {
            z = this._head._next == this._head;
        }
        return z;
    }

    public long getTimeToNext() {
        synchronized (this._lock) {
            if (this._head._next == this._head) {
                return -1L;
            }
            long to_next = (this._duration + this._head._next._timestamp) - this._now;
            long j = 0;
            if (to_next >= 0) {
                j = to_next;
            }
            return j;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        for (Task task = this._head._next; task != this._head; task = task._next) {
            buf.append("-->");
            buf.append(task);
        }
        return buf.toString();
    }

    /* loaded from: classes.dex */
    public static class Task {
        long _delay;
        Timeout _timeout;
        long _timestamp = 0;
        boolean _expired = false;
        Task _prev = this;
        Task _next = this;

        public long getTimestamp() {
            return this._timestamp;
        }

        public long getAge() {
            Timeout t = this._timeout;
            if (t != null) {
                long now = t._now;
                if (now != 0 && this._timestamp != 0) {
                    return now - this._timestamp;
                }
            }
            return 0L;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void unlink() {
            this._next._prev = this._prev;
            this._prev._next = this._next;
            this._prev = this;
            this._next = this;
            this._expired = false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void link(Task task) {
            Task next_next = this._next;
            this._next._prev = task;
            this._next = task;
            this._next._next = next_next;
            this._next._prev = this;
        }

        public void schedule(Timeout timer) {
            timer.schedule(this);
        }

        public void schedule(Timeout timer, long delay) {
            timer.schedule(this, delay);
        }

        public void reschedule() {
            Timeout timeout = this._timeout;
            if (timeout != null) {
                timeout.schedule(this, this._delay);
            }
        }

        public void cancel() {
            Timeout timeout = this._timeout;
            if (timeout != null) {
                synchronized (timeout._lock) {
                    unlink();
                    this._timestamp = 0L;
                }
            }
        }

        public boolean isExpired() {
            return this._expired;
        }

        public boolean isScheduled() {
            return this._next != this;
        }

        protected void expire() {
        }

        public void expired() {
        }
    }
}
