package org.fourthline.cling.model.types;

import org.seamless.util.io.HexBin;
/* loaded from: classes.dex */
public class BinHexDatatype extends AbstractDatatype<byte[]> {
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
            return HexBin.stringToBytes(s);
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
            return HexBin.bytesToString(value);
        } catch (Exception ex) {
            throw new InvalidValueException(ex.getMessage(), ex);
        }
    }
}
