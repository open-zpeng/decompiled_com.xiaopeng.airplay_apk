package org.eclipse.jetty.client.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpEventListenerWrapper;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class SecurityListener extends HttpEventListenerWrapper {
    private static final Logger LOG = Log.getLogger(SecurityListener.class);
    private int _attempts;
    private HttpDestination _destination;
    private HttpExchange _exchange;
    private boolean _needIntercept;
    private boolean _requestComplete;
    private boolean _responseComplete;

    public SecurityListener(HttpDestination destination, HttpExchange ex) {
        super(ex.getEventListener(), true);
        this._attempts = 0;
        this._destination = destination;
        this._exchange = ex;
    }

    protected String scrapeAuthenticationType(String authString) {
        if (authString.indexOf(" ") == -1) {
            String authType = authString.toString().trim();
            return authType;
        }
        String authResponse = authString.toString();
        return authResponse.substring(0, authResponse.indexOf(" ")).trim();
    }

    protected Map<String, String> scrapeAuthenticationDetails(String authString) {
        Map<String, String> authenticationDetails = new HashMap<>();
        StringTokenizer strtok = new StringTokenizer(authString.substring(authString.indexOf(" ") + 1, authString.length()), ",");
        while (strtok.hasMoreTokens()) {
            String token = strtok.nextToken();
            String[] pair = token.split("=");
            if (pair.length == 2) {
                String itemName = pair[0].trim();
                String itemValue = pair[1].trim();
                authenticationDetails.put(itemName, StringUtil.unquote(itemValue));
            } else {
                Logger logger = LOG;
                logger.debug("SecurityListener: missed scraping authentication details - " + token, new Object[0]);
            }
        }
        return authenticationDetails;
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("SecurityListener:Response Status: " + status, new Object[0]);
        }
        if (status == 401 && this._attempts < this._destination.getHttpClient().maxRetries()) {
            setDelegatingResponses(false);
            this._needIntercept = true;
        } else {
            setDelegatingResponses(true);
            setDelegatingRequests(true);
            this._needIntercept = false;
        }
        super.onResponseStatus(version, status, reason);
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseHeader(Buffer name, Buffer value) throws IOException {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("SecurityListener:Header: " + name.toString() + " / " + value.toString(), new Object[0]);
        }
        if (!isDelegatingResponses()) {
            int header = HttpHeaders.CACHE.getOrdinal(name);
            if (header == 51) {
                String authString = value.toString();
                String type = scrapeAuthenticationType(authString);
                Map<String, String> details = scrapeAuthenticationDetails(authString);
                RealmResolver realmResolver = this._destination.getHttpClient().getRealmResolver();
                if (realmResolver != null) {
                    Realm realm = realmResolver.getRealm(details.get("realm"), this._destination, "/");
                    if (realm == null) {
                        Logger logger2 = LOG;
                        logger2.warn("Unknown Security Realm: " + details.get("realm"), new Object[0]);
                    } else if ("digest".equalsIgnoreCase(type)) {
                        this._destination.addAuthorization("/", new DigestAuthentication(realm, details));
                    } else if ("basic".equalsIgnoreCase(type)) {
                        this._destination.addAuthorization("/", new BasicAuthentication(realm));
                    }
                }
            }
        }
        super.onResponseHeader(name, value);
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onRequestComplete() throws IOException {
        this._requestComplete = true;
        if (this._needIntercept) {
            if (this._requestComplete && this._responseComplete) {
                if (LOG.isDebugEnabled()) {
                    Logger logger = LOG;
                    logger.debug("onRequestComplete, Both complete: Resending from onResponseComplete " + this._exchange, new Object[0]);
                }
                this._responseComplete = false;
                this._requestComplete = false;
                setDelegatingRequests(true);
                setDelegatingResponses(true);
                this._destination.resend(this._exchange);
                return;
            }
            if (LOG.isDebugEnabled()) {
                Logger logger2 = LOG;
                logger2.debug("onRequestComplete, Response not yet complete onRequestComplete, calling super for " + this._exchange, new Object[0]);
            }
            super.onRequestComplete();
            return;
        }
        if (LOG.isDebugEnabled()) {
            Logger logger3 = LOG;
            logger3.debug("onRequestComplete, delegating to super with Request complete=" + this._requestComplete + ", response complete=" + this._responseComplete + " " + this._exchange, new Object[0]);
        }
        super.onRequestComplete();
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onResponseComplete() throws IOException {
        this._responseComplete = true;
        if (this._needIntercept) {
            if (this._requestComplete && this._responseComplete) {
                if (LOG.isDebugEnabled()) {
                    Logger logger = LOG;
                    logger.debug("onResponseComplete, Both complete: Resending from onResponseComplete" + this._exchange, new Object[0]);
                }
                this._responseComplete = false;
                this._requestComplete = false;
                setDelegatingResponses(true);
                setDelegatingRequests(true);
                this._destination.resend(this._exchange);
                return;
            }
            if (LOG.isDebugEnabled()) {
                Logger logger2 = LOG;
                logger2.debug("onResponseComplete, Request not yet complete from onResponseComplete,  calling super " + this._exchange, new Object[0]);
            }
            super.onResponseComplete();
            return;
        }
        if (LOG.isDebugEnabled()) {
            Logger logger3 = LOG;
            logger3.debug("OnResponseComplete, delegating to super with Request complete=" + this._requestComplete + ", response complete=" + this._responseComplete + " " + this._exchange, new Object[0]);
        }
        super.onResponseComplete();
    }

    @Override // org.eclipse.jetty.client.HttpEventListenerWrapper, org.eclipse.jetty.client.HttpEventListener
    public void onRetry() {
        this._attempts++;
        setDelegatingRequests(true);
        setDelegatingResponses(true);
        this._requestComplete = false;
        this._responseComplete = false;
        this._needIntercept = false;
        super.onRetry();
    }
}
