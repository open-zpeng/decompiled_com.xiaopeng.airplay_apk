package org.fourthline.cling.transport.impl;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.http.HttpVersions;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.seamless.util.Exceptions;
import org.seamless.util.io.IO;
/* loaded from: classes.dex */
public abstract class HttpExchangeUpnpStream extends UpnpStream {
    private static Logger log = Logger.getLogger(UpnpStream.class.getName());
    private HttpExchange httpExchange;

    protected abstract Connection createConnection();

    public HttpExchangeUpnpStream(ProtocolFactory protocolFactory, HttpExchange httpExchange) {
        super(protocolFactory);
        this.httpExchange = httpExchange;
    }

    public HttpExchange getHttpExchange() {
        return this.httpExchange;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            Logger logger = log;
            logger.fine("Processing HTTP request: " + getHttpExchange().getRequestMethod() + " " + getHttpExchange().getRequestURI());
            StreamRequestMessage requestMessage = new StreamRequestMessage(UpnpRequest.Method.getByHttpName(getHttpExchange().getRequestMethod()), getHttpExchange().getRequestURI());
            if (((UpnpRequest) requestMessage.getOperation()).getMethod().equals(UpnpRequest.Method.UNKNOWN)) {
                Logger logger2 = log;
                logger2.fine("Method not supported by UPnP stack: " + getHttpExchange().getRequestMethod());
                throw new RuntimeException("Method not supported: " + getHttpExchange().getRequestMethod());
            }
            ((UpnpRequest) requestMessage.getOperation()).setHttpMinorVersion(getHttpExchange().getProtocol().toUpperCase(Locale.ROOT).equals(HttpVersions.HTTP_1_1) ? 1 : 0);
            Logger logger3 = log;
            logger3.fine("Created new request message: " + requestMessage);
            requestMessage.setConnection(createConnection());
            requestMessage.setHeaders(new UpnpHeaders(getHttpExchange().getRequestHeaders()));
            InputStream is = getHttpExchange().getRequestBody();
            byte[] bodyBytes = IO.readBytes(is);
            if (is != null) {
                is.close();
            }
            Logger logger4 = log;
            logger4.fine("Reading request body bytes: " + bodyBytes.length);
            if (bodyBytes.length > 0 && requestMessage.isContentTypeMissingOrText()) {
                log.fine("Request contains textual entity body, converting then setting string on message");
                requestMessage.setBodyCharacters(bodyBytes);
            } else if (bodyBytes.length > 0) {
                log.fine("Request contains binary entity body, setting bytes on message");
                requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);
            } else {
                log.fine("Request did not contain entity body");
            }
            StreamResponseMessage responseMessage = process(requestMessage);
            if (responseMessage != null) {
                Logger logger5 = log;
                logger5.fine("Preparing HTTP response message: " + responseMessage);
                getHttpExchange().getResponseHeaders().putAll(responseMessage.getHeaders());
                byte[] responseBodyBytes = responseMessage.hasBody() ? responseMessage.getBodyBytes() : null;
                int contentLength = responseBodyBytes != null ? responseBodyBytes.length : -1;
                Logger logger6 = log;
                logger6.fine("Sending HTTP response message: " + responseMessage + " with content length: " + contentLength);
                getHttpExchange().sendResponseHeaders(responseMessage.getOperation().getStatusCode(), (long) contentLength);
                if (contentLength > 0) {
                    log.fine("Response message has body, writing bytes to stream...");
                    OutputStream os = getHttpExchange().getResponseBody();
                    IO.writeBytes(os, responseBodyBytes);
                    os.flush();
                    if (os != null) {
                        os.close();
                    }
                }
            } else {
                log.fine("Sending HTTP response status: 404");
                getHttpExchange().sendResponseHeaders(404, -1L);
            }
            responseSent(responseMessage);
        } catch (Throwable t) {
            Logger logger7 = log;
            logger7.fine("Exception occured during UPnP stream processing: " + t);
            if (log.isLoggable(Level.FINE)) {
                Logger logger8 = log;
                Level level = Level.FINE;
                logger8.log(level, "Cause: " + Exceptions.unwrap(t), Exceptions.unwrap(t));
            }
            try {
                this.httpExchange.sendResponseHeaders(500, -1L);
            } catch (IOException ex) {
                Logger logger9 = log;
                logger9.warning("Couldn't send error response: " + ex);
            }
            responseException(t);
        }
    }
}
