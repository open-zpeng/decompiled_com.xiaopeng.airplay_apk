package sun.net.httpserver;

import java.util.Iterator;
import java.util.LinkedList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ContextList {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int MAX_CONTEXTS = 50;
    LinkedList<HttpContextImpl> list = new LinkedList<>();

    public synchronized void add(HttpContextImpl httpContextImpl) {
        this.list.add(httpContextImpl);
    }

    public synchronized int size() {
        return this.list.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized HttpContextImpl findContext(String str, String str2) {
        return findContext(str, str2, false);
    }

    synchronized HttpContextImpl findContext(String str, String str2, boolean z) {
        HttpContextImpl httpContextImpl;
        String lowerCase = str.toLowerCase();
        String str3 = "";
        httpContextImpl = null;
        Iterator<HttpContextImpl> it = this.list.iterator();
        while (it.hasNext()) {
            HttpContextImpl next = it.next();
            if (next.getProtocol().equals(lowerCase)) {
                String path = next.getPath();
                if (!z || path.equals(str2)) {
                    if (z || str2.startsWith(path)) {
                        if (path.length() > str3.length()) {
                            httpContextImpl = next;
                            str3 = path;
                        }
                    }
                }
            }
        }
        return httpContextImpl;
    }

    public synchronized void remove(String str, String str2) throws IllegalArgumentException {
        HttpContextImpl findContext = findContext(str, str2, true);
        if (findContext == null) {
            throw new IllegalArgumentException("cannot remove element from list");
        }
        this.list.remove(findContext);
    }

    public synchronized void remove(HttpContextImpl httpContextImpl) throws IllegalArgumentException {
        Iterator<HttpContextImpl> it = this.list.iterator();
        while (it.hasNext()) {
            HttpContextImpl next = it.next();
            if (next.equals(httpContextImpl)) {
                this.list.remove(next);
            }
        }
        throw new IllegalArgumentException("no such context in list");
    }
}
