package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class CharacterDatatype extends AbstractDatatype<Character> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return type == Character.TYPE || Character.class.isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Character valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        return Character.valueOf(s.charAt(0));
    }
}
