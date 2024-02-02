package org.eclipse.jetty.util.component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
/* loaded from: classes.dex */
public class FileDestroyable implements Destroyable {
    private static final Logger LOG = Log.getLogger(FileDestroyable.class);
    final List<File> _files = new ArrayList();

    public FileDestroyable() {
    }

    public FileDestroyable(String file) throws IOException {
        this._files.add(Resource.newResource(file).getFile());
    }

    public FileDestroyable(File file) {
        this._files.add(file);
    }

    public void addFile(String file) throws IOException {
        this._files.add(Resource.newResource(file).getFile());
    }

    public void addFile(File file) {
        this._files.add(file);
    }

    public void addFiles(Collection<File> files) {
        this._files.addAll(files);
    }

    public void removeFile(String file) throws IOException {
        this._files.remove(Resource.newResource(file).getFile());
    }

    public void removeFile(File file) {
        this._files.remove(file);
    }

    @Override // org.eclipse.jetty.util.component.Destroyable
    public void destroy() {
        for (File file : this._files) {
            if (file.exists()) {
                LOG.debug("Destroy {}", file);
                IO.delete(file);
            }
        }
    }
}
