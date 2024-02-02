package org.fourthline.cling.support.model;
/* loaded from: classes.dex */
public class DIDLAttribute {
    private String namespaceURI;
    private String prefix;
    private String value;

    public DIDLAttribute(String namespaceURI, String prefix, String value) {
        this.namespaceURI = namespaceURI;
        this.prefix = prefix;
        this.value = value;
    }

    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getValue() {
        return this.value;
    }
}
