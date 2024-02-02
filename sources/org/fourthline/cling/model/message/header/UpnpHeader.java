package org.fourthline.cling.model.message.header;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.seamless.util.Exceptions;
/* loaded from: classes.dex */
public abstract class UpnpHeader<T> {
    private static final Logger log = Logger.getLogger(UpnpHeader.class.getName());
    private T value;

    public abstract String getString();

    public abstract void setString(String str) throws InvalidHeaderException;

    /* loaded from: classes.dex */
    public enum Type {
        USN("USN", USNRootDeviceHeader.class, DeviceUSNHeader.class, ServiceUSNHeader.class, UDNHeader.class),
        NT("NT", RootDeviceHeader.class, UDADeviceTypeHeader.class, UDAServiceTypeHeader.class, DeviceTypeHeader.class, ServiceTypeHeader.class, UDNHeader.class, NTEventHeader.class),
        NTS("NTS", NTSHeader.class),
        HOST("HOST", HostHeader.class),
        SERVER("SERVER", ServerHeader.class),
        LOCATION("LOCATION", LocationHeader.class),
        MAX_AGE("CACHE-CONTROL", MaxAgeHeader.class),
        USER_AGENT("USER-AGENT", UserAgentHeader.class),
        CONTENT_TYPE("CONTENT-TYPE", ContentTypeHeader.class),
        MAN("MAN", MANHeader.class),
        MX("MX", MXHeader.class),
        ST("ST", STAllHeader.class, RootDeviceHeader.class, UDADeviceTypeHeader.class, UDAServiceTypeHeader.class, DeviceTypeHeader.class, ServiceTypeHeader.class, UDNHeader.class),
        EXT("EXT", EXTHeader.class),
        SOAPACTION("SOAPACTION", SoapActionHeader.class),
        TIMEOUT("TIMEOUT", TimeoutHeader.class),
        CALLBACK("CALLBACK", CallbackHeader.class),
        SID("SID", SubscriptionIdHeader.class),
        SEQ("SEQ", EventSequenceHeader.class),
        RANGE("RANGE", RangeHeader.class),
        CONTENT_RANGE("CONTENT-RANGE", ContentRangeHeader.class),
        PRAGMA("PRAGMA", PragmaHeader.class),
        EXT_IFACE_MAC("X-CLING-IFACE-MAC", InterfaceMacHeader.class),
        EXT_AV_CLIENT_INFO("X-AV-CLIENT-INFO", AVClientInfoHeader.class);
        
        private static Map<String, Type> byName = new HashMap<String, Type>() { // from class: org.fourthline.cling.model.message.header.UpnpHeader.Type.1
            {
                Type[] values;
                for (Type t : Type.values()) {
                    put(t.getHttpName(), t);
                }
            }
        };
        private Class<? extends UpnpHeader>[] headerTypes;
        private String httpName;

        @SafeVarargs
        Type(String httpName, Class... clsArr) {
            this.httpName = httpName;
            this.headerTypes = clsArr;
        }

        public String getHttpName() {
            return this.httpName;
        }

        public Class<? extends UpnpHeader>[] getHeaderTypes() {
            return this.headerTypes;
        }

        public boolean isValidHeaderType(Class<? extends UpnpHeader> clazz) {
            Class<? extends UpnpHeader>[] headerTypes;
            for (Class<? extends UpnpHeader> permissibleType : getHeaderTypes()) {
                if (permissibleType.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        }

        public static Type getByHttpName(String httpName) {
            if (httpName == null) {
                return null;
            }
            return byName.get(httpName.toUpperCase(Locale.ROOT));
        }
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public static UpnpHeader newInstance(Type type, String headerValue) {
        UpnpHeader upnpHeader = null;
        for (int i = 0; i < type.getHeaderTypes().length && upnpHeader == null; i++) {
            Class<? extends UpnpHeader> headerClass = type.getHeaderTypes()[i];
            try {
                Logger logger = log;
                logger.finest("Trying to parse '" + type + "' with class: " + headerClass.getSimpleName());
                upnpHeader = headerClass.newInstance();
                if (headerValue != null) {
                    upnpHeader.setString(headerValue);
                }
            } catch (InvalidHeaderException ex) {
                Logger logger2 = log;
                logger2.finest("Invalid header value for tested type: " + headerClass.getSimpleName() + " - " + ex.getMessage());
                upnpHeader = null;
            } catch (Exception ex2) {
                Logger logger3 = log;
                logger3.severe("Error instantiating header of type '" + type + "' with value: " + headerValue);
                log.log(Level.SEVERE, "Exception root cause: ", Exceptions.unwrap(ex2));
            }
        }
        return upnpHeader;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") '" + getValue() + "'";
    }
}
