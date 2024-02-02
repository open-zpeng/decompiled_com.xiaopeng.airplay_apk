package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;
/* loaded from: classes.dex */
public class TransferModeHeader extends DLNAHeader<Type> {

    /* loaded from: classes.dex */
    public enum Type {
        Streaming,
        Interactive,
        Background
    }

    public TransferModeHeader() {
        setValue(Type.Interactive);
    }

    public TransferModeHeader(Type mode) {
        setValue(mode);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            try {
                setValue(Type.valueOf(s));
                return;
            } catch (Exception e) {
            }
        }
        throw new InvalidHeaderException("Invalid TransferMode header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }
}
