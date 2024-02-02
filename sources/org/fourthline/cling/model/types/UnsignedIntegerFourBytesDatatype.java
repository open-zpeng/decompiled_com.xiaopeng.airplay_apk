package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class UnsignedIntegerFourBytesDatatype extends AbstractDatatype<UnsignedIntegerFourBytes> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public UnsignedIntegerFourBytes valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            return new UnsignedIntegerFourBytes(s);
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number or not in range: " + s, ex);
        }
    }
}
