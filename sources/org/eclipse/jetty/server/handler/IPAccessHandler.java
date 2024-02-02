package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.PathMap;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.IPAddressMap;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class IPAccessHandler extends HandlerWrapper {
    private static final Logger LOG = Log.getLogger(IPAccessHandler.class);
    IPAddressMap<PathMap> _white = new IPAddressMap<>();
    IPAddressMap<PathMap> _black = new IPAddressMap<>();

    public IPAccessHandler() {
    }

    public IPAccessHandler(String[] white, String[] black) {
        if (white != null && white.length > 0) {
            setWhite(white);
        }
        if (black != null && black.length > 0) {
            setBlack(black);
        }
    }

    public void addWhite(String entry) {
        add(entry, this._white);
    }

    public void addBlack(String entry) {
        add(entry, this._black);
    }

    public void setWhite(String[] entries) {
        set(entries, this._white);
    }

    public void setBlack(String[] entries) {
        set(entries, this._black);
    }

    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        EndPoint endp;
        String addr;
        AbstractHttpConnection connection = baseRequest.getConnection();
        if (connection != null && (endp = connection.getEndPoint()) != null && (addr = endp.getRemoteAddr()) != null && !isAddrUriAllowed(addr, baseRequest.getPathInfo())) {
            response.sendError(403);
            baseRequest.setHandled(true);
            return;
        }
        getHandler().handle(target, baseRequest, request, response);
    }

    protected void add(String entry, IPAddressMap<PathMap> patternMap) {
        int idx;
        if (entry != null && entry.length() > 0) {
            boolean deprecated = false;
            if (entry.indexOf(124) > 0) {
                idx = entry.indexOf(124);
            } else {
                idx = entry.indexOf(47);
                deprecated = idx >= 0;
            }
            String addr = idx > 0 ? entry.substring(0, idx) : entry;
            String path = idx > 0 ? entry.substring(idx) : "/*";
            if (addr.endsWith(".")) {
                deprecated = true;
            }
            if (path != null && (path.startsWith("|") || path.startsWith("/*."))) {
                path = path.substring(1);
            }
            PathMap pathMap = patternMap.get(addr);
            if (pathMap == null) {
                pathMap = new PathMap(true);
                patternMap.put(addr, (String) pathMap);
            }
            if (path != null && !"".equals(path)) {
                pathMap.put(path, path);
            }
            if (deprecated) {
                Logger logger = LOG;
                logger.debug(toString() + " - deprecated specification syntax: " + entry, new Object[0]);
            }
        }
    }

    protected void set(String[] entries, IPAddressMap<PathMap> patternMap) {
        patternMap.clear();
        if (entries != null && entries.length > 0) {
            for (String addrPath : entries) {
                add(addrPath, patternMap);
            }
        }
    }

    protected boolean isAddrUriAllowed(String addr, String path) {
        Object blackObj;
        if (this._white.size() > 0) {
            boolean match = false;
            Object whiteObj = this._white.getLazyMatches(addr);
            if (whiteObj != null) {
                List whiteList = whiteObj instanceof List ? (List) whiteObj : Collections.singletonList(whiteObj);
                for (Object entry : whiteList) {
                    PathMap pathMap = (PathMap) ((Map.Entry) entry).getValue();
                    boolean z = pathMap != null && (pathMap.size() == 0 || pathMap.match(path) != null);
                    match = z;
                    if (z) {
                        break;
                    }
                }
            }
            if (!match) {
                return false;
            }
        }
        if (this._black.size() > 0 && (blackObj = this._black.getLazyMatches(addr)) != null) {
            List blackList = blackObj instanceof List ? (List) blackObj : Collections.singletonList(blackObj);
            for (Object entry2 : blackList) {
                PathMap pathMap2 = (PathMap) ((Map.Entry) entry2).getValue();
                if (pathMap2 != null && (pathMap2.size() == 0 || pathMap2.match(path) != null)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.server.handler.HandlerWrapper, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        super.doStart();
        if (LOG.isDebugEnabled()) {
            System.err.println(dump());
        }
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public String dump() {
        StringBuilder buf = new StringBuilder();
        buf.append(toString());
        buf.append(" WHITELIST:\n");
        dump(buf, this._white);
        buf.append(toString());
        buf.append(" BLACKLIST:\n");
        dump(buf, this._black);
        return buf.toString();
    }

    protected void dump(StringBuilder buf, IPAddressMap<PathMap> patternMap) {
        for (String addr : patternMap.keySet()) {
            for (Object path : patternMap.get(addr).values()) {
                buf.append("# ");
                buf.append(addr);
                buf.append("|");
                buf.append(path);
                buf.append("\n");
            }
        }
    }
}
