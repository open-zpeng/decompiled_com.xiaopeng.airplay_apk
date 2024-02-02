package org.eclipse.jetty.util.preventers;

import java.security.Security;
/* loaded from: classes.dex */
public class SecurityProviderLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        Security.getProviders();
    }
}
