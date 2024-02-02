package org.seamless.util.time;

import java.io.Serializable;
import java.util.Date;
import org.fourthline.cling.model.Constants;
/* loaded from: classes.dex */
public class DateRange implements Serializable {
    protected Date end;
    protected Date start;

    /* loaded from: classes.dex */
    public enum Preset {
        ALL(new DateRange(null)),
        YEAR_TO_DATE(new DateRange(new Date(DateRange.getCurrentYear(), 0, 1))),
        MONTH_TO_DATE(new DateRange(new Date(DateRange.getCurrentYear(), DateRange.getCurrentMonth(), 1))),
        LAST_MONTH(DateRange.getMonthOf(new Date(DateRange.getCurrentYear(), DateRange.getCurrentMonth() - 1, 1))),
        LAST_YEAR(new DateRange(new Date(DateRange.getCurrentYear() - 1, 0, 1), new Date(DateRange.getCurrentYear() - 1, 11, 31)));
        
        DateRange dateRange;

        Preset(DateRange dateRange) {
            this.dateRange = dateRange;
        }

        public DateRange getDateRange() {
            return this.dateRange;
        }
    }

    public DateRange() {
    }

    public DateRange(Date start) {
        this.start = start;
    }

    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public DateRange(String startUnixtime, String endUnixtime) throws NumberFormatException {
        if (startUnixtime != null) {
            this.start = new Date(Long.valueOf(startUnixtime).longValue());
        }
        if (endUnixtime != null) {
            this.end = new Date(Long.valueOf(endUnixtime).longValue());
        }
    }

    public Date getStart() {
        return this.start;
    }

    public Date getEnd() {
        return this.end;
    }

    public boolean isStartAfter(Date date) {
        return getStart() != null && getStart().getTime() > date.getTime();
    }

    public Date getOneDayBeforeStart() {
        if (getStart() == null) {
            throw new IllegalStateException("Can't get day before start date because start date is null");
        }
        return new Date(getStart().getTime() - 86400000);
    }

    public static int getCurrentYear() {
        return new Date().getYear();
    }

    public static int getCurrentMonth() {
        return new Date().getMonth();
    }

    public static int getCurrentDayOfMonth() {
        return new Date().getDate();
    }

    public boolean hasStartOrEnd() {
        return (getStart() == null && getEnd() == null) ? false : true;
    }

    public static int getDaysInMonth(Date date) {
        int month = date.getMonth();
        int year = date.getYear() + Constants.UPNP_MULTICAST_PORT;
        boolean isLeapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        int[] daysInMonth = new int[12];
        daysInMonth[0] = 31;
        daysInMonth[1] = isLeapYear ? 29 : 28;
        daysInMonth[2] = 31;
        daysInMonth[3] = 30;
        daysInMonth[4] = 31;
        daysInMonth[5] = 30;
        daysInMonth[6] = 31;
        daysInMonth[7] = 31;
        daysInMonth[8] = 30;
        daysInMonth[9] = 31;
        daysInMonth[10] = 30;
        daysInMonth[11] = 31;
        return daysInMonth[month];
    }

    public static DateRange getMonthOf(Date date) {
        return new DateRange(new Date(date.getYear(), date.getMonth(), 1), new Date(date.getYear(), date.getMonth(), getDaysInMonth(date)));
    }

    public boolean isInRange(Date date) {
        return getStart() != null && getStart().getTime() < date.getTime() && (getEnd() == null || getEnd().getTime() > date.getTime());
    }

    public boolean isValid() {
        return getStart() != null && (getEnd() == null || getStart().getTime() <= getEnd().getTime());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DateRange dateRange = (DateRange) o;
        if (this.end == null ? dateRange.end != null : !this.end.equals(dateRange.end)) {
            return false;
        }
        if (this.start == null ? dateRange.start == null : this.start.equals(dateRange.start)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.start != null ? this.start.hashCode() : 0;
        return (31 * result) + (this.end != null ? this.end.hashCode() : 0);
    }

    public static DateRange valueOf(String s) {
        if (s.contains("dr=")) {
            String dr = s.substring(s.indexOf("dr=") + 3);
            String[] split = dr.substring(0, dr.indexOf(";")).split(",");
            if (split.length != 2) {
                return null;
            }
            try {
                return new DateRange(!split[0].equals("0") ? new Date(Long.valueOf(split[0]).longValue()) : null, !split[1].equals("0") ? new Date(Long.valueOf(split[1]).longValue()) : null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("dr=");
        sb.append(getStart() != null ? Long.valueOf(getStart().getTime()) : "0");
        sb.append(",");
        sb.append(getEnd() != null ? Long.valueOf(getEnd().getTime()) : "0");
        sb.append(";");
        return sb.toString();
    }
}
