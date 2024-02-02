package org.fourthline.cling.support.model.dlna;
/* loaded from: classes.dex */
public class DLNAProfileAttribute extends DLNAAttribute<DLNAProfiles> {
    public DLNAProfileAttribute() {
        setValue(DLNAProfiles.NONE);
    }

    public DLNAProfileAttribute(DLNAProfiles profile) {
        setValue(profile);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        DLNAProfiles value = DLNAProfiles.valueOf(s, cf);
        if (value == null) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA profile from: " + s);
        }
        setValue(value);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public String getString() {
        return getValue().getCode();
    }
}
