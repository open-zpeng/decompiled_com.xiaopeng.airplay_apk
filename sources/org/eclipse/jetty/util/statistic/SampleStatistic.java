package org.eclipse.jetty.util.statistic;

import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.util.Atomics;
/* loaded from: classes.dex */
public class SampleStatistic {
    protected final AtomicLong _max = new AtomicLong();
    protected final AtomicLong _total = new AtomicLong();
    protected final AtomicLong _count = new AtomicLong();
    protected final AtomicLong _totalVariance100 = new AtomicLong();

    public void reset() {
        this._max.set(0L);
        this._total.set(0L);
        this._count.set(0L);
        this._totalVariance100.set(0L);
    }

    public void set(long sample) {
        long total = this._total.addAndGet(sample);
        long count = this._count.incrementAndGet();
        if (count > 1) {
            long mean10 = (total * 10) / count;
            long delta10 = (10 * sample) - mean10;
            this._totalVariance100.addAndGet(delta10 * delta10);
        }
        Atomics.updateMax(this._max, sample);
    }

    public long getMax() {
        return this._max.get();
    }

    public long getTotal() {
        return this._total.get();
    }

    public long getCount() {
        return this._count.get();
    }

    public double getMean() {
        return this._total.get() / this._count.get();
    }

    public double getVariance() {
        long variance100 = this._totalVariance100.get();
        long count = this._count.get();
        if (count > 1) {
            return (variance100 / 100.0d) / (count - 1);
        }
        return 0.0d;
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }
}
