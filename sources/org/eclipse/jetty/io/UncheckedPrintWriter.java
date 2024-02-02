package org.eclipse.jetty.io;

import com.xpeng.airplay.service.NsdConstants;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class UncheckedPrintWriter extends PrintWriter {
    private static final Logger LOG = Log.getLogger(UncheckedPrintWriter.class);
    private boolean _autoFlush;
    private IOException _ioException;
    private boolean _isClosed;
    private String _lineSeparator;

    public UncheckedPrintWriter(Writer out) {
        this(out, false);
    }

    public UncheckedPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
        this._autoFlush = false;
        this._isClosed = false;
        this._autoFlush = autoFlush;
        this._lineSeparator = System.getProperty("line.separator");
    }

    public UncheckedPrintWriter(OutputStream out) {
        this(out, false);
    }

    public UncheckedPrintWriter(OutputStream out, boolean autoFlush) {
        this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
    }

    @Override // java.io.PrintWriter
    public boolean checkError() {
        return this._ioException != null || super.checkError();
    }

    private void setError(Throwable th) {
        super.setError();
        if (th instanceof IOException) {
            this._ioException = (IOException) th;
        } else {
            this._ioException = new IOException(String.valueOf(th));
            this._ioException.initCause(th);
        }
        LOG.debug(th);
    }

    @Override // java.io.PrintWriter
    protected void setError() {
        setError(new IOException());
    }

    private void isOpen() throws IOException {
        if (this._ioException != null) {
            throw new RuntimeIOException(this._ioException);
        }
        if (this._isClosed) {
            throw new IOException("Stream closed");
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer, java.io.Flushable
    public void flush() {
        try {
            synchronized (this.lock) {
                isOpen();
                this.out.flush();
            }
        } catch (IOException ex) {
            setError(ex);
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        try {
            synchronized (this.lock) {
                this.out.close();
                this._isClosed = true;
            }
        } catch (IOException ex) {
            setError(ex);
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(int c) {
        try {
            synchronized (this.lock) {
                isOpen();
                this.out.write(c);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            setError(ex);
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(char[] buf, int off, int len) {
        try {
            synchronized (this.lock) {
                isOpen();
                this.out.write(buf, off, len);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            setError(ex);
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(char[] buf) {
        write(buf, 0, buf.length);
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(String s, int off, int len) {
        try {
            synchronized (this.lock) {
                isOpen();
                this.out.write(s, off, len);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            setError(ex);
        }
    }

    @Override // java.io.PrintWriter, java.io.Writer
    public void write(String s) {
        write(s, 0, s.length());
    }

    private void newLine() {
        try {
            synchronized (this.lock) {
                isOpen();
                this.out.write(this._lineSeparator);
                if (this._autoFlush) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            setError(ex);
        }
    }

    @Override // java.io.PrintWriter
    public void print(boolean b) {
        write(b ? NsdConstants.AIRPLAY_TXT_VALUE_DA : "false");
    }

    @Override // java.io.PrintWriter
    public void print(char c) {
        write(c);
    }

    @Override // java.io.PrintWriter
    public void print(int i) {
        write(String.valueOf(i));
    }

    @Override // java.io.PrintWriter
    public void print(long l) {
        write(String.valueOf(l));
    }

    @Override // java.io.PrintWriter
    public void print(float f) {
        write(String.valueOf(f));
    }

    @Override // java.io.PrintWriter
    public void print(double d) {
        write(String.valueOf(d));
    }

    @Override // java.io.PrintWriter
    public void print(char[] s) {
        write(s);
    }

    @Override // java.io.PrintWriter
    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    @Override // java.io.PrintWriter
    public void print(Object obj) {
        write(String.valueOf(obj));
    }

    @Override // java.io.PrintWriter
    public void println() {
        newLine();
    }

    @Override // java.io.PrintWriter
    public void println(boolean x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(char x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(int x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(long x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(float x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(double x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(char[] x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(String x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    @Override // java.io.PrintWriter
    public void println(Object x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }
}
