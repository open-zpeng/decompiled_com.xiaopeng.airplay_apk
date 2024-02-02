package org.eclipse.jetty.server.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.io.nio.NetworkTrafficSelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SelectorManager;
/* loaded from: classes.dex */
public class NetworkTrafficSelectChannelConnector extends SelectChannelConnector {
    private final List<NetworkTrafficListener> listeners = new CopyOnWriteArrayList();

    public void addNetworkTrafficListener(NetworkTrafficListener listener) {
        this.listeners.add(listener);
    }

    public void removeNetworkTrafficListener(NetworkTrafficListener listener) {
        this.listeners.remove(listener);
    }

    @Override // org.eclipse.jetty.server.nio.SelectChannelConnector
    protected SelectChannelEndPoint newEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key) throws IOException {
        NetworkTrafficSelectChannelEndPoint endPoint = new NetworkTrafficSelectChannelEndPoint(channel, selectSet, key, this._maxIdleTime, this.listeners);
        endPoint.setConnection(selectSet.getManager().newConnection(channel, endPoint, key.attachment()));
        endPoint.notifyOpened();
        return endPoint;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.nio.SelectChannelConnector
    public void endPointClosed(SelectChannelEndPoint endpoint) {
        super.endPointClosed(endpoint);
        ((NetworkTrafficSelectChannelEndPoint) endpoint).notifyClosed();
    }
}
