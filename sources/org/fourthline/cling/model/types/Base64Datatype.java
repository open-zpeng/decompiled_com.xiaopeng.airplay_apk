package org.fourthline.cling.model.types;

import org.eclipse.jetty.util.StringUtil;
import org.seamless.util.io.Base64Coder;
/* loaded from: classes.dex */
public class Base64Datatype extends AbstractDatatype<byte[]> {
    @Override // org.fourthline.cling.model.types.AbstractDatatype
    public Class<byte[]> getValueType() {
        return byte[].class;
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public byte[] valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        try {
            return Base64Coder.decode(s);
        } catch (Exception ex) {
            throw new InvalidValueException(ex.getMessage(), ex);
        }
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public String getString(byte[] value) throws InvalidValueException {
        if (value == null) {
            return "";
        }
        try {
            return new String(Base64Coder.encode(value), StringUtil.__UTF8);
        } catch (Exception ex) {
            throw new InvalidValueException(ex.getMessage(), ex);
        }
    }
}
