package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class JarResource extends URLResource {
    private static final Logger LOG = Log.getLogger(JarResource.class);
    protected JarURLConnection _jarConnection;

    /* JADX INFO: Access modifiers changed from: package-private */
    public JarResource(URL url) {
        super(url, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public JarResource(URL url, boolean useCaches) {
        super(url, null, useCaches);
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public synchronized void release() {
        this._jarConnection = null;
        super.release();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.resource.URLResource
    public synchronized boolean checkConnection() {
        super.checkConnection();
        try {
            if (this._jarConnection != this._connection) {
                newConnection();
            }
        } catch (IOException e) {
            LOG.ignore(e);
            this._jarConnection = null;
        }
        return this._jarConnection != null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void newConnection() throws IOException {
        this._jarConnection = (JarURLConnection) this._connection;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public boolean exists() {
        if (this._urlString.endsWith("!/")) {
            return checkConnection();
        }
        return super.exists();
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public File getFile() throws IOException {
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.URLResource, org.eclipse.jetty.util.resource.Resource
    public InputStream getInputStream() throws IOException {
        checkConnection();
        if (!this._urlString.endsWith("!/")) {
            return new FilterInputStream(super.getInputStream()) { // from class: org.eclipse.jetty.util.resource.JarResource.1
                @Override // java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
                public void close() throws IOException {
                    this.in = IO.getClosedStream();
                }
            };
        }
        URL url = new URL(this._urlString.substring(4, this._urlString.length() - 2));
        InputStream is = url.openStream();
        return is;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public void copyTo(File directory) throws IOException {
        Manifest manifest;
        boolean shouldExtract;
        FileOutputStream fout;
        int endOfJarUrl;
        if (!exists()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Extract " + this + " to " + directory, new Object[0]);
        }
        String urlString = getURL().toExternalForm().trim();
        int endOfJarUrl2 = urlString.indexOf("!/");
        int startOfJarUrl = endOfJarUrl2 >= 0 ? 4 : 0;
        if (endOfJarUrl2 < 0) {
            throw new IOException("Not a valid jar url: " + urlString);
        }
        URL jarFileURL = new URL(urlString.substring(startOfJarUrl, endOfJarUrl2));
        String subEntryName = endOfJarUrl2 + 2 < urlString.length() ? urlString.substring(endOfJarUrl2 + 2) : null;
        int i = 1;
        boolean subEntryIsDir = subEntryName != null && subEntryName.endsWith("/");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Extracting entry = " + subEntryName + " from jar " + jarFileURL, new Object[0]);
        }
        InputStream is = jarFileURL.openConnection().getInputStream();
        JarInputStream jin = new JarInputStream(is);
        while (true) {
            JarEntry entry = jin.getNextJarEntry();
            if (entry == null) {
                break;
            }
            String entryName = entry.getName();
            if (subEntryName != null && entryName.startsWith(subEntryName)) {
                if (!subEntryIsDir && subEntryName.length() + i == entryName.length() && entryName.endsWith("/")) {
                    subEntryIsDir = true;
                }
                if (subEntryIsDir) {
                    entryName = entryName.substring(subEntryName.length());
                    if (!entryName.equals("")) {
                        shouldExtract = true;
                    } else {
                        shouldExtract = false;
                    }
                } else {
                    shouldExtract = true;
                }
            } else if (subEntryName != null && !entryName.startsWith(subEntryName)) {
                shouldExtract = false;
            } else {
                shouldExtract = true;
            }
            if (!shouldExtract) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping entry: " + entryName, new Object[0]);
                }
            } else {
                String dotCheck = entryName.replace('\\', '/');
                if (URIUtil.canonicalPath(dotCheck) != null) {
                    File file = new File(directory, entryName);
                    if (entry.isDirectory()) {
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        endOfJarUrl = endOfJarUrl2;
                    } else {
                        File dir = new File(file.getParent());
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        try {
                            fout = new FileOutputStream(file);
                            try {
                                IO.copy(jin, fout);
                                IO.close((OutputStream) fout);
                                if (entry.getTime() >= 0) {
                                    endOfJarUrl = endOfJarUrl2;
                                    file.setLastModified(entry.getTime());
                                } else {
                                    endOfJarUrl = endOfJarUrl2;
                                }
                            } catch (Throwable th) {
                                th = th;
                                IO.close((OutputStream) fout);
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            fout = null;
                        }
                    }
                    endOfJarUrl2 = endOfJarUrl;
                    i = 1;
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid entry: " + entryName, new Object[0]);
                }
            }
            endOfJarUrl = endOfJarUrl2;
            endOfJarUrl2 = endOfJarUrl;
            i = 1;
        }
        if ((subEntryName == null || (subEntryName != null && subEntryName.equalsIgnoreCase("META-INF/MANIFEST.MF"))) && (manifest = jin.getManifest()) != null) {
            File metaInf = new File(directory, "META-INF");
            metaInf.mkdir();
            File f = new File(metaInf, "MANIFEST.MF");
            FileOutputStream fout2 = new FileOutputStream(f);
            manifest.write(fout2);
            fout2.close();
        }
        IO.close((InputStream) jin);
    }

    public static Resource newJarResource(Resource resource) throws IOException {
        if (resource instanceof JarResource) {
            return resource;
        }
        return Resource.newResource("jar:" + resource + "!/");
    }
}
