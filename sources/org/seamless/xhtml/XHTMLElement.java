package org.seamless.xhtml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import org.seamless.xhtml.XHTML;
import org.seamless.xml.DOMElement;
import org.w3c.dom.Element;
/* loaded from: classes.dex */
public class XHTMLElement extends DOMElement<XHTMLElement, XHTMLElement> {
    public static final String XPATH_PREFIX = "h";

    public XHTMLElement(XPath xpath, Element element) {
        super(xpath, element);
    }

    @Override // org.seamless.xml.DOMElement
    protected DOMElement<XHTMLElement, XHTMLElement>.Builder<XHTMLElement> createParentBuilder(DOMElement el) {
        return new DOMElement<XHTMLElement, XHTMLElement>.Builder<XHTMLElement>(el) { // from class: org.seamless.xhtml.XHTMLElement.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.seamless.xml.DOMElement.Builder
            public XHTMLElement build(Element element) {
                return new XHTMLElement(XHTMLElement.this.getXpath(), element);
            }
        };
    }

    @Override // org.seamless.xml.DOMElement
    protected DOMElement<XHTMLElement, XHTMLElement>.ArrayBuilder<XHTMLElement> createChildBuilder(DOMElement el) {
        return new DOMElement<XHTMLElement, XHTMLElement>.ArrayBuilder<XHTMLElement>(el) { // from class: org.seamless.xhtml.XHTMLElement.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // org.seamless.xml.DOMElement.ArrayBuilder
            public XHTMLElement[] newChildrenArray(int length) {
                return new XHTMLElement[length];
            }

            @Override // org.seamless.xml.DOMElement.Builder
            public XHTMLElement build(Element element) {
                return new XHTMLElement(XHTMLElement.this.getXpath(), element);
            }
        };
    }

    @Override // org.seamless.xml.DOMElement
    protected String prefix(String localName) {
        return "h:" + localName;
    }

    public XHTML.ELEMENT getConstant() {
        return XHTML.ELEMENT.valueOf(getElementName());
    }

    public XHTMLElement[] getChildren(XHTML.ELEMENT el) {
        return (XHTMLElement[]) super.getChildren(el.name());
    }

    public XHTMLElement getFirstChild(XHTML.ELEMENT el) {
        return (XHTMLElement) super.getFirstChild(el.name());
    }

    public XHTMLElement[] findChildren(XHTML.ELEMENT el) {
        return (XHTMLElement[]) super.findChildren(el.name());
    }

    public XHTMLElement createChild(XHTML.ELEMENT el) {
        return (XHTMLElement) super.createChild(el.name(), XHTML.NAMESPACE_URI);
    }

    public String getAttribute(XHTML.ATTR attribute) {
        return getAttribute(attribute.name());
    }

    public XHTMLElement setAttribute(XHTML.ATTR attribute, String value) {
        super.setAttribute(attribute.name(), value);
        return this;
    }

    public String getId() {
        return getAttribute(XHTML.ATTR.id);
    }

    public XHTMLElement setId(String id) {
        setAttribute(XHTML.ATTR.id, id);
        return this;
    }

    public String getTitle() {
        return getAttribute(XHTML.ATTR.title);
    }

    public XHTMLElement setTitle(String title) {
        setAttribute(XHTML.ATTR.title, title);
        return this;
    }

    public XHTMLElement setClasses(String classes) {
        setAttribute(XHTML.ATTR.CLASS, classes);
        return this;
    }

    public XHTMLElement setClasses(String[] classes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; i++) {
            sb.append(classes[i]);
            if (i != classes.length - 1) {
                sb.append(" ");
            }
        }
        setAttribute(XHTML.ATTR.CLASS, sb.toString());
        return this;
    }

    public String[] getClasses() {
        String v = getAttribute(XHTML.ATTR.CLASS);
        return v == null ? new String[0] : v.split(" ");
    }

    public Option[] getOptions() {
        return Option.fromString(getAttribute(XHTML.ATTR.style));
    }

    public Option getOption(String key) {
        Option[] arr$ = getOptions();
        for (Option option : arr$) {
            if (option.getKey().equals(key)) {
                return option;
            }
        }
        return null;
    }

    public Anchor[] findAllAnchors() {
        return findAllAnchors(null, null);
    }

    public Anchor[] findAllAnchors(String requiredScheme) {
        return findAllAnchors(requiredScheme, null);
    }

    public Anchor[] findAllAnchors(String requiredScheme, String requiredClass) {
        XHTMLElement[] elements = findChildrenWithClass(XHTML.ELEMENT.a, requiredClass);
        List<Anchor> anchors = new ArrayList<>(elements.length);
        for (XHTMLElement element : elements) {
            String href = element.getAttribute(XHTML.ATTR.href);
            if (requiredScheme == null || (href != null && href.startsWith(requiredScheme))) {
                anchors.add(new Anchor(getXpath(), element.getW3CElement()));
            }
        }
        return (Anchor[]) anchors.toArray(new Anchor[anchors.size()]);
    }

    public XHTMLElement[] findChildrenWithClass(XHTML.ELEMENT el, String clazz) {
        List<XHTMLElement> list = new ArrayList<>();
        XHTMLElement[] children = findChildren(el);
        for (XHTMLElement child : children) {
            if (clazz == null) {
                list.add(child);
            } else {
                String[] arr$ = child.getClasses();
                int len$ = arr$.length;
                int i$ = 0;
                while (true) {
                    if (i$ < len$) {
                        String c = arr$[i$];
                        if (!c.matches(clazz)) {
                            i$++;
                        } else {
                            list.add(child);
                            break;
                        }
                    }
                }
            }
        }
        return (XHTMLElement[]) list.toArray(this.CHILD_BUILDER.newChildrenArray(list.size()));
    }

    @Override // org.seamless.xml.DOMElement
    /* renamed from: setContent */
    public DOMElement<XHTMLElement, XHTMLElement> setContent2(String content) {
        super.setContent(content);
        return this;
    }

    @Override // org.seamless.xml.DOMElement
    public XHTMLElement setAttribute(String attribute, String value) {
        super.setAttribute(attribute, value);
        return this;
    }
}
