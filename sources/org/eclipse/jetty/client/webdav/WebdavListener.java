package org.eclipse.jetty.client.webdav;

import java.io.IOException;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpEventListenerWrapper;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.security.SecurityListener;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class WebdavListener extends HttpEventListenerWrapper {
    private static final Logger LOG = Log.getLogger(WebdavListener.class);
    private HttpDestination _destination;
    private HttpExchange _exchange;
    private boolean _needIntercept;
    private boolean _requestComplete;
    private boolean _responseComplete;
    private boolean _webdavEnabled;

    public WebdavListener(HttpDestination destination, HttpExchange ex) {
        super(ex.getEventListener(), true);
        this._destination = destination;
        this._exchange = ex;
        if (HttpMethods.PUT.equalsIgnoreCase(this._exchange.getMethod())) {
            this._webdavEnabled = true;
        }
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        if (!this._webdavEnabled) {
            this._needIntercept = false;
            super.onResponseStatus(version, status, reason);
            return;
        }
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("WebdavListener:Response Status: " + status, new Object[0]);
        }
        if (status == 403 || status == 409) {
            if (this._webdavEnabled) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("WebdavListener:Response Status: dav enabled, taking a stab at resolving put issue", new Object[0]);
                }
                setDelegatingResponses(false);
                this._needIntercept = true;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("WebdavListener:Response Status: Webdav Disabled", new Object[0]);
                }
                setDelegatingResponses(true);
                setDelegatingRequests(true);
                this._needIntercept = false;
            }
        } else {
            this._needIntercept = false;
            setDelegatingResponses(true);
            setDelegatingRequests(true);
        }
        super.onResponseStatus(version, status, reason);
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseComplete() throws IOException {
        this._responseComplete = true;
        if (this._needIntercept) {
            if (this._requestComplete && this._responseComplete) {
                try {
                    if (resolveCollectionIssues()) {
                        setDelegatingRequests(true);
                        setDelegatingResponses(true);
                        this._requestComplete = false;
                        this._responseComplete = false;
                        this._destination.resend(this._exchange);
                    } else {
                        setDelegationResult(false);
                        setDelegatingRequests(true);
                        setDelegatingResponses(true);
                        super.onResponseComplete();
                    }
                    return;
                } catch (IOException e) {
                    LOG.debug("WebdavListener:Complete:IOException: might not be dealing with dav server, delegate", new Object[0]);
                    super.onResponseComplete();
                    return;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("WebdavListener:Not ready, calling super", new Object[0]);
            }
            super.onResponseComplete();
            return;
        }
        super.onResponseComplete();
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onRequestComplete() throws IOException {
        this._requestComplete = true;
        if (this._needIntercept) {
            if (this._requestComplete && this._responseComplete) {
                try {
                    if (resolveCollectionIssues()) {
                        setDelegatingRequests(true);
                        setDelegatingResponses(true);
                        this._requestComplete = false;
                        this._responseComplete = false;
                        this._destination.resend(this._exchange);
                    } else {
                        setDelegatingRequests(true);
                        setDelegatingResponses(true);
                        super.onRequestComplete();
                    }
                    return;
                } catch (IOException e) {
                    LOG.debug("WebdavListener:Complete:IOException: might not be dealing with dav server, delegate", new Object[0]);
                    super.onRequestComplete();
                    return;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("WebdavListener:Not ready, calling super", new Object[0]);
            }
            super.onRequestComplete();
            return;
        }
        super.onRequestComplete();
    }

    private boolean resolveCollectionIssues() throws IOException {
        String uri = this._exchange.getURI();
        String[] uriCollection = this._exchange.getURI().split("/");
        int checkNum = uriCollection.length;
        int rewind = 0;
        String parentUri = URIUtil.parentPath(uri);
        while (parentUri != null && !checkExists(parentUri)) {
            rewind++;
            parentUri = URIUtil.parentPath(parentUri);
        }
        if (checkWebdavSupported()) {
            while (0 < rewind) {
                makeCollection(parentUri + "/" + uriCollection[(checkNum - rewind) - 1]);
                parentUri = parentUri + "/" + uriCollection[(checkNum - rewind) - 1];
                rewind--;
            }
            return true;
        }
        return false;
    }

    private boolean checkExists(String uri) throws IOException {
        if (uri == null) {
            System.out.println("have failed miserably");
            return false;
        }
        PropfindExchange propfindExchange = new PropfindExchange();
        propfindExchange.setAddress(this._exchange.getAddress());
        propfindExchange.setMethod(HttpMethods.GET);
        propfindExchange.setScheme(this._exchange.getScheme());
        propfindExchange.setEventListener(new SecurityListener(this._destination, propfindExchange));
        propfindExchange.setConfigureListeners(false);
        propfindExchange.setRequestURI(uri);
        this._destination.send(propfindExchange);
        try {
            propfindExchange.waitForDone();
            return propfindExchange.exists();
        } catch (InterruptedException ie) {
            LOG.ignore(ie);
            return false;
        }
    }

    private boolean makeCollection(String uri) throws IOException {
        MkcolExchange mkcolExchange = new MkcolExchange();
        mkcolExchange.setAddress(this._exchange.getAddress());
        mkcolExchange.setMethod("MKCOL " + uri + " HTTP/1.1");
        mkcolExchange.setScheme(this._exchange.getScheme());
        mkcolExchange.setEventListener(new SecurityListener(this._destination, mkcolExchange));
        mkcolExchange.setConfigureListeners(false);
        mkcolExchange.setRequestURI(uri);
        this._destination.send(mkcolExchange);
        try {
            mkcolExchange.waitForDone();
            return mkcolExchange.exists();
        } catch (InterruptedException ie) {
            LOG.ignore(ie);
            return false;
        }
    }

    private boolean checkWebdavSupported() throws IOException {
        WebdavSupportedExchange supportedExchange = new WebdavSupportedExchange();
        supportedExchange.setAddress(this._exchange.getAddress());
        supportedExchange.setMethod(HttpMethods.OPTIONS);
        supportedExchange.setScheme(this._exchange.getScheme());
        supportedExchange.setEventListener(new SecurityListener(this._destination, supportedExchange));
        supportedExchange.setConfigureListeners(false);
        supportedExchange.setRequestURI(this._exchange.getURI());
        this._destination.send(supportedExchange);
        try {
            supportedExchange.waitTilCompletion();
            return supportedExchange.isWebdavSupported();
        } catch (InterruptedException ie) {
            LOG.ignore(ie);
            return false;
        }
    }
}
