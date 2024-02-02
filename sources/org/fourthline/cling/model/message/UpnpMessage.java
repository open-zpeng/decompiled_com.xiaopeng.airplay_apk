package org.fourthline.cling.model.message;

import java.io.UnsupportedEncodingException;
import org.eclipse.jetty.util.StringUtil;
import org.fourthline.cling.model.message.UpnpOperation;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
/* loaded from: classes.dex */
public abstract class UpnpMessage<O extends UpnpOperation> {
    private Object body;
    private BodyType bodyType;
    private UpnpHeaders headers;
    private O operation;
    private int udaMajorVersion;
    private int udaMinorVersion;

    /* loaded from: classes.dex */
    public enum BodyType {
        STRING,
        BYTES
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public UpnpMessage(UpnpMessage<O> source) {
        this.udaMajorVersion = 1;
        this.udaMinorVersion = 0;
        this.headers = new UpnpHeaders();
        this.bodyType = BodyType.STRING;
        this.operation = source.getOperation();
        this.headers = source.getHeaders();
        this.body = source.getBody();
        this.bodyType = source.getBodyType();
        this.udaMajorVersion = source.getUdaMajorVersion();
        this.udaMinorVersion = source.getUdaMinorVersion();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public UpnpMessage(O operation) {
        this.udaMajorVersion = 1;
        this.udaMinorVersion = 0;
        this.headers = new UpnpHeaders();
        this.bodyType = BodyType.STRING;
        this.operation = operation;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public UpnpMessage(O operation, BodyType bodyType, Object body) {
        this.udaMajorVersion = 1;
        this.udaMinorVersion = 0;
        this.headers = new UpnpHeaders();
        this.bodyType = BodyType.STRING;
        this.operation = operation;
        this.bodyType = bodyType;
        this.body = body;
    }

    public void setOperation(O operation) {
        this.operation = operation;
    }

    public int getUdaMajorVersion() {
        return this.udaMajorVersion;
    }

    public void setUdaMajorVersion(int udaMajorVersion) {
        this.udaMajorVersion = udaMajorVersion;
    }

    public int getUdaMinorVersion() {
        return this.udaMinorVersion;
    }

    public void setUdaMinorVersion(int udaMinorVersion) {
        this.udaMinorVersion = udaMinorVersion;
    }

    public UpnpHeaders getHeaders() {
        return this.headers;
    }

    public void setHeaders(UpnpHeaders headers) {
        this.headers = headers;
    }

    public Object getBody() {
        return this.body;
    }

    public void setBody(String string) {
        this.bodyType = BodyType.STRING;
        this.body = string;
    }

    public void setBody(BodyType bodyType, Object body) {
        this.bodyType = bodyType;
        this.body = body;
    }

    public void setBodyCharacters(byte[] characterData) throws UnsupportedEncodingException {
        String str;
        BodyType bodyType = BodyType.STRING;
        if (getContentTypeCharset() != null) {
            str = getContentTypeCharset();
        } else {
            str = StringUtil.__UTF8;
        }
        setBody(bodyType, new String(characterData, str));
    }

    public boolean hasBody() {
        return getBody() != null;
    }

    public BodyType getBodyType() {
        return this.bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public String getBodyString() {
        try {
            if (!hasBody()) {
                return null;
            }
            if (getBodyType().equals(BodyType.STRING)) {
                String body = (String) getBody();
                if (body.charAt(0) == 65279) {
                    return body.substring(1);
                }
                return body;
            }
            return new String((byte[]) getBody(), StringUtil.__UTF8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte[] getBodyBytes() {
        try {
            if (!hasBody()) {
                return null;
            }
            if (getBodyType().equals(BodyType.STRING)) {
                return getBodyString().getBytes();
            }
            return (byte[]) getBody();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public O getOperation() {
        return this.operation;
    }

    public boolean isContentTypeMissingOrText() {
        ContentTypeHeader contentTypeHeader = getContentTypeHeader();
        if (contentTypeHeader == null || contentTypeHeader.isText()) {
            return true;
        }
        return false;
    }

    public ContentTypeHeader getContentTypeHeader() {
        return (ContentTypeHeader) getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class);
    }

    public boolean isContentTypeText() {
        ContentTypeHeader ct = getContentTypeHeader();
        return ct != null && ct.isText();
    }

    public boolean isContentTypeTextUDA() {
        ContentTypeHeader ct = getContentTypeHeader();
        return ct != null && ct.isUDACompliantXML();
    }

    public String getContentTypeCharset() {
        ContentTypeHeader ct = getContentTypeHeader();
        if (ct != null) {
            return ct.getValue().getParameters().get("charset");
        }
        return null;
    }

    public boolean hasHostHeader() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.HOST) != null;
    }

    public boolean isBodyNonEmptyString() {
        return hasBody() && getBodyType().equals(BodyType.STRING) && getBodyString().length() > 0;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") " + getOperation().toString();
    }
}
