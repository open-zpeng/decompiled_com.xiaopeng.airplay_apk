package org.fourthline.cling.support.lastchange;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.shared.AbstractMap;
import org.seamless.util.Exceptions;
import org.seamless.util.io.IO;
import org.seamless.xml.DOMParser;
import org.seamless.xml.SAXParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/* loaded from: classes.dex */
public abstract class LastChangeParser extends SAXParser {
    private static final Logger log = Logger.getLogger(LastChangeParser.class.getName());

    protected abstract String getNamespace();

    /* loaded from: classes.dex */
    public enum CONSTANTS {
        Event,
        InstanceID,
        val;

        public boolean equals(String s) {
            return name().equals(s);
        }
    }

    protected Set<Class<? extends EventedValue>> getEventedVariables() {
        return Collections.EMPTY_SET;
    }

    protected EventedValue createValue(String name, Map.Entry<String, String>[] attributes) throws Exception {
        for (Class<? extends EventedValue> evType : getEventedVariables()) {
            if (evType.getSimpleName().equals(name)) {
                Constructor<? extends EventedValue> ctor = evType.getConstructor(Map.Entry[].class);
                return ctor.newInstance(attributes);
            }
        }
        return null;
    }

    public Event parseResource(String resource) throws Exception {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            return parse(IO.readLines(is));
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public Event parse(String xml) throws Exception {
        if (xml == null || xml.length() == 0) {
            throw new RuntimeException("Null or empty XML");
        }
        Event event = new Event();
        new RootHandler(event, this);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Parsing 'LastChange' event XML content");
            log.fine("===================================== 'LastChange' BEGIN ============================================");
            log.fine(xml);
            log.fine("====================================== 'LastChange' END  ============================================");
        }
        parse(new InputSource(new StringReader(xml)));
        Logger logger = log;
        logger.fine("Parsed event with instances IDs: " + event.getInstanceIDs().size());
        if (log.isLoggable(Level.FINEST)) {
            for (InstanceID instanceID : event.getInstanceIDs()) {
                Logger logger2 = log;
                logger2.finest("InstanceID '" + instanceID.getId() + "' has values: " + instanceID.getValues().size());
                for (EventedValue eventedValue : instanceID.getValues()) {
                    Logger logger3 = log;
                    logger3.finest(eventedValue.getName() + " => " + eventedValue.getValue());
                }
            }
        }
        return event;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class RootHandler extends SAXParser.Handler<Event> {
        RootHandler(Event instance, SAXParser parser) {
            super(instance, parser);
        }

        RootHandler(Event instance) {
            super(instance);
        }

        @Override // org.seamless.xml.SAXParser.Handler, org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String valAttr;
            super.startElement(uri, localName, qName, attributes);
            if (CONSTANTS.InstanceID.equals(localName) && (valAttr = attributes.getValue(CONSTANTS.val.name())) != null) {
                InstanceID instanceID = new InstanceID(new UnsignedIntegerFourBytes(valAttr));
                getInstance().getInstanceIDs().add(instanceID);
                new InstanceIDHandler(instanceID, this);
            }
        }
    }

    /* loaded from: classes.dex */
    class InstanceIDHandler extends SAXParser.Handler<InstanceID> {
        InstanceIDHandler(InstanceID instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override // org.seamless.xml.SAXParser.Handler, org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            Map.Entry[] attributeMap = new Map.Entry[attributes.getLength()];
            for (int i = 0; i < attributeMap.length; i++) {
                attributeMap[i] = new AbstractMap.SimpleEntry(attributes.getLocalName(i), attributes.getValue(i));
            }
            try {
                EventedValue esv = LastChangeParser.this.createValue(localName, attributeMap);
                if (esv != null) {
                    getInstance().getValues().add(esv);
                }
            } catch (Exception ex) {
                Logger logger = LastChangeParser.log;
                logger.warning("Error reading event XML, ignoring value: " + Exceptions.unwrap(ex));
            }
        }

        @Override // org.seamless.xml.SAXParser.Handler
        protected boolean isLastElement(String uri, String localName, String qName) {
            return CONSTANTS.InstanceID.equals(localName);
        }
    }

    public String generate(Event event) throws Exception {
        return XMLUtil.documentToFragmentString(buildDOM(event));
    }

    protected Document buildDOM(Event event) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document d = factory.newDocumentBuilder().newDocument();
        generateRoot(event, d);
        return d;
    }

    protected void generateRoot(Event event, Document descriptor) {
        Element eventElement = descriptor.createElementNS(getNamespace(), CONSTANTS.Event.name());
        descriptor.appendChild(eventElement);
        generateInstanceIDs(event, descriptor, eventElement);
    }

    protected void generateInstanceIDs(Event event, Document descriptor, Element rootElement) {
        for (InstanceID instanceID : event.getInstanceIDs()) {
            if (instanceID.getId() != null) {
                Element instanceIDElement = XMLUtil.appendNewElement(descriptor, rootElement, CONSTANTS.InstanceID.name());
                instanceIDElement.setAttribute(CONSTANTS.val.name(), instanceID.getId().toString());
                for (EventedValue eventedValue : instanceID.getValues()) {
                    generateEventedValue(eventedValue, descriptor, instanceIDElement);
                }
            }
        }
    }

    protected void generateEventedValue(EventedValue eventedValue, Document descriptor, Element parentElement) {
        String name = eventedValue.getName();
        Map.Entry<String, String>[] attributes = eventedValue.getAttributes();
        if (attributes != null && attributes.length > 0) {
            Element evElement = XMLUtil.appendNewElement(descriptor, parentElement, name);
            for (Map.Entry<String, String> attr : attributes) {
                evElement.setAttribute(attr.getKey(), DOMParser.escape(attr.getValue()));
            }
        }
    }
}
