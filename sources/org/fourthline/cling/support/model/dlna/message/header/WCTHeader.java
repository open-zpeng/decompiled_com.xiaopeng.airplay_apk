package org.fourthline.cling.support.model.dlna.message.header;

import com.xpeng.airplay.service.NsdConstants;
import java.util.regex.Pattern;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
/* loaded from: classes.dex */
public class WCTHeader extends DLNAHeader<Boolean> {
    static final Pattern pattern = Pattern.compile("^[01]{1}$", 2);

    public WCTHeader() {
        setValue(false);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (pattern.matcher(s).matches()) {
            setValue(Boolean.valueOf(s.equals(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS)));
            return;
        }
        throw new InvalidHeaderException("Invalid SCID header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().booleanValue() ? NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS : "0";
    }
}
