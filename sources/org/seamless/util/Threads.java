package org.seamless.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
/* loaded from: classes.dex */
public class Threads {
    public static ThreadGroup getRootThreadGroup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (true) {
            ThreadGroup ptg = tg.getParent();
            if (ptg != null) {
                tg = ptg;
            } else {
                return tg;
            }
        }
    }

    public static Thread[] getAllThreads() {
        Thread[] threads;
        int n;
        ThreadGroup root = getRootThreadGroup();
        ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return (Thread[]) Arrays.copyOf(threads, n);
    }

    public static Thread getThread(long id) {
        Thread[] threads = getAllThreads();
        for (Thread thread : threads) {
            if (thread.getId() == id) {
                return thread;
            }
        }
        return null;
    }
}
