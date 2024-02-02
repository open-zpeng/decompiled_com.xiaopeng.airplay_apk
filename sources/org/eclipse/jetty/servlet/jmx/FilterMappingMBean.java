package org.eclipse.jetty.servlet.jmx;

import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.servlet.FilterMapping;
/* loaded from: classes.dex */
public class FilterMappingMBean extends ObjectMBean {
    public FilterMappingMBean(Object managedObject) {
        super(managedObject);
    }

    public String getObjectNameBasis() {
        if (this._managed != null && (this._managed instanceof FilterMapping)) {
            FilterMapping mapping = (FilterMapping) this._managed;
            String name = mapping.getFilterName();
            if (name != null) {
                return name;
            }
        }
        return super.getObjectNameBasis();
    }
}
