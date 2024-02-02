package org.fourthline.cling.model.message;

import java.net.InetAddress;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpOperation;
/* loaded from: classes.dex */
public abstract class OutgoingDatagramMessage<O extends UpnpOperation> extends UpnpMessage<O> {
    private InetAddress destinationAddress;
    private int destinationPort;
    private UpnpHeaders headers;

    /* JADX INFO: Access modifiers changed from: protected */
    public OutgoingDatagramMessage(O operation, InetAddress destinationAddress, int destinationPort) {
        super(operation);
        this.headers = new UpnpHeaders(false);
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    protected OutgoingDatagramMessage(O operation, UpnpMessage.BodyType bodyType, Object body, InetAddress destinationAddress, int destinationPort) {
        super(operation, bodyType, body);
        this.headers = new UpnpHeaders(false);
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    public void setDestinationAddress(InetAddress ia) {
        this.destinationAddress = ia;
    }

    public InetAddress getDestinationAddress() {
        return this.destinationAddress;
    }

    public void setDestinationPort(int port) {
        this.destinationPort = port;
    }

    public int getDestinationPort() {
        return this.destinationPort;
    }

    @Override // org.fourthline.cling.model.message.UpnpMessage
    public UpnpHeaders getHeaders() {
        return this.headers;
    }
}
