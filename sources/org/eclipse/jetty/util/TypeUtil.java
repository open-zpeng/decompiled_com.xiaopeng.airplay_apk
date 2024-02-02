package org.eclipse.jetty.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class TypeUtil {
    private static final HashMap<Class<?>, String> class2Name;
    private static final HashMap<Class<?>, Method> class2Value;
    private static final Logger LOG = Log.getLogger(TypeUtil.class);
    public static int CR = 13;
    public static int LF = 10;
    private static final HashMap<String, Class<?>> name2Class = new HashMap<>();

    static {
        name2Class.put("boolean", Boolean.TYPE);
        name2Class.put("byte", Byte.TYPE);
        name2Class.put("char", Character.TYPE);
        name2Class.put("double", Double.TYPE);
        name2Class.put("float", Float.TYPE);
        name2Class.put("int", Integer.TYPE);
        name2Class.put("long", Long.TYPE);
        name2Class.put("short", Short.TYPE);
        name2Class.put("void", Void.TYPE);
        name2Class.put("java.lang.Boolean.TYPE", Boolean.TYPE);
        name2Class.put("java.lang.Byte.TYPE", Byte.TYPE);
        name2Class.put("java.lang.Character.TYPE", Character.TYPE);
        name2Class.put("java.lang.Double.TYPE", Double.TYPE);
        name2Class.put("java.lang.Float.TYPE", Float.TYPE);
        name2Class.put("java.lang.Integer.TYPE", Integer.TYPE);
        name2Class.put("java.lang.Long.TYPE", Long.TYPE);
        name2Class.put("java.lang.Short.TYPE", Short.TYPE);
        name2Class.put("java.lang.Void.TYPE", Void.TYPE);
        name2Class.put("java.lang.Boolean", Boolean.class);
        name2Class.put("java.lang.Byte", Byte.class);
        name2Class.put("java.lang.Character", Character.class);
        name2Class.put("java.lang.Double", Double.class);
        name2Class.put("java.lang.Float", Float.class);
        name2Class.put("java.lang.Integer", Integer.class);
        name2Class.put("java.lang.Long", Long.class);
        name2Class.put("java.lang.Short", Short.class);
        name2Class.put("Boolean", Boolean.class);
        name2Class.put("Byte", Byte.class);
        name2Class.put("Character", Character.class);
        name2Class.put("Double", Double.class);
        name2Class.put("Float", Float.class);
        name2Class.put("Integer", Integer.class);
        name2Class.put("Long", Long.class);
        name2Class.put("Short", Short.class);
        name2Class.put(null, Void.TYPE);
        name2Class.put("string", String.class);
        name2Class.put("String", String.class);
        name2Class.put("java.lang.String", String.class);
        class2Name = new HashMap<>();
        class2Name.put(Boolean.TYPE, "boolean");
        class2Name.put(Byte.TYPE, "byte");
        class2Name.put(Character.TYPE, "char");
        class2Name.put(Double.TYPE, "double");
        class2Name.put(Float.TYPE, "float");
        class2Name.put(Integer.TYPE, "int");
        class2Name.put(Long.TYPE, "long");
        class2Name.put(Short.TYPE, "short");
        class2Name.put(Void.TYPE, "void");
        class2Name.put(Boolean.class, "java.lang.Boolean");
        class2Name.put(Byte.class, "java.lang.Byte");
        class2Name.put(Character.class, "java.lang.Character");
        class2Name.put(Double.class, "java.lang.Double");
        class2Name.put(Float.class, "java.lang.Float");
        class2Name.put(Integer.class, "java.lang.Integer");
        class2Name.put(Long.class, "java.lang.Long");
        class2Name.put(Short.class, "java.lang.Short");
        class2Name.put(null, "void");
        class2Name.put(String.class, "java.lang.String");
        class2Value = new HashMap<>();
        try {
            Class<?>[] s = {String.class};
            class2Value.put(Boolean.TYPE, Boolean.class.getMethod("valueOf", s));
            class2Value.put(Byte.TYPE, Byte.class.getMethod("valueOf", s));
            class2Value.put(Double.TYPE, Double.class.getMethod("valueOf", s));
            class2Value.put(Float.TYPE, Float.class.getMethod("valueOf", s));
            class2Value.put(Integer.TYPE, Integer.class.getMethod("valueOf", s));
            class2Value.put(Long.TYPE, Long.class.getMethod("valueOf", s));
            class2Value.put(Short.TYPE, Short.class.getMethod("valueOf", s));
            class2Value.put(Boolean.class, Boolean.class.getMethod("valueOf", s));
            class2Value.put(Byte.class, Byte.class.getMethod("valueOf", s));
            class2Value.put(Double.class, Double.class.getMethod("valueOf", s));
            class2Value.put(Float.class, Float.class.getMethod("valueOf", s));
            class2Value.put(Integer.class, Integer.class.getMethod("valueOf", s));
            class2Value.put(Long.class, Long.class.getMethod("valueOf", s));
            class2Value.put(Short.class, Short.class.getMethod("valueOf", s));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static <T> List<T> asList(T[] a) {
        if (a == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(a);
    }

    public static Class<?> fromName(String name) {
        return name2Class.get(name);
    }

    public static String toName(Class<?> type) {
        return class2Name.get(type);
    }

    public static Object valueOf(Class<?> type, String value) {
        try {
            if (type.equals(String.class)) {
                return value;
            }
            Method m = class2Value.get(type);
            if (m != null) {
                return m.invoke(null, value);
            }
            if (!type.equals(Character.TYPE) && !type.equals(Character.class)) {
                Constructor<?> c = type.getConstructor(String.class);
                return c.newInstance(value);
            }
            return new Character(value.charAt(0));
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e2) {
            return null;
        } catch (NoSuchMethodException e3) {
            return null;
        } catch (InvocationTargetException e4) {
            if (e4.getTargetException() instanceof Error) {
                throw ((Error) e4.getTargetException());
            }
            return null;
        }
    }

    public static Object valueOf(String type, String value) {
        return valueOf(fromName(type), value);
    }

    public static int parseInt(String s, int offset, int length, int base) throws NumberFormatException {
        int value = 0;
        if (length < 0) {
            length = s.length() - offset;
        }
        for (int i = 0; i < length; i++) {
            char c = s.charAt(offset + i);
            int digit = convertHexDigit(c);
            if (digit < 0 || digit >= base) {
                throw new NumberFormatException(s.substring(offset, offset + length));
            }
            value = (value * base) + digit;
        }
        return value;
    }

    public static int parseInt(byte[] b, int offset, int length, int base) throws NumberFormatException {
        int value = 0;
        if (length < 0) {
            length = b.length - offset;
        }
        for (int i = 0; i < length; i++) {
            char c = (char) (255 & b[offset + i]);
            int digit = c - '0';
            if ((digit < 0 || digit >= base || digit >= 10) && (('\n' + c) - 65 < 10 || digit >= base)) {
                digit = ('\n' + c) - 97;
            }
            if (digit < 0 || digit >= base) {
                throw new NumberFormatException(new String(b, offset, length));
            }
            value = (value * base) + digit;
        }
        return value;
    }

    public static byte[] parseBytes(String s, int base) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            bytes[i / 2] = (byte) parseInt(s, i, 2, base);
        }
        return bytes;
    }

    public static String toString(byte[] bytes, int base) {
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            int bi = 255 & b;
            int c = ((bi / base) % base) + 48;
            if (c > 57) {
                c = 97 + ((c - 48) - 10);
            }
            buf.append((char) c);
            int c2 = 48 + (bi % base);
            if (c2 > 57) {
                c2 = 97 + ((c2 - 48) - 10);
            }
            buf.append((char) c2);
        }
        return buf.toString();
    }

    public static byte convertHexDigit(byte c) {
        byte b = (byte) (((c & 31) + ((c >> 6) * 25)) - 16);
        if (b < 0 || b > 15) {
            throw new IllegalArgumentException("!hex " + ((int) c));
        }
        return b;
    }

    public static int convertHexDigit(int c) {
        int d = ((c & 31) + ((c >> 6) * 25)) - 16;
        if (d < 0 || d > 15) {
            throw new NumberFormatException("!hex " + c);
        }
        return d;
    }

    public static void toHex(byte b, Appendable buf) {
        int d = ((240 & b) >> 4) & 15;
        try {
            buf.append((char) ((d > 9 ? 55 : 48) + d));
            int d2 = 15 & b;
            buf.append((char) ((d2 > 9 ? 55 : 48) + d2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void toHex(int value, Appendable buf) throws IOException {
        int d = (((-268435456) & value) >> 28) & 15;
        buf.append((char) ((d > 9 ? 55 : 48) + d));
        int d2 = 15 & ((251658240 & value) >> 24);
        buf.append((char) ((d2 > 9 ? 55 : 48) + d2));
        int d3 = 15 & ((15728640 & value) >> 20);
        buf.append((char) ((d3 > 9 ? 55 : 48) + d3));
        int d4 = 15 & ((983040 & value) >> 16);
        buf.append((char) ((d4 > 9 ? 55 : 48) + d4));
        int d5 = 15 & ((61440 & value) >> 12);
        buf.append((char) ((d5 > 9 ? 55 : 48) + d5));
        int d6 = 15 & ((3840 & value) >> 8);
        buf.append((char) ((d6 > 9 ? 55 : 48) + d6));
        int d7 = 15 & ((240 & value) >> 4);
        buf.append((char) ((d7 > 9 ? 55 : 48) + d7));
        int d8 = 15 & value;
        buf.append((char) ((d8 > 9 ? 55 : 48) + d8));
        Integer.toString(0, 36);
    }

    public static void toHex(long value, Appendable buf) throws IOException {
        toHex((int) (value >> 32), buf);
        toHex((int) value, buf);
    }

    public static String toHexString(byte b) {
        return toHexString(new byte[]{b}, 0, 1);
    }

    public static String toHexString(byte[] b) {
        return toHexString(b, 0, b.length);
    }

    public static String toHexString(byte[] b, int offset, int length) {
        StringBuilder buf = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            int bi = 255 & b[i];
            int c = ((bi / 16) % 16) + 48;
            if (c > 57) {
                c = 65 + ((c - 48) - 10);
            }
            buf.append((char) c);
            int c2 = 48 + (bi % 16);
            if (c2 > 57) {
                c2 = 97 + ((c2 - 48) - 10);
            }
            buf.append((char) c2);
        }
        return buf.toString();
    }

    public static byte[] fromHexString(String s) {
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException(s);
        }
        byte[] array = new byte[s.length() / 2];
        for (int i = 0; i < array.length; i++) {
            int b = Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16);
            array[i] = (byte) (255 & b);
        }
        return array;
    }

    public static void dump(Class<?> c) {
        PrintStream printStream = System.err;
        printStream.println("Dump: " + c);
        dump(c.getClassLoader());
    }

    public static void dump(ClassLoader cl) {
        System.err.println("Dump Loaders:");
        while (cl != null) {
            PrintStream printStream = System.err;
            printStream.println("  loader " + cl);
            cl = cl.getParent();
        }
    }

    public static byte[] readLine(InputStream in) throws IOException {
        int ch;
        byte[] buf = new byte[CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE];
        int loops = 0;
        int i = 0;
        byte[] buf2 = buf;
        while (true) {
            ch = in.read();
            if (ch < 0) {
                break;
            }
            loops++;
            if (loops != 1 || ch != LF) {
                if (ch == CR || ch == LF) {
                    break;
                }
                if (i >= buf2.length) {
                    byte[] old_buf = buf2;
                    buf2 = new byte[old_buf.length + CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE];
                    System.arraycopy(old_buf, 0, buf2, 0, old_buf.length);
                }
                buf2[i] = (byte) ch;
                i++;
            }
        }
        if (ch == -1 && i == 0) {
            return null;
        }
        if (ch == CR && in.available() >= 1 && in.markSupported()) {
            in.mark(1);
            if (in.read() != LF) {
                in.reset();
            }
        }
        byte[] old_buf2 = buf2;
        byte[] buf3 = new byte[i];
        System.arraycopy(old_buf2, 0, buf3, 0, i);
        return buf3;
    }

    public static URL jarFor(String className) {
        try {
            URL url = Loader.getResource(null, className.replace('.', '/') + ".class", false);
            String s = url.toString();
            if (s.startsWith("jar:file:")) {
                return new URL(s.substring(4, s.indexOf("!/")));
            }
        } catch (Exception e) {
            LOG.ignore(e);
        }
        return null;
    }

    public static Object call(Class<?> oClass, String method, Object obj, Object[] arg) throws InvocationTargetException, NoSuchMethodException {
        Method[] methods = oClass.getMethods();
        for (int c = 0; methods != null && c < methods.length; c++) {
            if (methods[c].getName().equals(method) && methods[c].getParameterTypes().length == arg.length) {
                if (Modifier.isStatic(methods[c].getModifiers()) == (obj == null) && (obj != null || methods[c].getDeclaringClass() == oClass)) {
                    try {
                        return methods[c].invoke(obj, arg);
                    } catch (IllegalAccessException e) {
                        LOG.ignore(e);
                    } catch (IllegalArgumentException e2) {
                        LOG.ignore(e2);
                    }
                }
            }
        }
        throw new NoSuchMethodException(method);
    }
}
