package org.eclipse.jetty.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
/* loaded from: classes.dex */
public class Atomics {
    private Atomics() {
    }

    public static void updateMin(AtomicLong currentMin, long newValue) {
        long oldValue = currentMin.get();
        while (newValue < oldValue && !currentMin.compareAndSet(oldValue, newValue)) {
            oldValue = currentMin.get();
        }
    }

    public static void updateMax(AtomicLong currentMax, long newValue) {
        long oldValue = currentMax.get();
        while (newValue > oldValue && !currentMax.compareAndSet(oldValue, newValue)) {
            oldValue = currentMax.get();
        }
    }

    public static void updateMin(AtomicInteger currentMin, int newValue) {
        int oldValue = currentMin.get();
        while (newValue < oldValue && !currentMin.compareAndSet(oldValue, newValue)) {
            oldValue = currentMin.get();
        }
    }

    public static void updateMax(AtomicInteger currentMax, int newValue) {
        int oldValue = currentMax.get();
        while (newValue > oldValue && !currentMax.compareAndSet(oldValue, newValue)) {
            oldValue = currentMax.get();
        }
    }
}
