package org.fourthline.cling.support.model.dlna.types;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.types.InvalidValueException;
/* loaded from: classes.dex */
public class NormalPlayTime {
    static final Pattern pattern = Pattern.compile("^(\\d+):(\\d{1,2}):(\\d{1,2})(\\.(\\d{1,3}))?|(\\d+)(\\.(\\d{1,3}))?$", 2);
    private long milliseconds;

    /* loaded from: classes.dex */
    public enum Format {
        SECONDS,
        TIME
    }

    public NormalPlayTime(long milliseconds) {
        if (milliseconds < 0) {
            throw new InvalidValueException("Invalid parameter milliseconds: " + milliseconds);
        }
        this.milliseconds = milliseconds;
    }

    public NormalPlayTime(long hours, long minutes, long seconds, long milliseconds) throws InvalidValueException {
        if (hours < 0) {
            throw new InvalidValueException("Invalid parameter hours: " + hours);
        } else if (minutes < 0 || minutes > 59) {
            throw new InvalidValueException("Invalid parameter minutes: " + hours);
        } else if (seconds < 0 || seconds > 59) {
            throw new InvalidValueException("Invalid parameter seconds: " + hours);
        } else if (milliseconds < 0 || milliseconds > 999) {
            throw new InvalidValueException("Invalid parameter milliseconds: " + milliseconds);
        } else {
            this.milliseconds = (((hours * 60 * 60) + (60 * minutes) + seconds) * 1000) + milliseconds;
        }
    }

    public long getMilliseconds() {
        return this.milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        if (milliseconds < 0) {
            throw new InvalidValueException("Invalid parameter milliseconds: " + milliseconds);
        }
        this.milliseconds = milliseconds;
    }

    public String getString() {
        return getString(Format.SECONDS);
    }

    public String getString(Format format) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(this.milliseconds);
        long ms = this.milliseconds % 1000;
        if (AnonymousClass1.$SwitchMap$org$fourthline$cling$support$model$dlna$types$NormalPlayTime$Format[format.ordinal()] != 1) {
            return String.format(Locale.ROOT, "%d.%03d", Long.valueOf(seconds), Long.valueOf(ms));
        }
        long seconds2 = TimeUnit.MILLISECONDS.toSeconds(this.milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this.milliseconds));
        long hours = TimeUnit.MILLISECONDS.toHours(this.milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(this.milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this.milliseconds));
        return String.format(Locale.ROOT, "%d:%02d:%02d.%03d", Long.valueOf(hours), Long.valueOf(minutes), Long.valueOf(seconds2), Long.valueOf(ms));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: org.fourthline.cling.support.model.dlna.types.NormalPlayTime$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$fourthline$cling$support$model$dlna$types$NormalPlayTime$Format = new int[Format.values().length];

        static {
            try {
                $SwitchMap$org$fourthline$cling$support$model$dlna$types$NormalPlayTime$Format[Format.TIME.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public static NormalPlayTime valueOf(String s) throws InvalidValueException {
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            try {
                if (matcher.group(1) != null) {
                    int msMultiplier = (int) Math.pow(10.0d, 3 - matcher.group(5).length());
                    return new NormalPlayTime(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3)), Long.parseLong(matcher.group(5)) * msMultiplier);
                }
                int msMultiplier2 = (int) Math.pow(10.0d, 3 - matcher.group(8).length());
                return new NormalPlayTime((Long.parseLong(matcher.group(6)) * 1000) + (Long.parseLong(matcher.group(8)) * msMultiplier2));
            } catch (NumberFormatException e) {
            }
        }
        throw new InvalidValueException("Can't parse NormalPlayTime: " + s);
    }
}
