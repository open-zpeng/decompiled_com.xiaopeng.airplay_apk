package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class UnsignedIntegerTwoBytesDatatype extends AbstractDatatype<UnsignedIntegerTwoBytes> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public UnsignedIntegerTwoBytes valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            return new UnsignedIntegerTwoBytes(s);
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number or not in range: " + s, ex);
        }
    }
}
