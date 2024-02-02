package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class ShortDatatype extends AbstractDatatype<Short> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return type == Short.TYPE || Short.class.isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Short valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            Short value = Short.valueOf(Short.parseShort(s.trim()));
            if (!isValid(value)) {
                throw new InvalidValueException("Not a valid short: " + s);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number: " + s, ex);
        }
    }
}
