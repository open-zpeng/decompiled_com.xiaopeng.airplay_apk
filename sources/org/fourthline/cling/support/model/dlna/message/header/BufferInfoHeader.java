package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.BufferInfoType;
/* loaded from: classes.dex */
public class BufferInfoHeader extends DLNAHeader<BufferInfoType> {
    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            try {
                setValue(BufferInfoType.valueOf(s));
                return;
            } catch (Exception e) {
            }
        }
        throw new InvalidHeaderException("Invalid BufferInfo header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().getString();
    }
}
