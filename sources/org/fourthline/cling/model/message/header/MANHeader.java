package org.fourthline.cling.model.message.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class MANHeader extends UpnpHeader<String> {
    public String namespace;
    public static final Pattern PATTERN = Pattern.compile("\"(.+?)\"(;.+?)??");
    public static final Pattern NAMESPACE_PATTERN = Pattern.compile(";\\s?ns\\s?=\\s?([0-9]{2})");

    public MANHeader() {
    }

    public MANHeader(String value) {
        setValue(value);
    }

    public MANHeader(String value, String namespace) {
        this(value);
        this.namespace = namespace;
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        Matcher matcher = PATTERN.matcher(s);
        if (matcher.matches()) {
            setValue(matcher.group(1));
            if (matcher.group(2) != null) {
                Matcher nsMatcher = NAMESPACE_PATTERN.matcher(matcher.group(2));
                if (nsMatcher.matches()) {
                    setNamespace(nsMatcher.group(1));
                    return;
                }
                throw new InvalidHeaderException("Invalid namespace in MAN header value: " + s);
            }
            return;
        }
        throw new InvalidHeaderException("Invalid MAN header value: " + s);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        if (getValue() == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        s.append("\"");
        s.append(getValue());
        s.append("\"");
        if (getNamespace() != null) {
            s.append("; ns=");
            s.append(getNamespace());
        }
        return s.toString();
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
