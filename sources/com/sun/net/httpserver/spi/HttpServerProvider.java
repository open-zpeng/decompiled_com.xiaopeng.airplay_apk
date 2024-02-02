package com.sun.net.httpserver.spi;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import sun.misc.Service;
import sun.misc.ServiceConfigurationError;
import sun.net.httpserver.DefaultHttpServerProvider;
/* loaded from: classes.dex */
public abstract class HttpServerProvider {
    private static final Object lock = new Object();
    private static HttpServerProvider provider = null;

    public abstract HttpServer createHttpServer(InetSocketAddress inetSocketAddress, int i) throws IOException;

    public abstract HttpsServer createHttpsServer(InetSocketAddress inetSocketAddress, int i) throws IOException;

    static /* synthetic */ boolean access$000() {
        return loadProviderFromProperty();
    }

    static /* synthetic */ boolean access$200() {
        return loadProviderAsService();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public HttpServerProvider() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("httpServerProvider"));
        }
    }

    private static boolean loadProviderFromProperty() {
        String property = System.getProperty("com.sun.net.httpserver.HttpServerProvider");
        if (property == null) {
            return false;
        }
        try {
            provider = (HttpServerProvider) Class.forName(property, true, ClassLoader.getSystemClassLoader()).newInstance();
            return true;
        } catch (ClassNotFoundException e) {
            throw new ServiceConfigurationError(e);
        } catch (IllegalAccessException e2) {
            throw new ServiceConfigurationError(e2);
        } catch (InstantiationException e3) {
            throw new ServiceConfigurationError(e3);
        } catch (SecurityException e4) {
            throw new ServiceConfigurationError(e4);
        }
    }

    private static boolean loadProviderAsService() {
        Iterator providers = Service.providers(HttpServerProvider.class, ClassLoader.getSystemClassLoader());
        do {
            try {
                if (!providers.hasNext()) {
                    return false;
                }
                provider = (HttpServerProvider) providers.next();
                return true;
            } catch (ServiceConfigurationError e) {
                if (!(e.getCause() instanceof SecurityException)) {
                    throw e;
                }
            }
        } while (!(e.getCause() instanceof SecurityException));
        throw e;
    }

    public static HttpServerProvider provider() {
        synchronized (lock) {
            if (provider != null) {
                return provider;
            }
            return (HttpServerProvider) AccessController.doPrivileged(new PrivilegedAction<Object>() { // from class: com.sun.net.httpserver.spi.HttpServerProvider.1
                @Override // java.security.PrivilegedAction
                public Object run() {
                    if (!HttpServerProvider.access$000() && !HttpServerProvider.access$200()) {
                        HttpServerProvider unused = HttpServerProvider.provider = new DefaultHttpServerProvider();
                        return HttpServerProvider.provider;
                    }
                    return HttpServerProvider.provider;
                }
            });
        }
    }
}
