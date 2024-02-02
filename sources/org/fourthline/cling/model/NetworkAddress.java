package org.fourthline.cling.model;

import java.net.InetAddress;
import java.util.Arrays;
/* loaded from: classes.dex */
public class NetworkAddress {
    protected InetAddress address;
    protected byte[] hardwareAddress;
    protected int port;

    public NetworkAddress(InetAddress address, int port) {
        this(address, port, null);
    }

    public NetworkAddress(InetAddress address, int port, byte[] hardwareAddress) {
        this.address = address;
        this.port = port;
        this.hardwareAddress = hardwareAddress;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public byte[] getHardwareAddress() {
        return this.hardwareAddress;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkAddress that = (NetworkAddress) o;
        if (this.port == that.port && this.address.equals(that.address) && Arrays.equals(this.hardwareAddress, that.hardwareAddress)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.address.hashCode();
        return (31 * ((31 * result) + this.port)) + (this.hardwareAddress != null ? Arrays.hashCode(this.hardwareAddress) : 0);
    }
}
