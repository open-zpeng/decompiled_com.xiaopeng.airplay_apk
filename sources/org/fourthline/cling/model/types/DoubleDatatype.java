package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class DoubleDatatype extends AbstractDatatype<Double> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return type == Double.TYPE || Double.class.isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Double valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            return Double.valueOf(Double.parseDouble(s));
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number: " + s, ex);
        }
    }
}
