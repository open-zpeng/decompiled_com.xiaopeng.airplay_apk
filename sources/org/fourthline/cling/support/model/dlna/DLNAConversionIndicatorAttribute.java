package org.fourthline.cling.support.model.dlna;
/* loaded from: classes.dex */
public class DLNAConversionIndicatorAttribute extends DLNAAttribute<DLNAConversionIndicator> {
    public DLNAConversionIndicatorAttribute() {
        setValue(DLNAConversionIndicator.NONE);
    }

    public DLNAConversionIndicatorAttribute(DLNAConversionIndicator indicator) {
        setValue(indicator);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        DLNAConversionIndicator value = null;
        try {
            value = DLNAConversionIndicator.valueOf(Integer.parseInt(s));
        } catch (NumberFormatException e) {
        }
        if (value == null) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speed integer from: " + s);
        }
        setValue(value);
    }

    @Override // org.fourthline.cling.support.model.dlna.DLNAAttribute
    public String getString() {
        return Integer.toString(getValue().getCode());
    }
}
