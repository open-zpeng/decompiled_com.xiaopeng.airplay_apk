package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTime;
/* loaded from: classes.dex */
public class RealTimeInfoHeader extends DLNAHeader<NormalPlayTime> {
    public static final String PREFIX = "DLNA.ORG_TLAG=";

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0 && s.startsWith(PREFIX)) {
            try {
                s = s.substring(PREFIX.length());
                setValue(s.equals("*") ? null : NormalPlayTime.valueOf(s));
                return;
            } catch (Exception e) {
            }
        }
        throw new InvalidHeaderException("Invalid RealTimeInfo header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        NormalPlayTime v = getValue();
        if (v == null) {
            return "DLNA.ORG_TLAG=*";
        }
        return PREFIX + v.getString();
    }
}
