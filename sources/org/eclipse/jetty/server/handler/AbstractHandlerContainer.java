package org.eclipse.jetty.server.handler;

import java.io.IOException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.TypeUtil;
/* loaded from: classes.dex */
public abstract class AbstractHandlerContainer extends AbstractHandler implements HandlerContainer {
    @Override // org.eclipse.jetty.server.HandlerContainer
    public Handler[] getChildHandlers() {
        Object list = expandChildren(null, null);
        return (Handler[]) LazyList.toArray(list, Handler.class);
    }

    @Override // org.eclipse.jetty.server.HandlerContainer
    public Handler[] getChildHandlersByClass(Class<?> byclass) {
        Object list = expandChildren(null, byclass);
        return (Handler[]) LazyList.toArray(list, byclass);
    }

    @Override // org.eclipse.jetty.server.HandlerContainer
    public <T extends Handler> T getChildHandlerByClass(Class<T> byclass) {
        Object list = expandChildren(null, byclass);
        if (list == null) {
            return null;
        }
        return (T) LazyList.get(list, 0);
    }

    protected Object expandChildren(Object list, Class<?> byClass) {
        return list;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Object expandHandler(Handler handler, Object list, Class<Handler> byClass) {
        if (handler == null) {
            return list;
        }
        if (byClass == null || byClass.isAssignableFrom(handler.getClass())) {
            list = LazyList.add(list, handler);
        }
        if (handler instanceof AbstractHandlerContainer) {
            return ((AbstractHandlerContainer) handler).expandChildren(list, byClass);
        }
        if (handler instanceof HandlerContainer) {
            HandlerContainer container = (HandlerContainer) handler;
            Handler[] handlers = byClass == null ? container.getChildHandlers() : container.getChildHandlersByClass(byClass);
            return LazyList.addArray(list, handlers);
        }
        return list;
    }

    public static <T extends HandlerContainer> T findContainerOf(HandlerContainer root, Class<T> type, Handler handler) {
        Handler[] branches;
        if (root != null && handler != null && (branches = root.getChildHandlersByClass(type)) != null) {
            for (Handler h : branches) {
                T container = (T) h;
                Handler[] candidates = container.getChildHandlersByClass(handler.getClass());
                if (candidates != null) {
                    for (Handler c : candidates) {
                        if (c == handler) {
                            return container;
                        }
                    }
                    continue;
                }
            }
        }
        return null;
    }

    @Override // org.eclipse.jetty.util.component.AggregateLifeCycle, org.eclipse.jetty.util.component.Dumpable
    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        dump(out, indent, getBeans(), TypeUtil.asList(getHandlers()));
    }
}
