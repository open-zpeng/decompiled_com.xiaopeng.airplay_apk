package org.eclipse.jetty.server.handler.jmx;

import java.io.IOException;
import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AbstractHandlerMBean extends ObjectMBean {
    private static final Logger LOG = Log.getLogger(AbstractHandlerMBean.class);

    public AbstractHandlerMBean(Object managedObject) {
        super(managedObject);
    }

    public String getObjectContextBasis() {
        AbstractHandler handler;
        Server server;
        ContextHandler context;
        if (this._managed != null) {
            String basis = null;
            if (this._managed instanceof ContextHandler) {
                return null;
            }
            if ((this._managed instanceof AbstractHandler) && (server = (handler = (AbstractHandler) this._managed).getServer()) != null && (context = (ContextHandler) AbstractHandlerContainer.findContainerOf(server, ContextHandler.class, handler)) != null) {
                basis = getContextName(context);
            }
            if (basis != null) {
                return basis;
            }
        }
        return super.getObjectContextBasis();
    }

    public String getObjectNameBasis() {
        if (this._managed != null) {
            String name = null;
            if (this._managed instanceof ContextHandler) {
                ContextHandler context = (ContextHandler) this._managed;
                name = getContextName(context);
            }
            if (name != null) {
                return name;
            }
        }
        String name2 = super.getObjectNameBasis();
        return name2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getContextName(ContextHandler context) {
        String name = null;
        if (context.getContextPath() != null && context.getContextPath().length() > 0) {
            int idx = context.getContextPath().lastIndexOf(47);
            name = idx < 0 ? context.getContextPath() : context.getContextPath().substring(idx + 1);
            if (name == null || name.length() == 0) {
                name = "ROOT";
            }
        }
        if (name == null && context.getBaseResource() != null) {
            try {
                if (context.getBaseResource().getFile() != null) {
                    return context.getBaseResource().getFile().getName();
                }
                return name;
            } catch (IOException e) {
                LOG.ignore(e);
                return context.getBaseResource().getName();
            }
        }
        return name;
    }
}
