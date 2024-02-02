package org.fourthline.cling.support.model;

import java.net.URI;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/* loaded from: classes.dex */
public class DescMeta<M> {
    protected String id;
    protected M metadata;
    protected URI nameSpace;
    protected String type;

    public DescMeta() {
    }

    public DescMeta(String id, String type, URI nameSpace, M metadata) {
        this.id = id;
        this.type = type;
        this.nameSpace = nameSpace;
        this.metadata = metadata;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URI getNameSpace() {
        return this.nameSpace;
    }

    public void setNameSpace(URI nameSpace) {
        this.nameSpace = nameSpace;
    }

    public M getMetadata() {
        return this.metadata;
    }

    public void setMetadata(M metadata) {
        this.metadata = metadata;
    }

    public Document createMetadataDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document d = factory.newDocumentBuilder().newDocument();
            Element rootElement = d.createElementNS(DIDLContent.DESC_WRAPPER_NAMESPACE_URI, "desc-wrapper");
            d.appendChild(rootElement);
            return d;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
