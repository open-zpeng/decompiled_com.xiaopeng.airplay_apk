package org.eclipse.jetty.io;

import java.net.Socket;
/* loaded from: classes.dex */
public interface NetworkTrafficListener {
    void closed(Socket socket);

    void incoming(Socket socket, Buffer buffer);

    void opened(Socket socket);

    void outgoing(Socket socket, Buffer buffer);

    /* loaded from: classes.dex */
    public static class Empty implements NetworkTrafficListener {
        @Override // org.eclipse.jetty.io.NetworkTrafficListener
        public void opened(Socket socket) {
        }

        @Override // org.eclipse.jetty.io.NetworkTrafficListener
        public void incoming(Socket socket, Buffer bytes) {
        }

        @Override // org.eclipse.jetty.io.NetworkTrafficListener
        public void outgoing(Socket socket, Buffer bytes) {
        }

        @Override // org.eclipse.jetty.io.NetworkTrafficListener
        public void closed(Socket socket) {
        }
    }
}
