package org.seamless.util.time;

import java.io.Serializable;
/* loaded from: classes.dex */
public class DateFormat implements Serializable {
    protected String label;
    protected String pattern;

    /* loaded from: classes.dex */
    public enum Preset {
        DD_MM_YYYY_DOT(new DateFormat("31.12.2010", "dd.MM.yyyy")),
        MM_DD_YYYY_DOT(new DateFormat("12.31.2010", "MM.dd.yyyy")),
        YYYY_MM_DD_DOT(new DateFormat("2010.12.31", "yyyy.MM.dd")),
        YYYY_DD_MM_DOT(new DateFormat("2010.31.12", "yyyy.dd.MM")),
        DD_MM_YYYY_SLASH(new DateFormat("31/12/2010", "dd/MM/yyyy")),
        MM_DD_YYYY_SLASH(new DateFormat("12/31/2010", "MM/dd/yyyy")),
        YYYY_MM_DD_SLASH(new DateFormat("2010/12/31", "yyyy/MM/dd")),
        YYYY_DD_MM_SLASH(new DateFormat("2010/31/12", "yyyy/dd/MM")),
        YYYY_MMM_DD(new DateFormat("2010 Dec 31", "yyyy MMM dd")),
        DD_MMM_YYYY(new DateFormat("31 Dec 2010", "dd MMM yyyy")),
        MMM_DD_YYYY(new DateFormat("Dec 31 2010", "MMM dd yyyy"));
        
        protected DateFormat dateFormat;

        Preset(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        public DateFormat getDateFormat() {
            return this.dateFormat;
        }
    }

    public DateFormat() {
    }

    DateFormat(String label, String pattern) {
        this.label = label;
        this.pattern = pattern;
    }

    public DateFormat(String pattern) {
        this.label = pattern;
        this.pattern = pattern;
    }

    public String getLabel() {
        return this.label;
    }

    public String getPattern() {
        return this.pattern;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DateFormat that = (DateFormat) o;
        if (this.pattern == null ? that.pattern == null : this.pattern.equals(that.pattern)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (this.pattern != null) {
            return this.pattern.hashCode();
        }
        return 0;
    }

    public String toString() {
        return getLabel() + ", Pattern: " + getPattern();
    }
}
