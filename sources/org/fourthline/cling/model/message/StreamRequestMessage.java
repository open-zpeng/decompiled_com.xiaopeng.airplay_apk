package org.fourthline.cling.model.message;

import java.net.URI;
import java.net.URL;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpRequest;
/* loaded from: classes.dex */
public class StreamRequestMessage extends UpnpMessage<UpnpRequest> {
    protected Connection connection;

    public StreamRequestMessage(StreamRequestMessage source) {
        super(source);
        this.connection = source.getConnection();
    }

    public StreamRequestMessage(UpnpRequest operation) {
        super(operation);
    }

    public StreamRequestMessage(UpnpRequest.Method method, URI uri) {
        super(new UpnpRequest(method, uri));
    }

    public StreamRequestMessage(UpnpRequest.Method method, URL url) {
        super(new UpnpRequest(method, url));
    }

    public StreamRequestMessage(UpnpRequest operation, String body) {
        super(operation, UpnpMessage.BodyType.STRING, body);
    }

    public StreamRequestMessage(UpnpRequest.Method method, URI uri, String body) {
        super(new UpnpRequest(method, uri), UpnpMessage.BodyType.STRING, body);
    }

    public StreamRequestMessage(UpnpRequest.Method method, URL url, String body) {
        super(new UpnpRequest(method, url), UpnpMessage.BodyType.STRING, body);
    }

    public StreamRequestMessage(UpnpRequest operation, byte[] body) {
        super(operation, UpnpMessage.BodyType.BYTES, body);
    }

    public StreamRequestMessage(UpnpRequest.Method method, URI uri, byte[] body) {
        super(new UpnpRequest(method, uri), UpnpMessage.BodyType.BYTES, body);
    }

    public StreamRequestMessage(UpnpRequest.Method method, URL url, byte[] body) {
        super(new UpnpRequest(method, url), UpnpMessage.BodyType.BYTES, body);
    }

    public URI getUri() {
        return getOperation().getURI();
    }

    public void setUri(URI uri) {
        getOperation().setUri(uri);
    }

    public void setMethod(UpnpRequest.Method method) {
        getOperation().setMethod(method);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
