package org.eclipse.jetty.client;

import java.io.IOException;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
/* loaded from: classes.dex */
public class RedirectListener extends HttpEventListenerWrapper {
    private int _attempts;
    private HttpDestination _destination;
    private final HttpExchange _exchange;
    private String _location;
    private boolean _redirected;
    private boolean _requestComplete;
    private boolean _responseComplete;

    public RedirectListener(HttpDestination destination, HttpExchange ex) {
        super(ex.getEventListener(), true);
        this._destination = destination;
        this._exchange = ex;
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        this._redirected = (status == 301 || status == 302) && this._attempts < this._destination.getHttpClient().maxRedirects();
        if (this._redirected) {
            setDelegatingRequests(false);
            setDelegatingResponses(false);
        }
        super.onResponseStatus(version, status, reason);
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseHeader(Buffer name, Buffer value) throws IOException {
        if (this._redirected) {
            int header = HttpHeaders.CACHE.getOrdinal(name);
            if (header == 45) {
                this._location = value.toString();
            }
        }
        super.onResponseHeader(name, value);
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onRequestComplete() throws IOException {
        this._requestComplete = true;
        if (checkExchangeComplete()) {
            super.onRequestComplete();
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseComplete() throws IOException {
        this._responseComplete = true;
        if (checkExchangeComplete()) {
            super.onResponseComplete();
        }
    }

    public boolean checkExchangeComplete() throws IOException {
        if (this._redirected && this._requestComplete && this._responseComplete) {
            if (this._location != null) {
                if (this._location.indexOf("://") > 0) {
                    this._exchange.setURL(this._location);
                } else {
                    this._exchange.setRequestURI(this._location);
                }
                boolean isHttps = "https".equals(String.valueOf(this._exchange.getScheme()));
                HttpDestination destination = this._destination.getHttpClient().getDestination(this._exchange.getAddress(), isHttps);
                if (this._destination == destination) {
                    this._destination.resend(this._exchange);
                } else {
                    HttpEventListener listener = this;
                    while (listener instanceof HttpEventListenerWrapper) {
                        listener = ((HttpEventListenerWrapper) listener).getEventListener();
                    }
                    this._exchange.getEventListener().onRetry();
                    this._exchange.reset();
                    this._exchange.setEventListener(listener);
                    Address address = this._exchange.getAddress();
                    int port = address.getPort();
                    StringBuilder hostHeader = new StringBuilder(64);
                    hostHeader.append(address.getHost());
                    if ((port != 80 || isHttps) && (port != 443 || !isHttps)) {
                        hostHeader.append(':');
                        hostHeader.append(port);
                    }
                    this._exchange.setRequestHeader(HttpHeaders.HOST, hostHeader.toString());
                    destination.send(this._exchange);
                }
                return false;
            }
            setDelegationResult(false);
            return true;
        }
        return true;
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onRetry() {
        this._redirected = false;
        this._attempts++;
        setDelegatingRequests(true);
        setDelegatingResponses(true);
        this._requestComplete = false;
        this._responseComplete = false;
        super.onRetry();
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onConnectionFailed(Throwable ex) {
        setDelegatingRequests(true);
        setDelegatingResponses(true);
        super.onConnectionFailed(ex);
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onException(Throwable ex) {
        setDelegatingRequests(true);
        setDelegatingResponses(true);
        super.onException(ex);
    }
}
