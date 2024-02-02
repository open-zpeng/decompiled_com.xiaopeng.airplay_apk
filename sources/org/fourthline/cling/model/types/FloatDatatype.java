package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class FloatDatatype extends AbstractDatatype<Float> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return type == Float.TYPE || Float.class.isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Float valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            return Float.valueOf(Float.parseFloat(s.trim()));
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number: " + s, ex);
        }
    }
}
