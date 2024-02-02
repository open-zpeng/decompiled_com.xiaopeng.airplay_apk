package org.eclipse.jetty.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.Timeout;
/* loaded from: classes.dex */
public class HttpExchange {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final Logger LOG = Log.getLogger(HttpExchange.class);
    public static final int STATUS_CANCELLED = 11;
    public static final int STATUS_CANCELLING = 10;
    public static final int STATUS_COMPLETED = 7;
    public static final int STATUS_EXCEPTED = 9;
    public static final int STATUS_EXPIRED = 8;
    public static final int STATUS_PARSING_CONTENT = 6;
    public static final int STATUS_PARSING_HEADERS = 5;
    public static final int STATUS_SENDING_COMPLETED = 14;
    public static final int STATUS_SENDING_PARSING_CONTENT = 13;
    public static final int STATUS_SENDING_PARSING_HEADERS = 12;
    public static final int STATUS_SENDING_REQUEST = 3;
    public static final int STATUS_START = 0;
    public static final int STATUS_WAITING_FOR_COMMIT = 2;
    public static final int STATUS_WAITING_FOR_CONNECTION = 1;
    public static final int STATUS_WAITING_FOR_RESPONSE = 4;
    private Address _address;
    private volatile AbstractHttpConnection _connection;
    boolean _onDone;
    boolean _onRequestCompleteDone;
    boolean _onResponseCompleteDone;
    private Buffer _requestContent;
    private InputStream _requestContentSource;
    private volatile Timeout.Task _timeoutTask;
    private String _uri;
    private String _method = HttpMethods.GET;
    private Buffer _scheme = HttpSchemes.HTTP_BUFFER;
    private int _version = 11;
    private final HttpFields _requestFields = new HttpFields();
    private AtomicInteger _status = new AtomicInteger(0);
    private boolean _retryStatus = false;
    private boolean _configureListeners = true;
    private HttpEventListener _listener = new Listener();
    private Address _localAddress = null;
    private long _timeout = -1;
    private long _lastStateChange = System.currentTimeMillis();
    private long _sent = -1;
    private int _lastState = -1;
    private int _lastStatePeriod = -1;

    @Deprecated
    /* loaded from: classes.dex */
    public static class ContentExchange extends org.eclipse.jetty.client.ContentExchange {
    }

    protected void expire(HttpDestination destination) {
        AbstractHttpConnection connection = this._connection;
        int status = getStatus();
        if (status < 7 || status == 12 || status == 13 || status == 14) {
            setStatus(8);
        }
        destination.exchangeExpired(this);
        if (connection != null) {
            connection.exchangeExpired(this);
        }
    }

    public int getStatus() {
        return this._status.get();
    }

    @Deprecated
    public void waitForStatus(int status) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public int waitForDone() throws InterruptedException {
        int i;
        synchronized (this) {
            while (!isDone()) {
                wait();
            }
            i = this._status.get();
        }
        return i;
    }

    public void reset() {
        synchronized (this) {
            this._timeoutTask = null;
            this._onRequestCompleteDone = false;
            this._onResponseCompleteDone = false;
            this._onDone = false;
            setStatus(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean setStatus(int newStatus) {
        boolean set = false;
        try {
            int oldStatus = this._status.get();
            boolean ignored = false;
            if (oldStatus != newStatus) {
                long now = System.currentTimeMillis();
                this._lastStatePeriod = (int) (now - this._lastStateChange);
                this._lastState = oldStatus;
                this._lastStateChange = now;
                if (newStatus == 3) {
                    this._sent = this._lastStateChange;
                }
            }
            switch (oldStatus) {
                case 0:
                    switch (newStatus) {
                        default:
                            switch (newStatus) {
                                case 8:
                                    set = setStatusExpired(newStatus, oldStatus);
                                    break;
                            }
                        case 0:
                        case 1:
                        case 2:
                            set = this._status.compareAndSet(oldStatus, newStatus);
                            break;
                    }
                case 1:
                    if (newStatus != 2) {
                        switch (newStatus) {
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                        }
                        break;
                    }
                    set = this._status.compareAndSet(oldStatus, newStatus);
                    break;
                case 2:
                    if (newStatus != 3) {
                        switch (newStatus) {
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                        }
                        break;
                    }
                    set = this._status.compareAndSet(oldStatus, newStatus);
                    break;
                case 3:
                    switch (newStatus) {
                        case 4:
                            boolean compareAndSet = this._status.compareAndSet(oldStatus, newStatus);
                            set = compareAndSet;
                            if (compareAndSet) {
                                getEventListener().onRequestCommitted();
                                break;
                            }
                            break;
                        case 5:
                            set = this._status.compareAndSet(oldStatus, 12);
                            break;
                        case 8:
                            set = setStatusExpired(newStatus, oldStatus);
                            break;
                        case 9:
                        case 10:
                            set = this._status.compareAndSet(oldStatus, newStatus);
                            break;
                    }
                    break;
                case 4:
                    if (newStatus != 5) {
                        switch (newStatus) {
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                        }
                        break;
                    }
                    set = this._status.compareAndSet(oldStatus, newStatus);
                    break;
                case 5:
                    if (newStatus == 6) {
                        boolean compareAndSet2 = this._status.compareAndSet(oldStatus, newStatus);
                        set = compareAndSet2;
                        if (compareAndSet2) {
                            getEventListener().onResponseHeaderComplete();
                        }
                    } else {
                        switch (newStatus) {
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                            case 9:
                            case 10:
                                set = this._status.compareAndSet(oldStatus, newStatus);
                                break;
                        }
                    }
                    break;
                case 6:
                    switch (newStatus) {
                        case 7:
                            boolean compareAndSet3 = this._status.compareAndSet(oldStatus, newStatus);
                            set = compareAndSet3;
                            if (compareAndSet3) {
                                getEventListener().onResponseComplete();
                                break;
                            }
                            break;
                        case 8:
                            set = setStatusExpired(newStatus, oldStatus);
                            break;
                        case 9:
                        case 10:
                            set = this._status.compareAndSet(oldStatus, newStatus);
                            break;
                    }
                    break;
                case 7:
                    if (newStatus == 0) {
                        set = this._status.compareAndSet(oldStatus, newStatus);
                    } else if (newStatus == 4) {
                        if (isResponseCompleted()) {
                            ignored = true;
                            getEventListener().onRequestCommitted();
                        } else {
                            set = this._status.compareAndSet(oldStatus, newStatus);
                        }
                    } else {
                        switch (newStatus) {
                            case 8:
                            case 9:
                            case 10:
                                ignored = true;
                                break;
                        }
                    }
                    break;
                case 8:
                case 9:
                case 11:
                    if (newStatus == 0) {
                        set = this._status.compareAndSet(oldStatus, newStatus);
                        break;
                    } else if (newStatus == 7) {
                        ignored = true;
                        done();
                        break;
                    } else {
                        ignored = true;
                        break;
                    }
                case 10:
                    if (newStatus == 9 || newStatus == 11) {
                        boolean compareAndSet4 = this._status.compareAndSet(oldStatus, newStatus);
                        set = compareAndSet4;
                        if (compareAndSet4) {
                            done();
                            break;
                        }
                    } else {
                        ignored = true;
                        break;
                    }
                    break;
                case 12:
                    if (newStatus == 4) {
                        boolean compareAndSet5 = this._status.compareAndSet(oldStatus, 5);
                        set = compareAndSet5;
                        if (compareAndSet5) {
                            getEventListener().onRequestCommitted();
                            break;
                        }
                    } else if (newStatus == 6) {
                        boolean compareAndSet6 = this._status.compareAndSet(oldStatus, 13);
                        set = compareAndSet6;
                        if (compareAndSet6) {
                            getEventListener().onResponseHeaderComplete();
                            break;
                        }
                    } else {
                        switch (newStatus) {
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                            case 9:
                            case 10:
                                set = this._status.compareAndSet(oldStatus, newStatus);
                                break;
                        }
                    }
                    break;
                case 13:
                    if (newStatus == 4) {
                        boolean compareAndSet7 = this._status.compareAndSet(oldStatus, 6);
                        set = compareAndSet7;
                        if (compareAndSet7) {
                            getEventListener().onRequestCommitted();
                            break;
                        }
                    } else {
                        switch (newStatus) {
                            case 7:
                                boolean compareAndSet8 = this._status.compareAndSet(oldStatus, 14);
                                set = compareAndSet8;
                                if (compareAndSet8) {
                                    getEventListener().onResponseComplete();
                                    break;
                                }
                                break;
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                            case 9:
                            case 10:
                                set = this._status.compareAndSet(oldStatus, newStatus);
                                break;
                        }
                    }
                    break;
                case 14:
                    if (newStatus == 4) {
                        boolean compareAndSet9 = this._status.compareAndSet(oldStatus, 7);
                        set = compareAndSet9;
                        if (compareAndSet9) {
                            getEventListener().onRequestCommitted();
                            break;
                        }
                    } else {
                        switch (newStatus) {
                            case 8:
                                set = setStatusExpired(newStatus, oldStatus);
                                break;
                            case 9:
                            case 10:
                                set = this._status.compareAndSet(oldStatus, newStatus);
                                break;
                        }
                    }
                    break;
                default:
                    throw new AssertionError(oldStatus + " => " + newStatus);
            }
            if (!set && !ignored) {
                throw new IllegalStateException(toState(oldStatus) + " => " + toState(newStatus));
            }
            LOG.debug("setStatus {} {}", Integer.valueOf(newStatus), this);
        } catch (IOException x) {
            LOG.warn(x);
        }
        return set;
    }

    private boolean isResponseCompleted() {
        boolean z;
        synchronized (this) {
            z = this._onResponseCompleteDone;
        }
        return z;
    }

    private boolean setStatusExpired(int newStatus, int oldStatus) {
        boolean set = this._status.compareAndSet(oldStatus, newStatus);
        if (set) {
            getEventListener().onExpire();
        }
        return set;
    }

    public boolean isDone() {
        boolean z;
        synchronized (this) {
            z = this._onDone;
        }
        return z;
    }

    @Deprecated
    public boolean isDone(int status) {
        return isDone();
    }

    public HttpEventListener getEventListener() {
        return this._listener;
    }

    public void setEventListener(HttpEventListener listener) {
        this._listener = listener;
    }

    public void setTimeout(long timeout) {
        this._timeout = timeout;
    }

    public long getTimeout() {
        return this._timeout;
    }

    public void setURL(String url) {
        setURI(URI.create(url));
    }

    public void setAddress(Address address) {
        this._address = address;
    }

    public Address getAddress() {
        return this._address;
    }

    public Address getLocalAddress() {
        return this._localAddress;
    }

    public void setScheme(Buffer scheme) {
        this._scheme = scheme;
    }

    public void setScheme(String scheme) {
        if (scheme != null) {
            if ("http".equalsIgnoreCase(scheme)) {
                setScheme(HttpSchemes.HTTP_BUFFER);
            } else if ("https".equalsIgnoreCase(scheme)) {
                setScheme(HttpSchemes.HTTPS_BUFFER);
            } else {
                setScheme(new ByteArrayBuffer(scheme));
            }
        }
    }

    public Buffer getScheme() {
        return this._scheme;
    }

    public void setVersion(int version) {
        this._version = version;
    }

    public void setVersion(String version) {
        BufferCache.CachedBuffer v = HttpVersions.CACHE.get(version);
        if (v == null) {
            this._version = 10;
        } else {
            this._version = v.getOrdinal();
        }
    }

    public int getVersion() {
        return this._version;
    }

    public void setMethod(String method) {
        this._method = method;
    }

    public String getMethod() {
        return this._method;
    }

    @Deprecated
    public String getURI() {
        return getRequestURI();
    }

    public String getRequestURI() {
        return this._uri;
    }

    @Deprecated
    public void setURI(String uri) {
        setRequestURI(uri);
    }

    public void setRequestURI(String uri) {
        this._uri = uri;
    }

    public void setURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("!Absolute URI: " + uri);
        } else if (uri.isOpaque()) {
            throw new IllegalArgumentException("Opaque URI: " + uri);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("URI = {}", uri.toASCIIString());
            }
            String scheme = uri.getScheme();
            int port = uri.getPort();
            if (port <= 0) {
                port = "https".equalsIgnoreCase(scheme) ? 443 : 80;
            }
            setScheme(scheme);
            setAddress(new Address(uri.getHost(), port));
            HttpURI httpUri = new HttpURI(uri);
            String completePath = httpUri.getCompletePath();
            setRequestURI(completePath == null ? "/" : completePath);
        }
    }

    public void addRequestHeader(String name, String value) {
        getRequestFields().add(name, value);
    }

    public void addRequestHeader(Buffer name, Buffer value) {
        getRequestFields().add(name, value);
    }

    public void setRequestHeader(String name, String value) {
        getRequestFields().put(name, value);
    }

    public void setRequestHeader(Buffer name, Buffer value) {
        getRequestFields().put(name, value);
    }

    public void setRequestContentType(String value) {
        getRequestFields().put(HttpHeaders.CONTENT_TYPE_BUFFER, value);
    }

    public HttpFields getRequestFields() {
        return this._requestFields;
    }

    public void setRequestContent(Buffer requestContent) {
        this._requestContent = requestContent;
    }

    public void setRequestContentSource(InputStream stream) {
        this._requestContentSource = stream;
        if (this._requestContentSource != null && this._requestContentSource.markSupported()) {
            this._requestContentSource.mark(Integer.MAX_VALUE);
        }
    }

    public InputStream getRequestContentSource() {
        return this._requestContentSource;
    }

    public Buffer getRequestContentChunk(Buffer buffer) throws IOException {
        synchronized (this) {
            if (this._requestContentSource != null) {
                if (buffer == null) {
                    buffer = new ByteArrayBuffer((int) CompressedResponseWrapper.DEFAULT_BUFFER_SIZE);
                }
                int space = buffer.space();
                int length = this._requestContentSource.read(buffer.array(), buffer.putIndex(), space);
                if (length >= 0) {
                    buffer.setPutIndex(buffer.putIndex() + length);
                    return buffer;
                }
            }
            return null;
        }
    }

    public Buffer getRequestContent() {
        return this._requestContent;
    }

    public boolean getRetryStatus() {
        return this._retryStatus;
    }

    public void setRetryStatus(boolean retryStatus) {
        this._retryStatus = retryStatus;
    }

    public void cancel() {
        setStatus(10);
        abort();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void done() {
        synchronized (this) {
            disassociate();
            this._onDone = true;
            notifyAll();
        }
    }

    private void abort() {
        AbstractHttpConnection httpConnection = this._connection;
        try {
            if (httpConnection != null) {
                try {
                    httpConnection.close();
                } catch (IOException x) {
                    LOG.debug(x);
                }
            }
        } finally {
            disassociate();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void associate(AbstractHttpConnection connection) {
        if (connection.getEndPoint().getLocalAddr() != null) {
            this._localAddress = new Address(connection.getEndPoint().getLocalAddr(), connection.getEndPoint().getLocalPort());
        }
        this._connection = connection;
        if (getStatus() == 10) {
            abort();
        }
    }

    boolean isAssociated() {
        return this._connection != null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AbstractHttpConnection disassociate() {
        AbstractHttpConnection result = this._connection;
        this._connection = null;
        if (getStatus() == 10) {
            setStatus(11);
        }
        return result;
    }

    public static String toState(int s) {
        switch (s) {
            case 0:
                return "START";
            case 1:
                return "CONNECTING";
            case 2:
                return "CONNECTED";
            case 3:
                return "SENDING";
            case 4:
                return "WAITING";
            case 5:
                return "HEADERS";
            case 6:
                return "CONTENT";
            case 7:
                return "COMPLETED";
            case 8:
                return "EXPIRED";
            case 9:
                return "EXCEPTED";
            case 10:
                return "CANCELLING";
            case 11:
                return "CANCELLED";
            case 12:
                return "SENDING+HEADERS";
            case 13:
                return "SENDING+CONTENT";
            case 14:
                return "SENDING+COMPLETED";
            default:
                return "UNKNOWN";
        }
    }

    public String toString() {
        String state = toState(getStatus());
        long now = System.currentTimeMillis();
        long forMs = now - this._lastStateChange;
        String s = this._lastState >= 0 ? String.format("%s@%x=%s//%s%s#%s(%dms)->%s(%dms)", getClass().getSimpleName(), Integer.valueOf(hashCode()), this._method, this._address, this._uri, toState(this._lastState), Integer.valueOf(this._lastStatePeriod), state, Long.valueOf(forMs)) : String.format("%s@%x=%s//%s%s#%s(%dms)", getClass().getSimpleName(), Integer.valueOf(hashCode()), this._method, this._address, this._uri, state, Long.valueOf(forMs));
        if (getStatus() >= 3 && this._sent > 0) {
            return s + "sent=" + (now - this._sent) + "ms";
        }
        return s;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Connection onSwitchProtocol(EndPoint endp) throws IOException {
        return null;
    }

    protected void onRequestCommitted() throws IOException {
    }

    protected void onRequestComplete() throws IOException {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onResponseHeader(Buffer name, Buffer value) throws IOException {
    }

    protected void onResponseHeaderComplete() throws IOException {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onResponseContent(Buffer content) throws IOException {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onResponseComplete() throws IOException {
    }

    protected void onConnectionFailed(Throwable x) {
        Logger logger = LOG;
        logger.warn("CONNECTION FAILED " + this, x);
    }

    protected void onException(Throwable x) {
        Logger logger = LOG;
        logger.warn(Log.EXCEPTION + this, x);
    }

    protected void onExpire() {
        Logger logger = LOG;
        logger.warn("EXPIRED " + this, new Object[0]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onRetry() throws IOException {
        if (this._requestContentSource != null) {
            if (this._requestContentSource.markSupported()) {
                this._requestContent = null;
                this._requestContentSource.reset();
                return;
            }
            throw new IOException("Unsupported retry attempt");
        }
    }

    public boolean configureListeners() {
        return this._configureListeners;
    }

    public void setConfigureListeners(boolean autoConfigure) {
        this._configureListeners = autoConfigure;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void scheduleTimeout(final HttpDestination destination) {
        this._timeoutTask = new Timeout.Task() { // from class: org.eclipse.jetty.client.HttpExchange.1
            @Override // org.eclipse.jetty.util.thread.Timeout.Task
            public void expired() {
                HttpExchange.this.expire(destination);
            }
        };
        HttpClient httpClient = destination.getHttpClient();
        long timeout = getTimeout();
        if (timeout > 0) {
            httpClient.schedule(this._timeoutTask, timeout);
        } else {
            httpClient.schedule(this._timeoutTask);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void cancelTimeout(HttpClient httpClient) {
        Timeout.Task task = this._timeoutTask;
        if (task != null) {
            httpClient.cancel(task);
        }
        this._timeoutTask = null;
    }

    /* loaded from: classes.dex */
    private class Listener implements HttpEventListener {
        private Listener() {
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onConnectionFailed(Throwable ex) {
            try {
                HttpExchange.this.onConnectionFailed(ex);
            } finally {
                HttpExchange.this.done();
            }
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onException(Throwable ex) {
            try {
                HttpExchange.this.onException(ex);
            } finally {
                HttpExchange.this.done();
            }
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onExpire() {
            try {
                HttpExchange.this.onExpire();
            } finally {
                HttpExchange.this.done();
            }
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onRequestCommitted() throws IOException {
            HttpExchange.this.onRequestCommitted();
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onRequestComplete() throws IOException {
            try {
                HttpExchange.this.onRequestComplete();
                synchronized (HttpExchange.this) {
                    HttpExchange.this._onRequestCompleteDone = true;
                    HttpExchange.this._onDone |= HttpExchange.this._onResponseCompleteDone;
                    if (HttpExchange.this._onDone) {
                        HttpExchange.this.disassociate();
                    }
                    HttpExchange.this.notifyAll();
                }
            } catch (Throwable th) {
                synchronized (HttpExchange.this) {
                    HttpExchange.this._onRequestCompleteDone = true;
                    HttpExchange.this._onDone |= HttpExchange.this._onResponseCompleteDone;
                    if (HttpExchange.this._onDone) {
                        HttpExchange.this.disassociate();
                    }
                    HttpExchange.this.notifyAll();
                    throw th;
                }
            }
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseComplete() throws IOException {
            try {
                HttpExchange.this.onResponseComplete();
                synchronized (HttpExchange.this) {
                    HttpExchange.this._onResponseCompleteDone = true;
                    HttpExchange.this._onDone |= HttpExchange.this._onRequestCompleteDone;
                    if (HttpExchange.this._onDone) {
                        HttpExchange.this.disassociate();
                    }
                    HttpExchange.this.notifyAll();
                }
            } catch (Throwable th) {
                synchronized (HttpExchange.this) {
                    HttpExchange.this._onResponseCompleteDone = true;
                    HttpExchange.this._onDone |= HttpExchange.this._onRequestCompleteDone;
                    if (HttpExchange.this._onDone) {
                        HttpExchange.this.disassociate();
                    }
                    HttpExchange.this.notifyAll();
                    throw th;
                }
            }
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseContent(Buffer content) throws IOException {
            HttpExchange.this.onResponseContent(content);
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseHeader(Buffer name, Buffer value) throws IOException {
            HttpExchange.this.onResponseHeader(name, value);
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseHeaderComplete() throws IOException {
            HttpExchange.this.onResponseHeaderComplete();
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
            HttpExchange.this.onResponseStatus(version, status, reason);
        }

        @Override // org.eclipse.jetty.client.HttpEventListener
        public void onRetry() {
            HttpExchange.this.setRetryStatus(true);
            try {
                HttpExchange.this.onRetry();
            } catch (IOException e) {
                HttpExchange.LOG.debug(e);
            }
        }
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static class CachedExchange extends org.eclipse.jetty.client.CachedExchange {
        public CachedExchange(boolean cacheFields) {
            super(cacheFields);
        }
    }
}
