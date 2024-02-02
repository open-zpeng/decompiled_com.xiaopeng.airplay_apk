package org.seamless.xhtml;

import javax.xml.xpath.XPath;
import org.seamless.xhtml.XHTML;
import org.seamless.xml.DOMElement;
import org.w3c.dom.Element;
/* loaded from: classes.dex */
public class Root extends XHTMLElement {
    public Root(XPath xpath, Element element) {
        super(xpath, element);
    }

    public Head getHead() {
        return new DOMElement<XHTMLElement, XHTMLElement>.Builder<Head>(this) { // from class: org.seamless.xhtml.Root.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.seamless.xml.DOMElement.Builder
            public Head build(Element element) {
                return new Head(Root.this.getXpath(), element);
            }
        }.firstChildOrNull(XHTML.ELEMENT.head.name());
    }

    public Body getBody() {
        return new DOMElement<XHTMLElement, XHTMLElement>.Builder<Body>(this) { // from class: org.seamless.xhtml.Root.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.seamless.xml.DOMElement.Builder
            public Body build(Element element) {
                return new Body(Root.this.getXpath(), element);
            }
        }.firstChildOrNull(XHTML.ELEMENT.body.name());
    }
}
