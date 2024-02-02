package org.eclipse.jetty.server;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AsyncNCSARequestLog extends NCSARequestLog {
    private static final Logger LOG = Log.getLogger(AsyncNCSARequestLog.class);
    private final BlockingQueue<String> _queue;
    private transient WriterThread _thread;
    private boolean _warnedFull;

    public AsyncNCSARequestLog() {
        this(null, null);
    }

    public AsyncNCSARequestLog(BlockingQueue<String> queue) {
        this(null, queue);
    }

    public AsyncNCSARequestLog(String filename) {
        this(filename, null);
    }

    public AsyncNCSARequestLog(String filename, BlockingQueue<String> queue) {
        super(filename);
        this._queue = queue == null ? new BlockingArrayQueue(1024) : queue;
    }

    /* loaded from: classes.dex */
    private class WriterThread extends Thread {
        WriterThread() {
            setName("AsyncNCSARequestLog@" + Integer.toString(AsyncNCSARequestLog.this.hashCode(), 16));
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (AsyncNCSARequestLog.this.isRunning()) {
                try {
                    String log = (String) AsyncNCSARequestLog.this._queue.poll(10L, TimeUnit.SECONDS);
                    if (log != null) {
                        AsyncNCSARequestLog.super.write(log);
                    }
                    while (!AsyncNCSARequestLog.this._queue.isEmpty()) {
                        String log2 = (String) AsyncNCSARequestLog.this._queue.poll();
                        if (log2 != null) {
                            AsyncNCSARequestLog.super.write(log2);
                        }
                    }
                } catch (IOException e) {
                    AsyncNCSARequestLog.LOG.warn(e);
                } catch (InterruptedException e2) {
                    AsyncNCSARequestLog.LOG.ignore(e2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.NCSARequestLog, org.eclipse.jetty.util.component.AbstractLifeCycle
    public synchronized void doStart() throws Exception {
        super.doStart();
        this._thread = new WriterThread();
        this._thread.start();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.NCSARequestLog, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._thread.interrupt();
        this._thread.join();
        super.doStop();
        this._thread = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.NCSARequestLog
    public void write(String log) throws IOException {
        if (!this._queue.offer(log)) {
            if (this._warnedFull) {
                LOG.warn("Log Queue overflow", new Object[0]);
            }
            this._warnedFull = true;
        }
    }
}
