package org.seamless.http;

import com.xpeng.airplay.service.NsdConstants;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.http.HttpHeaders;
/* loaded from: classes.dex */
public class RequestInfo {
    private static final Logger log = Logger.getLogger(RequestInfo.class.getName());

    public static void reportRequest(StringBuilder builder, HttpServletRequest req) {
        builder.append("Request: ");
        builder.append(req.getMethod());
        builder.append(' ');
        builder.append(req.getRequestURL());
        String queryString = req.getQueryString();
        if (queryString != null) {
            builder.append('?');
            builder.append(queryString);
        }
        builder.append(" - ");
        String sessionId = req.getRequestedSessionId();
        if (sessionId != null) {
            builder.append("\nSession ID: ");
        }
        if (sessionId == null) {
            builder.append("No Session");
        } else if (req.isRequestedSessionIdValid()) {
            builder.append(sessionId);
            builder.append(" (from ");
            if (req.isRequestedSessionIdFromCookie()) {
                builder.append("cookie)\n");
            } else if (req.isRequestedSessionIdFromURL()) {
                builder.append("url)\n");
            } else {
                builder.append("unknown)\n");
            }
        } else {
            builder.append("Invalid Session ID\n");
        }
    }

    public static void reportParameters(StringBuilder builder, HttpServletRequest req) {
        Enumeration names = req.getParameterNames();
        if (names != null && names.hasMoreElements()) {
            builder.append("Parameters:\n");
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String[] values = req.getParameterValues(name);
                if (values != null) {
                    for (String value : values) {
                        builder.append("    ");
                        builder.append(name);
                        builder.append(" = ");
                        builder.append(value);
                        builder.append('\n');
                    }
                }
            }
        }
    }

    public static void reportHeaders(StringBuilder builder, HttpServletRequest req) {
        Enumeration names = req.getHeaderNames();
        if (names != null && names.hasMoreElements()) {
            builder.append("Headers:\n");
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String value = req.getHeader(name);
                builder.append("    ");
                builder.append(name);
                builder.append(": ");
                builder.append(value);
                builder.append('\n');
            }
        }
    }

    public static void reportCookies(StringBuilder builder, HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null && (l = cookies.length) > 0) {
            builder.append("Cookies:\n");
            for (Cookie cookie : cookies) {
                builder.append("    ");
                builder.append(cookie.getName());
                builder.append(" = ");
                builder.append(cookie.getValue());
                builder.append('\n');
            }
        }
    }

    public static void reportClient(StringBuilder builder, HttpServletRequest req) {
        builder.append("Remote Address: ");
        builder.append(req.getRemoteAddr());
        builder.append("\n");
        if (!req.getRemoteAddr().equals(req.getRemoteHost())) {
            builder.append("Remote Host: ");
            builder.append(req.getRemoteHost());
            builder.append("\n");
        }
        builder.append("Remote Port: ");
        builder.append(req.getRemotePort());
        builder.append("\n");
        if (req.getRemoteUser() != null) {
            builder.append("Remote User: ");
            builder.append(req.getRemoteUser());
            builder.append("\n");
        }
    }

    public static boolean isPS3Request(String userAgent, String avClientInfo) {
        return (userAgent != null && userAgent.contains("PLAYSTATION 3")) || (avClientInfo != null && avClientInfo.contains("PLAYSTATION 3"));
    }

    public static boolean isAndroidBubbleUPnPRequest(String userAgent) {
        return userAgent != null && userAgent.contains("BubbleUPnP");
    }

    public static boolean isPS3Request(HttpServletRequest request) {
        return isPS3Request(request.getHeader(HttpHeaders.USER_AGENT), request.getHeader("X-AV-Client-Info"));
    }

    public static boolean isJRiverRequest(HttpServletRequest request) {
        return isJRiverRequest(request.getHeader(HttpHeaders.USER_AGENT));
    }

    public static boolean isJRiverRequest(String userAgent) {
        return userAgent != null && (userAgent.contains("J-River") || userAgent.contains("J. River"));
    }

    public static boolean isWMPRequest(String userAgent) {
        return (userAgent == null || !userAgent.contains("Windows-Media-Player") || isJRiverRequest(userAgent)) ? false : true;
    }

    public static boolean isXbox360Request(HttpServletRequest request) {
        return isXbox360Request(request.getHeader(HttpHeaders.USER_AGENT), request.getHeader(HttpHeaders.SERVER));
    }

    public static boolean isXbox360Request(String userAgent, String server) {
        return (userAgent != null && (userAgent.contains("Xbox") || userAgent.contains("Xenon"))) || (server != null && server.contains("Xbox"));
    }

    public static boolean isXbox360AlbumArtRequest(HttpServletRequest request) {
        return NsdConstants.AIRPLAY_TXT_VALUE_DA.equals(request.getParameter("albumArt")) && isXbox360Request(request);
    }

    public static void dumpRequestHeaders(long timestamp, HttpServletRequest request) {
        dumpRequestHeaders(timestamp, "REQUEST HEADERS", request);
    }

    public static void dumpRequestString(long timestamp, HttpServletRequest request) {
        log.info(getRequestInfoString(timestamp, request));
    }

    public static void dumpRequestHeaders(long timestamp, String text, HttpServletRequest request) {
        log.info(text);
        dumpRequestString(timestamp, request);
        Enumeration headers = request.getHeaderNames();
        if (headers != null) {
            while (headers.hasMoreElements()) {
                String headerName = headers.nextElement();
                log.info(String.format("%s: %s", headerName, request.getHeader(headerName)));
            }
        }
        log.info("----------------------------------------");
    }

    public static String getRequestInfoString(long timestamp, HttpServletRequest request) {
        return String.format("%s %s %s %s %s %d", request.getMethod(), request.getRequestURI(), request.getProtocol(), request.getParameterMap(), request.getRemoteAddr(), Long.valueOf(timestamp));
    }

    public static String getRequestFullURL(HttpServletRequest req) {
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        String contextPath = req.getContextPath();
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String queryString = req.getQueryString();
        StringBuffer url = new StringBuffer();
        url.append(scheme);
        url.append("://");
        url.append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            url.append(":");
            url.append(serverPort);
        }
        url.append(contextPath);
        url.append(servletPath);
        if (pathInfo != null) {
            url.append(pathInfo);
        }
        if (queryString != null) {
            url.append("?");
            url.append(queryString);
        }
        return url.toString();
    }
}
