package org.fourthline.cling.support.model.dlna;

import com.xpeng.airplay.service.NsdConstants;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
/* loaded from: classes.dex */
public class DLNAPlaySpeedAttribute extends DLNAAttribute<AVTransportVariable.TransportPlaySpeed[]> {
    public DLNAPlaySpeedAttribute() {
        setValue(new AVTransportVariable.TransportPlaySpeed[0]);
    }

    public DLNAPlaySpeedAttribute(AVTransportVariable.TransportPlaySpeed[] speeds) {
        setValue(speeds);
    }

    public DLNAPlaySpeedAttribute(String[] speeds) {
        AVTransportVariable.TransportPlaySpeed[] sp = new AVTransportVariable.TransportPlaySpeed[speeds.length];
        for (int i = 0; i < speeds.length; i++) {
            try {
                sp[i] = new AVTransportVariable.TransportPlaySpeed(speeds[i]);
            } catch (InvalidValueException e) {
                throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speeds.");
            }
        }
        setValue(sp);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        AVTransportVariable.TransportPlaySpeed[] value = null;
        if (s != null && s.length() != 0) {
            String[] speeds = s.split(",");
            try {
                value = new AVTransportVariable.TransportPlaySpeed[speeds.length];
                for (int i = 0; i < speeds.length; i++) {
                    value[i] = new AVTransportVariable.TransportPlaySpeed(speeds[i]);
                }
            } catch (InvalidValueException e) {
                value = null;
            }
        }
        if (value == null) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speeds from: " + s);
        }
        setValue(value);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public String getString() {
        AVTransportVariable.TransportPlaySpeed[] value;
        String s = "";
        for (AVTransportVariable.TransportPlaySpeed speed : getValue()) {
            if (!speed.getValue().equals(NsdConstants.AIRPLAY_TXT_VALUE_TXTVERS)) {
                StringBuilder sb = new StringBuilder();
                sb.append(s);
                sb.append(s.length() == 0 ? "" : ",");
                sb.append(speed);
                s = sb.toString();
            }
        }
        return s;
    }
}
