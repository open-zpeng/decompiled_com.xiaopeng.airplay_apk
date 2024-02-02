package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.UDN;
/* loaded from: classes.dex */
public class UDNHeader extends UpnpHeader<UDN> {
    public UDNHeader() {
    }

    public UDNHeader(UDN udn) {
        setValue(udn);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (!s.startsWith("uuid:")) {
            throw new InvalidHeaderException("Invalid UDA header value, must start with 'uuid:': " + s);
        } else if (s.contains("::urn")) {
            throw new InvalidHeaderException("Invalid UDA header value, must not contain '::urn': " + s);
        } else {
            UDN udn = new UDN(s.substring("uuid:".length()));
            setValue(udn);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
