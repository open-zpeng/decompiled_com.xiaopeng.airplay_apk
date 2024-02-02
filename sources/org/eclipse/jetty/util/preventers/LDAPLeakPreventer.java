package org.eclipse.jetty.util.preventers;
/* loaded from: classes.dex */
public class LDAPLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        try {
            Class.forName("com.sun.jndi.LdapPoolManager", true, loader);
        } catch (ClassNotFoundException e) {
            LOG.ignore(e);
        }
    }
}
