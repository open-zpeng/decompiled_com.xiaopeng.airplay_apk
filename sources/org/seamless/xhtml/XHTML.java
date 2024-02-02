package org.seamless.xhtml;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import org.seamless.xml.DOM;
import org.w3c.dom.Document;
/* loaded from: classes.dex */
public class XHTML extends DOM {
    public static final String NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
    public static final String SCHEMA_RESOURCE = "org/seamless/schemas/xhtml1-strict.xsd";

    /* loaded from: classes.dex */
    public enum ATTR {
        id,
        style,
        title,
        type,
        href,
        name,
        content,
        scheme,
        rel,
        rev,
        colspan,
        rowspan,
        src,
        alt,
        action,
        method;
        
        public static final String CLASS = "class";
    }

    /* loaded from: classes.dex */
    public enum ELEMENT {
        html,
        head,
        title,
        meta,
        link,
        script,
        style,
        body,
        div,
        span,
        p,
        object,
        a,
        img,
        pre,
        h1,
        h2,
        h3,
        h4,
        h5,
        h6,
        table,
        thead,
        tfoot,
        tbody,
        tr,
        th,
        td,
        ul,
        ol,
        li,
        dl,
        dt,
        dd,
        form,
        input,
        select,
        option
    }

    public static Source[] createSchemaSources() {
        return new Source[]{new StreamSource(XHTML.class.getClassLoader().getResourceAsStream(SCHEMA_RESOURCE))};
    }

    public XHTML(Document dom) {
        super(dom);
    }

    public Root createRoot(XPath xpath, ELEMENT elememt) {
        super.createRoot(elememt.name());
        return getRoot(xpath);
    }

    @Override // org.seamless.xml.DOM
    public String getRootElementNamespace() {
        return NAMESPACE_URI;
    }

    @Override // org.seamless.xml.DOM
    public Root getRoot(XPath xpath) {
        return new Root(xpath, getW3CDocument().getDocumentElement());
    }

    @Override // org.seamless.xml.DOM
    public XHTML copy() {
        return new XHTML((Document) getW3CDocument().cloneNode(true));
    }
}
