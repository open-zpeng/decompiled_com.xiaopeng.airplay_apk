package org.fourthline.cling.model.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class DLNADoc {
    public static final Pattern PATTERN = Pattern.compile("(.+?)[ -]([0-9].[0-9]{2})");
    private final String devClass;
    private final String version;

    /* loaded from: classes.dex */
    public enum Version {
        V1_0("1.00"),
        V1_5("1.50");
        
        String s;

        Version(String s) {
            this.s = s;
        }

        @Override // java.lang.Enum
        public String toString() {
            return this.s;
        }
    }

    public DLNADoc(String devClass, String version) {
        this.devClass = devClass;
        this.version = version;
    }

    public DLNADoc(String devClass, Version version) {
        this.devClass = devClass;
        this.version = version.s;
    }

    public String getDevClass() {
        return this.devClass;
    }

    public String getVersion() {
        return this.version;
    }

    public static DLNADoc valueOf(String s) throws InvalidValueException {
        Matcher matcher = PATTERN.matcher(s);
        if (matcher.matches()) {
            return new DLNADoc(matcher.group(1), matcher.group(2));
        }
        throw new InvalidValueException("Can't parse DLNADoc: " + s);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DLNADoc dlnaDoc = (DLNADoc) o;
        if (this.devClass.equals(dlnaDoc.devClass) && this.version.equals(dlnaDoc.version)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.devClass.hashCode();
        return (31 * result) + this.version.hashCode();
    }

    public String toString() {
        return getDevClass() + "-" + getVersion();
    }
}
