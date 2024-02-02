package org.eclipse.jetty.servlet.jmx;

import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.servlet.Holder;
/* loaded from: classes.dex */
public class HolderMBean extends ObjectMBean {
    public HolderMBean(Object managedObject) {
        super(managedObject);
    }

    public String getObjectNameBasis() {
        if (this._managed != null && (this._managed instanceof Holder)) {
            Holder holder = (Holder) this._managed;
            String name = holder.getName();
            if (name != null) {
                return name;
            }
        }
        return super.getObjectNameBasis();
    }
}
