package org.eclipse.jetty.server.jmx;

import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
/* loaded from: classes.dex */
public class ServerMBean extends ObjectMBean {
    private final Server server;
    private final long startupTime;

    public ServerMBean(Object managedObject) {
        super(managedObject);
        this.startupTime = System.currentTimeMillis();
        this.server = (Server) managedObject;
    }

    public Handler[] getContexts() {
        return this.server.getChildHandlersByClass(ContextHandler.class);
    }

    public long getStartupTime() {
        return this.startupTime;
    }
}
