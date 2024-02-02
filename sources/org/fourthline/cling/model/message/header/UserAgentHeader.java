package org.fourthline.cling.model.message.header;
/* loaded from: classes.dex */
public class UserAgentHeader extends UpnpHeader<String> {
    public UserAgentHeader() {
    }

    public UserAgentHeader(String s) {
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
