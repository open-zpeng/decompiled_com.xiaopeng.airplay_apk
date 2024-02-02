package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class CustomDatatype extends AbstractDatatype<String> {
    private String name;

    public CustomDatatype(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public String valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        return s;
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype
    public String toString() {
        return "(" + getClass().getSimpleName() + ") '" + getName() + "'";
    }
}
