package org.seamless.xml;

import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
/* loaded from: classes.dex */
public abstract class NamespaceContextMap extends HashMap<String, String> implements NamespaceContext {
    protected abstract String getDefaultNamespaceURI();

    @Override // javax.xml.namespace.NamespaceContext
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }
        if (prefix.equals("")) {
            return getDefaultNamespaceURI();
        }
        if (get(prefix) != null) {
            return get(prefix);
        }
        return "";
    }

    @Override // javax.xml.namespace.NamespaceContext
    public String getPrefix(String namespaceURI) {
        return null;
    }

    @Override // javax.xml.namespace.NamespaceContext
    public Iterator getPrefixes(String s) {
        return null;
    }
}
