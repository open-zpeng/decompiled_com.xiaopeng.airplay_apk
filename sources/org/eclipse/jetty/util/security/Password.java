package org.eclipse.jetty.util.security;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import org.eclipse.jetty.http.gzip.CompressedResponseWrapper;
import org.eclipse.jetty.server.HttpWriter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Credential;
/* loaded from: classes.dex */
public class Password extends Credential {
    private static final Logger LOG = Log.getLogger(Password.class);
    public static final String __OBFUSCATE = "OBF:";
    private static final long serialVersionUID = 5062906681431569445L;
    private String _pw;

    public Password(String password) {
        this._pw = password;
        while (this._pw != null && this._pw.startsWith(__OBFUSCATE)) {
            this._pw = deobfuscate(this._pw);
        }
    }

    public String toString() {
        return this._pw;
    }

    public String toStarString() {
        return "*****************************************************".substring(0, this._pw.length());
    }

    @Override // org.eclipse.jetty.util.security.Credential
    public boolean check(Object credentials) {
        if (this == credentials) {
            return true;
        }
        if (!(credentials instanceof Password) && !(credentials instanceof String)) {
            if (credentials instanceof char[]) {
                return Arrays.equals(this._pw.toCharArray(), (char[]) credentials);
            }
            if (credentials instanceof Credential) {
                return ((Credential) credentials).check(this._pw);
            }
            return false;
        }
        return credentials.equals(this._pw);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Password) {
            Password p = (Password) o;
            if (p._pw != this._pw) {
                if (this._pw != null && this._pw.equals(p._pw)) {
                    return true;
                }
                return false;
            }
            return true;
        } else if (!(o instanceof String)) {
            return false;
        } else {
            return o.equals(this._pw);
        }
    }

    public int hashCode() {
        return this._pw == null ? super.hashCode() : this._pw.hashCode();
    }

    public static String obfuscate(String s) {
        StringBuilder buf = new StringBuilder();
        byte[] b = s.getBytes();
        buf.append(__OBFUSCATE);
        for (int i = 0; i < b.length; i++) {
            byte b1 = b[i];
            byte b2 = b[s.length() - (i + 1)];
            int i1 = Byte.MAX_VALUE + b1 + b2;
            int i2 = (Byte.MAX_VALUE + b1) - b2;
            int i0 = (i1 * CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE) + i2;
            String x = Integer.toString(i0, 36);
            switch (x.length()) {
                case 1:
                    buf.append('0');
                    buf.append('0');
                    buf.append('0');
                    buf.append(x);
                    break;
                case 2:
                    buf.append('0');
                    buf.append('0');
                    buf.append(x);
                    break;
                case 3:
                    buf.append('0');
                    buf.append(x);
                    break;
                default:
                    buf.append(x);
                    break;
            }
        }
        return buf.toString();
    }

    public static String deobfuscate(String s) {
        if (s.startsWith(__OBFUSCATE)) {
            s = s.substring(4);
        }
        byte[] b = new byte[s.length() / 2];
        int l = 0;
        int l2 = 0;
        while (l2 < s.length()) {
            String x = s.substring(l2, l2 + 4);
            int i0 = Integer.parseInt(x, 36);
            int i1 = i0 / CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE;
            int i2 = i0 % CompressedResponseWrapper.DEFAULT_MIN_COMPRESS_SIZE;
            b[l] = (byte) (((i1 + i2) - 254) / 2);
            l2 += 4;
            l++;
        }
        return new String(b, 0, l);
    }

    public static Password getPassword(String realm, String dft, String promptDft) {
        String passwd = System.getProperty(realm, dft);
        if (passwd == null || passwd.length() == 0) {
            try {
                PrintStream printStream = System.out;
                StringBuilder sb = new StringBuilder();
                sb.append(realm);
                sb.append((promptDft == null || promptDft.length() <= 0) ? "" : " [dft]");
                sb.append(" : ");
                printStream.print(sb.toString());
                System.out.flush();
                byte[] buf = new byte[HttpWriter.MAX_OUTPUT_CHARS];
                int len = System.in.read(buf);
                if (len > 0) {
                    passwd = new String(buf, 0, len).trim();
                }
            } catch (IOException e) {
                LOG.warn(Log.EXCEPTION, e);
            }
            if (passwd == null || passwd.length() == 0) {
                passwd = promptDft;
            }
        }
        return new Password(passwd);
    }

    public static void main(String[] arg) {
        if (arg.length != 1 && arg.length != 2) {
            System.err.println("Usage - java org.eclipse.jetty.security.Password [<user>] <password>");
            System.err.println("If the password is ?, the user will be prompted for the password");
            System.exit(1);
        }
        String p = arg[arg.length == 1 ? (char) 0 : (char) 1];
        Password pw = new Password(p);
        System.err.println(pw.toString());
        System.err.println(obfuscate(pw.toString()));
        System.err.println(Credential.MD5.digest(p));
        if (arg.length == 2) {
            System.err.println(Credential.Crypt.crypt(arg[0], pw.toString()));
        }
    }
}
