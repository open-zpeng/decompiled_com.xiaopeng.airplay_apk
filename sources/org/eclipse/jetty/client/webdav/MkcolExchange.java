package org.eclipse.jetty.client.webdav;

import java.io.IOException;
import org.eclipse.jetty.client.CachedExchange;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class MkcolExchange extends CachedExchange {
    private static final Logger LOG = Log.getLogger(MkcolExchange.class);
    boolean exists;

    public MkcolExchange() {
        super(true);
        this.exists = false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.CachedExchange, org.eclipse.jetty.client.HttpExchange
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        if (status == 201) {
            LOG.debug("MkcolExchange:Status: Successfully created resource", new Object[0]);
            this.exists = true;
        }
        if (status == 405) {
            LOG.debug("MkcolExchange:Status: Resource must exist", new Object[0]);
            this.exists = true;
        }
        super.onResponseStatus(version, status, reason);
    }

    public boolean exists() {
        return this.exists;
    }
}
