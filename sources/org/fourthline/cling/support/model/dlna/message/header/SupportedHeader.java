package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
/* loaded from: classes.dex */
public class SupportedHeader extends DLNAHeader<String[]> {
    public SupportedHeader() {
        setValue(new String[0]);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            if (s.endsWith(";")) {
                s = s.substring(0, s.length() - 1);
            }
            setValue(s.split("\\s*,\\s*"));
            return;
        }
        throw new InvalidHeaderException("Invalid Supported header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        String[] v = getValue();
        String r = v.length > 0 ? v[0] : "";
        for (int i = 1; i < v.length; i++) {
            r = r + "," + v[i];
        }
        return r;
    }
}
