package org.eclipse.jetty.util.thread;
/* loaded from: classes.dex */
public interface ThreadPool {

    /* loaded from: classes.dex */
    public interface SizedThreadPool extends ThreadPool {
        int getMaxThreads();

        int getMinThreads();

        void setMaxThreads(int i);

        void setMinThreads(int i);
    }

    boolean dispatch(Runnable runnable);

    int getIdleThreads();

    int getThreads();

    boolean isLowOnThreads();

    void join() throws InterruptedException;
}
