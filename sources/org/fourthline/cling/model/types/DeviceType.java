package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.Constants;
/* loaded from: classes.dex */
public class DeviceType {
    public static final String UNKNOWN = "UNKNOWN";
    private String namespace;
    private String type;
    private int version;
    private static final Logger log = Logger.getLogger(DeviceType.class.getName());
    public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):device:([a-zA-Z_0-9\\-]{1,64}):([0-9]+).*");

    public DeviceType(String namespace, String type) {
        this(namespace, type, 1);
    }

    public DeviceType(String namespace, String type, int version) {
        this.version = 1;
        if (namespace != null && !namespace.matches(Constants.REGEX_NAMESPACE)) {
            throw new IllegalArgumentException("Device type namespace contains illegal characters");
        }
        this.namespace = namespace;
        if (type != null && !type.matches(Constants.REGEX_TYPE)) {
            throw new IllegalArgumentException("Device type suffix too long (64) or contains illegal characters");
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

    public static DeviceType valueOf(String s) throws InvalidValueException {
        DeviceType deviceType = null;
        String s2 = s.replaceAll("\\s", "");
        try {
            deviceType = UDADeviceType.valueOf(s2);
        } catch (Exception e) {
        }
        if (deviceType != null) {
            return deviceType;
        }
        try {
            Matcher matcher = PATTERN.matcher(s2);
            if (matcher.matches()) {
                return new DeviceType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)).intValue());
            }
            Matcher matcher2 = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):device::([0-9]+).*").matcher(s2);
            if (matcher2.matches() && matcher2.groupCount() >= 2) {
                Logger logger = log;
                logger.warning("UPnP specification violation, no device type token, defaulting to UNKNOWN: " + s2);
                return new DeviceType(matcher2.group(1), "UNKNOWN", Integer.valueOf(matcher2.group(2)).intValue());
            }
            Matcher matcher3 = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):device:(.+?):([0-9]+).*").matcher(s2);
            if (matcher3.matches() && matcher3.groupCount() >= 3) {
                String cleanToken = matcher3.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
                Logger logger2 = log;
                logger2.warning("UPnP specification violation, replacing invalid device type token '" + matcher3.group(2) + "' with: " + cleanToken);
                return new DeviceType(matcher3.group(1), cleanToken, Integer.valueOf(matcher3.group(3)).intValue());
            }
            throw new InvalidValueException("Can't parse device type string (namespace/type/version): " + s2);
        } catch (RuntimeException e2) {
            throw new InvalidValueException(String.format("Can't parse device type string (namespace/type/version) '%s': %s", s2, e2.toString()));
        }
    }

    public boolean implementsVersion(DeviceType that) {
        return this.namespace.equals(that.namespace) && this.type.equals(that.type) && this.version >= that.version;
    }

    public String getDisplayString() {
        return getType();
    }

    public String toString() {
        return "urn:" + getNamespace() + ":device:" + getType() + ":" + getVersion();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof DeviceType)) {
            return false;
        }
        DeviceType that = (DeviceType) o;
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
