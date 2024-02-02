package org.fourthline.cling.model.message.header;

import java.net.URI;
import org.fourthline.cling.model.types.SoapActionType;
/* loaded from: classes.dex */
public class SoapActionHeader extends UpnpHeader<SoapActionType> {
    public SoapActionHeader() {
    }

    public SoapActionHeader(URI uri) {
        setValue(SoapActionType.valueOf(uri.toString()));
    }

    public SoapActionHeader(SoapActionType value) {
        setValue(value);
    }

    public SoapActionHeader(String s) throws InvalidHeaderException {
        setString(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        try {
            if (!s.startsWith("\"") && s.endsWith("\"")) {
                throw new InvalidHeaderException("Invalid SOAP action header, must be enclosed in doublequotes:" + s);
            }
            SoapActionType t = SoapActionType.valueOf(s.substring(1, s.length() - 1));
            setValue(t);
        } catch (RuntimeException ex) {
            throw new InvalidHeaderException("Invalid SOAP action header value, " + ex.getMessage());
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return "\"" + getValue().toString() + "\"";
    }
}
