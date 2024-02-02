package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class NamedServiceType {
    private ServiceType serviceType;
    private UDN udn;

    public NamedServiceType(UDN udn, ServiceType serviceType) {
        this.udn = udn;
        this.serviceType = serviceType;
    }

    public UDN getUdn() {
        return this.udn;
    }

    public ServiceType getServiceType() {
        return this.serviceType;
    }

    public static NamedServiceType valueOf(String s) throws InvalidValueException {
        String[] strings = s.split("::");
        if (strings.length != 2) {
            throw new InvalidValueException("Can't parse UDN::ServiceType from: " + s);
        }
        try {
            UDN udn = UDN.valueOf(strings[0]);
            ServiceType serviceType = ServiceType.valueOf(strings[1]);
            return new NamedServiceType(udn, serviceType);
        } catch (Exception e) {
            throw new InvalidValueException("Can't parse UDN: " + strings[0]);
        }
    }

    public String toString() {
        return getUdn().toString() + "::" + getServiceType().toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NamedServiceType)) {
            return false;
        }
        NamedServiceType that = (NamedServiceType) o;
        if (this.serviceType.equals(that.serviceType) && this.udn.equals(that.udn)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.udn.hashCode();
        return (31 * result) + this.serviceType.hashCode();
    }
}
