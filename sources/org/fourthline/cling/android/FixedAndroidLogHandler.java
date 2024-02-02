package org.fourthline.cling.android;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
/* loaded from: classes.dex */
public class FixedAndroidLogHandler extends Handler {
    private static final Formatter THE_FORMATTER = new Formatter() { // from class: org.fourthline.cling.android.FixedAndroidLogHandler.1
        @Override // java.util.logging.Formatter
        public String format(LogRecord r) {
            Throwable thrown = r.getThrown();
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                sw.write(r.getMessage());
                sw.write("\n");
                thrown.printStackTrace(pw);
                pw.flush();
                return sw.toString();
            }
            return r.getMessage();
        }
    };

    public FixedAndroidLogHandler() {
        setFormatter(THE_FORMATTER);
    }

    @Override // java.util.logging.Handler
    public void close() {
    }

    @Override // java.util.logging.Handler
    public void flush() {
    }

    @Override // java.util.logging.Handler
    public void publish(LogRecord record) {
        try {
            int level = getAndroidLevel(record.getLevel());
            String tag = record.getLoggerName();
            if (tag == null) {
                tag = "null";
            } else {
                int length = tag.length();
                if (length > 23) {
                    int lastPeriod = tag.lastIndexOf(".");
                    if ((length - lastPeriod) - 1 > 23) {
                        tag = tag.substring(tag.length() - 23);
                    } else {
                        tag = tag.substring(lastPeriod + 1);
                    }
                }
            }
            String message = getFormatter().format(record);
            Log.println(level, tag, message);
        } catch (RuntimeException e) {
            Log.e("AndroidHandler", "Error logging message.", e);
        }
    }

    static int getAndroidLevel(Level level) {
        int value = level.intValue();
        if (value >= 1000) {
            return 6;
        }
        if (value >= 900) {
            return 5;
        }
        if (value >= 800) {
            return 4;
        }
        return 3;
    }
}
