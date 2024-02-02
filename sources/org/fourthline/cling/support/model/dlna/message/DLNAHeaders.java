package org.fourthline.cling.support.model.dlna.message;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.support.model.dlna.message.header.DLNAHeader;
/* loaded from: classes.dex */
public class DLNAHeaders extends UpnpHeaders {
    private static final Logger log = Logger.getLogger(DLNAHeaders.class.getName());
    protected Map<DLNAHeader.Type, List<UpnpHeader>> parsedDLNAHeaders;

    public DLNAHeaders() {
    }

    public DLNAHeaders(Map<String, List<String>> headers) {
        super(headers);
    }

    public DLNAHeaders(ByteArrayInputStream inputStream) {
        super(inputStream);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.model.message.UpnpHeaders
    public void parseHeaders() {
        if (this.parsedHeaders == null) {
            super.parseHeaders();
        }
        this.parsedDLNAHeaders = new LinkedHashMap();
        log.log(Level.FINE, "Parsing all HTTP headers for known UPnP headers: {0}", Integer.valueOf(size()));
        for (Map.Entry<String, List<String>> entry : entrySet()) {
            if (entry.getKey() != null) {
                DLNAHeader.Type type = DLNAHeader.Type.getByHttpName(entry.getKey());
                if (type == null) {
                    log.log(Level.FINE, "Ignoring non-UPNP HTTP header: {0}", entry.getKey());
                } else {
                    for (String value : entry.getValue()) {
                        UpnpHeader upnpHeader = DLNAHeader.newInstance(type, value);
                        if (upnpHeader == null || upnpHeader.getValue() == 0) {
                            log.log(Level.FINE, "Ignoring known but non-parsable header (value violates the UDA specification?) '{0}': {1}", new Object[]{type.getHttpName(), value});
                        } else {
                            addParsedValue(type, upnpHeader);
                        }
                    }
                }
            }
        }
    }

    protected void addParsedValue(DLNAHeader.Type type, UpnpHeader value) {
        log.log(Level.FINE, "Adding parsed header: {0}", value);
        List<UpnpHeader> list = this.parsedDLNAHeaders.get(type);
        if (list == null) {
            list = new LinkedList();
            this.parsedDLNAHeaders.put(type, list);
        }
        list.add(value);
    }

    @Override // org.fourthline.cling.model.message.UpnpHeaders, org.seamless.http.Headers, java.util.Map
    public List<String> put(String key, List<String> values) {
        this.parsedDLNAHeaders = null;
        return super.put(key, values);
    }

    @Override // org.fourthline.cling.model.message.UpnpHeaders, org.seamless.http.Headers
    public void add(String key, String value) {
        this.parsedDLNAHeaders = null;
        super.add(key, value);
    }

    @Override // org.fourthline.cling.model.message.UpnpHeaders, org.seamless.http.Headers, java.util.Map
    public List<String> remove(Object key) {
        this.parsedDLNAHeaders = null;
        return super.remove(key);
    }

    @Override // org.fourthline.cling.model.message.UpnpHeaders, org.seamless.http.Headers, java.util.Map
    public void clear() {
        this.parsedDLNAHeaders = null;
        super.clear();
    }

    public boolean containsKey(DLNAHeader.Type type) {
        if (this.parsedDLNAHeaders == null) {
            parseHeaders();
        }
        return this.parsedDLNAHeaders.containsKey(type);
    }

    public List<UpnpHeader> get(DLNAHeader.Type type) {
        if (this.parsedDLNAHeaders == null) {
            parseHeaders();
        }
        return this.parsedDLNAHeaders.get(type);
    }

    public void add(DLNAHeader.Type type, UpnpHeader value) {
        super.add(type.getHttpName(), value.getString());
        if (this.parsedDLNAHeaders != null) {
            addParsedValue(type, value);
        }
    }

    public void remove(DLNAHeader.Type type) {
        super.remove((Object) type.getHttpName());
        if (this.parsedDLNAHeaders != null) {
            this.parsedDLNAHeaders.remove(type);
        }
    }

    public UpnpHeader[] getAsArray(DLNAHeader.Type type) {
        if (this.parsedDLNAHeaders == null) {
            parseHeaders();
        }
        if (this.parsedDLNAHeaders.get(type) != null) {
            return (UpnpHeader[]) this.parsedDLNAHeaders.get(type).toArray(new UpnpHeader[this.parsedDLNAHeaders.get(type).size()]);
        }
        return new UpnpHeader[0];
    }

    public UpnpHeader getFirstHeader(DLNAHeader.Type type) {
        if (getAsArray(type).length > 0) {
            return getAsArray(type)[0];
        }
        return null;
    }

    public <H extends UpnpHeader> H getFirstHeader(DLNAHeader.Type type, Class<H> subtype) {
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

    @Override // org.fourthline.cling.model.message.UpnpHeaders
    public void log() {
        if (log.isLoggable(Level.FINE)) {
            super.log();
            if (this.parsedDLNAHeaders != null && this.parsedDLNAHeaders.size() > 0) {
                log.fine("########################## PARSED DLNA HEADERS ##########################");
                for (Map.Entry<DLNAHeader.Type, List<UpnpHeader>> entry : this.parsedDLNAHeaders.entrySet()) {
                    log.log(Level.FINE, "=== TYPE: {0}", entry.getKey());
                    for (UpnpHeader upnpHeader : entry.getValue()) {
                        log.log(Level.FINE, "HEADER: {0}", upnpHeader);
                    }
                }
            }
            log.fine("####################################################################");
        }
    }
}
