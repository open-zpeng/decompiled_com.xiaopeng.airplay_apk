package org.fourthline.cling.model.message.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class TimeoutHeader extends UpnpHeader<Integer> {
    public static final Integer INFINITE_VALUE = Integer.MAX_VALUE;
    public static final Pattern PATTERN = Pattern.compile("Second-(?:([0-9]+)|infinite)");

    public TimeoutHeader() {
        setValue(1800);
    }

    public TimeoutHeader(int timeoutSeconds) {
        setValue(Integer.valueOf(timeoutSeconds));
    }

    public TimeoutHeader(Integer timeoutSeconds) {
        setValue(timeoutSeconds);
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public void setString(String s) throws InvalidHeaderException {
        Matcher matcher = PATTERN.matcher(s);
        if (!matcher.matches()) {
            throw new InvalidHeaderException("Can't parse timeout seconds integer from: " + s);
        } else if (matcher.group(1) != null) {
            setValue(Integer.valueOf(Integer.parseInt(matcher.group(1))));
        } else {
            setValue(INFINITE_VALUE);
        }
    }

    @Override // org.fourthline.cling.model.message.header.UpnpHeader
    public String getString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Second-");
        sb.append(getValue().equals(INFINITE_VALUE) ? "infinite" : getValue());
        return sb.toString();
    }
}
