package org.fourthline.cling.model;

import java.util.logging.Logger;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class VariableValue {
    private static final Logger log = Logger.getLogger(VariableValue.class.getName());
    private final Datatype datatype;
    private final Object value;

    public VariableValue(Datatype datatype, Object value) throws InvalidValueException {
        this.datatype = datatype;
        this.value = value instanceof String ? datatype.valueOf((String) value) : value;
        if (ModelUtil.ANDROID_RUNTIME) {
            return;
        }
        if (!getDatatype().isValid(getValue())) {
            throw new InvalidValueException("Invalid value for " + getDatatype() + ": " + getValue());
        }
        logInvalidXML(toString());
    }

    public Datatype getDatatype() {
        return this.datatype;
    }

    public Object getValue() {
        return this.value;
    }

    protected void logInvalidXML(String s) {
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            if (cp != 9 && cp != 10 && cp != 13 && ((cp < 32 || cp > 55295) && ((cp < 57344 || cp > 65533) && (cp < 65536 || cp > 1114111)))) {
                Logger logger = log;
                logger.warning("Found invalid XML char code: " + cp);
            }
            i += Character.charCount(cp);
        }
    }

    public String toString() {
        return getDatatype().getString(getValue());
    }
}
