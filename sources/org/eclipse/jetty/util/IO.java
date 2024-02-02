package org.eclipse.jetty.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
/* loaded from: classes.dex */
public class IO {
    public static final String CRLF = "\r\n";
    private static final Logger LOG = Log.getLogger(IO.class);
    public static final byte[] CRLF_BYTES = {HttpTokens.CARRIAGE_RETURN, 10};
    public static int bufferSize = 65536;
    private static NullOS __nullStream = new NullOS();
    private static ClosedIS __closedStream = new ClosedIS();
    private static NullWrite __nullWriter = new NullWrite();
    private static PrintWriter __nullPrintWriter = new PrintWriter(__nullWriter);

    /* loaded from: classes.dex */
    private static class Singleton {
        static final QueuedThreadPool __pool = new QueuedThreadPool();

        private Singleton() {
        }

        static {
            try {
                __pool.start();
            } catch (Exception e) {
                IO.LOG.warn(e);
                System.exit(1);
            }
        }
    }

    /* loaded from: classes.dex */
    static class Job implements Runnable {
        InputStream in;
        OutputStream out;
        Reader read;
        Writer write;

        Job(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
            this.read = null;
            this.write = null;
        }

        Job(Reader read, Writer write) {
            this.in = null;
            this.out = null;
            this.read = read;
            this.write = write;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                if (this.in != null) {
                    IO.copy(this.in, this.out, -1L);
                } else {
                    IO.copy(this.read, this.write, -1L);
                }
            } catch (IOException e) {
                IO.LOG.ignore(e);
                try {
                    if (this.out != null) {
                        this.out.close();
                    }
                    if (this.write != null) {
                        this.write.close();
                    }
                } catch (IOException e2) {
                    IO.LOG.ignore(e2);
                }
            }
        }
    }

    public static void copyThread(InputStream in, OutputStream out) {
        try {
            Job job = new Job(in, out);
            if (!Singleton.__pool.dispatch(job)) {
                job.run();
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, -1L);
    }

    public static void copyThread(Reader in, Writer out) {
        try {
            Job job = new Job(in, out);
            if (!Singleton.__pool.dispatch(job)) {
                job.run();
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

    public static void copy(Reader in, Writer out) throws IOException {
        copy(in, out, -1L);
    }

    public static void copy(InputStream in, OutputStream out, long byteCount) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int i = bufferSize;
        if (byteCount >= 0) {
            while (byteCount > 0) {
                int max = byteCount < ((long) bufferSize) ? (int) byteCount : bufferSize;
                int len = in.read(buffer, 0, max);
                if (len != -1) {
                    byteCount -= len;
                    out.write(buffer, 0, len);
                } else {
                    return;
                }
            }
            return;
        }
        while (true) {
            int len2 = in.read(buffer, 0, bufferSize);
            if (len2 >= 0) {
                out.write(buffer, 0, len2);
            } else {
                return;
            }
        }
    }

    public static void copy(Reader in, Writer out, long byteCount) throws IOException {
        int len;
        int len2;
        char[] buffer = new char[bufferSize];
        int i = bufferSize;
        if (byteCount >= 0) {
            while (byteCount > 0) {
                if (byteCount < bufferSize) {
                    len2 = in.read(buffer, 0, (int) byteCount);
                } else {
                    len2 = in.read(buffer, 0, bufferSize);
                }
                if (len2 != -1) {
                    byteCount -= len2;
                    out.write(buffer, 0, len2);
                } else {
                    return;
                }
            }
        } else if (out instanceof PrintWriter) {
            PrintWriter pout = (PrintWriter) out;
            while (!pout.checkError() && (len = in.read(buffer, 0, bufferSize)) != -1) {
                out.write(buffer, 0, len);
            }
        } else {
            while (true) {
                int len3 = in.read(buffer, 0, bufferSize);
                if (len3 != -1) {
                    out.write(buffer, 0, len3);
                } else {
                    return;
                }
            }
        }
    }

    public static void copy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            copyDir(from, to);
        } else {
            copyFile(from, to);
        }
    }

    public static void copyDir(File from, File to) throws IOException {
        if (to.exists()) {
            if (!to.isDirectory()) {
                throw new IllegalArgumentException(to.toString());
            }
        } else {
            to.mkdirs();
        }
        File[] files = from.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (!".".equals(name) && !"..".equals(name)) {
                    copy(files[i], new File(to, name));
                }
            }
        }
    }

    public static void copyFile(File from, File to) throws IOException {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(to);
        copy(in, out);
        in.close();
        out.close();
    }

    public static String toString(InputStream in) throws IOException {
        return toString(in, null);
    }

    public static String toString(InputStream in, String encoding) throws IOException {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding);
        copy(reader, writer);
        return writer.toString();
    }

    public static String toString(Reader in) throws IOException {
        StringWriter writer = new StringWriter();
        copy(in, writer);
        return writer.toString();
    }

    public static boolean delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; files != null && i < files.length; i++) {
                    delete(files[i]);
                }
            }
            return file.delete();
        }
        return false;
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
    }

    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
    }

    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
    }

    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(in, bout);
        return bout.toByteArray();
    }

    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LOG.ignore(e);
            }
        }
    }

    public static OutputStream getNullStream() {
        return __nullStream;
    }

    public static InputStream getClosedStream() {
        return __closedStream;
    }

    /* loaded from: classes.dex */
    private static class NullOS extends OutputStream {
        private NullOS() {
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
        }

        @Override // java.io.OutputStream, java.io.Flushable
        public void flush() {
        }

        @Override // java.io.OutputStream
        public void write(byte[] b) {
        }

        @Override // java.io.OutputStream
        public void write(byte[] b, int i, int l) {
        }

        @Override // java.io.OutputStream
        public void write(int b) {
        }
    }

    /* loaded from: classes.dex */
    private static class ClosedIS extends InputStream {
        private ClosedIS() {
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            return -1;
        }
    }

    public static Writer getNullWriter() {
        return __nullWriter;
    }

    public static PrintWriter getNullPrintWriter() {
        return __nullPrintWriter;
    }

    /* loaded from: classes.dex */
    private static class NullWrite extends Writer {
        private NullWrite() {
        }

        @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
        }

        @Override // java.io.Writer, java.io.Flushable
        public void flush() {
        }

        @Override // java.io.Writer
        public void write(char[] b) {
        }

        @Override // java.io.Writer
        public void write(char[] b, int o, int l) {
        }

        @Override // java.io.Writer
        public void write(int b) {
        }

        @Override // java.io.Writer
        public void write(String s) {
        }

        @Override // java.io.Writer
        public void write(String s, int o, int l) {
        }
    }
}
