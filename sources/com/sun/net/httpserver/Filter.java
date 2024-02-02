package com.sun.net.httpserver;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
/* loaded from: classes.dex */
public abstract class Filter {
    public abstract String description();

    public abstract void doFilter(HttpExchange httpExchange, Chain chain) throws IOException;

    /* loaded from: classes.dex */
    public static class Chain {
        private List<Filter> filters;
        private HttpHandler handler;
        private ListIterator<Filter> iter;

        public Chain(List<Filter> list, HttpHandler httpHandler) {
            this.filters = list;
            this.iter = list.listIterator();
            this.handler = httpHandler;
        }

        public void doFilter(HttpExchange httpExchange) throws IOException {
            if (!this.iter.hasNext()) {
                this.handler.handle(httpExchange);
            } else {
                this.iter.next().doFilter(httpExchange, this);
            }
        }
    }
}
