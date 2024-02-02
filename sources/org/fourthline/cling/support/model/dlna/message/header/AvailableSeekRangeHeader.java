package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.BytesRange;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.dlna.types.AvailableSeekRangeType;
import org.fourthline.cling.support.model.dlna.types.NormalPlayTimeRange;
/* loaded from: classes.dex */
public class AvailableSeekRangeHeader extends DLNAHeader<AvailableSeekRangeType> {
    public AvailableSeekRangeHeader() {
    }

    public AvailableSeekRangeHeader(AvailableSeekRangeType timeSeekRange) {
        setValue(timeSeekRange);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            String[] params = s.split(" ");
            if (params.length > 1) {
                NormalPlayTimeRange timeRange = null;
                BytesRange byteRange = null;
                try {
                    try {
                        AvailableSeekRangeType.Mode mode = AvailableSeekRangeType.Mode.valueOf("MODE_" + params[0]);
                        boolean useTime = true;
                        try {
                            timeRange = NormalPlayTimeRange.valueOf(params[1], true);
                        } catch (InvalidValueException e) {
                            try {
                                byteRange = BytesRange.valueOf(params[1]);
                                useTime = false;
                            } catch (InvalidValueException e2) {
                                throw new InvalidValueException("Invalid AvailableSeekRange Range");
                            }
                        }
                        if (useTime) {
                            if (params.length > 2) {
                                BytesRange byteRange2 = BytesRange.valueOf(params[2]);
                                setValue(new AvailableSeekRangeType(mode, timeRange, byteRange2));
                                return;
                            }
                            setValue(new AvailableSeekRangeType(mode, timeRange));
                            return;
                        }
                        setValue(new AvailableSeekRangeType(mode, byteRange));
                        return;
                    } catch (IllegalArgumentException e3) {
                        throw new InvalidValueException("Invalid AvailableSeekRange Mode");
                    }
                } catch (InvalidValueException invalidValueException) {
                    throw new InvalidHeaderException("Invalid AvailableSeekRange header value: " + s + "; " + invalidValueException.getMessage());
                }
            }
        }
        throw new InvalidHeaderException("Invalid AvailableSeekRange header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        AvailableSeekRangeType t = getValue();
        String s = Integer.toString(t.getModeFlag().ordinal());
        if (t.getNormalPlayTimeRange() != null) {
            s = s + " " + t.getNormalPlayTimeRange().getString(false);
        }
        if (t.getBytesRange() != null) {
            return s + " " + t.getBytesRange().getString(false);
        }
        return s;
    }
}
