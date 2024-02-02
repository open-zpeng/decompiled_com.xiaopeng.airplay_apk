package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class Resource implements ResourceFactory {
    private static final Logger LOG = Log.getLogger(Resource.class);
    public static boolean __defaultUseCaches = true;
    volatile Object _associate;

    public abstract Resource addPath(String str) throws IOException, MalformedURLException;

    public abstract boolean delete() throws SecurityException;

    public abstract boolean exists();

    public abstract File getFile() throws IOException;

    public abstract InputStream getInputStream() throws IOException;

    public abstract String getName();

    public abstract OutputStream getOutputStream() throws IOException, SecurityException;

    public abstract URL getURL();

    public abstract boolean isContainedIn(Resource resource) throws MalformedURLException;

    public abstract boolean isDirectory();

    public abstract long lastModified();

    public abstract long length();

    public abstract String[] list();

    public abstract void release();

    public abstract boolean renameTo(Resource resource) throws SecurityException;

    public static void setDefaultUseCaches(boolean useCaches) {
        __defaultUseCaches = useCaches;
    }

    public static boolean getDefaultUseCaches() {
        return __defaultUseCaches;
    }

    public static Resource newResource(URI uri) throws IOException {
        return newResource(uri.toURL());
    }

    public static Resource newResource(URL url) throws IOException {
        return newResource(url, __defaultUseCaches);
    }

    static Resource newResource(URL url, boolean useCaches) {
        if (url == null) {
            return null;
        }
        String url_string = url.toExternalForm();
        if (url_string.startsWith("file:")) {
            try {
                FileResource fileResource = new FileResource(url);
                return fileResource;
            } catch (Exception e) {
                LOG.debug(Log.EXCEPTION, e);
                return new BadResource(url, e.toString());
            }
        } else if (url_string.startsWith("jar:file:")) {
            return new JarFileResource(url, useCaches);
        } else {
            if (url_string.startsWith("jar:")) {
                return new JarResource(url, useCaches);
            }
            return new URLResource(url, null, useCaches);
        }
    }

    public static Resource newResource(String resource) throws MalformedURLException, IOException {
        return newResource(resource, __defaultUseCaches);
    }

    public static Resource newResource(String resource, boolean useCaches) throws MalformedURLException, IOException {
        try {
            return newResource(new URL(resource));
        } catch (MalformedURLException e) {
            if (!resource.startsWith("ftp:") && !resource.startsWith("file:") && !resource.startsWith("jar:")) {
                try {
                    if (resource.startsWith("./")) {
                        resource = resource.substring(2);
                    }
                    File file = new File(resource).getCanonicalFile();
                    URL url = toURL(file);
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(useCaches);
                    return new FileResource(url, connection, file);
                } catch (Exception e2) {
                    LOG.debug(Log.EXCEPTION, e2);
                    throw e;
                }
            }
            Logger logger = LOG;
            logger.warn("Bad Resource: " + resource, new Object[0]);
            throw e;
        }
    }

    public static Resource newResource(File file) throws MalformedURLException, IOException {
        File file2 = file.getCanonicalFile();
        URL url = toURL(file2);
        URLConnection connection = url.openConnection();
        FileResource fileResource = new FileResource(url, connection, file2);
        return fileResource;
    }

    public static Resource newSystemResource(String resource) throws IOException {
        ClassLoader loader;
        URL url = null;
        ClassLoader loader2 = Thread.currentThread().getContextClassLoader();
        if (loader2 != null) {
            try {
                url = loader2.getResource(resource);
                if (url == null && resource.startsWith("/")) {
                    url = loader2.getResource(resource.substring(1));
                }
            } catch (IllegalArgumentException e) {
                url = null;
            }
        }
        if (url == null && (loader = Resource.class.getClassLoader()) != null && (url = loader.getResource(resource)) == null && resource.startsWith("/")) {
            url = loader.getResource(resource.substring(1));
        }
        if (url == null && (url = ClassLoader.getSystemResource(resource)) == null && resource.startsWith("/")) {
            url = ClassLoader.getSystemResource(resource.substring(1));
        }
        if (url == null) {
            return null;
        }
        return newResource(url);
    }

    public static Resource newClassPathResource(String resource) {
        return newClassPathResource(resource, true, false);
    }

    public static Resource newClassPathResource(String name, boolean useCaches, boolean checkParents) {
        URL url = Resource.class.getResource(name);
        if (url == null) {
            url = Loader.getResource(Resource.class, name, checkParents);
        }
        if (url == null) {
            return null;
        }
        return newResource(url, useCaches);
    }

    public static boolean isContainedIn(Resource r, Resource containingResource) throws MalformedURLException {
        return r.isContainedIn(containingResource);
    }

    protected void finalize() {
        release();
    }

    public URI getURI() {
        try {
            return getURL().toURI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override // org.eclipse.jetty.util.resource.ResourceFactory
    public Resource getResource(String path) {
        try {
            return addPath(path);
        } catch (Exception e) {
            LOG.debug(e);
            return null;
        }
    }

    public String encode(String uri) {
        return URIUtil.encodePath(uri);
    }

    public Object getAssociate() {
        return this._associate;
    }

    public void setAssociate(Object o) {
        this._associate = o;
    }

    public URL getAlias() {
        return null;
    }

    public String getListHTML(String base, boolean parent) throws IOException {
        String[] ls;
        String base2 = URIUtil.canonicalPath(base);
        if (base2 == null || !isDirectory() || (ls = list()) == null) {
            return null;
        }
        Arrays.sort(ls);
        String decodedBase = URIUtil.decodePath(base2);
        String title = "Directory: " + deTag(decodedBase);
        StringBuilder buf = new StringBuilder(4096);
        buf.append("<HTML><HEAD>");
        buf.append("<LINK HREF=\"");
        buf.append("jetty-dir.css");
        buf.append("\" REL=\"stylesheet\" TYPE=\"text/css\"/><TITLE>");
        buf.append(title);
        buf.append("</TITLE></HEAD><BODY>\n<H1>");
        buf.append(title);
        buf.append("</H1>\n<TABLE BORDER=0>\n");
        if (parent) {
            buf.append("<TR><TD><A HREF=\"");
            buf.append(URIUtil.addPaths(base2, "../"));
            buf.append("\">Parent Directory</A></TD><TD></TD><TD></TD></TR>\n");
        }
        String encodedBase = hrefEncodeURI(base2);
        DateFormat dfmt = DateFormat.getDateTimeInstance(2, 2);
        for (int i = 0; i < ls.length; i++) {
            Resource item = addPath(ls[i]);
            buf.append("\n<TR><TD><A HREF=\"");
            String path = URIUtil.addPaths(encodedBase, URIUtil.encodePath(ls[i]));
            buf.append(path);
            if (item.isDirectory() && !path.endsWith("/")) {
                buf.append("/");
            }
            buf.append("\">");
            buf.append(deTag(ls[i]));
            buf.append("&nbsp;");
            buf.append("</A></TD><TD ALIGN=right>");
            buf.append(item.length());
            buf.append(" bytes&nbsp;</TD><TD>");
            buf.append(dfmt.format(new Date(item.lastModified())));
            buf.append("</TD></TR>");
        }
        buf.append("</TABLE>\n");
        buf.append("</BODY></HTML>\n");
        return buf.toString();
    }

    private static String hrefEncodeURI(String raw) {
        StringBuffer buf = null;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\"' || c == '\'' || c == '<' || c == '>') {
                buf = new StringBuffer(raw.length() << 1);
                break;
            }
        }
        if (buf == null) {
            return raw;
        }
        for (int i2 = 0; i2 < raw.length(); i2++) {
            char c2 = raw.charAt(i2);
            if (c2 == '\"') {
                buf.append("%22");
            } else if (c2 == '\'') {
                buf.append("%27");
            } else if (c2 == '<') {
                buf.append("%3C");
            } else if (c2 == '>') {
                buf.append("%3E");
            } else {
                buf.append(c2);
            }
        }
        return buf.toString();
    }

    private static String deTag(String raw) {
        return StringUtil.replace(StringUtil.replace(raw, "<", "&lt;"), ">", "&gt;");
    }

    public void writeTo(OutputStream out, long start, long count) throws IOException {
        InputStream in = getInputStream();
        try {
            in.skip(start);
            if (count < 0) {
                IO.copy(in, out);
            } else {
                IO.copy(in, out, count);
            }
        } finally {
            in.close();
        }
    }

    public void copyTo(File destination) throws IOException {
        if (destination.exists()) {
            throw new IllegalArgumentException(destination + " exists");
        }
        writeTo(new FileOutputStream(destination), 0L, -1L);
    }

    public String getWeakETag() {
        try {
            StringBuilder b = new StringBuilder(32);
            b.append("W/\"");
            String name = getName();
            int length = name.length();
            long lhash = 0;
            for (int i = 0; i < length; i++) {
                lhash = (31 * lhash) + name.charAt(i);
            }
            B64Code.encode(lastModified() ^ lhash, b);
            B64Code.encode(length() ^ lhash, b);
            b.append('\"');
            return b.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL toURL(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }
}
