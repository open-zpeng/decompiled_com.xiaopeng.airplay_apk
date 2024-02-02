package org.fourthline.cling.model.message.header;

import org.seamless.util.io.HexBin;
/* loaded from: classes.dex */
public class InterfaceMacHeader extends UpnpHeader<byte[]> {
    public InterfaceMacHeader() {
    }

    public InterfaceMacHeader(byte[] value) {
        setValue(value);
    }

    public InterfaceMacHeader(String s) {
        setString(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        byte[] bytes = HexBin.stringToBytes(s, ":");
        setValue(bytes);
        if (bytes.length != 6) {
            throw new InvalidHeaderException("Invalid MAC address: " + s);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return HexBin.bytesToString(getValue(), ":");
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String toString() {
        return "(" + getClass().getSimpleName() + ") '" + getString() + "'";
    }
}
