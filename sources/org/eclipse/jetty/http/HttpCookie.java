package org.eclipse.jetty.http;
/* loaded from: classes.dex */
public class HttpCookie {
    private final String _comment;
    private final String _domain;
    private final boolean _httpOnly;
    private final int _maxAge;
    private final String _name;
    private final String _path;
    private final boolean _secure;
    private final String _value;
    private final int _version;

    public HttpCookie(String name, String value) {
        this._name = name;
        this._value = value;
        this._comment = null;
        this._domain = null;
        this._httpOnly = false;
        this._maxAge = -1;
        this._path = null;
        this._secure = false;
        this._version = 0;
    }

    public HttpCookie(String name, String value, String domain, String path) {
        this._name = name;
        this._value = value;
        this._comment = null;
        this._domain = domain;
        this._httpOnly = false;
        this._maxAge = -1;
        this._path = path;
        this._secure = false;
        this._version = 0;
    }

    public HttpCookie(String name, String value, int maxAge) {
        this._name = name;
        this._value = value;
        this._comment = null;
        this._domain = null;
        this._httpOnly = false;
        this._maxAge = maxAge;
        this._path = null;
        this._secure = false;
        this._version = 0;
    }

    public HttpCookie(String name, String value, String domain, String path, int maxAge, boolean httpOnly, boolean secure) {
        this._comment = null;
        this._domain = domain;
        this._httpOnly = httpOnly;
        this._maxAge = maxAge;
        this._name = name;
        this._path = path;
        this._secure = secure;
        this._value = value;
        this._version = 0;
    }

    public HttpCookie(String name, String value, String domain, String path, int maxAge, boolean httpOnly, boolean secure, String comment, int version) {
        this._comment = comment;
        this._domain = domain;
        this._httpOnly = httpOnly;
        this._maxAge = maxAge;
        this._name = name;
        this._path = path;
        this._secure = secure;
        this._value = value;
        this._version = version;
    }

    public String getName() {
        return this._name;
    }

    public String getValue() {
        return this._value;
    }

    public String getComment() {
        return this._comment;
    }

    public String getDomain() {
        return this._domain;
    }

    public int getMaxAge() {
        return this._maxAge;
    }

    public String getPath() {
        return this._path;
    }

    public boolean isSecure() {
        return this._secure;
    }

    public int getVersion() {
        return this._version;
    }

    public boolean isHttpOnly() {
        return this._httpOnly;
    }
}
