package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.PragmaType;
/* loaded from: classes.dex */
public class PragmaHeader extends UpnpHeader<PragmaType> {
    public PragmaHeader() {
    }

    public PragmaHeader(PragmaType value) {
        setValue(value);
    }

    public PragmaHeader(String s) {
        setString(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        try {
            setValue(PragmaType.valueOf(s));
        } catch (InvalidValueException invalidValueException) {
            throw new InvalidHeaderException("Invalid Range Header: " + invalidValueException.getMessage());
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().getString();
    }
}
