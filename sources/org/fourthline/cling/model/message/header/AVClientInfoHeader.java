package org.fourthline.cling.model.message.header;
/* loaded from: classes.dex */
public class AVClientInfoHeader extends UpnpHeader<String> {
    public AVClientInfoHeader() {
    }

    public AVClientInfoHeader(String s) {
        setValue(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        setValue(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue();
    }
}
