package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
/* loaded from: classes.dex */
public class PlaySpeedHeader extends DLNAHeader<AVTransportVariable.TransportPlaySpeed> {
    public PlaySpeedHeader() {
    }

    public PlaySpeedHeader(AVTransportVariable.TransportPlaySpeed speed) {
        setValue(speed);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            try {
                AVTransportVariable.TransportPlaySpeed t = new AVTransportVariable.TransportPlaySpeed(s);
                setValue(t);
                return;
            } catch (InvalidValueException e) {
            }
        }
        throw new InvalidHeaderException("Invalid PlaySpeed header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().getValue();
    }
}
