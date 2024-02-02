package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class EventSequenceHeader extends UpnpHeader<UnsignedIntegerFourBytes> {
    public EventSequenceHeader() {
    }

    public EventSequenceHeader(long value) {
        setValue(new UnsignedIntegerFourBytes(value));
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (!"0".equals(s)) {
            while (s.startsWith("0")) {
                s = s.substring(1);
            }
        }
        try {
            setValue(new UnsignedIntegerFourBytes(s));
        } catch (NumberFormatException ex) {
            throw new InvalidHeaderException("Invalid event sequence, " + ex.getMessage());
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
