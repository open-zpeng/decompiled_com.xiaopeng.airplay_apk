package org.fourthline.cling.model;

import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class ServiceReference {
    public static final String DELIMITER = "/";
    private final ServiceId serviceId;
    private final UDN udn;

    public ServiceReference(String s) {
        String[] split = s.split("/");
        if (split.length == 2) {
            this.udn = UDN.valueOf(split[0]);
            this.serviceId = ServiceId.valueOf(split[1]);
            return;
        }
        this.udn = null;
        this.serviceId = null;
    }

    public ServiceReference(UDN udn, ServiceId serviceId) {
        this.udn = udn;
        this.serviceId = serviceId;
    }

    public UDN getUdn() {
        return this.udn;
    }

    public ServiceId getServiceId() {
        return this.serviceId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceReference that = (ServiceReference) o;
        if (this.serviceId.equals(that.serviceId) && this.udn.equals(that.udn)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.udn.hashCode();
        return (31 * result) + this.serviceId.hashCode();
    }

    public String toString() {
        if (this.udn == null || this.serviceId == null) {
            return "";
        }
        return this.udn.toString() + "/" + this.serviceId.toString();
    }
}
