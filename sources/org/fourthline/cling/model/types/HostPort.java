package org.fourthline.cling.model.types;
/* loaded from: classes.dex */
public class HostPort {
    private String host;
    private int port;

    public HostPort() {
    }

    public HostPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostPort hostPort = (HostPort) o;
        if (this.port == hostPort.port && this.host.equals(hostPort.host)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.host.hashCode();
        return (31 * result) + this.port;
    }

    public String toString() {
        return this.host + ":" + this.port;
    }
}
