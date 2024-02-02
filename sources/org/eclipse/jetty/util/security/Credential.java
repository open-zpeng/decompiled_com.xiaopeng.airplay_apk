package org.eclipse.jetty.util.security;

import java.io.Serializable;
import java.security.MessageDigest;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public abstract class Credential implements Serializable {
    private static final Logger LOG = Log.getLogger(Credential.class);
    private static final long serialVersionUID = -7760551052768181572L;

    public abstract boolean check(Object obj);

    public static Credential getCredential(String credential) {
        return credential.startsWith(Crypt.__TYPE) ? new Crypt(credential) : credential.startsWith(MD5.__TYPE) ? new MD5(credential) : new Password(credential);
    }

    /* loaded from: classes.dex */
    public static class Crypt extends Credential {
        public static final String __TYPE = "CRYPT:";
        private static final long serialVersionUID = -2027792997664744210L;
        private final String _cooked;

        Crypt(String cooked) {
            this._cooked = cooked.startsWith(__TYPE) ? cooked.substring(__TYPE.length()) : cooked;
        }

        @Override // org.eclipse.jetty.util.security.Credential
        public boolean check(Object credentials) {
            if (credentials instanceof char[]) {
                credentials = new String((char[]) credentials);
            }
            if (!(credentials instanceof String) && !(credentials instanceof Password)) {
                Logger logger = Credential.LOG;
                logger.warn("Can't check " + credentials.getClass() + " against CRYPT", new Object[0]);
            }
            String passwd = credentials.toString();
            return this._cooked.equals(UnixCrypt.crypt(passwd, this._cooked));
        }

        public static String crypt(String user, String pw) {
            return __TYPE + UnixCrypt.crypt(pw, user);
        }
    }

    /* loaded from: classes.dex */
    public static class MD5 extends Credential {
        public static final String __TYPE = "MD5:";
        private static MessageDigest __md = null;
        public static final Object __md5Lock = new Object();
        private static final long serialVersionUID = 5533846540822684240L;
        private final byte[] _digest;

        MD5(String digest) {
            this._digest = TypeUtil.parseBytes(digest.startsWith(__TYPE) ? digest.substring(__TYPE.length()) : digest, 16);
        }

        public byte[] getDigest() {
            return this._digest;
        }

        @Override // org.eclipse.jetty.util.security.Credential
        public boolean check(Object credentials) {
            byte[] digest;
            try {
                if (credentials instanceof char[]) {
                    credentials = new String((char[]) credentials);
                }
                if (!(credentials instanceof Password) && !(credentials instanceof String)) {
                    if (credentials instanceof MD5) {
                        MD5 md5 = (MD5) credentials;
                        if (this._digest.length != md5._digest.length) {
                            return false;
                        }
                        for (int i = 0; i < this._digest.length; i++) {
                            if (this._digest[i] != md5._digest[i]) {
                                return false;
                            }
                        }
                        return true;
                    } else if (!(credentials instanceof Credential)) {
                        Credential.LOG.warn("Can't check " + credentials.getClass() + " against MD5", new Object[0]);
                        return false;
                    } else {
                        return ((Credential) credentials).check(this);
                    }
                }
                synchronized (__md5Lock) {
                    if (__md == null) {
                        __md = MessageDigest.getInstance("MD5");
                    }
                    __md.reset();
                    __md.update(credentials.toString().getBytes(StringUtil.__ISO_8859_1));
                    digest = __md.digest();
                }
                if (digest != null && digest.length == this._digest.length) {
                    for (int i2 = 0; i2 < digest.length; i2++) {
                        if (digest[i2] != this._digest[i2]) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            } catch (Exception e) {
                Credential.LOG.warn(e);
                return false;
            }
        }

        public static String digest(String password) {
            try {
                synchronized (__md5Lock) {
                    try {
                        if (__md == null) {
                            try {
                                __md = MessageDigest.getInstance("MD5");
                            } catch (Exception e) {
                                Credential.LOG.warn(e);
                                return null;
                            }
                        }
                        __md.reset();
                        __md.update(password.getBytes(StringUtil.__ISO_8859_1));
                        byte[] digest = __md.digest();
                        try {
                            return __TYPE + TypeUtil.toString(digest, 16);
                        } catch (Throwable th) {
                            th = th;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        while (true) {
                            break;
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Exception e2) {
                Credential.LOG.warn(e2);
                return null;
            }
        }
    }
}
