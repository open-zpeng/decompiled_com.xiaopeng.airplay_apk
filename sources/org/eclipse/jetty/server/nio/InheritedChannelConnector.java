package org.eclipse.jetty.server.nio;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class InheritedChannelConnector extends SelectChannelConnector {
    private static final Logger LOG = Log.getLogger(InheritedChannelConnector.class);

    @Override // org.eclipse.jetty.server.nio.SelectChannelConnector, org.eclipse.jetty.server.Connector
    public void open() throws IOException {
        synchronized (this) {
            try {
                Channel channel = System.inheritedChannel();
                if (channel instanceof ServerSocketChannel) {
                    this._acceptChannel = (ServerSocketChannel) channel;
                } else {
                    Logger logger = LOG;
                    logger.warn("Unable to use System.inheritedChannel() [" + channel + "]. Trying a new ServerSocketChannel at " + getHost() + ":" + getPort(), new Object[0]);
                }
                if (this._acceptChannel != null) {
                    this._acceptChannel.configureBlocking(true);
                }
            } catch (NoSuchMethodError e) {
                LOG.warn("Need at least Java 5 to use socket inherited from xinetd/inetd.", new Object[0]);
            }
            if (this._acceptChannel == null) {
                super.open();
            }
        }
    }
}
