package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class StringDatatype extends AbstractDatatype<String> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public String valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        return s;
    }
}
