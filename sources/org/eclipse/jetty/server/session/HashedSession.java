package org.eclipse.jetty.server.session;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class HashedSession extends AbstractSession {
    private static final Logger LOG = Log.getLogger(HashedSession.class);
    private final HashSessionManager _hashSessionManager;
    private transient boolean _idled;
    private transient boolean _saveFailed;

    /* JADX INFO: Access modifiers changed from: protected */
    public HashedSession(HashSessionManager hashSessionManager, HttpServletRequest request) {
        super(hashSessionManager, request);
        this._idled = false;
        this._saveFailed = false;
        this._hashSessionManager = hashSessionManager;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public HashedSession(HashSessionManager hashSessionManager, long created, long accessed, String clusterId) {
        super(hashSessionManager, created, accessed, clusterId);
        this._idled = false;
        this._saveFailed = false;
        this._hashSessionManager = hashSessionManager;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.session.AbstractSession
    public void checkValid() {
        if (this._hashSessionManager._idleSavePeriodMs != 0) {
            deIdle();
        }
        super.checkValid();
    }

    @Override // org.eclipse.jetty.server.session.AbstractSession, javax.servlet.http.HttpSession
    public void setMaxInactiveInterval(int secs) {
        super.setMaxInactiveInterval(secs);
        if (getMaxInactiveInterval() > 0 && (getMaxInactiveInterval() * 1000) / 10 < this._hashSessionManager._scavengePeriodMs) {
            this._hashSessionManager.setScavengePeriod((secs + 9) / 10);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.session.AbstractSession
    public void doInvalidate() throws IllegalStateException {
        super.doInvalidate();
        if (this._hashSessionManager._storeDir != null && getId() != null) {
            String id = getId();
            File f = new File(this._hashSessionManager._storeDir, id);
            f.delete();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void save(boolean reactivate) throws Exception {
        if (!isIdled() && !this._saveFailed) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Saving {} {}", super.getId(), Boolean.valueOf(reactivate));
            }
            File file = null;
            try {
                File file2 = new File(this._hashSessionManager._storeDir, super.getId());
                if (file2.exists()) {
                    file2.delete();
                }
                file2.createNewFile();
                FileOutputStream fos = new FileOutputStream(file2);
                willPassivate();
                save(fos);
                IO.close((OutputStream) fos);
                if (reactivate) {
                    didActivate();
                } else {
                    clearAttributes();
                }
            } catch (Exception e) {
                saveFailed();
                if (0 != 0) {
                    IO.close((OutputStream) null);
                }
                if (0 != 0) {
                    file.delete();
                }
                throw e;
            }
        }
    }

    public synchronized void save(OutputStream os) throws IOException {
        DataOutputStream out = new DataOutputStream(os);
        out.writeUTF(getClusterId());
        out.writeUTF(getNodeId());
        out.writeLong(getCreationTime());
        out.writeLong(getAccessed());
        out.writeInt(getRequests());
        out.writeInt(getAttributes());
        ObjectOutputStream oos = new ObjectOutputStream(out);
        Enumeration<String> e = getAttributeNames();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            oos.writeUTF(key);
            oos.writeObject(doGet(key));
        }
        oos.close();
    }

    public synchronized void deIdle() {
        if (isIdled()) {
            access(System.currentTimeMillis());
            if (LOG.isDebugEnabled()) {
                Logger logger = LOG;
                logger.debug("De-idling " + super.getId(), new Object[0]);
            }
            try {
                File file = new File(this._hashSessionManager._storeDir, super.getId());
                if (!file.exists() || !file.canRead()) {
                    throw new FileNotFoundException(file.getName());
                }
                FileInputStream fis = new FileInputStream(file);
                this._idled = false;
                this._hashSessionManager.restoreSession(fis, this);
                IO.close((InputStream) fis);
                didActivate();
                if (this._hashSessionManager._savePeriodMs == 0) {
                    file.delete();
                }
            } catch (Exception e) {
                Logger logger2 = LOG;
                logger2.warn("Problem de-idling session " + super.getId(), e);
                if (0 != 0) {
                    IO.close((InputStream) null);
                }
                invalidate();
            }
        }
    }

    public synchronized void idle() throws Exception {
        save(false);
        this._idled = true;
    }

    public synchronized boolean isIdled() {
        return this._idled;
    }

    public synchronized boolean isSaveFailed() {
        return this._saveFailed;
    }

    public synchronized void saveFailed() {
        this._saveFailed = true;
    }
}
