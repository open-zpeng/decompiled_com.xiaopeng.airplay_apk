package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class JarFileResource extends JarResource {
    private static final Logger LOG = Log.getLogger(JarFileResource.class);
    private boolean _directory;
    private JarEntry _entry;
    private boolean _exists;
    private File _file;
    private JarFile _jarFile;
    private String _jarUrl;
    private String[] _list;
    private String _path;

    JarFileResource(URL url) {
        super(url);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public JarFileResource(URL url, boolean useCaches) {
        super(url, useCaches);
    }

    @Override // org.eclipse.jetty.util.resource.JarResource, org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public synchronized void release() {
        this._list = null;
        this._entry = null;
        this._file = null;
        if (!getUseCaches() && this._jarFile != null) {
            try {
                Logger logger = LOG;
                logger.debug("Closing JarFile " + this._jarFile.getName(), new Object[0]);
                this._jarFile.close();
            } catch (IOException ioe) {
                LOG.ignore(ioe);
            }
        }
        this._jarFile = null;
        super.release();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [java.util.jar.JarEntry, java.util.jar.JarFile, java.lang.String[], java.io.File] */
    @Override // org.eclipse.jetty.util.resource.JarResource, org.eclipse.jetty.util.resource.URLResource
    protected boolean checkConnection() {
        try {
            super.checkConnection();
            return this._jarFile != null;
        } finally {
            if (this._jarConnection == null) {
                this._entry = null;
                this._file = null;
                this._jarFile = null;
                this._list = null;
            }
        }
    }

    @Override // org.eclipse.jetty.util.resource.JarResource
    protected synchronized void newConnection() throws IOException {
        super.newConnection();
        this._entry = null;
        this._file = null;
        this._jarFile = null;
        this._list = null;
        int sep = this._urlString.indexOf("!/");
        this._jarUrl = this._urlString.substring(0, sep + 2);
        this._path = this._urlString.substring(sep + 2);
        if (this._path.length() == 0) {
            this._path = null;
        }
        this._jarFile = this._jarConnection.getJarFile();
        this._file = new File(this._jarFile.getName());
    }

    @Override // org.eclipse.jetty.util.resource.JarResource, org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean exists() {
        boolean z = true;
        if (this._exists) {
            return true;
        }
        if (this._urlString.endsWith("!/")) {
            String file_url = this._urlString.substring(4, this._urlString.length() - 2);
            try {
                return newResource(file_url).exists();
            } catch (Exception e) {
                LOG.ignore(e);
                return false;
            }
        }
        boolean check = checkConnection();
        if (this._jarUrl != null && this._path == null) {
            this._directory = check;
            return true;
        }
        JarFile jarFile = null;
        if (check) {
            jarFile = this._jarFile;
        } else {
            try {
                JarURLConnection c = (JarURLConnection) new URL(this._jarUrl).openConnection();
                c.setUseCaches(getUseCaches());
                jarFile = c.getJarFile();
            } catch (Exception e2) {
                LOG.ignore(e2);
            }
        }
        if (jarFile != null && this._entry == null && !this._directory) {
            Enumeration<JarEntry> e3 = jarFile.entries();
            while (true) {
                if (!e3.hasMoreElements()) {
                    break;
                }
                JarEntry entry = e3.nextElement();
                String name = entry.getName().replace('\\', '/');
                if (name.equals(this._path)) {
                    this._entry = entry;
                    this._directory = this._path.endsWith("/");
                    break;
                } else if (this._path.endsWith("/")) {
                    if (name.startsWith(this._path)) {
                        this._directory = true;
                        break;
                    }
                } else if (name.startsWith(this._path) && name.length() > this._path.length() && name.charAt(this._path.length()) == '/') {
                    this._directory = true;
                    break;
                }
            }
            if (this._directory && !this._urlString.endsWith("/")) {
                this._urlString += "/";
                try {
                    this._url = new URL(this._urlString);
                } catch (MalformedURLException ex) {
                    LOG.warn(ex);
                }
            }
        }
        if (!this._directory && this._entry == null) {
            z = false;
        }
        this._exists = z;
        return this._exists;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean isDirectory() {
        return this._urlString.endsWith("/") || (exists() && this._directory);
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public long lastModified() {
        if (checkConnection() && this._file != null) {
            if (exists() && this._entry != null) {
                return this._entry.getTime();
            }
            return this._file.lastModified();
        }
        return -1L;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public synchronized String[] list() {
        List<String> list;
        if (isDirectory() && this._list == null) {
            try {
                list = listEntries();
            } catch (Exception e) {
                Logger logger = LOG;
                logger.warn("Retrying list:" + e, new Object[0]);
                LOG.debug(e);
                release();
                list = listEntries();
            }
            if (list != null) {
                this._list = new String[list.size()];
                list.toArray(this._list);
            }
        }
        return this._list;
    }

    private List<String> listEntries() {
        checkConnection();
        ArrayList<String> list = new ArrayList<>(32);
        JarFile jarFile = this._jarFile;
        if (jarFile == null) {
            try {
                JarURLConnection jc = (JarURLConnection) new URL(this._jarUrl).openConnection();
                jc.setUseCaches(getUseCaches());
                jarFile = jc.getJarFile();
            } catch (Exception e) {
                e.printStackTrace();
                LOG.ignore(e);
            }
        }
        Enumeration<JarEntry> e2 = jarFile.entries();
        String dir = this._urlString.substring(this._urlString.indexOf("!/") + 2);
        while (e2.hasMoreElements()) {
            JarEntry entry = e2.nextElement();
            String name = entry.getName().replace('\\', '/');
            if (name.startsWith(dir) && name.length() != dir.length()) {
                String listName = name.substring(dir.length());
                int dash = listName.indexOf(47);
                if (dash >= 0) {
                    if (dash != 0 || listName.length() != 1) {
                        if (dash == 0) {
                            listName = listName.substring(dash + 1, listName.length());
                        } else {
                            listName = listName.substring(0, dash + 1);
                        }
                        if (list.contains(listName)) {
                        }
                    }
                }
                list.add(listName);
            }
        }
        return list;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public long length() {
        if (isDirectory() || this._entry == null) {
            return -1L;
        }
        return this._entry.getSize();
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public String encode(String uri) {
        return uri;
    }

    public static Resource getNonCachingResource(Resource resource) {
        if (!(resource instanceof JarFileResource)) {
            return resource;
        }
        JarFileResource oldResource = (JarFileResource) resource;
        JarFileResource newResource = new JarFileResource(oldResource.getURL(), false);
        return newResource;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean isContainedIn(Resource resource) throws MalformedURLException {
        String string = this._urlString;
        int index = string.indexOf("!/");
        if (index > 0) {
            string = string.substring(0, index);
        }
        if (string.startsWith("jar:")) {
            string = string.substring(4);
        }
        URL url = new URL(string);
        return url.sameFile(resource.getURL());
    }
}
