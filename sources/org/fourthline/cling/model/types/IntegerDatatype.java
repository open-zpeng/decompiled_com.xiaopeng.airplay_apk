package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class IntegerDatatype extends AbstractDatatype<Integer> {
    private int byteSize;

    public IntegerDatatype(int byteSize) {
        this.byteSize = byteSize;
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return type == Integer.TYPE || Integer.class.isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Integer valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            Integer value = Integer.valueOf(Integer.parseInt(s.trim()));
            if (!isValid(value)) {
                throw new InvalidValueException("Not a " + getByteSize() + " byte(s) integer: " + s);
            }
            return value;
        } catch (NumberFormatException ex) {
            if (s.equals("NOT_IMPLEMENTED")) {
                return Integer.valueOf(getMaxValue());
            }
            throw new InvalidValueException("Can't convert string to number: " + s, ex);
        }
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isValid(Integer value) {
        return value == null || (value.intValue() >= getMinValue() && value.intValue() <= getMaxValue());
    }

    public int getMinValue() {
        int byteSize = getByteSize();
        if (byteSize != 4) {
            switch (byteSize) {
                case 1:
                    return -128;
                case 2:
                    return -32768;
                default:
                    throw new IllegalArgumentException("Invalid integer byte size: " + getByteSize());
            }
        }
        return Integer.MIN_VALUE;
    }

    public int getMaxValue() {
        int byteSize = getByteSize();
        if (byteSize != 4) {
            switch (byteSize) {
                case 1:
                    return 127;
                case 2:
                    return 32767;
                default:
                    throw new IllegalArgumentException("Invalid integer byte size: " + getByteSize());
            }
        }
        return Integer.MAX_VALUE;
    }

    public int getByteSize() {
        return this.byteSize;
    }
}
