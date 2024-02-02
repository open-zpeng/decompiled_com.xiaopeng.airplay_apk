package org.fourthline.cling.model.message.header;
/* loaded from: classes.dex */
public class MXHeader extends UpnpHeader<Integer> {
    public static final Integer DEFAULT_VALUE = 3;

    public MXHeader() {
        setValue(DEFAULT_VALUE);
    }

    public MXHeader(Integer delayInSeconds) {
        setValue(delayInSeconds);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        try {
            Integer value = Integer.valueOf(Integer.parseInt(s));
            if (value.intValue() < 0 || value.intValue() > 120) {
                setValue(DEFAULT_VALUE);
            } else {
                setValue(value);
            }
        } catch (Exception e) {
            throw new InvalidHeaderException("Can't parse MX seconds integer from: " + s);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
