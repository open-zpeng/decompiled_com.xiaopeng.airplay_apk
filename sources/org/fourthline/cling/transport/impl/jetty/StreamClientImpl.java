package org.fourthline.cling.transport.impl.jetty;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.transport.spi.AbstractStreamClient;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.seamless.util.Exceptions;
import org.seamless.util.MimeType;
/* loaded from: classes.dex */
public class StreamClientImpl extends AbstractStreamClient<StreamClientConfigurationImpl, HttpContentExchange> {
    private static final Logger log = Logger.getLogger(StreamClient.class.getName());
    protected final HttpClient client;
    protected final StreamClientConfigurationImpl configuration;

    public StreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
        this.configuration = configuration;
        log.info("Starting Jetty HttpClient...");
        this.client = new HttpClient();
        this.client.setThreadPool(new ExecutorThreadPool(getConfiguration().getRequestExecutorService()) { // from class: org.fourthline.cling.transport.impl.jetty.StreamClientImpl.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // org.eclipse.jetty.util.thread.ExecutorThreadPool, org.eclipse.jetty.util.component.AbstractLifeCycle
            public void doStop() throws Exception {
            }
        });
        this.client.setTimeout((configuration.getTimeoutSeconds() + 5) * 1000);
        this.client.setConnectTimeout((configuration.getTimeoutSeconds() + 5) * 1000);
        this.client.setMaxRetries(configuration.getRequestRetryCount());
        try {
            this.client.start();
        } catch (Exception ex) {
            throw new InitializationException("Could not start Jetty HTTP client: " + ex, ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.StreamClient
    public StreamClientConfigurationImpl getConfiguration() {
        return this.configuration;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.transport.spi.AbstractStreamClient
    public HttpContentExchange createRequest(StreamRequestMessage requestMessage) {
        return new HttpContentExchange(getConfiguration(), this.client, requestMessage);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.transport.spi.AbstractStreamClient
    public Callable<StreamResponseMessage> createCallable(final StreamRequestMessage requestMessage, final HttpContentExchange exchange) {
        return new Callable<StreamResponseMessage>() { // from class: org.fourthline.cling.transport.impl.jetty.StreamClientImpl.2
            @Override // java.util.concurrent.Callable
            public StreamResponseMessage call() throws Exception {
                if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                    Logger logger = StreamClientImpl.log;
                    logger.fine("Sending HTTP request: " + requestMessage);
                }
                StreamClientImpl.this.client.send(exchange);
                int exchangeState = exchange.waitForDone();
                if (exchangeState == 7) {
                    try {
                        return exchange.createResponse();
                    } catch (Throwable t) {
                        Logger logger2 = StreamClientImpl.log;
                        Level level = Level.WARNING;
                        logger2.log(level, "Error reading response: " + requestMessage, Exceptions.unwrap(t));
                        return null;
                    }
                } else if (exchangeState == 11 || exchangeState == 9) {
                    return null;
                } else {
                    Logger logger3 = StreamClientImpl.log;
                    logger3.warning("Unhandled HTTP exchange status: " + exchangeState);
                    return null;
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.fourthline.cling.transport.spi.AbstractStreamClient
    public void abort(HttpContentExchange exchange) {
        exchange.cancel();
    }

    @Override // org.fourthline.cling.transport.spi.AbstractStreamClient
    protected boolean logExecutionException(Throwable t) {
        return false;
    }

    @Override // org.fourthline.cling.transport.spi.StreamClient
    public void stop() {
        try {
            this.client.stop();
        } catch (Exception ex) {
            Logger logger = log;
            logger.info("Error stopping HTTP client: " + ex);
        }
    }

    /* loaded from: classes.dex */
    public static class HttpContentExchange extends ContentExchange {
        protected final HttpClient client;
        protected final StreamClientConfigurationImpl configuration;
        protected Throwable exception;
        protected final StreamRequestMessage requestMessage;

        public HttpContentExchange(StreamClientConfigurationImpl configuration, HttpClient client, StreamRequestMessage requestMessage) {
            super(true);
            this.configuration = configuration;
            this.client = client;
            this.requestMessage = requestMessage;
            applyRequestURLMethod();
            applyRequestHeaders();
            applyRequestBody();
        }

        @Override // org.eclipse.jetty.client.HttpExchange
        protected void onConnectionFailed(Throwable t) {
            Logger logger = StreamClientImpl.log;
            Level level = Level.WARNING;
            logger.log(level, "HTTP connection failed: " + this.requestMessage, Exceptions.unwrap(t));
        }

        @Override // org.eclipse.jetty.client.HttpExchange
        protected void onException(Throwable t) {
            Logger logger = StreamClientImpl.log;
            Level level = Level.WARNING;
            logger.log(level, "HTTP request failed: " + this.requestMessage, Exceptions.unwrap(t));
        }

        public StreamClientConfigurationImpl getConfiguration() {
            return this.configuration;
        }

        public StreamRequestMessage getRequestMessage() {
            return this.requestMessage;
        }

        protected void applyRequestURLMethod() {
            UpnpRequest requestOperation = getRequestMessage().getOperation();
            if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                Logger logger = StreamClientImpl.log;
                logger.fine("Preparing HTTP request message with method '" + requestOperation.getHttpMethodName() + "': " + getRequestMessage());
            }
            setURL(requestOperation.getURI().toString());
            setMethod(requestOperation.getHttpMethodName());
        }

        protected void applyRequestHeaders() {
            UpnpHeaders headers = getRequestMessage().getHeaders();
            if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                Logger logger = StreamClientImpl.log;
                logger.fine("Writing headers on HttpContentExchange: " + headers.size());
            }
            if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
                setRequestHeader(UpnpHeader.Type.USER_AGENT.getHttpName(), getConfiguration().getUserAgentValue(getRequestMessage().getUdaMajorVersion(), getRequestMessage().getUdaMinorVersion()));
            }
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String v : entry.getValue()) {
                    String headerName = entry.getKey();
                    if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                        Logger logger2 = StreamClientImpl.log;
                        logger2.fine("Setting header '" + headerName + "': " + v);
                    }
                    addRequestHeader(headerName, v);
                }
            }
        }

        protected void applyRequestBody() {
            MimeType contentType;
            String charset;
            if (getRequestMessage().hasBody()) {
                if (getRequestMessage().getBodyType() == UpnpMessage.BodyType.STRING) {
                    if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                        Logger logger = StreamClientImpl.log;
                        logger.fine("Writing textual request body: " + getRequestMessage());
                    }
                    if (getRequestMessage().getContentTypeHeader() != null) {
                        contentType = getRequestMessage().getContentTypeHeader().getValue();
                    } else {
                        contentType = ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8;
                    }
                    if (getRequestMessage().getContentTypeCharset() != null) {
                        charset = getRequestMessage().getContentTypeCharset();
                    } else {
                        charset = StringUtil.__UTF8;
                    }
                    setRequestContentType(contentType.toString());
                    try {
                        ByteArrayBuffer buffer = new ByteArrayBuffer(getRequestMessage().getBodyString(), charset);
                        setRequestHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
                        setRequestContent(buffer);
                        return;
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException("Unsupported character encoding: " + charset, ex);
                    }
                }
                if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                    Logger logger2 = StreamClientImpl.log;
                    logger2.fine("Writing binary request body: " + getRequestMessage());
                }
                if (getRequestMessage().getContentTypeHeader() == null) {
                    throw new RuntimeException("Missing content type header in request message: " + this.requestMessage);
                }
                MimeType contentType2 = getRequestMessage().getContentTypeHeader().getValue();
                setRequestContentType(contentType2.toString());
                ByteArrayBuffer buffer2 = new ByteArrayBuffer(getRequestMessage().getBodyBytes());
                setRequestHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer2.length()));
                setRequestContent(buffer2);
            }
        }

        protected StreamResponseMessage createResponse() {
            UpnpResponse responseOperation = new UpnpResponse(getResponseStatus(), UpnpResponse.Status.getByStatusCode(getResponseStatus()).getStatusMsg());
            if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                Logger logger = StreamClientImpl.log;
                logger.fine("Received response: " + responseOperation);
            }
            StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);
            UpnpHeaders headers = new UpnpHeaders();
            HttpFields responseFields = getResponseFields();
            for (String name : responseFields.getFieldNamesCollection()) {
                for (String value : responseFields.getValuesCollection(name)) {
                    headers.add(name, value);
                }
            }
            responseMessage.setHeaders(headers);
            byte[] bytes = getResponseContentBytes();
            if (bytes != null && bytes.length > 0 && responseMessage.isContentTypeMissingOrText()) {
                if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                    StreamClientImpl.log.fine("Response contains textual entity body, converting then setting string on message");
                }
                try {
                    responseMessage.setBodyCharacters(bytes);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("Unsupported character encoding: " + ex, ex);
                }
            } else if (bytes == null || bytes.length <= 0) {
                if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                    StreamClientImpl.log.fine("Response did not contain entity body");
                }
            } else {
                if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                    StreamClientImpl.log.fine("Response contains binary entity body, setting bytes on message");
                }
                responseMessage.setBody(UpnpMessage.BodyType.BYTES, bytes);
            }
            if (StreamClientImpl.log.isLoggable(Level.FINE)) {
                Logger logger2 = StreamClientImpl.log;
                logger2.fine("Response message complete: " + responseMessage);
            }
            return responseMessage;
        }
    }
}
