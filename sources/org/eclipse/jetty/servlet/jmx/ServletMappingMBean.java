package org.eclipse.jetty.servlet.jmx;

import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.servlet.ServletMapping;
/* loaded from: classes.dex */
public class ServletMappingMBean extends ObjectMBean {
    public ServletMappingMBean(Object managedObject) {
        super(managedObject);
    }

    public String getObjectNameBasis() {
        if (this._managed != null && (this._managed instanceof ServletMapping)) {
            ServletMapping mapping = (ServletMapping) this._managed;
            String name = mapping.getServletName();
            if (name != null) {
                return name;
            }
        }
        return super.getObjectNameBasis();
    }
}
