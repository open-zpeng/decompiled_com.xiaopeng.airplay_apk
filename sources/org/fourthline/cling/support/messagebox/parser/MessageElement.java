package org.fourthline.cling.support.messagebox.parser;

import javax.xml.xpath.XPath;
import org.seamless.xml.DOMElement;
import org.w3c.dom.Element;
/* loaded from: classes.dex */
public class MessageElement extends DOMElement<MessageElement, MessageElement> {
    public static final String XPATH_PREFIX = "m";

    public MessageElement(XPath xpath, Element element) {
        super(xpath, element);
    }

    @Override // org.seamless.xml.DOMElement
    protected String prefix(String localName) {
        return "m:" + localName;
    }

    @Override // org.seamless.xml.DOMElement
    protected DOMElement<MessageElement, MessageElement>.Builder<MessageElement> createParentBuilder(DOMElement el) {
        return new DOMElement<MessageElement, MessageElement>.Builder<MessageElement>(el) { // from class: org.fourthline.cling.support.messagebox.parser.MessageElement.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.seamless.xml.DOMElement.Builder
            public MessageElement build(Element element) {
                return new MessageElement(MessageElement.this.getXpath(), element);
            }
        };
    }

    @Override // org.seamless.xml.DOMElement
    protected DOMElement<MessageElement, MessageElement>.ArrayBuilder<MessageElement> createChildBuilder(DOMElement el) {
        return new DOMElement<MessageElement, MessageElement>.ArrayBuilder<MessageElement>(el) { // from class: org.fourthline.cling.support.messagebox.parser.MessageElement.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.seamless.xml.DOMElement.ArrayBuilder
            public MessageElement[] newChildrenArray(int length) {
                return new MessageElement[length];
            }

            @Override // org.seamless.xml.DOMElement.Builder
            public MessageElement build(Element element) {
                return new MessageElement(MessageElement.this.getXpath(), element);
            }
        };
    }
}
