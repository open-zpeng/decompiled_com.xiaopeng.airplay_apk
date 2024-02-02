package org.eclipse.jetty.server;

import javax.servlet.http.Cookie;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class CookieCutter {
    private static final Logger LOG = Log.getLogger(CookieCutter.class);
    private Cookie[] _cookies;
    int _fields;
    private Cookie[] _lastCookies;
    Object _lazyFields;

    public Cookie[] getCookies() {
        if (this._cookies != null) {
            return this._cookies;
        }
        if (this._lastCookies != null && this._lazyFields != null && this._fields == LazyList.size(this._lazyFields)) {
            this._cookies = this._lastCookies;
        } else {
            parseFields();
        }
        this._lastCookies = this._cookies;
        return this._cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this._cookies = cookies;
        this._lastCookies = null;
        this._lazyFields = null;
        this._fields = 0;
    }

    public void reset() {
        this._cookies = null;
        this._fields = 0;
    }

    public void addCookieField(String f) {
        if (f == null) {
            return;
        }
        String f2 = f.trim();
        if (f2.length() == 0) {
            return;
        }
        if (LazyList.size(this._lazyFields) > this._fields) {
            if (f2.equals(LazyList.get(this._lazyFields, this._fields))) {
                this._fields++;
                return;
            } else {
                while (LazyList.size(this._lazyFields) > this._fields) {
                    this._lazyFields = LazyList.remove(this._lazyFields, this._fields);
                }
            }
        }
        this._cookies = null;
        this._lastCookies = null;
        Object obj = this._lazyFields;
        int i = this._fields;
        this._fields = i + 1;
        this._lazyFields = LazyList.add(obj, i, f2);
    }

    /* JADX WARN: Removed duplicated region for block: B:81:0x0117 A[ADDED_TO_REGION] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void parseFields() {
        /*
            Method dump skipped, instructions count: 495
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.CookieCutter.parseFields():void");
    }
}
