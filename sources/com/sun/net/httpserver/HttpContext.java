package com.sun.net.httpserver;

import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public abstract class HttpContext {
    public abstract Map<String, Object> getAttributes();

    public abstract Authenticator getAuthenticator();

    public abstract List<Filter> getFilters();

    public abstract HttpHandler getHandler();

    public abstract String getPath();

    public abstract HttpServer getServer();

    public abstract Authenticator setAuthenticator(Authenticator authenticator);

    public abstract void setHandler(HttpHandler httpHandler);
}
