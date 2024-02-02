package org.seamless.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
/* loaded from: classes.dex */
public class SystemOutLoggingHandler extends StreamHandler {
    public SystemOutLoggingHandler() {
        super(System.out, new SimpleFormatter());
        setLevel(Level.ALL);
    }

    @Override // java.util.logging.StreamHandler, java.util.logging.Handler
    public void close() {
        flush();
    }

    @Override // java.util.logging.StreamHandler, java.util.logging.Handler
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /* loaded from: classes.dex */
    public static class SimpleFormatter extends Formatter {
        @Override // java.util.logging.Formatter
        public String format(LogRecord record) {
            StringBuffer buf = new StringBuffer(180);
            DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss,SS");
            buf.append("[");
            buf.append(pad(Thread.currentThread().getName(), 32));
            buf.append("] ");
            buf.append(pad(record.getLevel().toString(), 12));
            buf.append(" - ");
            buf.append(pad(dateFormat.format(new Date(record.getMillis())), 24));
            buf.append(" - ");
            buf.append(toClassString(record.getSourceClassName(), 30));
            buf.append('#');
            buf.append(record.getSourceMethodName());
            buf.append(": ");
            buf.append(formatMessage(record));
            buf.append("\n");
            Throwable throwable = record.getThrown();
            if (throwable != null) {
                StringWriter sink = new StringWriter();
                throwable.printStackTrace(new PrintWriter((Writer) sink, true));
                buf.append(sink.toString());
            }
            return buf.toString();
        }

        public String pad(String s, int size) {
            if (s.length() < size) {
                for (int i = s.length(); i < size - s.length(); i++) {
                    s = s + " ";
                }
            }
            return s;
        }

        public String toClassString(String name, int maxLength) {
            return name.length() > maxLength ? name.substring(name.length() - maxLength) : name;
        }
    }
}
