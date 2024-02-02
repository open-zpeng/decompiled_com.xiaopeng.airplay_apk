package org.eclipse.jetty.server.handler.jmx;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Attributes;
/* loaded from: classes.dex */
public class ContextHandlerMBean extends AbstractHandlerMBean {
    public ContextHandlerMBean(Object managedObject) {
        super(managedObject);
    }

    public Map getContextAttributes() {
        Map map = new HashMap();
        Attributes attrs = ((ContextHandler) this._managed).getAttributes();
        Enumeration en = attrs.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            Object value = attrs.getAttribute(name);
            map.put(name, value);
        }
        return map;
    }

    public void setContextAttribute(String name, Object value) {
        Attributes attrs = ((ContextHandler) this._managed).getAttributes();
        attrs.setAttribute(name, value);
    }

    public void setContextAttribute(String name, String value) {
        Attributes attrs = ((ContextHandler) this._managed).getAttributes();
        attrs.setAttribute(name, value);
    }

    public void removeContextAttribute(String name) {
        Attributes attrs = ((ContextHandler) this._managed).getAttributes();
        attrs.removeAttribute(name);
    }
}
