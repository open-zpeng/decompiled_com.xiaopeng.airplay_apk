package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.BytesRange;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange;
import org.fourthline.cling.support.model.dlna.types.TimeSeekRangeType;
/* loaded from: classes.dex */
public class TimeSeekRangeHeader extends DLNAHeader<TimeSeekRangeType> {
    public TimeSeekRangeHeader() {
    }

    public TimeSeekRangeHeader(TimeSeekRangeType timeSeekRange) {
        setValue(timeSeekRange);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            String[] params = s.split(" ");
            if (params.length > 0) {
                try {
                    TimeSeekRangeType t = new TimeSeekRangeType(NormalPlayTimeRange.valueOf(params[0]));
                    if (params.length > 1) {
                        t.setBytesRange(BytesRange.valueOf(params[1]));
                    }
                    setValue(t);
                    return;
                } catch (InvalidValueException invalidValueException) {
                    throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s + "; " + invalidValueException.getMessage());
                }
            }
        }
        throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        TimeSeekRangeType t = getValue();
        String s = t.getNormalPlayTimeRange().getString();
        if (t.getBytesRange() != null) {
            return s + " " + t.getBytesRange().getString(true);
        }
        return s;
    }
}
