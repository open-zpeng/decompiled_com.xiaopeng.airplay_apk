package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.Constants;
import org.fourthline.cling.model.types.HostPort;
/* loaded from: classes.dex */
public class HostHeader extends UpnpHeader<HostPort> {
    int port = Constants.UPNP_MULTICAST_PORT;
    String group = Constants.IPV4_UPNP_MULTICAST_GROUP;

    public HostHeader() {
        setValue(new HostPort(this.group, this.port));
    }

    public HostHeader(int port) {
        setValue(new HostPort(this.group, port));
    }

    public HostHeader(String host, int port) {
        setValue(new HostPort(host, port));
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.contains(":")) {
            try {
                this.port = Integer.valueOf(s.substring(s.indexOf(":") + 1)).intValue();
                this.group = s.substring(0, s.indexOf(":"));
                setValue(new HostPort(this.group, this.port));
                return;
            } catch (NumberFormatException ex) {
                throw new InvalidHeaderException("Invalid HOST header value, can't parse port: " + s + " - " + ex.getMessage());
            }
        }
        this.group = s;
        setValue(new HostPort(this.group, this.port));
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
