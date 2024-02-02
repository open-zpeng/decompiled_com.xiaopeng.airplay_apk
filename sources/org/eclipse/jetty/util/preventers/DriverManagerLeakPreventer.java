package org.eclipse.jetty.util.preventers;

import java.sql.DriverManager;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class DriverManagerLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        Logger logger = LOG;
        logger.debug("Pinning DriverManager classloader with " + loader, new Object[0]);
        DriverManager.getDrivers();
    }
}
