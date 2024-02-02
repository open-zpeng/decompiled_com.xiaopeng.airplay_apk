package org.eclipse.jetty.util.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ThreadPool;
/* loaded from: classes.dex */
public class QueuedThreadPool extends AbstractLifeCycle implements ThreadPool.SizedThreadPool, Executor, Dumpable {
    private static final Logger LOG = Log.getLogger(QueuedThreadPool.class);
    private boolean _daemon;
    private boolean _detailedDump;
    private BlockingQueue<Runnable> _jobs;
    private final Object _joinLock;
    private final AtomicLong _lastShrink;
    private int _maxIdleTimeMs;
    private int _maxQueued;
    private int _maxStopTime;
    private int _maxThreads;
    private int _minThreads;
    private String _name;
    private int _priority;
    private Runnable _runnable;
    private final ConcurrentHashSet<Thread> _threads;
    private final AtomicInteger _threadsIdle;
    private final AtomicInteger _threadsStarted;

    public QueuedThreadPool() {
        this._threadsStarted = new AtomicInteger();
        this._threadsIdle = new AtomicInteger();
        this._lastShrink = new AtomicLong();
        this._threads = new ConcurrentHashSet<>();
        this._joinLock = new Object();
        this._maxIdleTimeMs = 60000;
        this._maxThreads = 254;
        this._minThreads = 8;
        this._maxQueued = -1;
        this._priority = 5;
        this._daemon = false;
        this._maxStopTime = 100;
        this._detailedDump = false;
        this._runnable = new Runnable() { // from class: org.eclipse.jetty.util.thread.QueuedThreadPool.3
            /* JADX WARN: Code restructure failed: missing block: B:43:0x00f0, code lost:
                if (r1 == false) goto L58;
             */
            /* JADX WARN: Code restructure failed: missing block: B:53:0x0108, code lost:
                if (0 != 0) goto L56;
             */
            @Override // java.lang.Runnable
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct add '--show-bad-code' argument
            */
            public void run() {
                /*
                    Method dump skipped, instructions count: 315
                    To view this dump add '--comments-level debug' option
                */
                throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.thread.QueuedThreadPool.AnonymousClass3.run():void");
            }
        };
        this._name = "qtp" + super.hashCode();
    }

    public QueuedThreadPool(int maxThreads) {
        this();
        setMaxThreads(maxThreads);
    }

    public QueuedThreadPool(BlockingQueue<Runnable> jobQ) {
        this();
        this._jobs = jobQ;
        this._jobs.clear();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        this._threadsStarted.set(0);
        if (this._jobs == null) {
            this._jobs = this._maxQueued > 0 ? new ArrayBlockingQueue<>(this._maxQueued) : new BlockingArrayQueue<>(this._minThreads, this._minThreads);
        }
        int threads = this._threadsStarted.get();
        while (isRunning() && threads < this._minThreads) {
            startThread(threads);
            threads = this._threadsStarted.get();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        long start = System.currentTimeMillis();
        while (this._threadsStarted.get() > 0 && System.currentTimeMillis() - start < this._maxStopTime / 2) {
            Thread.sleep(1L);
        }
        this._jobs.clear();
        Runnable noop = new Runnable() { // from class: org.eclipse.jetty.util.thread.QueuedThreadPool.1
            @Override // java.lang.Runnable
            public void run() {
            }
        };
        int i = this._threadsIdle.get();
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            }
            this._jobs.offer(noop);
            i = i2;
        }
        Thread.yield();
        if (this._threadsStarted.get() > 0) {
            Iterator i$ = this._threads.iterator();
            while (i$.hasNext()) {
                Thread thread = i$.next();
                thread.interrupt();
            }
        }
        while (this._threadsStarted.get() > 0 && System.currentTimeMillis() - start < this._maxStopTime) {
            Thread.sleep(1L);
        }
        Thread.yield();
        int size = this._threads.size();
        if (size > 0) {
            LOG.warn(size + " threads could not be stopped", new Object[0]);
            if (size == 1 || LOG.isDebugEnabled()) {
                Iterator i$2 = this._threads.iterator();
                while (i$2.hasNext()) {
                    Thread unstopped = i$2.next();
                    LOG.info("Couldn't stop " + unstopped, new Object[0]);
                    StackTraceElement[] arr$ = unstopped.getStackTrace();
                    int len$ = arr$.length;
                    for (int i$3 = 0; i$3 < len$; i$3++) {
                        StackTraceElement element = arr$[i$3];
                        LOG.info(" at " + element, new Object[0]);
                    }
                }
            }
        }
        synchronized (this._joinLock) {
            this._joinLock.notifyAll();
        }
    }

    public void setDaemon(boolean daemon) {
        this._daemon = daemon;
    }

    public void setMaxIdleTimeMs(int maxIdleTimeMs) {
        this._maxIdleTimeMs = maxIdleTimeMs;
    }

    public void setMaxStopTimeMs(int stopTimeMs) {
        this._maxStopTime = stopTimeMs;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool
    public void setMaxThreads(int maxThreads) {
        this._maxThreads = maxThreads;
        if (this._minThreads > this._maxThreads) {
            this._minThreads = this._maxThreads;
        }
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool
    public void setMinThreads(int minThreads) {
        this._minThreads = minThreads;
        if (this._minThreads > this._maxThreads) {
            this._maxThreads = this._minThreads;
        }
        int threads = this._threadsStarted.get();
        while (isStarted() && threads < this._minThreads) {
            startThread(threads);
            threads = this._threadsStarted.get();
        }
    }

    public void setName(String name) {
        if (isRunning()) {
            throw new IllegalStateException("started");
        }
        this._name = name;
    }

    public void setThreadsPriority(int priority) {
        this._priority = priority;
    }

    public int getMaxQueued() {
        return this._maxQueued;
    }

    public void setMaxQueued(int max) {
        if (isRunning()) {
            throw new IllegalStateException("started");
        }
        this._maxQueued = max;
    }

    public int getMaxIdleTimeMs() {
        return this._maxIdleTimeMs;
    }

    public int getMaxStopTimeMs() {
        return this._maxStopTime;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool
    public int getMaxThreads() {
        return this._maxThreads;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool.SizedThreadPool
    public int getMinThreads() {
        return this._minThreads;
    }

    public String getName() {
        return this._name;
    }

    public int getThreadsPriority() {
        return this._priority;
    }

    public boolean isDaemon() {
        return this._daemon;
    }

    public boolean isDetailedDump() {
        return this._detailedDump;
    }

    public void setDetailedDump(boolean detailedDump) {
        this._detailedDump = detailedDump;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public boolean dispatch(Runnable job) {
        int threads;
        if (isRunning()) {
            int jobQ = this._jobs.size();
            int idle = getIdleThreads();
            if (this._jobs.offer(job)) {
                if ((idle == 0 || jobQ > idle) && (threads = this._threadsStarted.get()) < this._maxThreads) {
                    startThread(threads);
                }
                return true;
            }
        }
        LOG.debug("Dispatched {} to stopped {}", job, this);
        return false;
    }

    @Override // java.util.concurrent.Executor
    public void execute(Runnable job) {
        if (!dispatch(job)) {
            throw new RejectedExecutionException();
        }
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public void join() throws InterruptedException {
        synchronized (this._joinLock) {
            while (isRunning()) {
                this._joinLock.wait();
            }
        }
        while (isStopping()) {
            Thread.sleep(1L);
        }
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public int getThreads() {
        return this._threadsStarted.get();
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public int getIdleThreads() {
        return this._threadsIdle.get();
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public boolean isLowOnThreads() {
        return this._threadsStarted.get() == this._maxThreads && this._jobs.size() >= this._threadsIdle.get();
    }

    private boolean startThread(int threads) {
        int next = threads + 1;
        boolean started = false;
        if (!this._threadsStarted.compareAndSet(threads, next)) {
            return started;
        }
        try {
            Thread thread = newThread(this._runnable);
            thread.setDaemon(this._daemon);
            thread.setPriority(this._priority);
            thread.setName(this._name + "-" + thread.getId());
            this._threads.add(thread);
            thread.start();
            started = true;
            return started;
        } finally {
            if (!started) {
                this._threadsStarted.decrementAndGet();
            }
        }
    }

    protected Thread newThread(Runnable runnable) {
        return new Thread(runnable);
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public String dump() {
        return AggregateLifeCycle.dump(this);
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        List<Object> dump = new ArrayList<>(getMaxThreads());
        Iterator i$ = this._threads.iterator();
        while (i$.hasNext()) {
            final Thread thread = i$.next();
            final StackTraceElement[] trace = thread.getStackTrace();
            boolean inIdleJobPoll = false;
            if (trace != null) {
                int len$ = trace.length;
                int i$2 = 0;
                while (true) {
                    if (i$2 >= len$) {
                        break;
                    }
                    StackTraceElement t = trace[i$2];
                    if (!"idleJobPoll".equals(t.getMethodName())) {
                        i$2++;
                    } else {
                        inIdleJobPoll = true;
                        break;
                    }
                }
            }
            final boolean idle = inIdleJobPoll;
            if (this._detailedDump) {
                dump.add(new Dumpable() { // from class: org.eclipse.jetty.util.thread.QueuedThreadPool.2
                    @Override // org.eclipse.jetty.util.component.Dumpable
                    public void dump(Appendable out2, String indent2) throws IOException {
                        out2.append(String.valueOf(thread.getId())).append(' ').append(thread.getName()).append(' ').append(thread.getState().toString()).append(idle ? " IDLE" : "").append('\n');
                        if (!idle) {
                            AggregateLifeCycle.dump(out2, indent2, Arrays.asList(trace));
                        }
                    }

                    @Override // org.eclipse.jetty.util.component.Dumpable
                    public String dump() {
                        return null;
                    }
                });
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(thread.getId());
                sb.append(" ");
                sb.append(thread.getName());
                sb.append(" ");
                sb.append(thread.getState());
                sb.append(" @ ");
                sb.append(trace.length > 0 ? trace[0] : "???");
                sb.append(idle ? " IDLE" : "");
                dump.add(sb.toString());
            }
        }
        AggregateLifeCycle.dumpObject(out, this);
        AggregateLifeCycle.dump(out, indent, dump);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._name);
        sb.append("{");
        sb.append(getMinThreads());
        sb.append("<=");
        sb.append(getIdleThreads());
        sb.append("<=");
        sb.append(getThreads());
        sb.append("/");
        sb.append(getMaxThreads());
        sb.append(",");
        sb.append(this._jobs == null ? -1 : this._jobs.size());
        sb.append("}");
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Runnable idleJobPoll() throws InterruptedException {
        return this._jobs.poll(this._maxIdleTimeMs, TimeUnit.MILLISECONDS);
    }

    protected void runJob(Runnable job) {
        job.run();
    }

    protected BlockingQueue<Runnable> getQueue() {
        return this._jobs;
    }

    @Deprecated
    public boolean stopThread(long id) {
        Iterator i$ = this._threads.iterator();
        while (i$.hasNext()) {
            Thread thread = i$.next();
            if (thread.getId() == id) {
                thread.stop();
                return true;
            }
        }
        return false;
    }

    public boolean interruptThread(long id) {
        Iterator i$ = this._threads.iterator();
        while (i$.hasNext()) {
            Thread thread = i$.next();
            if (thread.getId() == id) {
                thread.interrupt();
                return true;
            }
        }
        return false;
    }

    public String dumpThread(long id) {
        Iterator i$ = this._threads.iterator();
        while (i$.hasNext()) {
            Thread thread = i$.next();
            if (thread.getId() == id) {
                StringBuilder buf = new StringBuilder();
                buf.append(thread.getId());
                buf.append(" ");
                buf.append(thread.getName());
                buf.append(" ");
                buf.append(thread.getState());
                buf.append(":\n");
                StackTraceElement[] arr$ = thread.getStackTrace();
                for (StackTraceElement element : arr$) {
                    buf.append("  at ");
                    buf.append(element.toString());
                    buf.append('\n');
                }
                return buf.toString();
            }
        }
        return null;
    }
}
