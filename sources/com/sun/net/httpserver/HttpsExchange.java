package com.sun.net.httpserver;

import javax.net.ssl.SSLSession;
/* loaded from: classes.dex */
public abstract class HttpsExchange extends HttpExchange {
    public abstract SSLSession getSSLSession();
}
