package org.fourthline.cling.model.message;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.seamless.http.Headers;
/* loaded from: classes.dex */
public class UpnpHeaders extends Headers {
    private static final Logger log = Logger.getLogger(UpnpHeaders.class.getName());
    protected Map<UpnpHeader.Type, List<UpnpHeader>> parsedHeaders;

    public UpnpHeaders() {
    }

    public UpnpHeaders(Map<String, List<String>> headers) {
        super(headers);
    }

    public UpnpHeaders(ByteArrayInputStream inputStream) {
        super(inputStream);
    }

    public UpnpHeaders(boolean normalizeHeaders) {
        super(normalizeHeaders);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void parseHeaders() {
        this.parsedHeaders = new LinkedHashMap();
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Parsing all HTTP headers for known UPnP headers: " + size());
        }
        for (Map.Entry<String, List<String>> entry : entrySet()) {
            if (entry.getKey() != null) {
                UpnpHeader.Type type = UpnpHeader.Type.getByHttpName(entry.getKey());
                if (type == null) {
                    if (log.isLoggable(Level.FINE)) {
                        Logger logger2 = log;
                        logger2.fine("Ignoring non-UPNP HTTP header: " + entry.getKey());
                    }
                } else {
                    for (String value : entry.getValue()) {
                        UpnpHeader upnpHeader = UpnpHeader.newInstance(type, value);
                        if (upnpHeader == null || upnpHeader.getValue() == null) {
                            if (log.isLoggable(Level.FINE)) {
                                Logger logger3 = log;
                                logger3.fine("Ignoring known but irrelevant header (value violates the UDA specification?) '" + type.getHttpName() + "': " + value);
                            }
                        } else {
                            addParsedValue(type, upnpHeader);
                        }
                    }
                }
            }
        }
    }

    protected void addParsedValue(UpnpHeader.Type type, UpnpHeader value) {
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Adding parsed header: " + value);
        }
        List<UpnpHeader> list = this.parsedHeaders.get(type);
        if (list == null) {
            list = new LinkedList();
            this.parsedHeaders.put(type, list);
        }
        list.add(value);
    }

    @Override // org.seamless.http.Headers, java.util.Map
    public List<String> put(String key, List<String> values) {
        this.parsedHeaders = null;
        return super.put(key, values);
    }

    @Override // org.seamless.http.Headers
    public void add(String key, String value) {
        this.parsedHeaders = null;
        super.add(key, value);
    }

    @Override // org.seamless.http.Headers, java.util.Map
    public List<String> remove(Object key) {
        this.parsedHeaders = null;
        return super.remove(key);
    }

    @Override // org.seamless.http.Headers, java.util.Map
    public void clear() {
        this.parsedHeaders = null;
        super.clear();
    }

    public boolean containsKey(UpnpHeader.Type type) {
        if (this.parsedHeaders == null) {
            parseHeaders();
        }
        return this.parsedHeaders.containsKey(type);
    }

    public List<UpnpHeader> get(UpnpHeader.Type type) {
        if (this.parsedHeaders == null) {
            parseHeaders();
        }
        return this.parsedHeaders.get(type);
    }

    public void add(UpnpHeader.Type type, UpnpHeader value) {
        super.add(type.getHttpName(), value.getString());
        if (this.parsedHeaders != null) {
            addParsedValue(type, value);
        }
    }

    public void remove(UpnpHeader.Type type) {
        super.remove((Object) type.getHttpName());
        if (this.parsedHeaders != null) {
            this.parsedHeaders.remove(type);
        }
    }

    public UpnpHeader[] getAsArray(UpnpHeader.Type type) {
        if (this.parsedHeaders == null) {
            parseHeaders();
        }
        if (this.parsedHeaders.get(type) != null) {
            return (UpnpHeader[]) this.parsedHeaders.get(type).toArray(new UpnpHeader[this.parsedHeaders.get(type).size()]);
        }
        return new UpnpHeader[0];
    }

    public UpnpHeader getFirstHeader(UpnpHeader.Type type) {
        if (getAsArray(type).length > 0) {
            return getAsArray(type)[0];
        }
        return null;
    }

    public <H extends UpnpHeader> H getFirstHeader(UpnpHeader.Type type, Class<H> subtype) {
        UpnpHeader[] headers = getAsArray(type);
        if (headers.length == 0) {
            return null;
        }
        for (UpnpHeader upnpHeader : headers) {
            H h = (H) upnpHeader;
            if (subtype.isAssignableFrom(h.getClass())) {
                return h;
            }
        }
        return null;
    }

    public String getFirstHeaderString(UpnpHeader.Type type) {
        UpnpHeader header = getFirstHeader(type);
        if (header != null) {
            return header.getString();
        }
        return null;
    }

    public void log() {
        if (log.isLoggable(Level.FINE)) {
            log.fine("############################ RAW HEADERS ###########################");
            for (Map.Entry<String, List<String>> entry : entrySet()) {
                Logger logger = log;
                logger.fine("=== NAME : " + entry.getKey());
                for (String v : entry.getValue()) {
                    Logger logger2 = log;
                    logger2.fine("VALUE: " + v);
                }
            }
            if (this.parsedHeaders != null && this.parsedHeaders.size() > 0) {
                log.fine("########################## PARSED HEADERS ##########################");
                for (Map.Entry<UpnpHeader.Type, List<UpnpHeader>> entry2 : this.parsedHeaders.entrySet()) {
                    Logger logger3 = log;
                    logger3.fine("=== TYPE: " + entry2.getKey());
                    for (UpnpHeader upnpHeader : entry2.getValue()) {
                        Logger logger4 = log;
                        logger4.fine("HEADER: " + upnpHeader);
                    }
                }
            }
            log.fine("####################################################################");
        }
    }
}
