package org.fourthline.cling.model.message.header;

import java.util.Locale;
/* loaded from: classes.dex */
public class NTEventHeader extends UpnpHeader<String> {
    public NTEventHeader() {
        setValue("upnp:event");
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (!s.toLowerCase(Locale.ROOT).equals(getValue())) {
            throw new InvalidHeaderException("Invalid event NT header value: " + s);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue();
    }
}
