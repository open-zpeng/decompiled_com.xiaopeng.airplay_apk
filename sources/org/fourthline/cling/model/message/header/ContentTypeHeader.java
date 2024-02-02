package org.fourthline.cling.model.message.header;

import org.eclipse.jetty.http.MimeTypes;
import org.seamless.util.MimeType;
/* loaded from: classes.dex */
public class ContentTypeHeader extends UpnpHeader<MimeType> {
    public static final MimeType DEFAULT_CONTENT_TYPE = MimeType.valueOf(MimeTypes.TEXT_XML);
    public static final MimeType DEFAULT_CONTENT_TYPE_UTF8 = MimeType.valueOf("text/xml;charset=\"utf-8\"");

    public ContentTypeHeader() {
        setValue(DEFAULT_CONTENT_TYPE);
    }

    public ContentTypeHeader(MimeType contentType) {
        setValue(contentType);
    }

    public ContentTypeHeader(String s) throws InvalidHeaderException {
        setString(s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        setValue(MimeType.valueOf(s));
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        return getValue().toString();
    }

    public boolean isUDACompliantXML() {
        return isText() && getValue().getSubtype().equals(DEFAULT_CONTENT_TYPE.getSubtype());
    }

    public boolean isText() {
        return getValue() != null && getValue().getType().equals(DEFAULT_CONTENT_TYPE.getType());
    }
}
