package org.seamless.xhtml;

import javax.xml.xpath.XPath;
import org.seamless.xhtml.XHTML;
import org.w3c.dom.Element;
/* loaded from: classes.dex */
public class Link extends XHTMLElement {
    public Link(XPath xpath, Element element) {
        super(xpath, element);
    }

    public Href getHref() {
        return Href.fromString(getAttribute(XHTML.ATTR.href));
    }

    public String getRel() {
        return getAttribute(XHTML.ATTR.rel);
    }

    public String getRev() {
        return getAttribute(XHTML.ATTR.rev);
    }
}
