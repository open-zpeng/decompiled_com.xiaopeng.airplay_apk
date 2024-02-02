package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class NamedDeviceType {
    private DeviceType deviceType;
    private UDN udn;

    public NamedDeviceType(UDN udn, DeviceType deviceType) {
        this.udn = udn;
        this.deviceType = deviceType;
    }

    public UDN getUdn() {
        return this.udn;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public static NamedDeviceType valueOf(String s) throws InvalidValueException {
        String[] strings = s.split("::");
        if (strings.length != 2) {
            throw new InvalidValueException("Can't parse UDN::DeviceType from: " + s);
        }
        try {
            UDN udn = UDN.valueOf(strings[0]);
            DeviceType deviceType = DeviceType.valueOf(strings[1]);
            return new NamedDeviceType(udn, deviceType);
        } catch (Exception e) {
            throw new InvalidValueException("Can't parse UDN: " + strings[0]);
        }
    }

    public String toString() {
        return getUdn().toString() + "::" + getDeviceType().toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NamedDeviceType)) {
            return false;
        }
        NamedDeviceType that = (NamedDeviceType) o;
        if (this.deviceType.equals(that.deviceType) && this.udn.equals(that.udn)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.udn.hashCode();
        return (31 * result) + this.deviceType.hashCode();
    }
}
