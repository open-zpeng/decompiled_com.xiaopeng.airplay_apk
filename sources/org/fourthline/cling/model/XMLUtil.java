package org.fourthline.cling.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/* loaded from: classes.dex */
public class XMLUtil {
    public static String documentToString(Document document) throws Exception {
        return documentToString(document, true);
    }

    public static String documentToString(Document document, boolean standalone) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"");
        sb.append(standalone ? "yes" : "no");
        sb.append("\"?>");
        String prol = sb.toString();
        return prol + nodeToString(document.getDocumentElement(), new HashSet(), document.getDocumentElement().getNamespaceURI());
    }

    public static String documentToFragmentString(Document document) throws Exception {
        return nodeToString(document.getDocumentElement(), new HashSet(), document.getDocumentElement().getNamespaceURI());
    }

    protected static String nodeToString(Node node, Set<String> parentPrefixes, String namespaceURI) throws Exception {
        if (node == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        if (node instanceof Element) {
            Element element = (Element) node;
            b.append("<");
            b.append(element.getNodeName());
            Map<String, String> thisLevelPrefixes = new HashMap<>();
            if (element.getPrefix() != null && !parentPrefixes.contains(element.getPrefix())) {
                thisLevelPrefixes.put(element.getPrefix(), element.getNamespaceURI());
            }
            if (element.hasAttributes()) {
                NamedNodeMap map = element.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    Node attr = map.item(i);
                    if (!attr.getNodeName().startsWith("xmlns")) {
                        if (attr.getPrefix() != null && !parentPrefixes.contains(attr.getPrefix())) {
                            thisLevelPrefixes.put(attr.getPrefix(), element.getNamespaceURI());
                        }
                        b.append(" ");
                        b.append(attr.getNodeName());
                        b.append("=\"");
                        b.append(attr.getNodeValue());
                        b.append("\"");
                    }
                }
            }
            if (namespaceURI != null && !thisLevelPrefixes.containsValue(namespaceURI) && !namespaceURI.equals(element.getParentNode().getNamespaceURI())) {
                b.append(" xmlns=\"");
                b.append(namespaceURI);
                b.append("\"");
            }
            for (Map.Entry<String, String> entry : thisLevelPrefixes.entrySet()) {
                b.append(" xmlns:");
                b.append(entry.getKey());
                b.append("=\"");
                b.append(entry.getValue());
                b.append("\"");
                parentPrefixes.add(entry.getKey());
            }
            NodeList children = element.getChildNodes();
            boolean hasOnlyAttributes = true;
            int i2 = 0;
            while (true) {
                if (i2 >= children.getLength()) {
                    break;
                }
                Node child = children.item(i2);
                if (child.getNodeType() == 2) {
                    i2++;
                } else {
                    hasOnlyAttributes = false;
                    break;
                }
            }
            if (!hasOnlyAttributes) {
                b.append(">");
                for (int i3 = 0; i3 < children.getLength(); i3++) {
                    b.append(nodeToString(children.item(i3), parentPrefixes, children.item(i3).getNamespaceURI()));
                }
                b.append("</");
                b.append(element.getNodeName());
                b.append(">");
            } else {
                b.append("/>");
            }
            for (String thisLevelPrefix : thisLevelPrefixes.keySet()) {
                parentPrefixes.remove(thisLevelPrefix);
            }
        } else if (node.getNodeValue() != null) {
            b.append(encodeText(node.getNodeValue(), node instanceof Attr));
        }
        return b.toString();
    }

    public static String encodeText(String s) {
        return encodeText(s, true);
    }

    public static String encodeText(String s, boolean encodeQuotes) {
        String s2 = s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        if (encodeQuotes) {
            return s2.replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
        }
        return s2;
    }

    public static Element appendNewElement(Document document, Element parent, Enum el) {
        return appendNewElement(document, parent, el.toString());
    }

    public static Element appendNewElement(Document document, Element parent, String element) {
        Element child = document.createElement(element);
        parent.appendChild(child);
        return child;
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, Enum el, Object content) {
        return appendNewElementIfNotNull(document, parent, el, content, (String) null);
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, Enum el, Object content, String namespace) {
        return appendNewElementIfNotNull(document, parent, el.toString(), content, namespace);
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, String element, Object content) {
        return appendNewElementIfNotNull(document, parent, element, content, (String) null);
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, String element, Object content, String namespace) {
        return content == null ? parent : appendNewElement(document, parent, element, content, namespace);
    }

    public static Element appendNewElement(Document document, Element parent, String element, Object content) {
        return appendNewElement(document, parent, element, content, null);
    }

    public static Element appendNewElement(Document document, Element parent, String element, Object content, String namespace) {
        Element childElement;
        if (namespace != null) {
            childElement = document.createElementNS(namespace, element);
        } else {
            childElement = document.createElement(element);
        }
        if (content != null) {
            childElement.appendChild(document.createTextNode(content.toString()));
        }
        parent.appendChild(childElement);
        return childElement;
    }

    public static String getTextContent(Node node) {
        StringBuffer buffer = new StringBuffer();
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child.getNodeType() == 3) {
                buffer.append(child.getNodeValue());
            }
        }
        return buffer.toString();
    }
}
