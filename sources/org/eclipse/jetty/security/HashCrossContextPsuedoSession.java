package org.eclipse.jetty.security;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/* loaded from: classes.dex */
public class HashCrossContextPsuedoSession<T> implements CrossContextPsuedoSession<T> {
    private final String _cookieName;
    private final String _cookiePath;
    private final Random _random = new SecureRandom();
    private final Map<String, T> _data = new HashMap();

    public HashCrossContextPsuedoSession(String cookieName, String cookiePath) {
        this._cookieName = cookieName;
        this._cookiePath = cookiePath == null ? "/" : cookiePath;
    }

    @Override // org.eclipse.jetty.security.CrossContextPsuedoSession
    public T fetch(HttpServletRequest request) {
        Cookie[] arr$ = request.getCookies();
        for (Cookie cookie : arr$) {
            if (this._cookieName.equals(cookie.getName())) {
                String key = cookie.getValue();
                return this._data.get(key);
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.security.CrossContextPsuedoSession
    public void store(T datum, HttpServletResponse response) {
        String key;
        synchronized (this._data) {
            do {
                key = Long.toString(Math.abs(this._random.nextLong()), 30 + ((int) (System.currentTimeMillis() % 7)));
            } while (this._data.containsKey(key));
            this._data.put(key, datum);
        }
        Cookie cookie = new Cookie(this._cookieName, key);
        cookie.setPath(this._cookiePath);
        response.addCookie(cookie);
    }

    @Override // org.eclipse.jetty.security.CrossContextPsuedoSession
    public void clear(HttpServletRequest request) {
        Cookie[] arr$ = request.getCookies();
        for (Cookie cookie : arr$) {
            if (this._cookieName.equals(cookie.getName())) {
                String key = cookie.getValue();
                this._data.remove(key);
                return;
            }
        }
    }
}
