package org.eclipse.jetty.client.webdav;

import java.io.IOException;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class PropfindExchange extends HttpExchange {
    private static final Logger LOG = Log.getLogger(PropfindExchange.class);
    boolean _propertyExists = false;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.client.HttpExchange
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        if (status == 200) {
            LOG.debug("PropfindExchange:Status: Exists", new Object[0]);
            this._propertyExists = true;
        } else {
            LOG.debug("PropfindExchange:Status: Not Exists", new Object[0]);
        }
        super.onResponseStatus(version, status, reason);
    }

    public boolean exists() {
        return this._propertyExists;
    }
}
