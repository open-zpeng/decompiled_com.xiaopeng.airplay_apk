package org.fourthline.cling.model.message;

import java.net.InetAddress;
import org.fourthline.cling.model.message.UpnpOperation;
/* loaded from: classes.dex */
public class IncomingDatagramMessage<O extends UpnpOperation> extends UpnpMessage<O> {
    private InetAddress localAddress;
    private InetAddress sourceAddress;
    private int sourcePort;

    public IncomingDatagramMessage(O operation, InetAddress sourceAddress, int sourcePort, InetAddress localAddress) {
        super(operation);
        this.sourceAddress = sourceAddress;
        this.sourcePort = sourcePort;
        this.localAddress = localAddress;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public IncomingDatagramMessage(IncomingDatagramMessage<O> source) {
        super(source);
        this.sourceAddress = source.getSourceAddress();
        this.sourcePort = source.getSourcePort();
        this.localAddress = source.getLocalAddress();
    }

    public InetAddress getSourceAddress() {
        return this.sourceAddress;
    }

    public void setSourceAddress(InetAddress ia) {
        this.sourceAddress = ia;
    }

    public int getSourcePort() {
        return this.sourcePort;
    }

    public void setSourcePort(int port) {
        this.sourcePort = port;
    }

    public InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalAddress(InetAddress ia) {
        this.localAddress = ia;
    }
}
