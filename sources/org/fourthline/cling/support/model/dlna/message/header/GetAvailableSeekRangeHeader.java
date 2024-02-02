package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
/* loaded from: classes.dex */
public class GetAvailableSeekRangeHeader extends DLNAHeader<Integer> {
    public GetAvailableSeekRangeHeader() {
        setValue(1);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            try {
                int t = Integer.parseInt(s);
                if (t == 1) {
                    return;
                }
            } catch (Exception e) {
            }
        }
        throw new InvalidHeaderException("Invalid GetAvailableSeekRange header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
