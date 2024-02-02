package com.sun.net.httpserver;

import java.io.IOException;
/* loaded from: classes.dex */
public interface HttpHandler {
    void handle(HttpExchange httpExchange) throws IOException;
}
