package org.eclipse.jetty.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Scanner extends AbstractLifeCycle {
    private static final Logger LOG = Log.getLogger(Scanner.class);
    private static int __scannerId = 0;
    private FilenameFilter _filter;
    private int _scanInterval;
    private TimerTask _task;
    private Timer _timer;
    private int _scanCount = 0;
    private final List<Listener> _listeners = new ArrayList();
    private final Map<String, TimeNSize> _prevScan = new HashMap();
    private final Map<String, TimeNSize> _currentScan = new HashMap();
    private final List<File> _scanDirs = new ArrayList();
    private volatile boolean _running = false;
    private boolean _reportExisting = true;
    private boolean _reportDirs = true;
    private int _scanDepth = 0;
    private final Map<String, Notification> _notifications = new HashMap();

    /* loaded from: classes.dex */
    public interface BulkListener extends Listener {
        void filesChanged(List<String> list) throws Exception;
    }

    /* loaded from: classes.dex */
    public interface DiscreteListener extends Listener {
        void fileAdded(String str) throws Exception;

        void fileChanged(String str) throws Exception;

        void fileRemoved(String str) throws Exception;
    }

    /* loaded from: classes.dex */
    public interface Listener {
    }

    /* loaded from: classes.dex */
    public enum Notification {
        ADDED,
        CHANGED,
        REMOVED
    }

    /* loaded from: classes.dex */
    public interface ScanCycleListener extends Listener {
        void scanEnded(int i) throws Exception;

        void scanStarted(int i) throws Exception;
    }

    /* loaded from: classes.dex */
    public interface ScanListener extends Listener {
        void scan();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class TimeNSize {
        final long _lastModified;
        final long _size;

        public TimeNSize(long lastModified, long size) {
            this._lastModified = lastModified;
            this._size = size;
        }

        public int hashCode() {
            return ((int) this._lastModified) ^ ((int) this._size);
        }

        public boolean equals(Object o) {
            if (o instanceof TimeNSize) {
                TimeNSize tns = (TimeNSize) o;
                return tns._lastModified == this._lastModified && tns._size == this._size;
            }
            return false;
        }

        public String toString() {
            return "[lm=" + this._lastModified + ",s=" + this._size + "]";
        }
    }

    public int getScanInterval() {
        return this._scanInterval;
    }

    public synchronized void setScanInterval(int scanInterval) {
        this._scanInterval = scanInterval;
        schedule();
    }

    @Deprecated
    public void setScanDir(File dir) {
        this._scanDirs.clear();
        this._scanDirs.add(dir);
    }

    @Deprecated
    public File getScanDir() {
        if (this._scanDirs == null) {
            return null;
        }
        return this._scanDirs.get(0);
    }

    public void setScanDirs(List<File> dirs) {
        this._scanDirs.clear();
        this._scanDirs.addAll(dirs);
    }

    public synchronized void addScanDir(File dir) {
        this._scanDirs.add(dir);
    }

    public List<File> getScanDirs() {
        return Collections.unmodifiableList(this._scanDirs);
    }

    public void setRecursive(boolean recursive) {
        this._scanDepth = recursive ? -1 : 0;
    }

    public boolean getRecursive() {
        return this._scanDepth == -1;
    }

    public int getScanDepth() {
        return this._scanDepth;
    }

    public void setScanDepth(int scanDepth) {
        this._scanDepth = scanDepth;
    }

    public void setFilenameFilter(FilenameFilter filter) {
        this._filter = filter;
    }

    public FilenameFilter getFilenameFilter() {
        return this._filter;
    }

    public void setReportExistingFilesOnStartup(boolean reportExisting) {
        this._reportExisting = reportExisting;
    }

    public boolean getReportExistingFilesOnStartup() {
        return this._reportExisting;
    }

    public void setReportDirs(boolean dirs) {
        this._reportDirs = dirs;
    }

    public boolean getReportDirs() {
        return this._reportDirs;
    }

    public synchronized void addListener(Listener listener) {
        if (listener == null) {
            return;
        }
        this._listeners.add(listener);
    }

    public synchronized void removeListener(Listener listener) {
        if (listener == null) {
            return;
        }
        this._listeners.remove(listener);
    }

    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public synchronized void doStart() {
        if (this._running) {
            return;
        }
        this._running = true;
        if (this._reportExisting) {
            scan();
            scan();
        } else {
            scanFiles();
            this._prevScan.putAll(this._currentScan);
        }
        schedule();
    }

    public TimerTask newTimerTask() {
        return new TimerTask() { // from class: org.eclipse.jetty.util.Scanner.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                Scanner.this.scan();
            }
        };
    }

    public Timer newTimer() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scanner-");
        int i = __scannerId;
        __scannerId = i + 1;
        sb.append(i);
        return new Timer(sb.toString(), true);
    }

    public void schedule() {
        if (this._running) {
            if (this._timer != null) {
                this._timer.cancel();
            }
            if (this._task != null) {
                this._task.cancel();
            }
            if (getScanInterval() > 0) {
                this._timer = newTimer();
                this._task = newTimerTask();
                this._timer.schedule(this._task, getScanInterval() * 1010, 1010 * getScanInterval());
            }
        }
    }

    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public synchronized void doStop() {
        if (this._running) {
            this._running = false;
            if (this._timer != null) {
                this._timer.cancel();
            }
            if (this._task != null) {
                this._task.cancel();
            }
            this._task = null;
            this._timer = null;
        }
    }

    public synchronized void scan() {
        int i = this._scanCount + 1;
        this._scanCount = i;
        reportScanStart(i);
        scanFiles();
        reportDifferences(this._currentScan, this._prevScan);
        this._prevScan.clear();
        this._prevScan.putAll(this._currentScan);
        reportScanEnd(this._scanCount);
        for (Listener l : this._listeners) {
            try {
                try {
                    if (l instanceof ScanListener) {
                        ((ScanListener) l).scan();
                    }
                } catch (Exception e) {
                    LOG.warn(e);
                }
            } catch (Error e2) {
                LOG.warn(e2);
            }
        }
    }

    public synchronized void scanFiles() {
        if (this._scanDirs == null) {
            return;
        }
        this._currentScan.clear();
        for (File dir : this._scanDirs) {
            if (dir != null && dir.exists()) {
                try {
                    scanFile(dir.getCanonicalFile(), this._currentScan, 0);
                } catch (IOException e) {
                    LOG.warn("Error scanning files.", e);
                }
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:52:0x0134  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0135 A[Catch: all -> 0x014c, TryCatch #0 {, blocks: (B:3:0x0001, B:4:0x0012, B:6:0x0019, B:8:0x002b, B:10:0x0037, B:11:0x003f, B:13:0x0043, B:15:0x004b, B:17:0x005b, B:19:0x0067, B:22:0x0072, B:24:0x007a, B:25:0x0082, B:27:0x0088, B:29:0x0094, B:31:0x00a0, B:34:0x00ab, B:36:0x00b1, B:38:0x00b9, B:39:0x00de, B:40:0x00ed, B:42:0x00f3, B:44:0x0105, B:50:0x011d, B:51:0x0131, B:53:0x0135, B:54:0x0139, B:55:0x013d, B:47:0x0116, B:57:0x0141, B:59:0x0147), top: B:65:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:54:0x0139 A[Catch: all -> 0x014c, TryCatch #0 {, blocks: (B:3:0x0001, B:4:0x0012, B:6:0x0019, B:8:0x002b, B:10:0x0037, B:11:0x003f, B:13:0x0043, B:15:0x004b, B:17:0x005b, B:19:0x0067, B:22:0x0072, B:24:0x007a, B:25:0x0082, B:27:0x0088, B:29:0x0094, B:31:0x00a0, B:34:0x00ab, B:36:0x00b1, B:38:0x00b9, B:39:0x00de, B:40:0x00ed, B:42:0x00f3, B:44:0x0105, B:50:0x011d, B:51:0x0131, B:53:0x0135, B:54:0x0139, B:55:0x013d, B:47:0x0116, B:57:0x0141, B:59:0x0147), top: B:65:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x013d A[Catch: all -> 0x014c, TryCatch #0 {, blocks: (B:3:0x0001, B:4:0x0012, B:6:0x0019, B:8:0x002b, B:10:0x0037, B:11:0x003f, B:13:0x0043, B:15:0x004b, B:17:0x005b, B:19:0x0067, B:22:0x0072, B:24:0x007a, B:25:0x0082, B:27:0x0088, B:29:0x0094, B:31:0x00a0, B:34:0x00ab, B:36:0x00b1, B:38:0x00b9, B:39:0x00de, B:40:0x00ed, B:42:0x00f3, B:44:0x0105, B:50:0x011d, B:51:0x0131, B:53:0x0135, B:54:0x0139, B:55:0x013d, B:47:0x0116, B:57:0x0141, B:59:0x0147), top: B:65:0x0001 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public synchronized void reportDifferences(java.util.Map<java.lang.String, org.eclipse.jetty.util.Scanner.TimeNSize> r9, java.util.Map<java.lang.String, org.eclipse.jetty.util.Scanner.TimeNSize> r10) {
        /*
            Method dump skipped, instructions count: 354
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.Scanner.reportDifferences(java.util.Map, java.util.Map):void");
    }

    private void scanFile(File f, Map<String, TimeNSize> scanInfoMap, int depth) {
        try {
            if (!f.exists()) {
                return;
            }
            if ((f.isFile() || (depth > 0 && this._reportDirs && f.isDirectory())) && (this._filter == null || (this._filter != null && this._filter.accept(f.getParentFile(), f.getName())))) {
                String name = f.getCanonicalPath();
                scanInfoMap.put(name, new TimeNSize(f.lastModified(), f.length()));
            }
            if (f.isDirectory()) {
                if (depth < this._scanDepth || this._scanDepth == -1 || this._scanDirs.contains(f)) {
                    File[] files = f.listFiles();
                    if (files == null) {
                        LOG.warn("Error listing files in directory {}", f);
                        return;
                    }
                    for (File file : files) {
                        scanFile(file, scanInfoMap, depth + 1);
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Error scanning watched files", e);
        }
    }

    private void warn(Object listener, String filename, Throwable th) {
        Logger logger = LOG;
        logger.warn(listener + " failed on '" + filename, th);
    }

    private void reportAddition(String filename) {
        for (Listener l : this._listeners) {
            try {
                if (l instanceof DiscreteListener) {
                    ((DiscreteListener) l).fileAdded(filename);
                }
            } catch (Error e) {
                warn(l, filename, e);
            } catch (Exception e2) {
                warn(l, filename, e2);
            }
        }
    }

    private void reportRemoval(String filename) {
        for (Object l : this._listeners) {
            try {
                if (l instanceof DiscreteListener) {
                    ((DiscreteListener) l).fileRemoved(filename);
                }
            } catch (Error e) {
                warn(l, filename, e);
            } catch (Exception e2) {
                warn(l, filename, e2);
            }
        }
    }

    private void reportChange(String filename) {
        for (Listener l : this._listeners) {
            try {
                if (l instanceof DiscreteListener) {
                    ((DiscreteListener) l).fileChanged(filename);
                }
            } catch (Error e) {
                warn(l, filename, e);
            } catch (Exception e2) {
                warn(l, filename, e2);
            }
        }
    }

    private void reportBulkChanges(List<String> filenames) {
        for (Listener l : this._listeners) {
            try {
                if (l instanceof BulkListener) {
                    ((BulkListener) l).filesChanged(filenames);
                }
            } catch (Error e) {
                warn(l, filenames.toString(), e);
            } catch (Exception e2) {
                warn(l, filenames.toString(), e2);
            }
        }
    }

    private void reportScanStart(int cycle) {
        for (Listener listener : this._listeners) {
            try {
                if (listener instanceof ScanCycleListener) {
                    ((ScanCycleListener) listener).scanStarted(cycle);
                }
            } catch (Exception e) {
                Logger logger = LOG;
                logger.warn(listener + " failed on scan start for cycle " + cycle, e);
            }
        }
    }

    private void reportScanEnd(int cycle) {
        for (Listener listener : this._listeners) {
            try {
                if (listener instanceof ScanCycleListener) {
                    ((ScanCycleListener) listener).scanEnded(cycle);
                }
            } catch (Exception e) {
                Logger logger = LOG;
                logger.warn(listener + " failed on scan end for cycle " + cycle, e);
            }
        }
    }
}
