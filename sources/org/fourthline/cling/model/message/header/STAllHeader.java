package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.NotificationSubtype;
/* loaded from: classes.dex */
public class STAllHeader extends UpnpHeader<NotificationSubtype> {
    public STAllHeader() {
        setValue(NotificationSubtype.ALL);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (!s.equals(NotificationSubtype.ALL.getHeaderString())) {
            throw new InvalidHeaderException("Invalid ST header value (not " + NotificationSubtype.ALL + "): " + s);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().getHeaderString();
    }
}
