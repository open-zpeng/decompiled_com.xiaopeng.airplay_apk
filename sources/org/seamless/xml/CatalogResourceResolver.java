package org.seamless.xml;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
/* loaded from: classes.dex */
public class CatalogResourceResolver implements LSResourceResolver {
    private static Logger log = Logger.getLogger(CatalogResourceResolver.class.getName());
    private final Map<URI, URL> catalog;

    public CatalogResourceResolver(Map<URI, URL> catalog) {
        this.catalog = catalog;
    }

    @Override // org.w3c.dom.ls.LSResourceResolver
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        Logger logger = log;
        logger.finest("Trying to resolve system identifier URI in catalog: " + systemId);
        URL systemURL = this.catalog.get(URI.create(systemId));
        if (systemURL != null) {
            Logger logger2 = log;
            logger2.finest("Loading catalog resource: " + systemURL);
            try {
                Input i = new Input(systemURL.openStream());
                i.setBaseURI(baseURI);
                i.setSystemId(systemId);
                i.setPublicId(publicId);
                return i;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        Logger logger3 = log;
        logger3.info("System identifier not found in catalog, continuing with default resolution (this most likely means remote HTTP request!): " + systemId);
        return null;
    }

    /* loaded from: classes.dex */
    private static final class Input implements LSInput {
        InputStream in;

        public Input(InputStream in) {
            this.in = in;
        }

        @Override // org.w3c.dom.ls.LSInput
        public Reader getCharacterStream() {
            return null;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setCharacterStream(Reader characterStream) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public InputStream getByteStream() {
            return this.in;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setByteStream(InputStream byteStream) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public String getStringData() {
            return null;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setStringData(String stringData) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public String getSystemId() {
            return null;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setSystemId(String systemId) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public String getPublicId() {
            return null;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setPublicId(String publicId) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public String getBaseURI() {
            return null;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setBaseURI(String baseURI) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public String getEncoding() {
            return null;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setEncoding(String encoding) {
        }

        @Override // org.w3c.dom.ls.LSInput
        public boolean getCertifiedText() {
            return false;
        }

        @Override // org.w3c.dom.ls.LSInput
        public void setCertifiedText(boolean certifiedText) {
        }
    }
}
