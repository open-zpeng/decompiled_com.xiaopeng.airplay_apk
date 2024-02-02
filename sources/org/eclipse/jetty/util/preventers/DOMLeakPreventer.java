package org.eclipse.jetty.util.preventers;

import javax.xml.parsers.DocumentBuilderFactory;
/* loaded from: classes.dex */
public class DOMLeakPreventer extends AbstractLeakPreventer {
    @Override // org.eclipse.jetty.util.preventers.AbstractLeakPreventer
    public void prevent(ClassLoader loader) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.newDocumentBuilder();
        } catch (Exception e) {
            LOG.warn(e);
        }
    }
}
