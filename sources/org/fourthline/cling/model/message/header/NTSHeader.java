package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.NotificationSubtype;
/* loaded from: classes.dex */
public class NTSHeader extends UpnpHeader<NotificationSubtype> {
    public NTSHeader() {
    }

    public NTSHeader(NotificationSubtype type) {
        setValue(type);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        NotificationSubtype[] values = NotificationSubtype.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            NotificationSubtype type = values[i];
            if (!s.equals(type.getHeaderString())) {
                i++;
            } else {
                setValue(type);
                break;
            }
        }
        if (getValue() == null) {
            throw new InvalidHeaderException("Invalid NTS header value: " + s);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().getHeaderString();
    }
}
