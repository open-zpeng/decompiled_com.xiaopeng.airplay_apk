package org.fourthline.cling.transport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.seamless.http.Headers;
import org.seamless.util.Exceptions;
import org.seamless.util.URIUtil;
import org.seamless.util.io.IO;
/* loaded from: classes.dex */
public class StreamClientImpl implements StreamClient {
    static final String HACK_STREAM_HANDLER_SYSTEM_PROPERTY = "hackStreamHandlerProperty";
    private static final Logger log = Logger.getLogger(StreamClient.class.getName());
    private final String TAG = "StreamClientImpl";
    protected final StreamClientConfigurationImpl configuration;

    public StreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
        this.configuration = configuration;
        if (ModelUtil.ANDROID_EMULATOR || ModelUtil.ANDROID_RUNTIME) {
            throw new InitializationException("This client does not work on Android. The design of HttpURLConnection is broken, we can not add additional 'permitted' HTTP methods. Read the Cling manual.");
        }
        Logger logger = log;
        logger.fine("Using persistent HTTP stream client connections: " + configuration.isUsePersistentConnections());
        System.setProperty("http.keepAlive", Boolean.toString(configuration.isUsePersistentConnections()));
        if (System.getProperty(HACK_STREAM_HANDLER_SYSTEM_PROPERTY) == null) {
            log.fine("Setting custom static URLStreamHandlerFactory to work around bad JDK defaults");
            try {
                URL.setURLStreamHandlerFactory((URLStreamHandlerFactory) Class.forName("org.fourthline.cling.transport.impl.FixedSunURLStreamHandler").newInstance());
                System.setProperty(HACK_STREAM_HANDLER_SYSTEM_PROPERTY, "alreadyWorkedAroundTheEvilJDK");
            } catch (Throwable th) {
                throw new InitializationException("Failed to set modified URLStreamHandlerFactory in this environment. Can't use bundled default client based on HTTPURLConnection, see manual.");
            }
        }
    }

    @Override // org.fourthline.cling.transport.spi.StreamClient
    public StreamClientConfigurationImpl getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.transport.spi.StreamClient
    public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage) {
        UpnpRequest requestOperation = requestMessage.getOperation();
        Logger logger = log;
        logger.fine("Preparing HTTP request message with method '" + requestOperation.getHttpMethodName() + "': " + requestMessage);
        URL url = URIUtil.toURL(requestOperation.getURI());
        HttpURLConnection urlConnection = null;
        try {
            try {
                try {
                    try {
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod(requestOperation.getHttpMethodName());
                        urlConnection.setReadTimeout(this.configuration.getTimeoutSeconds() * 1000);
                        urlConnection.setConnectTimeout(this.configuration.getTimeoutSeconds() * 1000);
                        applyRequestProperties(urlConnection, requestMessage);
                        applyRequestBody(urlConnection, requestMessage);
                        Logger logger2 = log;
                        logger2.fine("Sending HTTP request: " + requestMessage);
                        InputStream inputStream = urlConnection.getInputStream();
                        StreamResponseMessage createResponse = createResponse(urlConnection, inputStream);
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        return createResponse;
                    } catch (IOException ex) {
                        if (urlConnection == null) {
                            Logger logger3 = log;
                            Level level = Level.WARNING;
                            logger3.log(level, "HTTP request failed: " + requestMessage, Exceptions.unwrap(ex));
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            return null;
                        } else if (ex instanceof SocketTimeoutException) {
                            Logger logger4 = log;
                            logger4.info("Timeout of " + getConfiguration().getTimeoutSeconds() + " seconds while waiting for HTTP request to complete, aborting: " + requestMessage);
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            return null;
                        } else {
                            if (log.isLoggable(Level.FINE)) {
                                Logger logger5 = log;
                                logger5.fine("Exception occurred, trying to read the error stream: " + Exceptions.unwrap(ex));
                            }
                            try {
                                InputStream inputStream2 = urlConnection.getErrorStream();
                                StreamResponseMessage createResponse2 = createResponse(urlConnection, inputStream2);
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                return createResponse2;
                            } catch (Exception errorEx) {
                                if (log.isLoggable(Level.FINE)) {
                                    Logger logger6 = log;
                                    logger6.fine("Could not read error stream: " + errorEx);
                                }
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                                return null;
                            }
                        }
                    }
                } catch (ProtocolException ex2) {
                    Logger logger7 = log;
                    Level level2 = Level.WARNING;
                    logger7.log(level2, "HTTP request failed: " + requestMessage, Exceptions.unwrap(ex2));
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    return null;
                }
            } catch (Exception ex3) {
                Logger logger8 = log;
                Level level3 = Level.WARNING;
                logger8.log(level3, "HTTP request failed: " + requestMessage, Exceptions.unwrap(ex3));
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return null;
            }
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw th;
        }
    }

    @Override // org.fourthline.cling.transport.spi.StreamClient
    public void stop() {
    }

    protected void applyRequestProperties(HttpURLConnection urlConnection, StreamRequestMessage requestMessage) {
        urlConnection.setInstanceFollowRedirects(false);
        if (!requestMessage.getHeaders().containsKey(UpnpHeader.Type.USER_AGENT)) {
            urlConnection.setRequestProperty(UpnpHeader.Type.USER_AGENT.getHttpName(), getConfiguration().getUserAgentValue(requestMessage.getUdaMajorVersion(), requestMessage.getUdaMinorVersion()));
        }
        applyHeaders(urlConnection, requestMessage.getHeaders());
    }

    protected void applyHeaders(HttpURLConnection urlConnection, Headers headers) {
        Logger logger = log;
        logger.fine("Writing headers on HttpURLConnection: " + headers.size());
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String v : entry.getValue()) {
                String headerName = entry.getKey();
                Logger logger2 = log;
                logger2.fine("Setting header '" + headerName + "': " + v);
                urlConnection.setRequestProperty(headerName, v);
            }
        }
    }

    protected void applyRequestBody(HttpURLConnection urlConnection, StreamRequestMessage requestMessage) throws IOException {
        if (requestMessage.hasBody()) {
            urlConnection.setDoOutput(true);
            if (requestMessage.getBodyType().equals(UpnpMessage.BodyType.STRING)) {
                IO.writeUTF8(urlConnection.getOutputStream(), requestMessage.getBodyString());
            } else if (requestMessage.getBodyType().equals(UpnpMessage.BodyType.BYTES)) {
                IO.writeBytes(urlConnection.getOutputStream(), requestMessage.getBodyBytes());
            }
            urlConnection.getOutputStream().flush();
            return;
        }
        urlConnection.setDoOutput(false);
    }

    protected StreamResponseMessage createResponse(HttpURLConnection urlConnection, InputStream inputStream) throws Exception {
        if (urlConnection.getResponseCode() == -1) {
            Logger logger = log;
            logger.warning("Received an invalid HTTP response: " + urlConnection.getURL());
            log.warning("Is your Cling-based server sending connection heartbeats with RemoteClientInfo#isRequestCancelled? This client can't handle heartbeats, read the manual.");
            return null;
        }
        UpnpResponse responseOperation = new UpnpResponse(urlConnection.getResponseCode(), urlConnection.getResponseMessage());
        Logger logger2 = log;
        logger2.fine("Received response: " + responseOperation);
        StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);
        responseMessage.setHeaders(new UpnpHeaders(urlConnection.getHeaderFields()));
        byte[] bodyBytes = null;
        if (inputStream != null) {
            try {
                bodyBytes = IO.readBytes(inputStream);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        if (bodyBytes != null && bodyBytes.length > 0 && responseMessage.isContentTypeMissingOrText()) {
            log.fine("Response contains textual entity body, converting then setting string on message");
            responseMessage.setBodyCharacters(bodyBytes);
        } else if (bodyBytes != null && bodyBytes.length > 0) {
            log.fine("Response contains binary entity body, setting bytes on message");
            responseMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);
        } else {
            log.fine("Response did not contain entity body");
        }
        Logger logger3 = log;
        logger3.fine("Response message complete: " + responseMessage);
        return responseMessage;
    }
}
