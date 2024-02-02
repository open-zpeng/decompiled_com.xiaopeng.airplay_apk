package org.eclipse.jetty.io.nio;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public abstract class SelectorManager extends AbstractLifeCycle implements Dumpable {
    private long _lowResourcesConnections;
    private int _lowResourcesMaxIdleTime;
    private int _maxIdleTime;
    private SelectSet[] _selectSet;
    public static final Logger LOG = Log.getLogger("org.eclipse.jetty.io.nio");
    private static final int __MONITOR_PERIOD = Integer.getInteger("org.eclipse.jetty.io.nio.MONITOR_PERIOD", 1000).intValue();
    private static final int __MAX_SELECTS = Integer.getInteger("org.eclipse.jetty.io.nio.MAX_SELECTS", 100000).intValue();
    private static final int __BUSY_PAUSE = Integer.getInteger("org.eclipse.jetty.io.nio.BUSY_PAUSE", 50).intValue();
    private static final int __IDLE_TICK = Integer.getInteger("org.eclipse.jetty.io.nio.IDLE_TICK", 400).intValue();
    private int _selectSets = 1;
    private volatile int _set = 0;
    private boolean _deferringInterestedOps0 = true;
    private int _selectorPriorityDelta = 0;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface ChangeTask extends Runnable {
    }

    public abstract boolean dispatch(Runnable runnable);

    protected abstract void endPointClosed(SelectChannelEndPoint selectChannelEndPoint);

    protected abstract void endPointOpened(SelectChannelEndPoint selectChannelEndPoint);

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract void endPointUpgraded(ConnectedEndPoint connectedEndPoint, Connection connection);

    public abstract AsyncConnection newConnection(SocketChannel socketChannel, AsyncEndPoint asyncEndPoint, Object obj);

    protected abstract SelectChannelEndPoint newEndPoint(SocketChannel socketChannel, SelectSet selectSet, SelectionKey selectionKey) throws IOException;

    public void setMaxIdleTime(long maxIdleTime) {
        this._maxIdleTime = (int) maxIdleTime;
    }

    public void setSelectSets(int selectSets) {
        long lrc = this._lowResourcesConnections * this._selectSets;
        this._selectSets = selectSets;
        this._lowResourcesConnections = lrc / this._selectSets;
    }

    public long getMaxIdleTime() {
        return this._maxIdleTime;
    }

    public int getSelectSets() {
        return this._selectSets;
    }

    public SelectSet getSelectSet(int i) {
        return this._selectSet[i];
    }

    public void register(SocketChannel channel, Object att) {
        int s = this._set;
        this._set = s + 1;
        if (s < 0) {
            s = -s;
        }
        int s2 = s % this._selectSets;
        SelectSet[] sets = this._selectSet;
        if (sets != null) {
            SelectSet set = sets[s2];
            set.addChange(channel, att);
            set.wakeup();
        }
    }

    public void register(SocketChannel channel) {
        int s = this._set;
        this._set = s + 1;
        if (s < 0) {
            s = -s;
        }
        int s2 = s % this._selectSets;
        SelectSet[] sets = this._selectSet;
        if (sets != null) {
            SelectSet set = sets[s2];
            set.addChange(channel);
            set.wakeup();
        }
    }

    public void register(ServerSocketChannel acceptChannel) {
        int s = this._set;
        this._set = s + 1;
        if (s < 0) {
            s = -s;
        }
        SelectSet set = this._selectSet[s % this._selectSets];
        set.addChange(acceptChannel);
        set.wakeup();
    }

    public int getSelectorPriorityDelta() {
        return this._selectorPriorityDelta;
    }

    public void setSelectorPriorityDelta(int delta) {
        this._selectorPriorityDelta = delta;
    }

    public long getLowResourcesConnections() {
        return this._lowResourcesConnections * this._selectSets;
    }

    public void setLowResourcesConnections(long lowResourcesConnections) {
        this._lowResourcesConnections = ((this._selectSets + lowResourcesConnections) - 1) / this._selectSets;
    }

    public long getLowResourcesMaxIdleTime() {
        return this._lowResourcesMaxIdleTime;
    }

    public void setLowResourcesMaxIdleTime(long lowResourcesMaxIdleTime) {
        this._lowResourcesMaxIdleTime = (int) lowResourcesMaxIdleTime;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        this._selectSet = new SelectSet[this._selectSets];
        for (int i = 0; i < this._selectSet.length; i++) {
            this._selectSet[i] = new SelectSet(i);
        }
        super.doStart();
        for (int i2 = 0; i2 < getSelectSets(); i2++) {
            final int id = i2;
            boolean selecting = dispatch(new Runnable() { // from class: org.eclipse.jetty.io.nio.SelectorManager.1
                @Override // java.lang.Runnable
                public void run() {
                    String name = Thread.currentThread().getName();
                    int priority = Thread.currentThread().getPriority();
                    try {
                        SelectSet[] sets = SelectorManager.this._selectSet;
                        if (sets != null) {
                            SelectSet set = sets[id];
                            Thread currentThread = Thread.currentThread();
                            currentThread.setName(name + " Selector" + id);
                            if (SelectorManager.this.getSelectorPriorityDelta() != 0) {
                                Thread.currentThread().setPriority(Thread.currentThread().getPriority() + SelectorManager.this.getSelectorPriorityDelta());
                            }
                            SelectorManager.LOG.debug("Starting {} on {}", Thread.currentThread(), this);
                            while (SelectorManager.this.isRunning()) {
                                try {
                                    set.doSelect();
                                } catch (IOException e) {
                                    SelectorManager.LOG.ignore(e);
                                } catch (Exception e2) {
                                    SelectorManager.LOG.warn(e2);
                                }
                            }
                            SelectorManager.LOG.debug("Stopped {} on {}", Thread.currentThread(), this);
                            Thread.currentThread().setName(name);
                            if (SelectorManager.this.getSelectorPriorityDelta() != 0) {
                                Thread.currentThread().setPriority(priority);
                                return;
                            }
                            return;
                        }
                        SelectorManager.LOG.debug("Stopped {} on {}", Thread.currentThread(), this);
                        Thread.currentThread().setName(name);
                        if (SelectorManager.this.getSelectorPriorityDelta() != 0) {
                            Thread.currentThread().setPriority(priority);
                        }
                    } catch (Throwable th) {
                        SelectorManager.LOG.debug("Stopped {} on {}", Thread.currentThread(), this);
                        Thread.currentThread().setName(name);
                        if (SelectorManager.this.getSelectorPriorityDelta() != 0) {
                            Thread.currentThread().setPriority(priority);
                        }
                        throw th;
                    }
                }
            });
            if (!selecting) {
                throw new IllegalStateException("!Selecting");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        SelectSet[] sets = this._selectSet;
        this._selectSet = null;
        if (sets != null) {
            for (SelectSet set : sets) {
                if (set != null) {
                    set.stop();
                }
            }
        }
        super.doStop();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void connectionFailed(SocketChannel channel, Throwable ex, Object attachment) {
        Logger logger = LOG;
        logger.warn(ex + "," + channel + "," + attachment, new Object[0]);
        LOG.debug(ex);
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public String dump() {
        return AggregateLifeCycle.dump(this);
    }

    @Override // org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        AggregateLifeCycle.dumpObject(out, this);
        AggregateLifeCycle.dump(out, indent, TypeUtil.asList(this._selectSet));
    }

    /* loaded from: classes.dex */
    public class SelectSet implements Dumpable {
        private int _busySelects;
        private long _monitorNext;
        private boolean _paused;
        private boolean _pausing;
        private volatile Thread _selecting;
        private volatile Selector _selector;
        private final int _setID;
        private final ConcurrentLinkedQueue<Object> _changes = new ConcurrentLinkedQueue<>();
        private ConcurrentMap<SelectChannelEndPoint, Object> _endPoints = new ConcurrentHashMap();
        private volatile long _idleTick = System.currentTimeMillis();
        private final Timeout _timeout = new Timeout(this);

        SelectSet(int acceptorID) throws Exception {
            this._setID = acceptorID;
            this._timeout.setDuration(0L);
            this._selector = Selector.open();
            this._monitorNext = System.currentTimeMillis() + SelectorManager.__MONITOR_PERIOD;
        }

        public void addChange(Object change) {
            this._changes.add(change);
        }

        public void addChange(SelectableChannel channel, Object att) {
            if (att == null) {
                addChange(channel);
            } else if (att instanceof EndPoint) {
                addChange(att);
            } else {
                addChange(new ChannelAndAttachment(channel, att));
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:165:0x0284, code lost:
            r0.selectedKeys().clear();
            r10 = java.lang.System.currentTimeMillis();
            r23._timeout.setNow(r10);
            r0 = r23._timeout.expired();
         */
        /* JADX WARN: Code restructure failed: missing block: B:166:0x029b, code lost:
            if (r0 == null) goto L196;
         */
        /* JADX WARN: Code restructure failed: missing block: B:168:0x029f, code lost:
            if ((r0 instanceof java.lang.Runnable) == false) goto L195;
         */
        /* JADX WARN: Code restructure failed: missing block: B:169:0x02a1, code lost:
            r23.this$0.dispatch((java.lang.Runnable) r0);
         */
        /* JADX WARN: Code restructure failed: missing block: B:170:0x02a9, code lost:
            r0 = r23._timeout.expired();
         */
        /* JADX WARN: Code restructure failed: missing block: B:172:0x02bc, code lost:
            if ((r10 - r23._idleTick) <= org.eclipse.jetty.io.nio.SelectorManager.__IDLE_TICK) goto L206;
         */
        /* JADX WARN: Code restructure failed: missing block: B:173:0x02be, code lost:
            r23._idleTick = r10;
         */
        /* JADX WARN: Code restructure failed: missing block: B:174:0x02ca, code lost:
            if (r23.this$0._lowResourcesConnections <= 0) goto L205;
         */
        /* JADX WARN: Code restructure failed: missing block: B:176:0x02dd, code lost:
            if (r0.keys().size() <= r23.this$0._lowResourcesConnections) goto L205;
         */
        /* JADX WARN: Code restructure failed: missing block: B:177:0x02df, code lost:
            r10 = (r23.this$0._maxIdleTime + r10) - r23.this$0._lowResourcesMaxIdleTime;
         */
        /* JADX WARN: Code restructure failed: missing block: B:178:0x02f0, code lost:
            r10 = r10;
         */
        /* JADX WARN: Code restructure failed: missing block: B:179:0x02f1, code lost:
            r23.this$0.dispatch(new org.eclipse.jetty.io.nio.SelectorManager.SelectSet.AnonymousClass1(r23));
         */
        /* JADX WARN: Code restructure failed: missing block: B:181:0x02ff, code lost:
            if (org.eclipse.jetty.io.nio.SelectorManager.__MONITOR_PERIOD <= 0) goto L212;
         */
        /* JADX WARN: Code restructure failed: missing block: B:183:0x0305, code lost:
            if (r10 <= r23._monitorNext) goto L212;
         */
        /* JADX WARN: Code restructure failed: missing block: B:184:0x0307, code lost:
            r23._busySelects = 0;
            r23._pausing = false;
            r23._monitorNext = org.eclipse.jetty.io.nio.SelectorManager.__MONITOR_PERIOD + r10;
         */
        /* JADX WARN: Code restructure failed: missing block: B:57:0x00dd, code lost:
            r0 = r0.selectNow();
            r7 = java.lang.System.currentTimeMillis();
         */
        /* JADX WARN: Code restructure failed: missing block: B:58:0x00e9, code lost:
            if (r0 != 0) goto L104;
         */
        /* JADX WARN: Code restructure failed: missing block: B:60:0x00f3, code lost:
            if (r0.selectedKeys().isEmpty() == false) goto L104;
         */
        /* JADX WARN: Code restructure failed: missing block: B:62:0x00f7, code lost:
            if (r23._pausing == false) goto L76;
         */
        /* JADX WARN: Code restructure failed: missing block: B:63:0x00f9, code lost:
            java.lang.Thread.sleep(org.eclipse.jetty.io.nio.SelectorManager.__BUSY_PAUSE);
         */
        /* JADX WARN: Code restructure failed: missing block: B:65:0x0102, code lost:
            r0 = move-exception;
         */
        /* JADX WARN: Code restructure failed: missing block: B:66:0x0103, code lost:
            org.eclipse.jetty.io.nio.SelectorManager.LOG.ignore(r0);
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void doSelect() throws java.io.IOException {
            /*
                Method dump skipped, instructions count: 824
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.io.nio.SelectorManager.SelectSet.doSelect():void");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void renewSelector() {
            try {
                synchronized (this) {
                    Selector selector = this._selector;
                    if (selector == null) {
                        return;
                    }
                    Selector new_selector = Selector.open();
                    for (SelectionKey k : selector.keys()) {
                        if (k.isValid() && k.interestOps() != 0) {
                            SelectableChannel channel = k.channel();
                            Object attachment = k.attachment();
                            if (attachment == null) {
                                addChange(channel);
                            } else {
                                addChange(channel, attachment);
                            }
                        }
                    }
                    this._selector.close();
                    this._selector = new_selector;
                }
            } catch (IOException e) {
                throw new RuntimeException("recreating selector", e);
            }
        }

        public SelectorManager getManager() {
            return SelectorManager.this;
        }

        public long getNow() {
            return this._timeout.getNow();
        }

        public void scheduleTimeout(Timeout.Task task, long timeoutMs) {
            if (!(task instanceof Runnable)) {
                throw new IllegalArgumentException("!Runnable");
            }
            this._timeout.schedule(task, timeoutMs);
        }

        public void cancelTimeout(Timeout.Task task) {
            task.cancel();
        }

        public void wakeup() {
            try {
                Selector selector = this._selector;
                if (selector != null) {
                    selector.wakeup();
                }
            } catch (Exception e) {
                addChange(new ChangeTask() { // from class: org.eclipse.jetty.io.nio.SelectorManager.SelectSet.2
                    @Override // java.lang.Runnable
                    public void run() {
                        SelectSet.this.renewSelector();
                    }
                });
                renewSelector();
            }
        }

        private SelectChannelEndPoint createEndPoint(SocketChannel channel, SelectionKey sKey) throws IOException {
            SelectChannelEndPoint endp = SelectorManager.this.newEndPoint(channel, this, sKey);
            SelectorManager.LOG.debug("created {}", endp);
            SelectorManager.this.endPointOpened(endp);
            this._endPoints.put(endp, this);
            return endp;
        }

        public void destroyEndPoint(SelectChannelEndPoint endp) {
            SelectorManager.LOG.debug("destroyEndPoint {}", endp);
            this._endPoints.remove(endp);
            SelectorManager.this.endPointClosed(endp);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public Selector getSelector() {
            return this._selector;
        }

        void stop() throws Exception {
            for (int i = 0; i < 100; i++) {
                try {
                    if (this._selecting == null) {
                        break;
                    }
                    wakeup();
                    Thread.sleep(10L);
                } catch (Exception e) {
                    SelectorManager.LOG.ignore(e);
                }
            }
            synchronized (this) {
                for (SelectionKey key : this._selector.keys()) {
                    if (key != null) {
                        Object att = key.attachment();
                        if (att instanceof EndPoint) {
                            EndPoint endpoint = (EndPoint) att;
                            try {
                                endpoint.close();
                            } catch (IOException e2) {
                                SelectorManager.LOG.ignore(e2);
                            }
                        }
                    }
                }
                this._timeout.cancelAll();
                try {
                    Selector selector = this._selector;
                    if (selector != null) {
                        selector.close();
                    }
                } catch (IOException e3) {
                    SelectorManager.LOG.ignore(e3);
                }
                this._selector = null;
            }
        }

        @Override // org.eclipse.jetty.util.component.Dumpable
        public String dump() {
            return AggregateLifeCycle.dump(this);
        }

        @Override // org.eclipse.jetty.util.component.Dumpable
        public void dump(Appendable out, String indent) throws IOException {
            out.append(String.valueOf(this)).append(" id=").append(String.valueOf(this._setID)).append("\n");
            Thread selecting = this._selecting;
            Object where = "not selecting";
            StackTraceElement[] trace = selecting == null ? null : selecting.getStackTrace();
            if (trace != null) {
                StackTraceElement[] arr$ = trace;
                int len$ = arr$.length;
                int i$ = 0;
                while (true) {
                    if (i$ >= len$) {
                        break;
                    }
                    StackTraceElement t = arr$[i$];
                    if (!t.getClassName().startsWith("org.eclipse.jetty.")) {
                        i$++;
                    } else {
                        where = t;
                        break;
                    }
                }
            }
            Selector selector = this._selector;
            if (selector != null) {
                final ArrayList<Object> dump = new ArrayList<>(selector.keys().size() * 2);
                dump.add(where);
                final CountDownLatch latch = new CountDownLatch(1);
                addChange(new ChangeTask() { // from class: org.eclipse.jetty.io.nio.SelectorManager.SelectSet.3
                    @Override // java.lang.Runnable
                    public void run() {
                        SelectSet.this.dumpKeyState(dump);
                        latch.countDown();
                    }
                });
                try {
                    latch.await(5L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    SelectorManager.LOG.ignore(e);
                }
                AggregateLifeCycle.dump(out, indent, dump);
            }
        }

        public void dumpKeyState(List<Object> dumpto) {
            Selector selector = this._selector;
            Set<SelectionKey> keys = selector.keys();
            dumpto.add(selector + " keys=" + keys.size());
            for (SelectionKey key : keys) {
                if (key.isValid()) {
                    dumpto.add(key.attachment() + " iOps=" + key.interestOps() + " rOps=" + key.readyOps());
                } else {
                    dumpto.add(key.attachment() + " iOps=-1 rOps=-1");
                }
            }
        }

        public String toString() {
            Selector selector = this._selector;
            Object[] objArr = new Object[3];
            objArr[0] = super.toString();
            int i = -1;
            objArr[1] = Integer.valueOf((selector == null || !selector.isOpen()) ? -1 : selector.keys().size());
            if (selector != null && selector.isOpen()) {
                i = selector.selectedKeys().size();
            }
            objArr[2] = Integer.valueOf(i);
            return String.format("%s keys=%d selected=%d", objArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ChannelAndAttachment {
        final Object _attachment;
        final SelectableChannel _channel;

        public ChannelAndAttachment(SelectableChannel channel, Object attachment) {
            this._channel = channel;
            this._attachment = attachment;
        }
    }

    public boolean isDeferringInterestedOps0() {
        return this._deferringInterestedOps0;
    }

    public void setDeferringInterestedOps0(boolean deferringInterestedOps0) {
        this._deferringInterestedOps0 = deferringInterestedOps0;
    }
}
