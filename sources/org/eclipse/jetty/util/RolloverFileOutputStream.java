package org.eclipse.jetty.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
/* loaded from: classes.dex */
public class RolloverFileOutputStream extends FilterOutputStream {
    static final String ROLLOVER_FILE_BACKUP_FORMAT = "HHmmssSSS";
    static final String ROLLOVER_FILE_DATE_FORMAT = "yyyy_MM_dd";
    static final int ROLLOVER_FILE_RETAIN_DAYS = 31;
    static final String YYYY_MM_DD = "yyyy_mm_dd";
    private static Timer __rollover;
    private boolean _append;
    private File _file;
    private SimpleDateFormat _fileBackupFormat;
    private SimpleDateFormat _fileDateFormat;
    private String _filename;
    private int _retainDays;
    private RollTask _rollTask;

    public RolloverFileOutputStream(String filename) throws IOException {
        this(filename, true, 31);
    }

    public RolloverFileOutputStream(String filename, boolean append) throws IOException {
        this(filename, append, 31);
    }

    public RolloverFileOutputStream(String filename, boolean append, int retainDays) throws IOException {
        this(filename, append, retainDays, TimeZone.getDefault());
    }

    public RolloverFileOutputStream(String filename, boolean append, int retainDays, TimeZone zone) throws IOException {
        this(filename, append, retainDays, zone, null, null);
    }

    public RolloverFileOutputStream(String filename, boolean append, int retainDays, TimeZone zone, String dateFormat, String backupFormat) throws IOException {
        super(null);
        String dateFormat2;
        String backupFormat2;
        String filename2;
        if (dateFormat == null) {
            dateFormat2 = ROLLOVER_FILE_DATE_FORMAT;
        } else {
            dateFormat2 = dateFormat;
        }
        this._fileDateFormat = new SimpleDateFormat(dateFormat2);
        if (backupFormat == null) {
            backupFormat2 = ROLLOVER_FILE_BACKUP_FORMAT;
        } else {
            backupFormat2 = backupFormat;
        }
        this._fileBackupFormat = new SimpleDateFormat(backupFormat2);
        this._fileBackupFormat.setTimeZone(zone);
        this._fileDateFormat.setTimeZone(zone);
        if (filename != null) {
            filename2 = filename.trim();
            if (filename2.length() == 0) {
                filename2 = null;
            }
        } else {
            filename2 = filename;
        }
        if (filename2 == null) {
            throw new IllegalArgumentException("Invalid filename");
        }
        this._filename = filename2;
        this._append = append;
        this._retainDays = retainDays;
        setFile();
        synchronized (RolloverFileOutputStream.class) {
            if (__rollover == null) {
                __rollover = new Timer(RolloverFileOutputStream.class.getName(), true);
            }
            this._rollTask = new RollTask();
            Calendar now = Calendar.getInstance();
            now.setTimeZone(zone);
            GregorianCalendar midnight = new GregorianCalendar(now.get(1), now.get(2), now.get(5), 23, 0);
            midnight.setTimeZone(zone);
            midnight.add(10, 1);
            __rollover.scheduleAtFixedRate(this._rollTask, midnight.getTime(), 86400000L);
        }
    }

    public String getFilename() {
        return this._filename;
    }

    public String getDatedFilename() {
        if (this._file == null) {
            return null;
        }
        return this._file.toString();
    }

    public int getRetainDays() {
        return this._retainDays;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void setFile() throws IOException {
        this._filename = new File(this._filename).getCanonicalPath();
        File file = new File(this._filename);
        File dir = new File(file.getParent());
        if (!dir.isDirectory() || !dir.canWrite()) {
            throw new IOException("Cannot write log directory " + dir);
        }
        Date now = new Date();
        String filename = file.getName();
        int i = filename.toLowerCase(Locale.ENGLISH).indexOf(YYYY_MM_DD);
        if (i >= 0) {
            file = new File(dir, filename.substring(0, i) + this._fileDateFormat.format(now) + filename.substring(YYYY_MM_DD.length() + i));
        }
        if (file.exists() && !file.canWrite()) {
            throw new IOException("Cannot write log file " + file);
        }
        if (this.out == null || !file.equals(this._file)) {
            this._file = file;
            if (!this._append && file.exists()) {
                file.renameTo(new File(file.toString() + "." + this._fileBackupFormat.format(now)));
            }
            OutputStream oldOut = this.out;
            this.out = new FileOutputStream(file.toString(), this._append);
            if (oldOut != null) {
                oldOut.close();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeOldFiles() {
        long now;
        if (this._retainDays > 0) {
            long now2 = System.currentTimeMillis();
            File file = new File(this._filename);
            File dir = new File(file.getParent());
            String fn = file.getName();
            int s = fn.toLowerCase(Locale.ENGLISH).indexOf(YYYY_MM_DD);
            if (s < 0) {
                return;
            }
            int i = 0;
            String prefix = fn.substring(0, s);
            String suffix = fn.substring(YYYY_MM_DD.length() + s);
            String[] logList = dir.list();
            while (i < logList.length) {
                String fn2 = logList[i];
                if (!fn2.startsWith(prefix) || fn2.indexOf(suffix, prefix.length()) < 0) {
                    now = now2;
                } else {
                    File f = new File(dir, fn2);
                    long date = f.lastModified();
                    now = now2;
                    long now3 = this._retainDays;
                    if ((now2 - date) / 86400000 > now3) {
                        f.delete();
                    }
                }
                i++;
                now2 = now;
            }
        }
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] buf) throws IOException {
        this.out.write(buf);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (RolloverFileOutputStream.class) {
            try {
                super.close();
                this.out = null;
                this._file = null;
                this._rollTask.cancel();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* loaded from: classes.dex */
    private class RollTask extends TimerTask {
        private RollTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            try {
                RolloverFileOutputStream.this.setFile();
                RolloverFileOutputStream.this.removeOldFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
