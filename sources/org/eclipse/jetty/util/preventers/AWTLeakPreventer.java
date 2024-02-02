package org.eclipse.jetty.util.preventers;

import java.awt.Toolkit;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AWTLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        Logger logger = LOG;
        logger.debug("Pinning classloader for java.awt.EventQueue using " + loader, new Object[0]);
        Toolkit.getDefaultToolkit();
    }
}
