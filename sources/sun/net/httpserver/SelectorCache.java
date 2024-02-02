package sun.net.httpserver;

import java.io.IOException;
import java.nio.channels.Selector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.ListIterator;
/* loaded from: classes.dex */
public class SelectorCache {
    static SelectorCache cache = null;
    LinkedList<SelectorWrapper> freeSelectors = new LinkedList<>();

    private SelectorCache() {
        ((CacheCleaner) AccessController.doPrivileged(new PrivilegedAction<CacheCleaner>() { // from class: sun.net.httpserver.SelectorCache.1
            @Override // java.security.PrivilegedAction
            public CacheCleaner run() {
                CacheCleaner cacheCleaner = new CacheCleaner();
                cacheCleaner.setDaemon(true);
                return cacheCleaner;
            }
        })).start();
    }

    public static SelectorCache getSelectorCache() {
        synchronized (SelectorCache.class) {
            if (cache == null) {
                cache = new SelectorCache();
            }
        }
        return cache;
    }

    /* loaded from: classes.dex */
    private static class SelectorWrapper {
        private boolean deleteFlag;
        private Selector sel;

        private SelectorWrapper(Selector selector) {
            this.sel = selector;
            this.deleteFlag = false;
        }

        public Selector getSelector() {
            return this.sel;
        }

        public boolean getDeleteFlag() {
            return this.deleteFlag;
        }

        public void setDeleteFlag(boolean z) {
            this.deleteFlag = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized Selector getSelector() throws IOException {
        Selector open;
        if (this.freeSelectors.size() > 0) {
            open = this.freeSelectors.remove().getSelector();
        } else {
            open = Selector.open();
        }
        return open;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void freeSelector(Selector selector) {
        this.freeSelectors.add(new SelectorWrapper(selector));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class CacheCleaner extends Thread {
        CacheCleaner() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            long selCacheTimeout = ServerConfig.getSelCacheTimeout() * 1000;
            while (true) {
                try {
                    Thread.sleep(selCacheTimeout);
                } catch (Exception e) {
                }
                synchronized (SelectorCache.this.freeSelectors) {
                    ListIterator<SelectorWrapper> listIterator = SelectorCache.this.freeSelectors.listIterator();
                    while (listIterator.hasNext()) {
                        SelectorWrapper next = listIterator.next();
                        if (next.getDeleteFlag()) {
                            try {
                                next.getSelector().close();
                            } catch (IOException e2) {
                            }
                            listIterator.remove();
                        } else {
                            next.setDeleteFlag(true);
                        }
                    }
                }
            }
        }
    }
}
