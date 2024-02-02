package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.PathMap;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class ContextHandlerCollection extends HandlerCollection {
    private static final Logger LOG = Log.getLogger(ContextHandlerCollection.class);
    private Class<? extends ContextHandler> _contextClass;
    private volatile PathMap _contextMap;

    public ContextHandlerCollection() {
        super(true);
        this._contextClass = ContextHandler.class;
    }

    public void mapContexts() {
        Handler[] handlers;
        Map hosts;
        PathMap contextMap = new PathMap();
        Handler[] branches = getHandlers();
        for (int b = 0; branches != null && b < branches.length; b++) {
            if (branches[b] instanceof ContextHandler) {
                handlers = new Handler[]{branches[b]};
            } else if (!(branches[b] instanceof HandlerContainer)) {
                continue;
            } else {
                handlers = ((HandlerContainer) branches[b]).getChildHandlersByClass(ContextHandler.class);
            }
            for (Handler handler : handlers) {
                ContextHandler handler2 = (ContextHandler) handler;
                String contextPath = handler2.getContextPath();
                if (contextPath == null || contextPath.indexOf(44) >= 0 || contextPath.startsWith("*")) {
                    throw new IllegalArgumentException("Illegal context spec:" + contextPath);
                }
                if (!contextPath.startsWith("/")) {
                    contextPath = '/' + contextPath;
                }
                if (contextPath.length() > 1) {
                    if (contextPath.endsWith("/")) {
                        contextPath = contextPath + "*";
                    } else if (!contextPath.endsWith("/*")) {
                        contextPath = contextPath + "/*";
                    }
                }
                Object contexts = contextMap.get(contextPath);
                String[] vhosts = handler2.getVirtualHosts();
                if (vhosts != null && vhosts.length > 0) {
                    if (contexts instanceof Map) {
                        hosts = (Map) contexts;
                    } else {
                        hosts = new HashMap();
                        hosts.put("*", contexts);
                        contextMap.put(contextPath, hosts);
                    }
                    for (String vhost : vhosts) {
                        hosts.put(vhost, LazyList.add(hosts.get(vhost), branches[b]));
                    }
                } else if (contexts instanceof Map) {
                    Map hosts2 = (Map) contexts;
                    hosts2.put("*", LazyList.add(hosts2.get("*"), branches[b]));
                } else {
                    contextMap.put(contextPath, LazyList.add(contexts, branches[b]));
                }
            }
            continue;
        }
        this._contextMap = contextMap;
    }

    @Override // org.eclipse.jetty.server.handler.HandlerCollection
    public void setHandlers(Handler[] handlers) {
        this._contextMap = null;
        super.setHandlers(handlers);
        if (isStarted()) {
            mapContexts();
        }
    }

    @Override // org.eclipse.jetty.server.handler.HandlerCollection, org.eclipse.jetty.server.handler.AbstractHandler, org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.AbstractLifeCycle
    protected void doStart() throws Exception {
        mapContexts();
        super.doStart();
    }

    @Override // org.eclipse.jetty.server.handler.HandlerCollection, org.eclipse.jetty.server.Handler
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ContextHandler context;
        Handler[] handlers = getHandlers();
        if (handlers == null || handlers.length == 0) {
            return;
        }
        AsyncContinuation async = baseRequest.getAsyncContinuation();
        if (async.isAsync() && (context = async.getContextHandler()) != null) {
            context.handle(target, baseRequest, request, response);
            return;
        }
        PathMap map = this._contextMap;
        if (map != null && target != null && target.startsWith("/")) {
            Object contexts = map.getLazyMatches(target);
            for (int i = 0; i < LazyList.size(contexts); i++) {
                Map.Entry entry = (Map.Entry) LazyList.get(contexts, i);
                Object list = entry.getValue();
                if (list instanceof Map) {
                    Map hosts = (Map) list;
                    String host = normalizeHostname(request.getServerName());
                    Object list2 = hosts.get(host);
                    for (int j = 0; j < LazyList.size(list2); j++) {
                        Handler handler = (Handler) LazyList.get(list2, j);
                        handler.handle(target, baseRequest, request, response);
                        if (baseRequest.isHandled()) {
                            return;
                        }
                    }
                    Object list3 = hosts.get("*." + host.substring(host.indexOf(".") + 1));
                    for (int j2 = 0; j2 < LazyList.size(list3); j2++) {
                        Handler handler2 = (Handler) LazyList.get(list3, j2);
                        handler2.handle(target, baseRequest, request, response);
                        if (baseRequest.isHandled()) {
                            return;
                        }
                    }
                    Object list4 = hosts.get("*");
                    for (int j3 = 0; j3 < LazyList.size(list4); j3++) {
                        Handler handler3 = (Handler) LazyList.get(list4, j3);
                        handler3.handle(target, baseRequest, request, response);
                        if (baseRequest.isHandled()) {
                            return;
                        }
                    }
                    continue;
                } else {
                    for (int j4 = 0; j4 < LazyList.size(list); j4++) {
                        Handler handler4 = (Handler) LazyList.get(list, j4);
                        handler4.handle(target, baseRequest, request, response);
                        if (baseRequest.isHandled()) {
                            return;
                        }
                    }
                    continue;
                }
            }
            return;
        }
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < handlers.length) {
                handlers[i3].handle(target, baseRequest, request, response);
                if (!baseRequest.isHandled()) {
                    i2 = i3 + 1;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    public ContextHandler addContext(String contextPath, String resourceBase) {
        try {
            ContextHandler context = this._contextClass.newInstance();
            context.setContextPath(contextPath);
            context.setResourceBase(resourceBase);
            addHandler(context);
            return context;
        } catch (Exception e) {
            LOG.debug(e);
            throw new Error(e);
        }
    }

    public Class getContextClass() {
        return this._contextClass;
    }

    public void setContextClass(Class contextClass) {
        if (contextClass == null || !ContextHandler.class.isAssignableFrom(contextClass)) {
            throw new IllegalArgumentException();
        }
        this._contextClass = contextClass;
    }

    private String normalizeHostname(String host) {
        if (host == null) {
            return null;
        }
        if (host.endsWith(".")) {
            return host.substring(0, host.length() - 1);
        }
        return host;
    }
}
