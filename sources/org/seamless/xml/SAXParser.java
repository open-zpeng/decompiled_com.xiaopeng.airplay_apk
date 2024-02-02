package org.seamless.xml;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
/* loaded from: classes.dex */
public class SAXParser {
    private final XMLReader xr;
    private static final Logger log = Logger.getLogger(SAXParser.class.getName());
    public static final URI XML_SCHEMA_NAMESPACE = URI.create("http://www.w3.org/2001/xml.xsd");
    public static final URL XML_SCHEMA_RESOURCE = Thread.currentThread().getContextClassLoader().getResource("org/seamless/schemas/xml.xsd");

    public SAXParser() {
        this(null);
    }

    public SAXParser(DefaultHandler handler) {
        this.xr = create();
        if (handler != null) {
            this.xr.setContentHandler(handler);
        }
    }

    public void setContentHandler(ContentHandler handler) {
        this.xr.setContentHandler(handler);
    }

    protected XMLReader create() {
        try {
            if (getSchemaSources() != null) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setSchema(createSchema(getSchemaSources()));
                XMLReader xmlReader = factory.newSAXParser().getXMLReader();
                xmlReader.setErrorHandler(getErrorHandler());
                return xmlReader;
            }
            return XMLReaderFactory.createXMLReader();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Schema createSchema(Source[] schemaSources) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            schemaFactory.setResourceResolver(new CatalogResourceResolver(new HashMap<URI, URL>() { // from class: org.seamless.xml.SAXParser.1
                {
                    put(SAXParser.XML_SCHEMA_NAMESPACE, SAXParser.XML_SCHEMA_RESOURCE);
                }
            }));
            return schemaFactory.newSchema(schemaSources);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Source[] getSchemaSources() {
        return null;
    }

    protected ErrorHandler getErrorHandler() {
        return new SimpleErrorHandler();
    }

    public void parse(InputSource source) throws ParserException {
        try {
            this.xr.parse(source);
        } catch (Exception ex) {
            throw new ParserException(ex);
        }
    }

    /* loaded from: classes.dex */
    public class SimpleErrorHandler implements ErrorHandler {
        public SimpleErrorHandler() {
        }

        @Override // org.xml.sax.ErrorHandler
        public void warning(SAXParseException e) throws SAXException {
            throw new SAXException(e);
        }

        @Override // org.xml.sax.ErrorHandler
        public void error(SAXParseException e) throws SAXException {
            throw new SAXException(e);
        }

        @Override // org.xml.sax.ErrorHandler
        public void fatalError(SAXParseException e) throws SAXException {
            throw new SAXException(e);
        }
    }

    /* loaded from: classes.dex */
    public static class Handler<I> extends DefaultHandler {
        protected Attributes attributes;
        protected StringBuilder characters;
        protected I instance;
        protected Handler parent;
        protected SAXParser parser;

        public Handler(I instance) {
            this(instance, null, null);
        }

        public Handler(I instance, SAXParser parser) {
            this(instance, parser, null);
        }

        public Handler(I instance, Handler parent) {
            this(instance, parent.getParser(), parent);
        }

        public Handler(I instance, SAXParser parser, Handler parent) {
            this.characters = new StringBuilder();
            this.instance = instance;
            this.parser = parser;
            this.parent = parent;
            if (parser != null) {
                parser.setContentHandler(this);
            }
        }

        public I getInstance() {
            return this.instance;
        }

        public SAXParser getParser() {
            return this.parser;
        }

        public Handler getParent() {
            return this.parent;
        }

        protected void switchToParent() {
            if (this.parser != null && this.parent != null) {
                this.parser.setContentHandler(this.parent);
                this.attributes = null;
            }
        }

        public String getCharacters() {
            return this.characters.toString();
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            this.characters = new StringBuilder();
            this.attributes = new AttributesImpl(attributes);
            Logger logger = SAXParser.log;
            logger.finer(getClass().getSimpleName() + " starting: " + localName);
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void characters(char[] ch, int start, int length) throws SAXException {
            this.characters.append(ch, start, length);
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (isLastElement(uri, localName, qName)) {
                Logger logger = SAXParser.log;
                logger.finer(getClass().getSimpleName() + ": last element, switching to parent: " + localName);
                switchToParent();
                return;
            }
            Logger logger2 = SAXParser.log;
            logger2.finer(getClass().getSimpleName() + " ending: " + localName);
        }

        protected boolean isLastElement(String uri, String localName, String qName) {
            return false;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public Attributes getAttributes() {
            return this.attributes;
        }
    }
}
