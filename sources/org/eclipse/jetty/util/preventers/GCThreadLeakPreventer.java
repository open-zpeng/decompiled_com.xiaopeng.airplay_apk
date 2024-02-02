package org.eclipse.jetty.util.preventers;

import java.lang.reflect.Method;
/* loaded from: classes.dex */
public class GCThreadLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        try {
            Class clazz = Class.forName("sun.misc.GC");
            Method requestLatency = clazz.getMethod("requestLatency", Long.TYPE);
            requestLatency.invoke(null, 9223372036854775806L);
        } catch (ClassNotFoundException e) {
            LOG.ignore(e);
        } catch (Exception e2) {
            LOG.warn(e2);
        }
    }
}
