package org.eclipse.jetty.util.preventers;

import javax.imageio.ImageIO;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AppContextLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        Logger logger = LOG;
        logger.debug("Pinning classloader for AppContext.getContext() with " + loader, new Object[0]);
        ImageIO.getUseCache();
    }
}
