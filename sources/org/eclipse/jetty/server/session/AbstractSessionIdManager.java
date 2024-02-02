package org.eclipse.jetty.server.session;

import java.security.SecureRandom;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class AbstractSessionIdManager extends AbstractLifeCycle implements SessionIdManager {
    private static final Logger LOG = Log.getLogger(AbstractSessionIdManager.class);
    private static final String __NEW_SESSION_ID = "org.eclipse.jetty.server.newSessionId";
    protected Random _random;
    protected long _reseed = 100000;
    protected boolean _weakRandom;
    protected String _workerName;

    public AbstractSessionIdManager() {
    }

    public AbstractSessionIdManager(Random random) {
        this._random = random;
    }

    public long getReseed() {
        return this._reseed;
    }

    public void setReseed(long reseed) {
        this._reseed = reseed;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public String getWorkerName() {
        return this._workerName;
    }

    public void setWorkerName(String workerName) {
        if (workerName.contains(".")) {
            throw new IllegalArgumentException("Name cannot contain '.'");
        }
        this._workerName = workerName;
    }

    public Random getRandom() {
        return this._random;
    }

    public synchronized void setRandom(Random random) {
        this._random = random;
        this._weakRandom = false;
    }

    @Override // org.eclipse.jetty.server.SessionIdManager
    public String newSessionId(HttpServletRequest request, long created) {
        synchronized (this) {
            if (request != null) {
                try {
                    String requested_id = request.getRequestedSessionId();
                    if (requested_id != null) {
                        String cluster_id = getClusterId(requested_id);
                        if (idInUse(cluster_id)) {
                            return cluster_id;
                        }
                    }
                    String new_id = (String) request.getAttribute(__NEW_SESSION_ID);
                    if (new_id != null && idInUse(new_id)) {
                        return new_id;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            String id = null;
            while (true) {
                if (id != null && id.length() != 0 && !idInUse(id)) {
                    request.setAttribute(__NEW_SESSION_ID, id);
                    return id;
                }
                long r0 = this._weakRandom ? ((hashCode() ^ Runtime.getRuntime().freeMemory()) ^ this._random.nextInt()) ^ (request.hashCode() << 32) : this._random.nextLong();
                if (r0 < 0) {
                    r0 = -r0;
                }
                if (this._reseed > 0 && r0 % this._reseed == 1) {
                    LOG.debug("Reseeding {}", this);
                    if (this._random instanceof SecureRandom) {
                        SecureRandom secure = (SecureRandom) this._random;
                        secure.setSeed(secure.generateSeed(8));
                    } else {
                        this._random.setSeed(((this._random.nextLong() ^ System.currentTimeMillis()) ^ request.hashCode()) ^ Runtime.getRuntime().freeMemory());
                    }
                }
                long r1 = this._weakRandom ? (request.hashCode() << 32) ^ ((hashCode() ^ Runtime.getRuntime().freeMemory()) ^ this._random.nextInt()) : this._random.nextLong();
                if (r1 < 0) {
                    r1 = -r1;
                }
                id = Long.toString(r0, 36) + Long.toString(r1, 36);
                if (this._workerName != null) {
                    id = this._workerName + id;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        initRandom();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
    }

    public void initRandom() {
        if (this._random == null) {
            try {
                this._random = new SecureRandom();
                return;
            } catch (Exception e) {
                LOG.warn("Could not generate SecureRandom for session-id randomness", e);
                this._random = new Random();
                this._weakRandom = true;
                return;
            }
        }
        this._random.setSeed(((this._random.nextLong() ^ System.currentTimeMillis()) ^ hashCode()) ^ Runtime.getRuntime().freeMemory());
    }
}
