package org.seamless.xhtml;

import javax.xml.xpath.XPath;
import org.seamless.xhtml.XHTML;
import org.w3c.dom.Element;
/* loaded from: classes.dex */
public class Anchor extends XHTMLElement {
    public Anchor(XPath xpath, Element element) {
        super(xpath, element);
    }

    public String getType() {
        return getAttribute(XHTML.ATTR.type);
    }

    public Anchor setType(String type) {
        setAttribute(XHTML.ATTR.type, type);
        return this;
    }

    public Href getHref() {
        return Href.fromString(getAttribute(XHTML.ATTR.href));
    }

    public Anchor setHref(String href) {
        setAttribute(XHTML.ATTR.href, href);
        return this;
    }

    @Override // org.seamless.xml.DOMElement
    public String toString() {
        return "(Anchor) " + getAttribute(XHTML.ATTR.href);
    }
}
