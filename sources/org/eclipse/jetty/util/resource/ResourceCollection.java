package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
/* loaded from: classes.dex */
public class ResourceCollection extends Resource {
    private Resource[] _resources;

    public ResourceCollection() {
        this._resources = new Resource[0];
    }

    public ResourceCollection(Resource... resources) {
        List<Resource> list = new ArrayList<>();
        for (Resource r : resources) {
            if (r != null) {
                if (r instanceof ResourceCollection) {
                    Resource[] arr$ = ((ResourceCollection) r).getResources();
                    for (Resource r2 : arr$) {
                        list.add(r2);
                    }
                } else {
                    list.add(r);
                }
            }
        }
        this._resources = (Resource[]) list.toArray(new Resource[list.size()]);
        Resource[] arr$2 = this._resources;
        for (Resource r3 : arr$2) {
            if (!r3.exists() || !r3.isDirectory()) {
                throw new IllegalArgumentException(r3 + " is not an existing directory.");
            }
        }
    }

    public ResourceCollection(String[] resources) {
        this._resources = new Resource[resources.length];
        for (int i = 0; i < resources.length; i++) {
            try {
                this._resources[i] = Resource.newResource(resources[i]);
                if (!this._resources[i].exists() || !this._resources[i].isDirectory()) {
                    throw new IllegalArgumentException(this._resources[i] + " is not an existing directory.");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public ResourceCollection(String csvResources) {
        setResourcesAsCSV(csvResources);
    }

    public Resource[] getResources() {
        return this._resources;
    }

    public void setResources(Resource[] resources) {
        this._resources = resources != null ? resources : new Resource[0];
    }

    public void setResourcesAsCSV(String csvResources) {
        StringTokenizer tokenizer = new StringTokenizer(csvResources, ",;");
        int len = tokenizer.countTokens();
        if (len == 0) {
            throw new IllegalArgumentException("ResourceCollection@setResourcesAsCSV(String)  argument must be a string containing one or more comma-separated resource strings.");
        }
        this._resources = new Resource[len];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            try {
                this._resources[i] = Resource.newResource(tokenizer.nextToken().trim());
                if (this._resources[i].exists() && this._resources[i].isDirectory()) {
                    i++;
                } else {
                    throw new IllegalArgumentException(this._resources[i] + " is not an existing directory.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public Resource addPath(String path) throws IOException, MalformedURLException {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        if (path == null) {
            throw new MalformedURLException();
        }
        if (path.length() == 0 || "/".equals(path)) {
            return this;
        }
        Resource resource = null;
        ArrayList<Resource> resources = null;
        int i = 0;
        while (true) {
            if (i >= this._resources.length) {
                break;
            }
            resource = this._resources[i].addPath(path);
            if (!resource.exists()) {
                i++;
            } else if (!resource.isDirectory()) {
                return resource;
            }
        }
        while (true) {
            i++;
            if (i >= this._resources.length) {
                break;
            }
            Resource r = this._resources[i].addPath(path);
            if (r.exists() && r.isDirectory()) {
                if (resource != null) {
                    resources = new ArrayList<>();
                    resources.add(resource);
                    resource = null;
                }
                resources.add(r);
            }
        }
        if (resource != null) {
            return resource;
        }
        if (resources != null) {
            return new ResourceCollection((Resource[]) resources.toArray(new Resource[resources.size()]));
        }
        return null;
    }

    protected Object findResource(String path) throws IOException, MalformedURLException {
        Resource resource = null;
        ArrayList<Resource> resources = null;
        int i = 0;
        while (true) {
            if (i >= this._resources.length) {
                break;
            }
            resource = this._resources[i].addPath(path);
            if (!resource.exists()) {
                i++;
            } else if (!resource.isDirectory()) {
                return resource;
            }
        }
        while (true) {
            i++;
            if (i >= this._resources.length) {
                break;
            }
            Resource r = this._resources[i].addPath(path);
            if (r.exists() && r.isDirectory()) {
                if (resource != null) {
                    resources = new ArrayList<>();
                    resources.add(resource);
                }
                resources.add(r);
            }
        }
        if (resource != null) {
            return resource;
        }
        if (resources != null) {
            return resources;
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public boolean delete() throws SecurityException {
        throw new UnsupportedOperationException();
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public boolean exists() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        return true;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public File getFile() throws IOException {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            File f = r.getFile();
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public InputStream getInputStream() throws IOException {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            InputStream is = r.getInputStream();
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public String getName() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            String name = r.getName();
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public OutputStream getOutputStream() throws IOException, SecurityException {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            OutputStream os = r.getOutputStream();
            if (os != null) {
                return os;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public URL getURL() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            URL url = r.getURL();
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public boolean isDirectory() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        return true;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public long lastModified() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            long lm = r.lastModified();
            if (lm != -1) {
                return lm;
            }
        }
        return -1L;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public long length() {
        return -1L;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public String[] list() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        HashSet<String> set = new HashSet<>();
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            String[] arr$2 = r.list();
            for (String s : arr$2) {
                set.add(s);
            }
        }
        String[] result = (String[]) set.toArray(new String[set.size()]);
        Arrays.sort(result);
        return result;
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public void release() {
        if (this._resources == null) {
            throw new IllegalStateException("*resources* not set.");
        }
        Resource[] arr$ = this._resources;
        for (Resource r : arr$) {
            r.release();
        }
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public boolean renameTo(Resource dest) throws SecurityException {
        throw new UnsupportedOperationException();
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public void copyTo(File destination) throws IOException {
        int r = this._resources.length;
        while (true) {
            int r2 = r - 1;
            if (r > 0) {
                this._resources[r2].copyTo(destination);
                r = r2;
            } else {
                return;
            }
        }
    }

    public String toString() {
        if (this._resources == null) {
            return "[]";
        }
        return String.valueOf(Arrays.asList(this._resources));
    }

    @Override // org.eclipse.jetty.util.resource.Resource
    public boolean isContainedIn(Resource r) throws MalformedURLException {
        return false;
    }
}
