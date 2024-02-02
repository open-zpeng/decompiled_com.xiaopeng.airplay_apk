package org.seamless.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class IO {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String LINE_SEPARATOR;

    /* loaded from: classes.dex */
    public interface FileFinder {
        void found(File file);
    }

    public static String makeRelativePath(String path, String base) {
        String p;
        if (path == null || path.length() <= 0) {
            return "";
        }
        if (path.startsWith("/")) {
            if (path.startsWith(base)) {
                p = path.substring(base.length());
            } else {
                p = base + path;
            }
        } else {
            p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        }
        return p.startsWith("/") ? p.substring(1) : p;
    }

    public static void recursiveRename(File dir, String from, String to) {
        File[] subfiles = dir.listFiles();
        for (File file : subfiles) {
            if (file.isDirectory()) {
                recursiveRename(file, from, to);
                file.renameTo(new File(dir, file.getName().replace(from, to)));
            } else {
                file.renameTo(new File(dir, file.getName().replace(from, to)));
            }
        }
    }

    public static void findFiles(File file, FileFinder finder) {
        finder.found(file);
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                findFiles(child, finder);
            }
        }
    }

    public static boolean deleteFile(File path) {
        File[] files;
        if (path.exists() && (files = path.listFiles()) != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFile(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0L, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static byte[] readBytes(InputStream is) throws IOException {
        return toByteArray(is);
    }

    public static byte[] readBytes(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return readBytes(is);
        } finally {
            is.close();
        }
    }

    public static void writeBytes(OutputStream outputStream, byte[] data) throws IOException {
        write(data, outputStream);
    }

    public static void writeBytes(File file, byte[] data) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File should not be null.");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        } else if (!file.isFile()) {
            throw new IllegalArgumentException("Should not be a directory: " + file);
        } else if (!file.canWrite()) {
            throw new IllegalArgumentException("File cannot be written: " + file);
        } else {
            OutputStream os = new FileOutputStream(file);
            try {
                writeBytes(os, data);
                os.flush();
            } finally {
                os.close();
            }
        }
    }

    public static void writeUTF8(OutputStream outputStream, String data) throws IOException {
        write(data, outputStream, StringUtil.__UTF8);
    }

    public static void writeUTF8(File file, String contents) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File should not be null.");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        } else if (!file.isFile()) {
            throw new IllegalArgumentException("Should not be a directory: " + file);
        } else if (!file.canWrite()) {
            throw new IllegalArgumentException("File cannot be written: " + file);
        } else {
            OutputStream os = new FileOutputStream(file);
            try {
                writeUTF8(os, contents);
                os.flush();
            } finally {
                os.close();
            }
        }
    }

    public static String readLines(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("Inputstream was null");
        }
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder input = new StringBuilder();
        while (true) {
            String inputLine = inputReader.readLine();
            if (inputLine == null) {
                break;
            }
            input.append(inputLine);
            input.append(System.getProperty("line.separator"));
        }
        return input.length() > 0 ? input.toString() : "";
    }

    public static String readLines(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return readLines(is);
        } finally {
            is.close();
        }
    }

    public static String[] readLines(File file, boolean trimLines) throws IOException {
        return readLines(file, trimLines, null);
    }

    public static String[] readLines(File file, boolean trimLines, Character commentChar) throws IOException {
        return readLines(file, trimLines, commentChar, false);
    }

    public static String[] readLines(File file, boolean trimLines, Character commentChar, boolean skipEmptyLines) throws IOException {
        List<String> contents = new ArrayList<>();
        BufferedReader input = new BufferedReader(new FileReader(file));
        while (true) {
            try {
                String line = input.readLine();
                if (line != null) {
                    if (commentChar != null) {
                        if (line.matches("^\\s*" + commentChar + ".*")) {
                        }
                    }
                    String l = trimLines ? line.trim() : line;
                    if (!skipEmptyLines || l.length() != 0) {
                        contents.add(l);
                    }
                } else {
                    input.close();
                    return (String[]) contents.toArray(new String[contents.size()]);
                }
            } catch (Throwable th) {
                input.close();
                throw th;
            }
        }
    }

    static {
        StringWriter buf = new StringWriter(4);
        PrintWriter out = new PrintWriter(buf);
        out.println();
        LINE_SEPARATOR = buf.toString();
    }

    public static void closeQuietly(Reader input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

    public static void closeQuietly(Writer output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
            }
        }
    }

    public static void closeQuietly(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

    public static void closeQuietly(OutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
            }
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static byte[] toByteArray(Reader input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static byte[] toByteArray(Reader input, String encoding) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output, encoding);
        return output.toByteArray();
    }

    public static byte[] toByteArray(String input) throws IOException {
        return input.getBytes();
    }

    public static char[] toCharArray(InputStream is) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        copy(is, output);
        return output.toCharArray();
    }

    public static char[] toCharArray(InputStream is, String encoding) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        copy(is, output, encoding);
        return output.toCharArray();
    }

    public static char[] toCharArray(Reader input) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(input, sw);
        return sw.toCharArray();
    }

    public static String toString(InputStream input) throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    public static String toString(InputStream input, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    public static String toString(Reader input) throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    public static String toString(byte[] input) throws IOException {
        return new String(input);
    }

    public static String toString(byte[] input, String encoding) throws IOException {
        if (encoding == null) {
            return new String(input);
        }
        return new String(input, encoding);
    }

    public static InputStream toInputStream(String input) {
        byte[] bytes = input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    public static InputStream toInputStream(String input, String encoding) throws IOException {
        byte[] bytes = encoding != null ? input.getBytes(encoding) : input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    public static void write(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    public static void write(byte[] data, Writer output) throws IOException {
        if (data != null) {
            output.write(new String(data));
        }
    }

    public static void write(byte[] data, Writer output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                write(data, output);
            } else {
                output.write(new String(data, encoding));
            }
        }
    }

    public static void write(char[] data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    public static void write(char[] data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes());
        }
    }

    public static void write(char[] data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                write(data, output);
            } else {
                output.write(new String(data).getBytes(encoding));
            }
        }
    }

    public static void write(String data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    public static void write(String data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data.getBytes());
        }
    }

    public static void write(String data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                write(data, output);
            } else {
                output.write(data.getBytes(encoding));
            }
        }
    }

    public static void write(StringBuffer data, Writer output) throws IOException {
        if (data != null) {
            output.write(data.toString());
        }
    }

    public static void write(StringBuffer data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes());
        }
    }

    public static void write(StringBuffer data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            if (encoding == null) {
                write(data, output);
            } else {
                output.write(data.toString().getBytes(encoding));
            }
        }
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 != n) {
                output.write(buffer, 0, n);
                count += n;
            } else {
                return count;
            }
        }
    }

    public static void copy(InputStream input, Writer output) throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    public static void copy(InputStream input, Writer output, String encoding) throws IOException {
        if (encoding == null) {
            copy(input, output);
            return;
        }
        InputStreamReader in = new InputStreamReader(input, encoding);
        copy(in, output);
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 != n) {
                output.write(buffer, 0, n);
                count += n;
            } else {
                return count;
            }
        }
    }

    public static void copy(Reader input, OutputStream output) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(output);
        copy(input, out);
        out.flush();
    }

    public static void copy(Reader input, OutputStream output, String encoding) throws IOException {
        if (encoding == null) {
            copy(input, output);
            return;
        }
        OutputStreamWriter out = new OutputStreamWriter(output, encoding);
        copy(input, out);
        out.flush();
    }

    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }
        for (int ch = input1.read(); -1 != ch; ch = input1.read()) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
        }
        int ch22 = input2.read();
        return ch22 == -1;
    }

    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        if (!(input1 instanceof BufferedReader)) {
            input1 = new BufferedReader(input1);
        }
        if (!(input2 instanceof BufferedReader)) {
            input2 = new BufferedReader(input2);
        }
        for (int ch = input1.read(); -1 != ch; ch = input1.read()) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
        }
        int ch22 = input2.read();
        return ch22 == -1;
    }
}
