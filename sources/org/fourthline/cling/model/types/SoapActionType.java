package org.fourthline.cling.model.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.ModelUtil;
/* loaded from: classes.dex */
public class SoapActionType {
    public static final String MAGIC_CONTROL_NS = "schemas-upnp-org";
    public static final String MAGIC_CONTROL_TYPE = "control-1-0";
    private String actionName;
    private String namespace;
    private String type;
    private Integer version;
    public static final Pattern PATTERN_MAGIC_CONTROL = Pattern.compile("urn:schemas-upnp-org:control-1-0#([a-zA-Z0-9^-_\\p{L}\\p{N}]{1}[a-zA-Z0-9^-_\\.\\\\p{L}\\\\p{N}\\p{Mc}\\p{Sk}]*)");
    public static final Pattern PATTERN = Pattern.compile("urn:([a-zA-Z0-9\\-\\.]+):service:([a-zA-Z_0-9\\-]{1,64}):([0-9]+)#([a-zA-Z0-9^-_\\p{L}\\p{N}]{1}[a-zA-Z0-9^-_\\.\\\\p{L}\\\\p{N}\\p{Mc}\\p{Sk}]*)");

    public SoapActionType(ServiceType serviceType, String actionName) {
        this(serviceType.getNamespace(), serviceType.getType(), Integer.valueOf(serviceType.getVersion()), actionName);
    }

    public SoapActionType(String namespace, String type, Integer version, String actionName) {
        this.namespace = namespace;
        this.type = type;
        this.version = version;
        this.actionName = actionName;
        if (actionName != null && !ModelUtil.isValidUDAName(actionName)) {
            throw new IllegalArgumentException("Action name contains illegal characters: " + actionName);
        }
    }

    public String getActionName() {
        return this.actionName;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getType() {
        return this.type;
    }

    public Integer getVersion() {
        return this.version;
    }

    public static SoapActionType valueOf(String s) throws InvalidValueException {
        Matcher magicControlMatcher = PATTERN_MAGIC_CONTROL.matcher(s);
        try {
            if (magicControlMatcher.matches()) {
                return new SoapActionType("schemas-upnp-org", MAGIC_CONTROL_TYPE, null, magicControlMatcher.group(1));
            }
            Matcher matcher = PATTERN.matcher(s);
            if (matcher.matches()) {
                return new SoapActionType(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)), matcher.group(4));
            }
            throw new InvalidValueException("Can't parse action type string (namespace/type/version#actionName): " + s);
        } catch (RuntimeException e) {
            throw new InvalidValueException(String.format("Can't parse action type string (namespace/type/version#actionName) '%s': %s", s, e.toString()));
        }
    }

    public ServiceType getServiceType() {
        if (this.version == null) {
            return null;
        }
        return new ServiceType(this.namespace, this.type, this.version.intValue());
    }

    public String toString() {
        return getTypeString() + "#" + getActionName();
    }

    public String getTypeString() {
        if (this.version == null) {
            return "urn:" + getNamespace() + ":" + getType();
        }
        return "urn:" + getNamespace() + ":service:" + getType() + ":" + getVersion();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof SoapActionType)) {
            return false;
        }
        SoapActionType that = (SoapActionType) o;
        if (!this.actionName.equals(that.actionName) || !this.namespace.equals(that.namespace) || !this.type.equals(that.type)) {
            return false;
        }
        if (this.version == null ? that.version == null : this.version.equals(that.version)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.namespace.hashCode();
        return (31 * ((31 * ((31 * result) + this.type.hashCode())) + this.actionName.hashCode())) + (this.version != null ? this.version.hashCode() : 0);
    }
}
