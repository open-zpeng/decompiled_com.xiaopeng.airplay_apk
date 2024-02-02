package org.eclipse.jetty.util.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ExecutorThreadPool extends AbstractLifeCycle implements ThreadPool, LifeCycle {
    private static final Logger LOG = Log.getLogger(ExecutorThreadPool.class);
    private final ExecutorService _executor;

    public ExecutorThreadPool(ExecutorService executor) {
        this._executor = executor;
    }

    public ExecutorThreadPool() {
        this(new ThreadPoolExecutor(CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE, CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue()));
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public ExecutorThreadPool(int r17) {
        /*
            r16 = this;
            r0 = r17
            if (r0 >= 0) goto L18
            java.util.concurrent.ThreadPoolExecutor r8 = new java.util.concurrent.ThreadPoolExecutor
            r2 = 256(0x100, float:3.59E-43)
            r3 = 256(0x100, float:3.59E-43)
            r4 = 60
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.SECONDS
            java.util.concurrent.LinkedBlockingQueue r7 = new java.util.concurrent.LinkedBlockingQueue
            r7.<init>()
            r1 = r8
            r1.<init>(r2, r3, r4, r6, r7)
            goto L41
        L18:
            if (r0 != 0) goto L2e
            java.util.concurrent.ThreadPoolExecutor r1 = new java.util.concurrent.ThreadPoolExecutor
            r10 = 32
            r11 = 256(0x100, float:3.59E-43)
            r12 = 60
            java.util.concurrent.TimeUnit r14 = java.util.concurrent.TimeUnit.SECONDS
            java.util.concurrent.SynchronousQueue r15 = new java.util.concurrent.SynchronousQueue
            r15.<init>()
            r9 = r1
            r9.<init>(r10, r11, r12, r14, r15)
            goto L41
        L2e:
            java.util.concurrent.ThreadPoolExecutor r1 = new java.util.concurrent.ThreadPoolExecutor
            r3 = 32
            r4 = 256(0x100, float:3.59E-43)
            r5 = 60
            java.util.concurrent.TimeUnit r7 = java.util.concurrent.TimeUnit.SECONDS
            java.util.concurrent.ArrayBlockingQueue r8 = new java.util.concurrent.ArrayBlockingQueue
            r8.<init>(r0)
            r2 = r1
            r2.<init>(r3, r4, r5, r7, r8)
        L41:
            r2 = r16
            r2.<init>(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.thread.ExecutorThreadPool.<init>(int):void");
    }

    public ExecutorThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS);
    }

    public ExecutorThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue());
    }

    public ExecutorThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue));
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public boolean dispatch(Runnable job) {
        try {
            this._executor.execute(job);
            return true;
        } catch (RejectedExecutionException e) {
            LOG.warn(e);
            return false;
        }
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public int getIdleThreads() {
        if (this._executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) this._executor;
            return tpe.getPoolSize() - tpe.getActiveCount();
        }
        return -1;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public int getThreads() {
        if (this._executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) this._executor;
            return tpe.getPoolSize();
        }
        return -1;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public boolean isLowOnThreads() {
        if (this._executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) this._executor;
            return tpe.getPoolSize() == tpe.getMaximumPoolSize() && tpe.getQueue().size() >= tpe.getPoolSize() - tpe.getActiveCount();
        }
        return false;
    }

    @Override // org.eclipse.jetty.util.thread.ThreadPool
    public void join() throws InterruptedException {
        this._executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        super.doStop();
        this._executor.shutdownNow();
    }
}
