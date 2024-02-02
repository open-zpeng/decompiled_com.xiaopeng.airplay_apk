package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.Cookie;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.PathMap;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class NCSARequestLog extends AbstractLifeCycle implements RequestLog {
    private static final Logger LOG = Log.getLogger(NCSARequestLog.class);
    private static ThreadLocal<StringBuilder> _buffers = new ThreadLocal<StringBuilder>() { // from class: org.eclipse.jetty.server.NCSARequestLog.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public StringBuilder initialValue() {
            return new StringBuilder((int) CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE);
        }
    };
    private boolean _closeOut;
    private transient OutputStream _fileOut;
    private String _filename;
    private transient PathMap _ignorePathMap;
    private String[] _ignorePaths;
    private transient DateCache _logDateCache;
    private transient OutputStream _out;
    private boolean _preferProxiedForAddress;
    private transient Writer _writer;
    private String _logDateFormat = "dd/MMM/yyyy:HH:mm:ss Z";
    private String _filenameDateFormat = null;
    private Locale _logLocale = Locale.getDefault();
    private String _logTimeZone = "GMT";
    private boolean _logLatency = false;
    private boolean _logCookies = false;
    private boolean _logServer = false;
    private boolean _logDispatch = false;
    private boolean _extended = true;
    private boolean _append = true;
    private int _retainDays = 31;

    public NCSARequestLog() {
    }

    public NCSARequestLog(String filename) {
        setFilename(filename);
    }

    public void setFilename(String filename) {
        if (filename != null) {
            filename = filename.trim();
            if (filename.length() == 0) {
                filename = null;
            }
        }
        this._filename = filename;
    }

    public String getFilename() {
        return this._filename;
    }

    public String getDatedFilename() {
        if (this._fileOut instanceof RolloverFileOutputStream) {
            return ((RolloverFileOutputStream) this._fileOut).getDatedFilename();
        }
        return null;
    }

    public void setLogDateFormat(String format) {
        this._logDateFormat = format;
    }

    public String getLogDateFormat() {
        return this._logDateFormat;
    }

    public void setLogLocale(Locale logLocale) {
        this._logLocale = logLocale;
    }

    public Locale getLogLocale() {
        return this._logLocale;
    }

    public void setLogTimeZone(String tz) {
        this._logTimeZone = tz;
    }

    public String getLogTimeZone() {
        return this._logTimeZone;
    }

    public void setRetainDays(int retainDays) {
        this._retainDays = retainDays;
    }

    public int getRetainDays() {
        return this._retainDays;
    }

    public void setExtended(boolean extended) {
        this._extended = extended;
    }

    public boolean isExtended() {
        return this._extended;
    }

    public void setAppend(boolean append) {
        this._append = append;
    }

    public boolean isAppend() {
        return this._append;
    }

    public void setIgnorePaths(String[] ignorePaths) {
        this._ignorePaths = ignorePaths;
    }

    public String[] getIgnorePaths() {
        return this._ignorePaths;
    }

    public void setLogCookies(boolean logCookies) {
        this._logCookies = logCookies;
    }

    public boolean getLogCookies() {
        return this._logCookies;
    }

    public void setLogServer(boolean logServer) {
        this._logServer = logServer;
    }

    public boolean getLogServer() {
        return this._logServer;
    }

    public void setLogLatency(boolean logLatency) {
        this._logLatency = logLatency;
    }

    public boolean getLogLatency() {
        return this._logLatency;
    }

    public void setPreferProxiedForAddress(boolean preferProxiedForAddress) {
        this._preferProxiedForAddress = preferProxiedForAddress;
    }

    public boolean getPreferProxiedForAddress() {
        return this._preferProxiedForAddress;
    }

    public void setFilenameDateFormat(String logFileDateFormat) {
        this._filenameDateFormat = logFileDateFormat;
    }

    public String getFilenameDateFormat() {
        return this._filenameDateFormat;
    }

    public void setLogDispatch(boolean value) {
        this._logDispatch = value;
    }

    public boolean isLogDispatch() {
        return this._logDispatch;
    }

    @Override // org.eclipse.jetty.server.RequestLog
    public void log(Request request, Response response) {
        try {
            if ((this._ignorePathMap != null && this._ignorePathMap.getMatch(request.getRequestURI()) != null) || this._fileOut == null) {
                return;
            }
            StringBuilder buf = _buffers.get();
            buf.setLength(0);
            if (this._logServer) {
                buf.append(request.getServerName());
                buf.append(' ');
            }
            String addr = null;
            if (this._preferProxiedForAddress) {
                addr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
            }
            if (addr == null) {
                addr = request.getRemoteAddr();
            }
            buf.append(addr);
            buf.append(" - ");
            Authentication authentication = request.getAuthentication();
            if (authentication instanceof Authentication.User) {
                buf.append(((Authentication.User) authentication).getUserIdentity().getUserPrincipal().getName());
            } else {
                buf.append(" - ");
            }
            buf.append(" [");
            if (this._logDateCache != null) {
                buf.append(this._logDateCache.format(request.getTimeStamp()));
            } else {
                buf.append(request.getTimeStampBuffer().toString());
            }
            buf.append("] \"");
            buf.append(request.getMethod());
            buf.append(' ');
            buf.append(request.getUri().toString());
            buf.append(' ');
            buf.append(request.getProtocol());
            buf.append("\" ");
            if (request.getAsyncContinuation().isInitial()) {
                int status = response.getStatus();
                if (status <= 0) {
                    status = 404;
                }
                buf.append((char) (((status / 100) % 10) + 48));
                buf.append((char) (((status / 10) % 10) + 48));
                buf.append((char) (48 + (status % 10)));
            } else {
                buf.append("Async");
            }
            long responseLength = response.getContentCount();
            if (responseLength >= 0) {
                buf.append(' ');
                if (responseLength > 99999) {
                    buf.append(responseLength);
                } else {
                    if (responseLength > 9999) {
                        buf.append((char) (48 + ((responseLength / 10000) % 10)));
                    }
                    if (responseLength > 999) {
                        buf.append((char) (((responseLength / 1000) % 10) + 48));
                    }
                    if (responseLength > 99) {
                        buf.append((char) (((responseLength / 100) % 10) + 48));
                    }
                    if (responseLength > 9) {
                        buf.append((char) (((responseLength / 10) % 10) + 48));
                    }
                    buf.append((char) (48 + (responseLength % 10)));
                }
                buf.append(' ');
            } else {
                buf.append(" - ");
            }
            if (this._extended) {
                try {
                    logExtended(request, response, buf);
                } catch (IOException e) {
                    e = e;
                    LOG.warn(e);
                    return;
                }
            }
            if (this._logCookies) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null && cookies.length != 0) {
                    buf.append(" \"");
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 >= cookies.length) {
                            break;
                        }
                        if (i2 != 0) {
                            buf.append(';');
                        }
                        buf.append(cookies[i2].getName());
                        buf.append('=');
                        buf.append(cookies[i2].getValue());
                        i = i2 + 1;
                    }
                    buf.append('\"');
                }
                buf.append(" -");
            }
            if (this._logDispatch || this._logLatency) {
                long now = System.currentTimeMillis();
                if (this._logDispatch) {
                    long d = request.getDispatchTime();
                    buf.append(' ');
                    buf.append(now - (d == 0 ? request.getTimeStamp() : d));
                }
                if (this._logLatency) {
                    buf.append(' ');
                    buf.append(now - request.getTimeStamp());
                }
            }
            buf.append(StringUtil.__LINE_SEPARATOR);
            String log = buf.toString();
            write(log);
        } catch (IOException e2) {
            e = e2;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void write(String log) throws IOException {
        synchronized (this) {
            if (this._writer == null) {
                return;
            }
            this._writer.write(log);
            this._writer.flush();
        }
    }

    protected void logExtended(Request request, Response response, StringBuilder b) throws IOException {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null) {
            b.append("\"-\" ");
        } else {
            b.append('\"');
            b.append(referer);
            b.append("\" ");
        }
        String agent = request.getHeader(HttpHeaders.USER_AGENT);
        if (agent == null) {
            b.append("\"-\" ");
            return;
        }
        b.append('\"');
        b.append(agent);
        b.append('\"');
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public synchronized void doStart() throws Exception {
        if (this._logDateFormat != null) {
            this._logDateCache = new DateCache(this._logDateFormat, this._logLocale);
            this._logDateCache.setTimeZoneID(this._logTimeZone);
        }
        int i = 0;
        if (this._filename != null) {
            this._fileOut = new RolloverFileOutputStream(this._filename, this._append, this._retainDays, TimeZone.getTimeZone(this._logTimeZone), this._filenameDateFormat, null);
            this._closeOut = true;
            Logger logger = LOG;
            logger.info("Opened " + getDatedFilename(), new Object[0]);
        } else {
            this._fileOut = System.err;
        }
        this._out = this._fileOut;
        if (this._ignorePaths != null && this._ignorePaths.length > 0) {
            this._ignorePathMap = new PathMap();
            while (true) {
                int i2 = i;
                if (i2 >= this._ignorePaths.length) {
                    break;
                }
                this._ignorePathMap.put(this._ignorePaths[i2], this._ignorePaths[i2]);
                i = i2 + 1;
            }
        } else {
            this._ignorePathMap = null;
        }
        synchronized (this) {
            this._writer = new OutputStreamWriter(this._out);
        }
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        synchronized (this) {
            super.doStop();
            try {
                if (this._writer != null) {
                    this._writer.flush();
                }
            } catch (IOException e) {
                LOG.ignore(e);
            }
            if (this._out != null && this._closeOut) {
                try {
                    this._out.close();
                } catch (IOException e2) {
                    LOG.ignore(e2);
                }
            }
            this._out = null;
            this._fileOut = null;
            this._closeOut = false;
            this._logDateCache = null;
            this._writer = null;
        }
    }
}
