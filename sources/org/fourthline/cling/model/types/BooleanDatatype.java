package org.fourthline.cling.model.types;

import com.xpeng.airplay.service.NsdConstants;
import java.util.Locale;
/* loaded from: classes.dex */
public class BooleanDatatype extends AbstractDatatype<Boolean> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public boolean isHandlingJavaType(Class type) {
        return type == Boolean.TYPE || Boolean.class.isAssignableFrom(type);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Boolean valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        if (s.equals(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS) || s.toUpperCase(Locale.ROOT).equals("YES") || s.toUpperCase(Locale.ROOT).equals("TRUE")) {
            return true;
        }
        if (s.equals("0") || s.toUpperCase(Locale.ROOT).equals("NO") || s.toUpperCase(Locale.ROOT).equals("FALSE")) {
            return false;
        }
        throw new InvalidValueException("Invalid boolean value string: " + s);
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public String getString(Boolean value) throws InvalidValueException {
        return value == null ? "" : value.booleanValue() ? NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS : "0";
    }
}
