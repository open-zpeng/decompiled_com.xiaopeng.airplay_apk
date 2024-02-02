package org.fourthline.cling.model.message.header;
/* loaded from: classes.dex */
public class SubscriptionIdHeader extends UpnpHeader<String> {
    public static final String PREFIX = "uuid:";

    public SubscriptionIdHeader() {
    }

    public SubscriptionIdHeader(String value) {
        setValue(value);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (!s.startsWith("uuid:")) {
            throw new InvalidHeaderException("Invalid subscription ID header value, must start with 'uuid:': " + s);
        }
        setValue(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue();
    }
}
