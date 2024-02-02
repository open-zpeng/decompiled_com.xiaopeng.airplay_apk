package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class DeviceIdentity implements Validatable {
    private final Integer maxAgeSeconds;
    private final UDN udn;

    public DeviceIdentity(UDN udn, DeviceIdentity template) {
        this.udn = udn;
        this.maxAgeSeconds = template.getMaxAgeSeconds();
    }

    public DeviceIdentity(UDN udn) {
        this.udn = udn;
        this.maxAgeSeconds = 1800;
    }

    public DeviceIdentity(UDN udn, Integer maxAgeSeconds) {
        this.udn = udn;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public UDN getUdn() {
        return this.udn;
    }

    public Integer getMaxAgeSeconds() {
        return this.maxAgeSeconds;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentity that = (DeviceIdentity) o;
        if (this.udn.equals(that.udn)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.udn.hashCode();
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") UDN: " + getUdn();
    }

    @Override // org.fourthline.cling.model.Validatable
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (getUdn() == null) {
            errors.add(new ValidationError(getClass(), "major", "Device has no UDN"));
        }
        return errors;
    }
}
