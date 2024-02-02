package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class FileResource extends URLResource {
    private static final Logger LOG = Log.getLogger(FileResource.class);
    private static boolean __checkAliases = true;
    private transient URL _alias;
    private transient boolean _aliasChecked;
    private File _file;

    public static void setCheckAliases(boolean checkAliases) {
        __checkAliases = checkAliases;
    }

    public static boolean getCheckAliases() {
        return __checkAliases;
    }

    public FileResource(URL url) throws IOException, URISyntaxException {
        super(url, null);
        this._alias = null;
        this._aliasChecked = false;
        try {
            this._file = new File(new URI(url.toString()));
        } catch (URISyntaxException e) {
            throw e;
        } catch (Exception e2) {
            LOG.ignore(e2);
            try {
                String file_url = "file:" + URIUtil.encodePath(url.toString().substring(5));
                URI uri = new URI(file_url);
                if (uri.getAuthority() == null) {
                    this._file = new File(uri);
                } else {
                    this._file = new File("//" + uri.getAuthority() + URIUtil.decodePath(url.getFile()));
                }
            } catch (Exception e22) {
                LOG.ignore(e22);
                checkConnection();
                Permission perm = this._connection.getPermission();
                this._file = new File(perm == null ? url.getFile() : perm.getName());
            }
        }
        if (this._file.isDirectory()) {
            if (!this._urlString.endsWith("/")) {
                this._urlString += "/";
            }
        } else if (this._urlString.endsWith("/")) {
            this._urlString = this._urlString.substring(0, this._urlString.length() - 1);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FileResource(URL url, URLConnection connection, File file) {
        super(url, connection);
        this._alias = null;
        this._aliasChecked = false;
        this._file = file;
        if (this._file.isDirectory() && !this._urlString.endsWith("/")) {
            this._urlString += "/";
        }
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public Resource addPath(String path) throws IOException, MalformedURLException {
        URLResource r;
        String path2 = URIUtil.canonicalPath(path);
        if ("/".equals(path2)) {
            return this;
        }
        if (!isDirectory()) {
            r = (FileResource) super.addPath(path2);
            String url = r._urlString;
        } else if (path2 == null) {
            throw new MalformedURLException();
        } else {
            String rel = path2;
            if (path2.startsWith("/")) {
                rel = path2.substring(1);
            }
            String url2 = URIUtil.addPaths(this._urlString, URIUtil.encodePath(rel));
            r = (URLResource) Resource.newResource(url2);
        }
        String encoded = URIUtil.encodePath(path2);
        int expected = r.toString().length() - encoded.length();
        int index = r._urlString.lastIndexOf(encoded, expected);
        if (expected != index && ((expected - 1 != index || path2.endsWith("/") || !r.isDirectory()) && (r instanceof FileResource))) {
            ((FileResource) r)._alias = ((FileResource) r)._file.getCanonicalFile().toURI().toURL();
            ((FileResource) r)._aliasChecked = true;
        }
        return r;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public URL getAlias() {
        if (__checkAliases && !this._aliasChecked) {
            try {
                String abs = this._file.getAbsolutePath();
                String can = this._file.getCanonicalPath();
                if (abs.length() != can.length() || !abs.equals(can)) {
                    this._alias = Resource.toURL(new File(can));
                }
                this._aliasChecked = true;
                if (this._alias != null && LOG.isDebugEnabled()) {
                    Logger logger = LOG;
                    logger.debug("ALIAS abs=" + abs, new Object[0]);
                    Logger logger2 = LOG;
                    logger2.debug("ALIAS can=" + can, new Object[0]);
                }
            } catch (Exception e) {
                LOG.warn(Log.EXCEPTION, e);
                return getURL();
            }
        }
        return this._alias;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean exists() {
        return this._file.exists();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public long lastModified() {
        return this._file.lastModified();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean isDirectory() {
        return this._file.isDirectory();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public long length() {
        return this._file.length();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public String getName() {
        return this._file.getAbsolutePath();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public File getFile() {
        return this._file;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this._file);
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public OutputStream getOutputStream() throws IOException, SecurityException {
        return new FileOutputStream(this._file);
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean delete() throws SecurityException {
        return this._file.delete();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean renameTo(Resource dest) throws SecurityException {
        if (dest instanceof FileResource) {
            return this._file.renameTo(((FileResource) dest)._file);
        }
        return false;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public String[] list() {
        String[] list = this._file.list();
        if (list == null) {
            return null;
        }
        int i = list.length;
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                if (new File(this._file, list[i2]).isDirectory() && !list[i2].endsWith("/")) {
                    list[i2] = list[i2] + "/";
                }
                i = i2;
            } else {
                return list;
            }
        }
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public String encode(String uri) {
        return uri;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof FileResource)) {
            return false;
        }
        FileResource f = (FileResource) o;
        if (f._file != this._file) {
            if (this._file != null && this._file.equals(f._file)) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource
    public int hashCode() {
        return this._file == null ? super.hashCode() : this._file.hashCode();
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public void copyTo(File destination) throws IOException {
        if (isDirectory()) {
            IO.copyDir(getFile(), destination);
        } else if (destination.exists()) {
            throw new IllegalArgumentException(destination + " exists");
        } else {
            IO.copy(getFile(), destination);
        }
    }
}
