package org.eclipse.jetty.security.authentication;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.BitSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpTokens;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Credential;
/* loaded from: classes.dex */
public class DigestAuthenticator extends LoginAuthenticator {
    private static final Logger LOG = Log.getLogger(DigestAuthenticator.class);
    SecureRandom _random = new SecureRandom();
    private long _maxNonceAgeMs = 60000;
    private int _maxNC = 1024;
    private ConcurrentMap<String, Nonce> _nonceMap = new ConcurrentHashMap();
    private Queue<Nonce> _nonceQueue = new ConcurrentLinkedQueue();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Nonce {
        final String _nonce;
        final BitSet _seen;
        final long _ts;

        public Nonce(String nonce, long ts, int size) {
            this._nonce = nonce;
            this._ts = ts;
            this._seen = new BitSet(size);
        }

        public boolean seen(int count) {
            synchronized (this) {
                if (count >= this._seen.size()) {
                    return true;
                }
                boolean s = this._seen.get(count);
                this._seen.set(count);
                return s;
            }
        }
    }

    @Override // org.eclipse.jetty.security.authentication.LoginAuthenticator, org.eclipse.jetty.security.Authenticator
    public void setConfiguration(Authenticator.AuthConfiguration configuration) {
        super.setConfiguration(configuration);
        String mna = configuration.getInitParameter("maxNonceAge");
        if (mna != null) {
            this._maxNonceAgeMs = Long.valueOf(mna).longValue();
        }
    }

    public int getMaxNonceCount() {
        return this._maxNC;
    }

    public void setMaxNonceCount(int maxNC) {
        this._maxNC = maxNC;
    }

    public void setMaxNonceAge(long maxNonceAgeInMillis) {
        this._maxNonceAgeMs = maxNonceAgeInMillis;
    }

    public long getMaxNonceAge() {
        return this._maxNonceAgeMs;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public String getAuthMethod() {
        return "DIGEST";
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
        return true;
    }

    @Override // org.eclipse.jetty.security.Authenticator
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
        String name;
        if (!mandatory) {
            return new DeferredAuthentication(this);
        }
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String credentials = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean stale = false;
        if (credentials != null) {
            try {
                char c = 0;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Credentials: " + credentials, new Object[0]);
                }
                QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(credentials, "=, ", true, false);
                Digest digest = new Digest(request.getMethod());
                String last = null;
                String name2 = null;
                while (tokenizer.hasMoreTokens()) {
                    String tok = tokenizer.nextToken();
                    char c2 = tok.length() == 1 ? tok.charAt(c) : c;
                    if (c2 != ' ') {
                        if (c2 == ',') {
                            name = null;
                        } else if (c2 == '=') {
                            name = last;
                            last = tok;
                        } else {
                            if (name2 != null) {
                                if ("username".equalsIgnoreCase(name2)) {
                                    digest.username = tok;
                                } else if ("realm".equalsIgnoreCase(name2)) {
                                    digest.realm = tok;
                                } else if ("nonce".equalsIgnoreCase(name2)) {
                                    digest.nonce = tok;
                                } else if ("nc".equalsIgnoreCase(name2)) {
                                    digest.nc = tok;
                                } else if ("cnonce".equalsIgnoreCase(name2)) {
                                    digest.cnonce = tok;
                                } else if ("qop".equalsIgnoreCase(name2)) {
                                    digest.qop = tok;
                                } else if ("uri".equalsIgnoreCase(name2)) {
                                    digest.uri = tok;
                                } else if ("response".equalsIgnoreCase(name2)) {
                                    digest.response = tok;
                                }
                                name2 = null;
                            }
                            last = tok;
                        }
                        name2 = name;
                    }
                    c = 0;
                }
                int n = checkNonce(digest, (Request) request);
                if (n > 0) {
                    UserIdentity user = login(digest.username, digest, req);
                    if (user != null) {
                        return new UserAuthentication(getAuthMethod(), user);
                    }
                } else if (n == 0) {
                    stale = true;
                }
            } catch (IOException e) {
                throw new ServerAuthException(e);
            }
        }
        if (!DeferredAuthentication.isDeferred(response)) {
            String domain = request.getContextPath();
            if (domain == null) {
                domain = "/";
            }
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Digest realm=\"" + this._loginService.getName() + "\", domain=\"" + domain + "\", nonce=\"" + newNonce((Request) request) + "\", algorithm=MD5, qop=\"auth\", stale=" + stale);
            response.sendError(401);
            return Authentication.SEND_CONTINUE;
        }
        return Authentication.UNAUTHENTICATED;
    }

    public String newNonce(Request request) {
        Nonce nonce;
        do {
            byte[] nounce = new byte[24];
            this._random.nextBytes(nounce);
            nonce = new Nonce(new String(B64Code.encode(nounce)), request.getTimeStamp(), this._maxNC);
        } while (this._nonceMap.putIfAbsent(nonce._nonce, nonce) != null);
        this._nonceQueue.add(nonce);
        return nonce._nonce;
    }

    private int checkNonce(Digest digest, Request request) {
        long expired = request.getTimeStamp() - this._maxNonceAgeMs;
        Nonce nonce = this._nonceQueue.peek();
        while (nonce != null && nonce._ts < expired) {
            this._nonceQueue.remove(nonce);
            this._nonceMap.remove(nonce._nonce);
            Nonce nonce2 = this._nonceQueue.peek();
            nonce = nonce2;
        }
        try {
            Nonce nonce3 = this._nonceMap.get(digest.nonce);
            if (nonce3 == null) {
                return 0;
            }
            long count = Long.parseLong(digest.nc, 16);
            if (count >= this._maxNC) {
                return 0;
            }
            if (nonce3.seen((int) count)) {
                return -1;
            }
            return 1;
        } catch (Exception e) {
            LOG.ignore(e);
            return -1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Digest extends Credential {
        private static final long serialVersionUID = -2484639019549527724L;
        final String method;
        String username = "";
        String realm = "";
        String nonce = "";
        String nc = "";
        String cnonce = "";
        String qop = "";
        String uri = "";
        String response = "";

        Digest(String m) {
            this.method = m;
        }

        @Override // org.eclipse.jetty.util.security.Credential
        public boolean check(Object credentials) {
            byte[] ha1;
            if (credentials instanceof char[]) {
                credentials = new String((char[]) credentials);
            }
            String password = credentials instanceof String ? credentials : credentials.toString();
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                if (credentials instanceof Credential.MD5) {
                    ha1 = ((Credential.MD5) credentials).getDigest();
                } else {
                    md.update(this.username.getBytes(StringUtil.__ISO_8859_1));
                    md.update(HttpTokens.COLON);
                    md.update(this.realm.getBytes(StringUtil.__ISO_8859_1));
                    md.update(HttpTokens.COLON);
                    md.update(password.getBytes(StringUtil.__ISO_8859_1));
                    ha1 = md.digest();
                }
                md.reset();
                md.update(this.method.getBytes(StringUtil.__ISO_8859_1));
                md.update(HttpTokens.COLON);
                md.update(this.uri.getBytes(StringUtil.__ISO_8859_1));
                byte[] ha2 = md.digest();
                md.update(TypeUtil.toString(ha1, 16).getBytes(StringUtil.__ISO_8859_1));
                md.update(HttpTokens.COLON);
                md.update(this.nonce.getBytes(StringUtil.__ISO_8859_1));
                md.update(HttpTokens.COLON);
                md.update(this.nc.getBytes(StringUtil.__ISO_8859_1));
                md.update(HttpTokens.COLON);
                md.update(this.cnonce.getBytes(StringUtil.__ISO_8859_1));
                md.update(HttpTokens.COLON);
                md.update(this.qop.getBytes(StringUtil.__ISO_8859_1));
                md.update(HttpTokens.COLON);
                md.update(TypeUtil.toString(ha2, 16).getBytes(StringUtil.__ISO_8859_1));
                byte[] digest = md.digest();
                return TypeUtil.toString(digest, 16).equalsIgnoreCase(this.response);
            } catch (Exception e) {
                DigestAuthenticator.LOG.warn(e);
                return false;
            }
        }

        public String toString() {
            return this.username + "," + this.response;
        }
    }
}
