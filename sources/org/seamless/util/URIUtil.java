package org.seamless.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.BitSet;
import org.eclipse.jetty.util.StringUtil;
/* loaded from: classes.dex */
public class URIUtil {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final BitSet ALLOWED = new BitSet() { // from class: org.seamless.util.URIUtil.1
        {
            for (int i = 97; i <= 122; i++) {
                set(i);
            }
            for (int i2 = 65; i2 <= 90; i2++) {
                set(i2);
            }
            for (int i3 = 48; i3 <= 57; i3++) {
                set(i3);
            }
            set(33);
            set(36);
            set(38);
            set(39);
            set(40);
            set(41);
            set(42);
            set(43);
            set(44);
            set(59);
            set(61);
            set(45);
            set(46);
            set(95);
            set(126);
            set(58);
            set(64);
        }
    };
    public static final BitSet PATH_SEGMENT = new BitSet() { // from class: org.seamless.util.URIUtil.2
        {
            or(URIUtil.ALLOWED);
            clear(59);
        }
    };
    public static final BitSet PATH_PARAM_NAME = new BitSet() { // from class: org.seamless.util.URIUtil.3
        {
            or(URIUtil.ALLOWED);
            clear(59);
            clear(61);
        }
    };
    public static final BitSet PATH_PARAM_VALUE = new BitSet() { // from class: org.seamless.util.URIUtil.4
        {
            or(URIUtil.ALLOWED);
            clear(59);
        }
    };
    public static final BitSet QUERY = new BitSet() { // from class: org.seamless.util.URIUtil.5
        {
            or(URIUtil.ALLOWED);
            set(47);
            set(63);
            clear(61);
            clear(38);
            clear(43);
        }
    };
    public static final BitSet FRAGMENT = new BitSet() { // from class: org.seamless.util.URIUtil.6
        {
            or(URIUtil.ALLOWED);
            set(47);
            set(63);
        }
    };

    public static URI createAbsoluteURI(URI base, String uri) throws IllegalArgumentException {
        return createAbsoluteURI(base, URI.create(uri));
    }

    public static URI createAbsoluteURI(URI base, URI relativeOrNot) throws IllegalArgumentException {
        if (base == null && !relativeOrNot.isAbsolute()) {
            throw new IllegalArgumentException("Base URI is null and given URI is not absolute");
        }
        if (base == null && relativeOrNot.isAbsolute()) {
            return relativeOrNot;
        }
        if (base.getPath().length() == 0) {
            try {
                base = new URI(base.getScheme(), base.getAuthority(), "/", base.getQuery(), base.getFragment());
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        return base.resolve(relativeOrNot);
    }

    public static URL createAbsoluteURL(URL base, String uri) throws IllegalArgumentException {
        return createAbsoluteURL(base, URI.create(uri));
    }

    public static URL createAbsoluteURL(URL base, URI relativeOrNot) throws IllegalArgumentException {
        if (base == null && !relativeOrNot.isAbsolute()) {
            throw new IllegalArgumentException("Base URL is null and given URI is not absolute");
        }
        if (base == null && relativeOrNot.isAbsolute()) {
            try {
                return relativeOrNot.toURL();
            } catch (Exception e) {
                throw new IllegalArgumentException("Base URL was null and given URI can't be converted to URL");
            }
        }
        try {
            URI baseURI = base.toURI();
            URI absoluteURI = createAbsoluteURI(baseURI, relativeOrNot);
            return absoluteURI.toURL();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Base URL is not an URI, or can't create absolute URI (null?), or absolute URI can not be converted to URL", ex);
        }
    }

    public static URL createAbsoluteURL(URI base, URI relativeOrNot) throws IllegalArgumentException {
        try {
            return createAbsoluteURI(base, relativeOrNot).toURL();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Absolute URI can not be converted to URL", ex);
        }
    }

    public static URL createAbsoluteURL(InetAddress address, int localStreamPort, URI relativeOrNot) throws IllegalArgumentException {
        try {
            if (address instanceof Inet6Address) {
                return createAbsoluteURL(new URL("http://[" + address.getHostAddress() + "]:" + localStreamPort), relativeOrNot);
            } else if (address instanceof Inet4Address) {
                return createAbsoluteURL(new URL("http://" + address.getHostAddress() + ":" + localStreamPort), relativeOrNot);
            } else {
                throw new IllegalArgumentException("InetAddress is neither IPv4 nor IPv6: " + address);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Address, port, and URI can not be converted to URL", ex);
        }
    }

    public static URI createRelativePathURI(URI uri) {
        assertRelativeURI("Given", uri);
        URI normalizedURI = uri.normalize();
        String uriString = normalizedURI.toString();
        while (true) {
            int idx = uriString.indexOf("../");
            if (idx == -1) {
                break;
            }
            uriString = uriString.substring(0, idx) + uriString.substring(idx + 3);
        }
        while (uriString.startsWith("/")) {
            uriString = uriString.substring(1);
        }
        return URI.create(uriString);
    }

    public static URI createRelativeURI(URI base, URI full) {
        return base.relativize(full);
    }

    public static URI createRelativeURI(URL base, URL full) throws IllegalArgumentException {
        try {
            return createRelativeURI(base.toURI(), full.toURI());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert base or full URL to URI", ex);
        }
    }

    public static URI createRelativeURI(URI base, URL full) throws IllegalArgumentException {
        try {
            return createRelativeURI(base, full.toURI());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert full URL to URI", ex);
        }
    }

    public static URI createRelativeURI(URL base, URI full) throws IllegalArgumentException {
        try {
            return createRelativeURI(base.toURI(), full);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert base URL to URI", ex);
        }
    }

    public static boolean isAbsoluteURI(String s) {
        URI uri = URI.create(s);
        return uri.isAbsolute();
    }

    public static void assertRelativeURI(String what, URI uri) {
        if (uri.isAbsolute()) {
            throw new IllegalArgumentException(what + " URI must be relative, without scheme and authority");
        }
    }

    public static URL toURL(URI uri) {
        if (uri == null) {
            return null;
        }
        try {
            return uri.toURL();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static URI toURI(URL url) {
        if (url == null) {
            return null;
        }
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String percentEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, StringUtil.__UTF8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String percentDecode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLDecoder.decode(s, StringUtil.__UTF8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String encodePathSegment(String pathSegment) {
        return encode(PATH_SEGMENT, pathSegment, StringUtil.__UTF8);
    }

    public static String encodePathParamName(String pathParamName) {
        return encode(PATH_PARAM_NAME, pathParamName, StringUtil.__UTF8);
    }

    public static String encodePathParamValue(String pathParamValue) {
        return encode(PATH_PARAM_VALUE, pathParamValue, StringUtil.__UTF8);
    }

    public static String encodeQueryNameOrValue(String queryNameOrValue) {
        return encode(QUERY, queryNameOrValue, StringUtil.__UTF8);
    }

    public static String encodeFragment(String fragment) {
        return encode(FRAGMENT, fragment, StringUtil.__UTF8);
    }

    public static String encode(BitSet allowedCharacters, String s, String charset) {
        if (s == null) {
            return null;
        }
        StringBuilder encoded = new StringBuilder(s.length() * 3);
        char[] characters = s.toCharArray();
        try {
            for (char c : characters) {
                if (allowedCharacters.get(c)) {
                    encoded.append(c);
                } else {
                    byte[] bytes = String.valueOf(c).getBytes(charset);
                    for (byte b : bytes) {
                        encoded.append(String.format("%%%1$02X", Integer.valueOf(b & 255)));
                    }
                }
            }
            return encoded.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
