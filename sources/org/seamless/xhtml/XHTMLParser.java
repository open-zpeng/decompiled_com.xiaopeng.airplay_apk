package org.seamless.xhtml;

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPath;
import org.seamless.xhtml.XHTML;
import org.seamless.xml.DOMParser;
import org.seamless.xml.NamespaceContextMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/* loaded from: classes.dex */
public class XHTMLParser extends DOMParser<XHTML> {
    public XHTMLParser() {
        super(XHTML.createSchemaSources());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.seamless.xml.DOMParser
    public XHTML createDOM(Document document) {
        if (document != null) {
            return new XHTML(document);
        }
        return null;
    }

    public void checkDuplicateIdentifiers(XHTML document) throws IllegalStateException {
        final Set<String> identifiers = new HashSet<>();
        accept(document.getW3CDocument().getDocumentElement(), new DOMParser.NodeVisitor((short) 1) { // from class: org.seamless.xhtml.XHTMLParser.1
            @Override // org.seamless.xml.DOMParser.NodeVisitor
            public void visit(Node node) {
                Element element = (Element) node;
                String id = element.getAttribute(XHTML.ATTR.id.name());
                if (!"".equals(id)) {
                    if (identifiers.contains(id)) {
                        throw new IllegalStateException("Duplicate identifier, override/change value: " + id);
                    }
                    identifiers.add(id);
                }
            }
        });
    }

    public NamespaceContextMap createDefaultNamespaceContext(String... optionalPrefixes) {
        NamespaceContextMap ctx = new NamespaceContextMap() { // from class: org.seamless.xhtml.XHTMLParser.2
            @Override // org.seamless.xml.NamespaceContextMap
            protected String getDefaultNamespaceURI() {
                return XHTML.NAMESPACE_URI;
            }
        };
        for (String optionalPrefix : optionalPrefixes) {
            ctx.put(optionalPrefix, XHTML.NAMESPACE_URI);
        }
        return ctx;
    }

    public XPath createXPath() {
        return super.createXPath(createDefaultNamespaceContext(XHTMLElement.XPATH_PREFIX));
    }
}
