package org.eclipse.jetty.server.handler;

import org.eclipse.jetty.server.Handler;
/* loaded from: classes.dex */
public class ProxyHandler extends ConnectHandler {
    public ProxyHandler() {
    }

    public ProxyHandler(Handler handler, String[] white, String[] black) {
        super(handler, white, black);
    }

    public ProxyHandler(Handler handler) {
        super(handler);
    }

    public ProxyHandler(String[] white, String[] black) {
        super(white, black);
    }
}
