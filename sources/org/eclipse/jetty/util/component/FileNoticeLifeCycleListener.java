package org.eclipse.jetty.util.component;

import java.io.FileWriter;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class FileNoticeLifeCycleListener implements LifeCycle.Listener {
    Logger LOG = Log.getLogger(FileNoticeLifeCycleListener.class);
    private final String _filename;

    public FileNoticeLifeCycleListener(String filename) {
        this._filename = filename;
    }

    private void writeState(String action, LifeCycle lifecycle) {
        try {
            FileWriter out = new FileWriter(this._filename, true);
            out.append((CharSequence) action).append((CharSequence) " ").append((CharSequence) lifecycle.toString()).append((CharSequence) "\n");
            out.close();
        } catch (Exception e) {
            this.LOG.warn(e);
        }
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
    public void lifeCycleStarting(LifeCycle event) {
        writeState(AbstractLifeCycle.STARTING, event);
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
    public void lifeCycleStarted(LifeCycle event) {
        writeState(AbstractLifeCycle.STARTED, event);
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        writeState(AbstractLifeCycle.FAILED, event);
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
    public void lifeCycleStopping(LifeCycle event) {
        writeState(AbstractLifeCycle.STOPPING, event);
    }

    @Override // org.eclipse.jetty.util.component.LifeCycle.Listener
    public void lifeCycleStopped(LifeCycle event) {
        writeState(AbstractLifeCycle.STOPPED, event);
    }
}
