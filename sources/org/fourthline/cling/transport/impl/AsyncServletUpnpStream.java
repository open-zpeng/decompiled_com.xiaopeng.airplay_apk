package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.seamless.util.io.IO;
/* loaded from: classes.dex */
public abstract class AsyncServletUpnpStream extends UpnpStream implements AsyncListener {
    private static final Logger log = Logger.getLogger(UpnpStream.class.getName());
    protected AsyncContext asyncContext;
    protected Connection connection;
    protected UpnpHeaders headers;
    protected HttpServletRequest request;
    protected StreamRequestMessage requestMessage;
    protected StreamResponseMessage responseMessage;

    protected abstract Connection createConnection();

    public AsyncServletUpnpStream(ProtocolFactory protocolFactory, AsyncContext asyncContext, HttpServletRequest request) {
        super(protocolFactory);
        this.asyncContext = asyncContext;
        this.request = request;
        asyncContext.addListener(this);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setAsyncContext(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    protected HttpServletResponse getResponse() {
        ServletResponse response = this.asyncContext.getResponse();
        if (response == null) {
            throw new IllegalStateException("Couldn't get response from asynchronous context, already timed out");
        }
        return (HttpServletResponse) response;
    }

    protected void complete() {
        try {
            responseSent(this.responseMessage);
            this.asyncContext.complete();
        } catch (IllegalStateException ex) {
            Logger logger = log;
            logger.info("Error calling servlet container's AsyncContext#complete() method: " + ex);
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            readRequestMessage();
            if (log.isLoggable(Level.FINER)) {
                Logger logger = log;
                logger.finer("Processing new request message: " + this.requestMessage);
            }
            XpDlnaRequestInfo xpDlnaRequestInfo = XpDlnaRequestInfo.getInstance();
            xpDlnaRequestInfo.setIp(getRequest().getRemoteAddr());
            this.responseMessage = process(this.requestMessage);
            if (this.responseMessage != null) {
                if (log.isLoggable(Level.FINER)) {
                    Logger logger2 = log;
                    logger2.finer("Preparing HTTP response message: " + this.responseMessage);
                }
                writeResponseMessage(this.responseMessage);
            } else {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("Sending HTTP response status: 404");
                }
                getResponse().setStatus(404);
            }
        } finally {
            try {
            } finally {
            }
        }
    }

    @Override // javax.servlet.AsyncListener
    public void onStartAsync(AsyncEvent event) throws IOException {
    }

    @Override // javax.servlet.AsyncListener
    public void onComplete(AsyncEvent event) throws IOException {
        if (log.isLoggable(Level.FINER)) {
            Logger logger = log;
            logger.finer("Completed asynchronous processing of HTTP request: " + event.getSuppliedRequest());
        }
        responseSent(this.responseMessage);
    }

    @Override // javax.servlet.AsyncListener
    public void onTimeout(AsyncEvent event) throws IOException {
        Logger logger = log;
        logger.severe("Asynchronous processing of HTTP request timed out: " + event.getSuppliedRequest());
        responseException(new Exception("Asynchronous request timed out"));
    }

    @Override // javax.servlet.AsyncListener
    public void onError(AsyncEvent event) throws IOException {
        Logger logger = log;
        logger.severe("Asynchronous processing of HTTP request error: " + event.getThrowable());
        responseException(event.getThrowable());
    }

    protected void readRequestMessage() throws IOException {
        String requestMethod = getRequest().getMethod();
        String requestURI = getRequest().getRequestURI();
        if (log.isLoggable(Level.FINER)) {
            Logger logger = log;
            logger.finer("Processing HTTP request: " + requestMethod + " " + requestURI);
        }
        try {
            if (this.requestMessage == null) {
                this.requestMessage = new StreamRequestMessage(UpnpRequest.Method.getByHttpName(requestMethod), URI.create(requestURI));
            } else {
                this.requestMessage.setMethod(UpnpRequest.Method.getByHttpName(requestMethod));
                this.requestMessage.setUri(URI.create(requestURI));
            }
            if (this.requestMessage.getOperation().getMethod().equals(UpnpRequest.Method.UNKNOWN)) {
                throw new RuntimeException("Method not supported: " + requestMethod);
            }
            this.requestMessage.setConnection(createConnection());
            if (this.headers == null) {
                this.headers = new UpnpHeaders();
            } else {
                this.headers.clear();
            }
            Enumeration<String> headerNames = getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValues = getRequest().getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    String headerValue = headerValues.nextElement();
                    this.headers.add(headerName, headerValue);
                }
            }
            this.requestMessage.setHeaders(this.headers);
            InputStream is = null;
            try {
                is = getRequest().getInputStream();
                byte[] bodyBytes = IO.readBytes(is);
                if (log.isLoggable(Level.FINER)) {
                    Logger logger2 = log;
                    logger2.finer("Reading request body bytes: " + bodyBytes.length);
                }
                if (bodyBytes.length > 0 && this.requestMessage.isContentTypeMissingOrText()) {
                    if (log.isLoggable(Level.FINER)) {
                        log.finer("Request contains textual entity body, converting then setting string on message");
                    }
                    this.requestMessage.setBodyCharacters(bodyBytes);
                } else if (bodyBytes.length > 0) {
                    if (log.isLoggable(Level.FINER)) {
                        log.finer("Request contains binary entity body, setting bytes on message");
                    }
                    this.requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);
                } else if (log.isLoggable(Level.FINER)) {
                    log.finer("Request did not contain entity body");
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid request URI: " + requestURI, ex);
        }
    }

    protected void writeResponseMessage(StreamResponseMessage responseMessage) throws IOException {
        if (log.isLoggable(Level.FINER)) {
            Logger logger = log;
            logger.finer("Sending HTTP response status: " + responseMessage.getOperation().getStatusCode());
        }
        getResponse().setStatus(responseMessage.getOperation().getStatusCode());
        for (Map.Entry<String, List<String>> entry : responseMessage.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                getResponse().addHeader(entry.getKey(), value);
            }
        }
        getResponse().setDateHeader(HttpHeaders.DATE, System.currentTimeMillis());
        byte[] responseBodyBytes = responseMessage.hasBody() ? responseMessage.getBodyBytes() : null;
        int contentLength = responseBodyBytes != null ? responseBodyBytes.length : -1;
        if (contentLength > 0) {
            getResponse().setContentLength(contentLength);
            log.finer("Response message has body, writing bytes to stream...");
            IO.writeBytes(getResponse().getOutputStream(), responseBodyBytes);
        }
    }
}
