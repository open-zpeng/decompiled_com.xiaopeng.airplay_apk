package org.eclipse.jetty.client;

import java.net.InetSocketAddress;
/* loaded from: classes.dex */
public class Address {
    private final String host;
    private final int port;

    public static Address from(String hostAndPort) {
        String host;
        int colon = hostAndPort.indexOf(58);
        int port = 0;
        if (colon >= 0) {
            String host2 = hostAndPort.substring(0, colon);
            int port2 = Integer.parseInt(hostAndPort.substring(colon + 1));
            host = host2;
            port = port2;
        } else {
            host = hostAndPort;
        }
        return new Address(host, port);
    }

    public Address(String host, int port) {
        if (host == null) {
            throw new IllegalArgumentException("Host is null");
        }
        this.host = host.trim();
        this.port = port;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Address that = (Address) obj;
        if (this.host.equals(that.host) && this.port == that.port) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.host.hashCode();
        return (31 * result) + this.port;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public InetSocketAddress toSocketAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    public String toString() {
        return this.host + ":" + this.port;
    }
}
