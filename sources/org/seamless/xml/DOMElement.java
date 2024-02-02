package org.seamless.xml;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.seamless.xml.DOMElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/* loaded from: classes.dex */
public abstract class DOMElement<CHILD extends DOMElement, PARENT extends DOMElement> {
    private Element element;
    private final XPath xpath;
    public final DOMElement<CHILD, PARENT>.Builder<PARENT> PARENT_BUILDER = createParentBuilder(this);
    public final DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> CHILD_BUILDER = createChildBuilder(this);

    protected abstract DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> createChildBuilder(DOMElement dOMElement);

    protected abstract DOMElement<CHILD, PARENT>.Builder<PARENT> createParentBuilder(DOMElement dOMElement);

    public DOMElement(XPath xpath, Element element) {
        this.xpath = xpath;
        this.element = element;
    }

    public Element getW3CElement() {
        return this.element;
    }

    public String getElementName() {
        return getW3CElement().getNodeName();
    }

    public String getContent() {
        return getW3CElement().getTextContent();
    }

    public DOMElement<CHILD, PARENT> setContent(String content) {
        getW3CElement().setTextContent(content);
        return this;
    }

    public String getAttribute(String attribute) {
        String v = getW3CElement().getAttribute(attribute);
        if (v.length() > 0) {
            return v;
        }
        return null;
    }

    public DOMElement setAttribute(String attribute, String value) {
        getW3CElement().setAttribute(attribute, value);
        return this;
    }

    public PARENT getParent() {
        return (PARENT) this.PARENT_BUILDER.build((Element) getW3CElement().getParentNode());
    }

    public CHILD[] getChildren() {
        NodeList nodes = getW3CElement().getChildNodes();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == 1) {
                arrayList.add(this.CHILD_BUILDER.build((Element) node));
            }
        }
        return (CHILD[]) ((DOMElement[]) arrayList.toArray(this.CHILD_BUILDER.newChildrenArray(arrayList.size())));
    }

    public CHILD[] getChildren(String name) {
        Collection<CHILD> list = getXPathChildElements(this.CHILD_BUILDER, prefix(name));
        return (CHILD[]) ((DOMElement[]) list.toArray(this.CHILD_BUILDER.newChildrenArray(list.size())));
    }

    public CHILD getRequiredChild(String name) throws ParserException {
        CHILD[] children = getChildren(name);
        if (children.length != 1) {
            throw new ParserException("Required single child element of '" + getElementName() + "' not found: " + name);
        }
        return children[0];
    }

    public CHILD[] findChildren(String name) {
        DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> arrayBuilder = this.CHILD_BUILDER;
        Collection<CHILD> list = getXPathChildElements(arrayBuilder, "descendant::" + prefix(name));
        return (CHILD[]) ((DOMElement[]) list.toArray(this.CHILD_BUILDER.newChildrenArray(list.size())));
    }

    public CHILD findChildWithIdentifier(String id) {
        DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> arrayBuilder = this.CHILD_BUILDER;
        Collection<CHILD> list = getXPathChildElements(arrayBuilder, "descendant::" + prefix("*") + "[@id=\"" + id + "\"]");
        if (list.size() == 1) {
            return list.iterator().next();
        }
        return null;
    }

    public CHILD getFirstChild(String name) {
        DOMElement<CHILD, PARENT>.ArrayBuilder<CHILD> arrayBuilder = this.CHILD_BUILDER;
        return getXPathChildElement(arrayBuilder, prefix(name) + "[1]");
    }

    public CHILD createChild(String name) {
        return createChild(name, null);
    }

    public CHILD createChild(String name, String namespaceURI) {
        CHILD child = (CHILD) this.CHILD_BUILDER.build(namespaceURI == null ? getW3CElement().getOwnerDocument().createElement(name) : getW3CElement().getOwnerDocument().createElementNS(namespaceURI, name));
        getW3CElement().appendChild(child.getW3CElement());
        return child;
    }

    public CHILD appendChild(CHILD el, boolean copy) {
        CHILD el2 = adoptOrImport(getW3CElement().getOwnerDocument(), el, copy);
        getW3CElement().appendChild(el2.getW3CElement());
        return el2;
    }

    public CHILD replaceChild(CHILD original, CHILD replacement, boolean copy) {
        CHILD replacement2 = adoptOrImport(getW3CElement().getOwnerDocument(), replacement, copy);
        getW3CElement().replaceChild(replacement2.getW3CElement(), original.getW3CElement());
        return replacement2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void replaceEqualChild(DOMElement source, String identifier) {
        CHILD findChildWithIdentifier = findChildWithIdentifier(identifier);
        DOMElement replacement = source.findChildWithIdentifier(identifier);
        findChildWithIdentifier.getParent().replaceChild(findChildWithIdentifier, replacement, true);
    }

    public void removeChild(CHILD el) {
        getW3CElement().removeChild(el.getW3CElement());
    }

    public void removeChildren() {
        NodeList children = getW3CElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            getW3CElement().removeChild(child);
        }
    }

    protected CHILD adoptOrImport(Document document, CHILD child, boolean copy) {
        if (document != null) {
            if (copy) {
                return (CHILD) this.CHILD_BUILDER.build((Element) document.importNode(child.getW3CElement(), true));
            }
            return (CHILD) this.CHILD_BUILDER.build((Element) document.adoptNode(child.getW3CElement()));
        }
        return child;
    }

    public String toSimpleXMLString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(getElementName());
        NamedNodeMap map = getW3CElement().getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node attr = map.item(i);
            sb.append(" ");
            sb.append(attr.getNodeName());
            sb.append("=\"");
            sb.append(attr.getTextContent());
            sb.append("\"");
        }
        if (getContent().length() > 0) {
            sb.append(">");
            sb.append(getContent());
            sb.append("</");
            sb.append(getElementName());
            sb.append(">");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(getClass().getSimpleName());
        sb.append(") ");
        sb.append(getW3CElement() == null ? "UNBOUND" : getElementName());
        return sb.toString();
    }

    public XPath getXpath() {
        return this.xpath;
    }

    protected String prefix(String localName) {
        return localName;
    }

    public Collection<PARENT> getXPathParentElements(DOMElement<CHILD, PARENT>.Builder<CHILD> builder, String expr) {
        return getXPathElements(builder, expr);
    }

    public Collection<CHILD> getXPathChildElements(DOMElement<CHILD, PARENT>.Builder<CHILD> builder, String expr) {
        return getXPathElements(builder, expr);
    }

    public PARENT getXPathParentElement(DOMElement<CHILD, PARENT>.Builder<PARENT> builder, String expr) {
        Node node = (Node) getXPathResult(getW3CElement(), expr, XPathConstants.NODE);
        if (node == null || node.getNodeType() != 1) {
            return null;
        }
        return (PARENT) builder.build((Element) node);
    }

    public CHILD getXPathChildElement(DOMElement<CHILD, PARENT>.Builder<CHILD> builder, String expr) {
        Node node = (Node) getXPathResult(getW3CElement(), expr, XPathConstants.NODE);
        if (node == null || node.getNodeType() != 1) {
            return null;
        }
        return (CHILD) builder.build((Element) node);
    }

    public Collection getXPathElements(Builder builder, String expr) {
        Collection col = new ArrayList();
        NodeList result = (NodeList) getXPathResult(getW3CElement(), expr, XPathConstants.NODESET);
        for (int i = 0; i < result.getLength(); i++) {
            DOMElement e = builder.build((Element) result.item(i));
            col.add(e);
        }
        return col;
    }

    public String getXPathString(XPath xpath, String expr) {
        return getXPathResult(getW3CElement(), expr, null).toString();
    }

    public Object getXPathResult(String expr, QName result) {
        return getXPathResult(getW3CElement(), expr, result);
    }

    public Object getXPathResult(Node context, String expr, QName result) {
        try {
            if (result == null) {
                return this.xpath.evaluate(expr, context);
            }
            return this.xpath.evaluate(expr, context, result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* loaded from: classes.dex */
    public abstract class Builder<T extends DOMElement> {
        public DOMElement element;

        public abstract T build(Element element);

        /* JADX INFO: Access modifiers changed from: protected */
        public Builder(DOMElement element) {
            this.element = element;
        }

        public T firstChildOrNull(String elementName) {
            DOMElement el = this.element.getFirstChild(elementName);
            if (el != null) {
                return build(el.getW3CElement());
            }
            return null;
        }
    }

    /* loaded from: classes.dex */
    public abstract class ArrayBuilder<T extends DOMElement> extends DOMElement<CHILD, PARENT>.Builder<T> {
        public abstract T[] newChildrenArray(int i);

        /* JADX INFO: Access modifiers changed from: protected */
        public ArrayBuilder(DOMElement element) {
            super(element);
        }

        public T[] getChildElements() {
            return buildArray(this.element.getChildren());
        }

        public T[] getChildElements(String elementName) {
            return buildArray(this.element.getChildren(elementName));
        }

        protected T[] buildArray(DOMElement[] list) {
            T[] children = newChildrenArray(list.length);
            for (int i = 0; i < children.length; i++) {
                children[i] = build(list[i].getW3CElement());
            }
            return children;
        }
    }
}
