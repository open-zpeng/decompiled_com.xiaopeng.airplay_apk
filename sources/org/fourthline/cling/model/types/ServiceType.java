package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.Constants;
/* loaded from: classes.dex */
public class ServiceType {
    private String namespace;
    private String type;
    private int version;
    private static final Logger log = Logger.getLogger(ServiceType.class.getName());
    public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");
    public static final Pattern BROKEN_PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):serviceId:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");

    public ServiceType(String namespace, String type) {
        this(namespace, type, 1);
    }

    public ServiceType(String namespace, String type, int version) {
        this.version = 1;
        if (namespace != null && !namespace.matches(Constants.REGEX_NAMESPACE)) {
            throw new IllegalArgumentException("Service type namespace contains illegal characters");
        }
        this.namespace = namespace;
        if (type != null && !type.matches(Constants.REGEX_TYPE)) {
            throw new IllegalArgumentException("Service type suffix too long (64) or contains illegal characters");
        }
        this.type = type;
        this.version = version;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getType() {
        return this.type;
    }

    public int getVersion() {
        return this.version;
    }

    public static ServiceType valueOf(String s) throws InvalidValueException {
        if (s == null) {
            throw new InvalidValueException("Can't parse null string");
        }
        ServiceType serviceType = null;
        String s2 = s.replaceAll("\\s", "");
        try {
            serviceType = UDAServiceType.valueOf(s2);
        } catch (Exception e) {
        }
        if (serviceType != null) {
            return serviceType;
        }
        try {
            Matcher matcher = PATTERN.matcher(s2);
            if (matcher.matches() && matcher.groupCount() >= 3) {
                return new ServiceType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)).intValue());
            }
            Matcher matcher2 = BROKEN_PATTERN.matcher(s2);
            if (matcher2.matches() && matcher2.groupCount() >= 3) {
                return new ServiceType(matcher2.group(1), matcher2.group(2), Integer.valueOf(matcher2.group(3)).intValue());
            }
            Matcher matcher3 = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:(.+?):([0-9]+).*").matcher(s2);
            if (matcher3.matches() && matcher3.groupCount() >= 3) {
                String cleanToken = matcher3.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
                Logger logger = log;
                logger.warning("UPnP specification violation, replacing invalid service type token '" + matcher3.group(2) + "' with: " + cleanToken);
                return new ServiceType(matcher3.group(1), cleanToken, Integer.valueOf(matcher3.group(3)).intValue());
            }
            Matcher matcher4 = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):serviceId:(.+?):([0-9]+).*").matcher(s2);
            if (matcher4.matches() && matcher4.groupCount() >= 3) {
                String cleanToken2 = matcher4.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
                Logger logger2 = log;
                logger2.warning("UPnP specification violation, replacing invalid service type token '" + matcher4.group(2) + "' with: " + cleanToken2);
                return new ServiceType(matcher4.group(1), cleanToken2, Integer.valueOf(matcher4.group(3)).intValue());
            }
            throw new InvalidValueException("Can't parse service type string (namespace/type/version): " + s2);
        } catch (RuntimeException e2) {
            throw new InvalidValueException(String.format("Can't parse service type string (namespace/type/version) '%s': %s", s2, e2.toString()));
        }
    }

    public boolean implementsVersion(ServiceType that) {
        if (that == null || !this.namespace.equals(that.namespace) || !this.type.equals(that.type) || this.version < that.version) {
            return false;
        }
        return true;
    }

    public String toFriendlyString() {
        return getNamespace() + ":" + getType() + ":" + getVersion();
    }

    public String toString() {
        return "urn:" + getNamespace() + ":service:" + getType() + ":" + getVersion();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ServiceType)) {
            return false;
        }
        ServiceType that = (ServiceType) o;
        if (this.version == that.version && this.namespace.equals(that.namespace) && this.type.equals(that.type)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.namespace.hashCode();
        return (31 * ((31 * result) + this.type.hashCode())) + this.version;
    }
}
