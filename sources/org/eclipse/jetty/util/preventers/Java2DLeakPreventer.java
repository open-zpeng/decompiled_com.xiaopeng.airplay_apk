package org.eclipse.jetty.util.preventers;
/* loaded from: classes.dex */
public class Java2DLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        try {
            Class.forName("sun.java2d.Disposer", true, loader);
        } catch (ClassNotFoundException e) {
            LOG.ignore(e);
        }
    }
}
