package org.eclipse.jetty.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class Loader {
    public static URL getResource(Class<?> loadClass, String name, boolean checkParents) {
        URL url = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (true) {
            ClassLoader classLoader = null;
            if (url != null || loader == null) {
                break;
            }
            url = loader.getResource(name);
            if (url == null && checkParents) {
                classLoader = loader.getParent();
            }
            loader = classLoader;
        }
        ClassLoader loader2 = loadClass == null ? null : loadClass.getClassLoader();
        while (url == null && loader2 != null) {
            url = loader2.getResource(name);
            loader2 = (url == null && checkParents) ? loader2.getParent() : null;
        }
        if (url == null) {
            URL url2 = ClassLoader.getSystemResource(name);
            return url2;
        }
        return url;
    }

    public static Class loadClass(Class loadClass, String name) throws ClassNotFoundException {
        return loadClass(loadClass, name, false);
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0031, code lost:
        r5 = null;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.Class loadClass(java.lang.Class r6, java.lang.String r7, boolean r8) throws java.lang.ClassNotFoundException {
        /*
            r0 = 0
            r1 = 0
            java.lang.Thread r2 = java.lang.Thread.currentThread()
            java.lang.ClassLoader r2 = r2.getContextClassLoader()
            java.util.HashSet r3 = new java.util.HashSet
            r3.<init>()
        Lf:
            r4 = 0
            if (r1 != 0) goto L2f
            if (r2 == 0) goto L2f
            boolean r5 = r3.add(r2)
            if (r5 == 0) goto L2f
            java.lang.Class r5 = r2.loadClass(r7)     // Catch: java.lang.ClassNotFoundException -> L20
            r1 = r5
            goto L24
        L20:
            r5 = move-exception
            if (r0 != 0) goto L24
            r0 = r5
        L24:
            if (r1 != 0) goto L2d
            if (r8 == 0) goto L2d
            java.lang.ClassLoader r4 = r2.getParent()
        L2d:
            r2 = r4
            goto Lf
        L2f:
            if (r6 != 0) goto L33
            r5 = r4
            goto L37
        L33:
            java.lang.ClassLoader r5 = r6.getClassLoader()
        L37:
            r2 = r5
        L38:
            if (r1 != 0) goto L58
            if (r2 == 0) goto L58
            boolean r5 = r3.add(r2)
            if (r5 == 0) goto L58
            java.lang.Class r5 = r2.loadClass(r7)     // Catch: java.lang.ClassNotFoundException -> L48
            r1 = r5
            goto L4c
        L48:
            r5 = move-exception
            if (r0 != 0) goto L4c
            r0 = r5
        L4c:
            if (r1 != 0) goto L55
            if (r8 == 0) goto L55
            java.lang.ClassLoader r5 = r2.getParent()
            goto L56
        L55:
            r5 = r4
        L56:
            r2 = r5
            goto L38
        L58:
            java.lang.Class<org.eclipse.jetty.util.Loader> r4 = org.eclipse.jetty.util.Loader.class
            java.lang.ClassLoader r2 = r4.getClassLoader()
            if (r1 != 0) goto L72
            if (r2 == 0) goto L72
            boolean r4 = r3.add(r2)
            if (r4 == 0) goto L72
            java.lang.Class r4 = java.lang.Class.forName(r7)     // Catch: java.lang.ClassNotFoundException -> L6e
            r1 = r4
            goto L72
        L6e:
            r4 = move-exception
            if (r0 != 0) goto L72
            r0 = r4
        L72:
            if (r1 == 0) goto L75
            return r1
        L75:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.Loader.loadClass(java.lang.Class, java.lang.String, boolean):java.lang.Class");
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0031, code lost:
        r5 = null;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.util.ResourceBundle getResourceBundle(java.lang.Class<?> r6, java.lang.String r7, boolean r8, java.util.Locale r9) throws java.util.MissingResourceException {
        /*
            r0 = 0
            r1 = 0
            java.lang.Thread r2 = java.lang.Thread.currentThread()
            java.lang.ClassLoader r2 = r2.getContextClassLoader()
            java.util.HashSet r3 = new java.util.HashSet
            r3.<init>()
        Lf:
            r4 = 0
            if (r1 != 0) goto L2f
            if (r2 == 0) goto L2f
            boolean r5 = r3.add(r2)
            if (r5 == 0) goto L2f
            java.util.ResourceBundle r5 = java.util.ResourceBundle.getBundle(r7, r9, r2)     // Catch: java.util.MissingResourceException -> L20
            r1 = r5
            goto L24
        L20:
            r5 = move-exception
            if (r0 != 0) goto L24
            r0 = r5
        L24:
            if (r1 != 0) goto L2d
            if (r8 == 0) goto L2d
            java.lang.ClassLoader r4 = r2.getParent()
        L2d:
            r2 = r4
            goto Lf
        L2f:
            if (r6 != 0) goto L33
            r5 = r4
            goto L37
        L33:
            java.lang.ClassLoader r5 = r6.getClassLoader()
        L37:
            r2 = r5
        L38:
            if (r1 != 0) goto L58
            if (r2 == 0) goto L58
            boolean r5 = r3.add(r2)
            if (r5 == 0) goto L58
            java.util.ResourceBundle r5 = java.util.ResourceBundle.getBundle(r7, r9, r2)     // Catch: java.util.MissingResourceException -> L48
            r1 = r5
            goto L4c
        L48:
            r5 = move-exception
            if (r0 != 0) goto L4c
            r0 = r5
        L4c:
            if (r1 != 0) goto L55
            if (r8 == 0) goto L55
            java.lang.ClassLoader r5 = r2.getParent()
            goto L56
        L55:
            r5 = r4
        L56:
            r2 = r5
            goto L38
        L58:
            java.lang.Class<org.eclipse.jetty.util.Loader> r4 = org.eclipse.jetty.util.Loader.class
            java.lang.ClassLoader r2 = r4.getClassLoader()
            if (r1 != 0) goto L72
            if (r2 == 0) goto L72
            boolean r4 = r3.add(r2)
            if (r4 == 0) goto L72
            java.util.ResourceBundle r4 = java.util.ResourceBundle.getBundle(r7, r9)     // Catch: java.util.MissingResourceException -> L6e
            r1 = r4
            goto L72
        L6e:
            r4 = move-exception
            if (r0 != 0) goto L72
            r0 = r4
        L72:
            if (r1 == 0) goto L75
            return r1
        L75:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.Loader.getResourceBundle(java.lang.Class, java.lang.String, boolean, java.util.Locale):java.util.ResourceBundle");
    }

    public static String getClassPath(ClassLoader loader) throws Exception {
        StringBuilder classpath = new StringBuilder();
        while (loader != null && (loader instanceof URLClassLoader)) {
            URL[] urls = ((URLClassLoader) loader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    Resource resource = Resource.newResource(url);
                    File file = resource.getFile();
                    if (file != null && file.exists()) {
                        if (classpath.length() > 0) {
                            classpath.append(File.pathSeparatorChar);
                        }
                        classpath.append(file.getAbsolutePath());
                    }
                }
            }
            loader = loader.getParent();
        }
        return classpath.toString();
    }
}
