package org.fourthline.cling.support.model.dlna.message.header;

import com.xpeng.airplay.service.NsdConstants;
import java.util.regex.Pattern;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.ScmsFlagType;
/* loaded from: classes.dex */
public class ScmsFlagHeader extends DLNAHeader<ScmsFlagType> {
    static final Pattern pattern = Pattern.compile("^[01]{2}$", 2);

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (pattern.matcher(s).matches()) {
            setValue(new ScmsFlagType(s.charAt(0) == '0', s.charAt(1) == '0'));
            return;
        }
        throw new InvalidHeaderException("Invalid ScmsFlag header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        ScmsFlagType v = getValue();
        StringBuilder sb = new StringBuilder();
        sb.append(v.isCopyright() ? "0" : NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS);
        sb.append(v.isOriginal() ? "0" : NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS);
        return sb.toString();
    }
}
