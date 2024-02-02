package org.eclipse.jetty.server.session.jmx;

import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.jmx.AbstractHandlerMBean;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
/* loaded from: classes.dex */
public class AbstractSessionManagerMBean extends AbstractHandlerMBean {
    public AbstractSessionManagerMBean(Object managedObject) {
        super(managedObject);
    }

    @Override // org.eclipse.jetty.server.handler.jmx.AbstractHandlerMBean
    public String getObjectContextBasis() {
        ContextHandler context;
        if (this._managed != null && (this._managed instanceof AbstractSessionManager)) {
            AbstractSessionManager manager = (AbstractSessionManager) this._managed;
            String basis = null;
            SessionHandler handler = manager.getSessionHandler();
            if (handler != null && (context = (ContextHandler) AbstractHandlerContainer.findContainerOf(handler.getServer(), ContextHandler.class, handler)) != null) {
                basis = getContextName(context);
            }
            if (basis != null) {
                return basis;
            }
        }
        return super.getObjectContextBasis();
    }
}
