package org.eclipse.jetty.util.statistic;

import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.util.Atomics;
/* loaded from: classes.dex */
public class CounterStatistic {
    protected final AtomicLong _max = new AtomicLong();
    protected final AtomicLong _curr = new AtomicLong();
    protected final AtomicLong _total = new AtomicLong();

    public void reset() {
        reset(0L);
    }

    public void reset(long value) {
        this._max.set(value);
        this._curr.set(value);
        this._total.set(0L);
    }

    public void add(long delta) {
        long value = this._curr.addAndGet(delta);
        if (delta > 0) {
            this._total.addAndGet(delta);
        }
        Atomics.updateMax(this._max, value);
    }

    public void subtract(long delta) {
        add(-delta);
    }

    public void increment() {
        add(1L);
    }

    public void decrement() {
        add(-1L);
    }

    public long getMax() {
        return this._max.get();
    }

    public long getCurrent() {
        return this._curr.get();
    }

    public long getTotal() {
        return this._total.get();
    }
}
