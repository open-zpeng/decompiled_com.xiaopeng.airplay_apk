package org.eclipse.jetty.io.nio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Locale;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ConnectedEndPoint;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.nio.SelectorManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public class SelectChannelEndPoint extends ChannelEndPoint implements AsyncEndPoint, ConnectedEndPoint {
    public static final Logger LOG = Log.getLogger("org.eclipse.jetty.io.nio");
    private static final int STATE_ASYNC = 2;
    private static final int STATE_DISPATCHED = 1;
    private static final int STATE_NEEDS_DISPATCH = -1;
    private static final int STATE_UNDISPATCHED = 0;
    private final boolean WORK_AROUND_JVM_BUG_6346658;
    private volatile boolean _checkIdle;
    private volatile AsyncConnection _connection;
    private final Runnable _handler;
    private volatile long _idleTimestamp;
    private int _interestOps;
    private boolean _interruptable;
    private boolean _ishut;
    private SelectionKey _key;
    private final SelectorManager _manager;
    private boolean _onIdle;
    private boolean _open;
    private boolean _readBlocked;
    private final SelectorManager.SelectSet _selectSet;
    private int _state;
    private volatile boolean _writable;
    private boolean _writeBlocked;

    public SelectChannelEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key, int maxIdleTime) throws IOException {
        super(channel, maxIdleTime);
        this.WORK_AROUND_JVM_BUG_6346658 = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
        this._handler = new Runnable() { // from class: org.eclipse.jetty.io.nio.SelectChannelEndPoint.1
            @Override // java.lang.Runnable
            public void run() {
                SelectChannelEndPoint.this.handle();
            }
        };
        this._writable = true;
        this._manager = selectSet.getManager();
        this._selectSet = selectSet;
        this._state = 0;
        this._onIdle = false;
        this._open = true;
        this._key = key;
        setCheckForIdle(true);
    }

    public SelectionKey getSelectionKey() {
        SelectionKey selectionKey;
        synchronized (this) {
            selectionKey = this._key;
        }
        return selectionKey;
    }

    public SelectorManager getSelectManager() {
        return this._manager;
    }

    @Override // org.eclipse.jetty.io.ConnectedEndPoint
    public Connection getConnection() {
        return this._connection;
    }

    @Override // org.eclipse.jetty.io.ConnectedEndPoint
    public void setConnection(Connection connection) {
        Connection old = this._connection;
        this._connection = (AsyncConnection) connection;
        if (old != null && old != this._connection) {
            this._manager.endPointUpgraded(this, old);
        }
    }

    public long getIdleTimestamp() {
        return this._idleTimestamp;
    }

    public void schedule() {
        synchronized (this) {
            if (this._key != null && this._key.isValid()) {
                if (!this._readBlocked && !this._writeBlocked) {
                    if ((this._key.readyOps() & 4) == 4 && (this._key.interestOps() & 4) == 4) {
                        this._interestOps = this._key.interestOps() & (-5);
                        this._key.interestOps(this._interestOps);
                        this._writable = true;
                    }
                    if (this._state >= 1) {
                        this._key.interestOps(0);
                    } else {
                        dispatch();
                        if (this._state >= 1 && !this._selectSet.getManager().isDeferringInterestedOps0()) {
                            this._key.interestOps(0);
                        }
                    }
                    return;
                }
                if (this._readBlocked && this._key.isReadable()) {
                    this._readBlocked = false;
                }
                if (this._writeBlocked && this._key.isWritable()) {
                    this._writeBlocked = false;
                }
                notifyAll();
                this._key.interestOps(0);
                if (this._state < 1) {
                    updateKey();
                }
                return;
            }
            this._readBlocked = false;
            this._writeBlocked = false;
            notifyAll();
        }
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void asyncDispatch() {
        synchronized (this) {
            switch (this._state) {
                case -1:
                case 0:
                    dispatch();
                    break;
                case 1:
                case 2:
                    this._state = 2;
                    break;
            }
        }
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void dispatch() {
        synchronized (this) {
            if (this._state <= 0) {
                if (this._onIdle) {
                    this._state = -1;
                } else {
                    this._state = 1;
                    boolean dispatched = this._manager.dispatch(this._handler);
                    if (!dispatched) {
                        this._state = -1;
                        Logger logger = LOG;
                        logger.warn("Dispatched Failed! " + this + " to " + this._manager, new Object[0]);
                        updateKey();
                    }
                }
            }
        }
    }

    protected boolean undispatch() {
        synchronized (this) {
            if (this._state == 2) {
                this._state = 1;
                return false;
            }
            this._state = 0;
            updateKey();
            return true;
        }
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void cancelTimeout(Timeout.Task task) {
        getSelectSet().cancelTimeout(task);
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void scheduleTimeout(Timeout.Task task, long timeoutMs) {
        getSelectSet().scheduleTimeout(task, timeoutMs);
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void setCheckForIdle(boolean check) {
        if (check) {
            this._idleTimestamp = System.currentTimeMillis();
            this._checkIdle = true;
            return;
        }
        this._checkIdle = false;
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public boolean isCheckForIdle() {
        return this._checkIdle;
    }

    protected void notIdle() {
        this._idleTimestamp = System.currentTimeMillis();
    }

    public void checkIdleTimestamp(long now) {
        if (isCheckForIdle() && this._maxIdleTime > 0) {
            final long idleForMs = now - this._idleTimestamp;
            if (idleForMs > this._maxIdleTime) {
                setCheckForIdle(false);
                this._manager.dispatch(new Runnable() { // from class: org.eclipse.jetty.io.nio.SelectChannelEndPoint.2
                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            SelectChannelEndPoint.this.onIdleExpired(idleForMs);
                        } finally {
                            SelectChannelEndPoint.this.setCheckForIdle(true);
                        }
                    }
                });
            }
        }
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void onIdleExpired(long idleForMs) {
        try {
            synchronized (this) {
                this._onIdle = true;
            }
            this._connection.onIdleExpired(idleForMs);
            synchronized (this) {
                this._onIdle = false;
                if (this._state == -1) {
                    dispatch();
                }
            }
        } catch (Throwable th) {
            synchronized (this) {
                this._onIdle = false;
                if (this._state == -1) {
                    dispatch();
                }
                throw th;
            }
        }
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public int fill(Buffer buffer) throws IOException {
        int fill = super.fill(buffer);
        if (fill > 0) {
            notIdle();
        }
        return fill;
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public int flush(Buffer header, Buffer buffer, Buffer trailer) throws IOException {
        int l = super.flush(header, buffer, trailer);
        if (l == 0 && ((header != null && header.hasContent()) || ((buffer != null && buffer.hasContent()) || (trailer != null && trailer.hasContent())))) {
            synchronized (this) {
                this._writable = false;
                if (this._state < 1) {
                    updateKey();
                }
            }
        } else if (l > 0) {
            this._writable = true;
            notIdle();
        }
        return l;
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public int flush(Buffer buffer) throws IOException {
        int l = super.flush(buffer);
        if (l == 0 && buffer != null && buffer.hasContent()) {
            synchronized (this) {
                this._writable = false;
                if (this._state < 1) {
                    updateKey();
                }
            }
        } else if (l > 0) {
            this._writable = true;
            notIdle();
        }
        return l;
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public boolean blockReadable(long timeoutMs) throws IOException {
        SelectorManager.SelectSet selectSet;
        synchronized (this) {
            if (isInputShutdown()) {
                throw new EofException();
            }
            long now = this._selectSet.getNow();
            long end = now + timeoutMs;
            boolean check = isCheckForIdle();
            setCheckForIdle(true);
            this._readBlocked = true;
            while (!isInputShutdown() && this._readBlocked) {
                try {
                    try {
                        updateKey();
                        wait(timeoutMs > 0 ? end - now : 10000L);
                        selectSet = this._selectSet;
                    } catch (InterruptedException e) {
                        LOG.warn(e);
                        if (this._interruptable) {
                            throw new InterruptedIOException() { // from class: org.eclipse.jetty.io.nio.SelectChannelEndPoint.3
                                {
                                    initCause(e);
                                }
                            };
                        }
                        selectSet = this._selectSet;
                    }
                    now = selectSet.getNow();
                    if (this._readBlocked && timeoutMs > 0 && now >= end) {
                        this._readBlocked = false;
                        setCheckForIdle(check);
                        return false;
                    }
                } catch (Throwable th) {
                    this._selectSet.getNow();
                    throw th;
                }
            }
            this._readBlocked = false;
            setCheckForIdle(check);
            return true;
        }
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public boolean blockWritable(long timeoutMs) throws IOException {
        SelectorManager.SelectSet selectSet;
        synchronized (this) {
            if (isOutputShutdown()) {
                throw new EofException();
            }
            long now = this._selectSet.getNow();
            long end = now + timeoutMs;
            boolean check = isCheckForIdle();
            setCheckForIdle(true);
            this._writeBlocked = true;
            while (this._writeBlocked && !isOutputShutdown()) {
                try {
                    try {
                        updateKey();
                        wait(timeoutMs > 0 ? end - now : 10000L);
                        selectSet = this._selectSet;
                    } catch (InterruptedException e) {
                        LOG.warn(e);
                        if (this._interruptable) {
                            throw new InterruptedIOException() { // from class: org.eclipse.jetty.io.nio.SelectChannelEndPoint.4
                                {
                                    initCause(e);
                                }
                            };
                        }
                        selectSet = this._selectSet;
                    }
                    now = selectSet.getNow();
                    if (this._writeBlocked && timeoutMs > 0 && now >= end) {
                        this._writeBlocked = false;
                        setCheckForIdle(check);
                        return false;
                    }
                } catch (Throwable th) {
                    this._selectSet.getNow();
                    throw th;
                }
            }
            this._writeBlocked = false;
            setCheckForIdle(check);
            return true;
        }
    }

    public void setInterruptable(boolean interupable) {
        synchronized (this) {
            this._interruptable = interupable;
        }
    }

    public boolean isInterruptable() {
        return this._interruptable;
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public void scheduleWrite() {
        if (this._writable) {
            LOG.debug("Required scheduleWrite {}", this);
        }
        this._writable = false;
        updateKey();
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public boolean isWritable() {
        return this._writable;
    }

    @Override // org.eclipse.jetty.io.AsyncEndPoint
    public boolean hasProgressed() {
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x003a A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:31:0x0047 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0053 A[Catch: Exception -> 0x0065, all -> 0x0082, TryCatch #1 {Exception -> 0x0065, blocks: (B:35:0x004f, B:37:0x0053, B:39:0x005b), top: B:58:0x004f, outer: #0 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void updateKey() {
        /*
            r7 = this;
            monitor-enter(r7)
            r0 = -1
            r1 = 0
            java.nio.channels.ByteChannel r2 = r7.getChannel()     // Catch: java.lang.Throwable -> L82
            boolean r2 = r2.isOpen()     // Catch: java.lang.Throwable -> L82
            r3 = 1
            if (r2 == 0) goto L6e
            boolean r2 = r7._readBlocked     // Catch: java.lang.Throwable -> L82
            if (r2 != 0) goto L21
            int r2 = r7._state     // Catch: java.lang.Throwable -> L82
            if (r2 >= r3) goto L1f
            org.eclipse.jetty.io.nio.AsyncConnection r2 = r7._connection     // Catch: java.lang.Throwable -> L82
            boolean r2 = r2.isSuspended()     // Catch: java.lang.Throwable -> L82
            if (r2 != 0) goto L1f
            goto L21
        L1f:
            r2 = r1
            goto L22
        L21:
            r2 = r3
        L22:
            boolean r4 = r7._writeBlocked     // Catch: java.lang.Throwable -> L82
            if (r4 != 0) goto L31
            int r4 = r7._state     // Catch: java.lang.Throwable -> L82
            if (r4 >= r3) goto L2f
            boolean r4 = r7._writable     // Catch: java.lang.Throwable -> L82
            if (r4 != 0) goto L2f
            goto L31
        L2f:
            r4 = r1
            goto L32
        L31:
            r4 = r3
        L32:
            java.net.Socket r5 = r7._socket     // Catch: java.lang.Throwable -> L82
            boolean r5 = r5.isInputShutdown()     // Catch: java.lang.Throwable -> L82
            if (r5 != 0) goto L3e
            if (r2 == 0) goto L3e
            r5 = r3
            goto L3f
        L3e:
            r5 = r1
        L3f:
            java.net.Socket r6 = r7._socket     // Catch: java.lang.Throwable -> L82
            boolean r6 = r6.isOutputShutdown()     // Catch: java.lang.Throwable -> L82
            if (r6 != 0) goto L4b
            if (r4 == 0) goto L4b
            r6 = 4
            goto L4c
        L4b:
            r6 = r1
        L4c:
            r5 = r5 | r6
            r7._interestOps = r5     // Catch: java.lang.Throwable -> L82
            java.nio.channels.SelectionKey r5 = r7._key     // Catch: java.lang.Exception -> L65 java.lang.Throwable -> L82
            if (r5 == 0) goto L62
            java.nio.channels.SelectionKey r5 = r7._key     // Catch: java.lang.Exception -> L65 java.lang.Throwable -> L82
            boolean r5 = r5.isValid()     // Catch: java.lang.Exception -> L65 java.lang.Throwable -> L82
            if (r5 == 0) goto L62
            java.nio.channels.SelectionKey r5 = r7._key     // Catch: java.lang.Exception -> L65 java.lang.Throwable -> L82
            int r5 = r5.interestOps()     // Catch: java.lang.Exception -> L65 java.lang.Throwable -> L82
            goto L63
        L62:
            r5 = -1
        L63:
            r0 = r5
            goto L6e
        L65:
            r5 = move-exception
            r6 = 0
            r7._key = r6     // Catch: java.lang.Throwable -> L82
            org.eclipse.jetty.util.log.Logger r6 = org.eclipse.jetty.io.nio.SelectChannelEndPoint.LOG     // Catch: java.lang.Throwable -> L82
            r6.ignore(r5)     // Catch: java.lang.Throwable -> L82
        L6e:
            int r2 = r7._interestOps     // Catch: java.lang.Throwable -> L82
            if (r2 == r0) goto L74
            r1 = r3
        L74:
            monitor-exit(r7)     // Catch: java.lang.Throwable -> L82
            if (r1 == 0) goto L81
            org.eclipse.jetty.io.nio.SelectorManager$SelectSet r0 = r7._selectSet
            r0.addChange(r7)
            org.eclipse.jetty.io.nio.SelectorManager$SelectSet r0 = r7._selectSet
            r0.wakeup()
        L81:
            return
        L82:
            r0 = move-exception
            monitor-exit(r7)     // Catch: java.lang.Throwable -> L82
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.io.nio.SelectChannelEndPoint.updateKey():void");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void doUpdateKey() {
        synchronized (this) {
            if (getChannel().isOpen()) {
                if (this._interestOps > 0) {
                    if (this._key != null && this._key.isValid()) {
                        this._key.interestOps(this._interestOps);
                    }
                    SelectableChannel sc = (SelectableChannel) getChannel();
                    if (sc.isRegistered()) {
                        updateKey();
                    } else {
                        try {
                            this._key = ((SelectableChannel) getChannel()).register(this._selectSet.getSelector(), this._interestOps, this);
                        } catch (Exception e) {
                            LOG.ignore(e);
                            if (this._key != null && this._key.isValid()) {
                                this._key.cancel();
                            }
                            if (this._open) {
                                this._selectSet.destroyEndPoint(this);
                            }
                            this._open = false;
                            this._key = null;
                        }
                    }
                } else if (this._key != null && this._key.isValid()) {
                    this._key.interestOps(0);
                } else {
                    this._key = null;
                }
            } else {
                if (this._key != null && this._key.isValid()) {
                    this._key.cancel();
                }
                if (this._open) {
                    this._open = false;
                    this._selectSet.destroyEndPoint(this);
                }
                this._key = null;
            }
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:29:0x005e
        	at jadx.core.dex.visitors.blocks.BlockProcessor.checkForUnreachableBlocks(BlockProcessor.java:81)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:47)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    protected void handle() {
        /*
            Method dump skipped, instructions count: 520
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle():void");
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public void close() throws IOException {
        if (this.WORK_AROUND_JVM_BUG_6346658) {
            try {
                SelectionKey key = this._key;
                if (key != null) {
                    key.cancel();
                }
            } catch (Throwable e) {
                LOG.ignore(e);
            }
        }
        try {
            try {
                super.close();
            } catch (IOException e2) {
                LOG.ignore(e2);
            }
        } finally {
            updateKey();
        }
    }

    public String toString() {
        SelectionKey key = this._key;
        String keyString = "";
        if (key != null) {
            if (key.isValid()) {
                if (key.isReadable()) {
                    keyString = "r";
                }
                if (key.isWritable()) {
                    keyString = keyString + "w";
                }
            } else {
                keyString = "!";
            }
        } else {
            keyString = "-";
        }
        return String.format("SCEP@%x{l(%s)<->r(%s),s=%d,open=%b,ishut=%b,oshut=%b,rb=%b,wb=%b,w=%b,i=%d%s}-{%s}", Integer.valueOf(hashCode()), this._socket.getRemoteSocketAddress(), this._socket.getLocalSocketAddress(), Integer.valueOf(this._state), Boolean.valueOf(isOpen()), Boolean.valueOf(isInputShutdown()), Boolean.valueOf(isOutputShutdown()), Boolean.valueOf(this._readBlocked), Boolean.valueOf(this._writeBlocked), Boolean.valueOf(this._writable), Integer.valueOf(this._interestOps), keyString, this._connection);
    }

    public SelectorManager.SelectSet getSelectSet() {
        return this._selectSet;
    }

    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public void setMaxIdleTime(int timeMs) throws IOException {
        this._maxIdleTime = timeMs;
    }
}
