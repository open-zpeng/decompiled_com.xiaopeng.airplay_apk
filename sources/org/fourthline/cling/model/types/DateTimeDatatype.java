package org.fourthline.cling.model.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/* loaded from: classes.dex */
public class DateTimeDatatype extends AbstractDatatype<Calendar> {
    protected String[] readFormats;
    protected String writeFormat;

    public DateTimeDatatype(String[] readFormats, String writeFormat) {
        this.readFormats = readFormats;
        this.writeFormat = writeFormat;
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public Calendar valueOf(String s) throws InvalidValueException {
        if (s.equals("")) {
            return null;
        }
        Date d = getDateValue(s, this.readFormats);
        if (d == null) {
            throw new InvalidValueException("Can't parse date/time from: " + s);
        }
        Calendar c = Calendar.getInstance(getTimeZone());
        c.setTime(d);
        return c;
    }

    @Override // org.fourthline.cling.model.types.AbstractDatatype, org.fourthline.cling.model.types.Datatype
    public String getString(Calendar value) throws InvalidValueException {
        if (value == null) {
            return "";
        }
        SimpleDateFormat sdt = new SimpleDateFormat(this.writeFormat);
        sdt.setTimeZone(getTimeZone());
        return sdt.format(value.getTime());
    }

    protected String normalizeTimeZone(String value) {
        if (value.endsWith("Z")) {
            return value.substring(0, value.length() - 1) + "+0000";
        } else if (value.length() > 7 && value.charAt(value.length() - 3) == ':') {
            if (value.charAt(value.length() - 6) == '-' || value.charAt(value.length() - 6) == '+') {
                return value.substring(0, value.length() - 3) + value.substring(value.length() - 2);
            }
            return value;
        } else {
            return value;
        }
    }

    protected Date getDateValue(String value, String[] formats) {
        String value2 = normalizeTimeZone(value);
        Date d = null;
        for (String format : formats) {
            SimpleDateFormat sdt = new SimpleDateFormat(format);
            sdt.setTimeZone(getTimeZone());
            try {
                d = sdt.parse(value2);
            } catch (ParseException e) {
            }
        }
        return d;
    }

    protected TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
}
