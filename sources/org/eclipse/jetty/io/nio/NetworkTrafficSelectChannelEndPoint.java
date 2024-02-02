package org.eclipse.jetty.io.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.io.nio.SelectorManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class NetworkTrafficSelectChannelEndPoint extends SelectChannelEndPoint {
    private static final Logger LOG = Log.getLogger(NetworkTrafficSelectChannelEndPoint.class);
    private final List<NetworkTrafficListener> listeners;

    public NetworkTrafficSelectChannelEndPoint(SocketChannel channel, SelectorManager.SelectSet selectSet, SelectionKey key, int maxIdleTime, List<NetworkTrafficListener> listeners) throws IOException {
        super(channel, selectSet, key, maxIdleTime);
        this.listeners = listeners;
    }

    @Override // org.eclipse.jetty.io.nio.SelectChannelEndPoint, org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public int fill(Buffer buffer) throws IOException {
        int read = super.fill(buffer);
        notifyIncoming(buffer, read);
        return read;
    }

    @Override // org.eclipse.jetty.io.nio.SelectChannelEndPoint, org.eclipse.jetty.io.nio.ChannelEndPoint, org.eclipse.jetty.io.EndPoint
    public int flush(Buffer buffer) throws IOException {
        int position = buffer.getIndex();
        int written = super.flush(buffer);
        notifyOutgoing(buffer, position, written);
        return written;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.io.nio.ChannelEndPoint
    public int gatheringFlush(Buffer header, ByteBuffer bbuf0, Buffer buffer, ByteBuffer bbuf1) throws IOException {
        int headerPosition = header.getIndex();
        int headerLength = header.length();
        int bufferPosition = buffer.getIndex();
        int written = super.gatheringFlush(header, bbuf0, buffer, bbuf1);
        notifyOutgoing(header, headerPosition, written > headerLength ? headerLength : written);
        notifyOutgoing(buffer, bufferPosition, written > headerLength ? written - headerLength : 0);
        return written;
    }

    public void notifyOpened() {
        if (this.listeners != null && !this.listeners.isEmpty()) {
            for (NetworkTrafficListener listener : this.listeners) {
                try {
                    listener.opened(this._socket);
                } catch (Exception x) {
                    LOG.warn(x);
                }
            }
        }
    }

    public void notifyIncoming(Buffer buffer, int read) {
        if (this.listeners != null && !this.listeners.isEmpty() && read > 0) {
            for (NetworkTrafficListener listener : this.listeners) {
                try {
                    Buffer view = buffer.asReadOnlyBuffer();
                    listener.incoming(this._socket, view);
                } catch (Exception x) {
                    LOG.warn(x);
                }
            }
        }
    }

    public void notifyOutgoing(Buffer buffer, int position, int written) {
        if (this.listeners != null && !this.listeners.isEmpty() && written > 0) {
            for (NetworkTrafficListener listener : this.listeners) {
                try {
                    Buffer view = buffer.asReadOnlyBuffer();
                    view.setGetIndex(position);
                    view.setPutIndex(position + written);
                    listener.outgoing(this._socket, view);
                } catch (Exception x) {
                    LOG.warn(x);
                }
            }
        }
    }

    public void notifyClosed() {
        if (this.listeners != null && !this.listeners.isEmpty()) {
            for (NetworkTrafficListener listener : this.listeners) {
                try {
                    listener.closed(this._socket);
                } catch (Exception x) {
                    LOG.warn(x);
                }
            }
        }
    }
}
