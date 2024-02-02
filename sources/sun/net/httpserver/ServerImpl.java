package sun.net.httpserver;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import sun.net.httpserver.Request;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ServerImpl implements TimeSource {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int CLOCK_TICK = ServerConfig.getClockTick();
    static final long IDLE_INTERVAL = ServerConfig.getIdleInterval();
    static final int MAX_IDLE_CONNECTIONS = ServerConfig.getMaxIdleConnections();
    static boolean debug = ServerConfig.debugEnabled();
    private InetSocketAddress address;
    private Set<HttpConnection> allConnections;
    private boolean bound;
    Dispatcher dispatcher;
    private List<Event> events;
    private Executor executor;
    private boolean https;
    private HttpsConfigurator httpsConfig;
    private Set<HttpConnection> idleConnections;
    private SelectionKey listenerKey;
    private String protocol;
    private Selector selector;
    private SSLContext sslContext;
    private volatile long ticks;
    private volatile long time;
    private Timer timer;
    private HttpServer wrapper;
    private Object lolock = new Object();
    private volatile boolean finished = false;
    private volatile boolean terminating = false;
    private boolean started = false;
    private int exchangeCount = 0;
    private Logger logger = Logger.getLogger("com.sun.net.httpserver");
    private ContextList contexts = new ContextList();
    private ServerSocketChannel schan = ServerSocketChannel.open();

    static /* synthetic */ long access$1708(ServerImpl serverImpl) {
        long j = serverImpl.ticks;
        serverImpl.ticks = 1 + j;
        return j;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ServerImpl(HttpServer httpServer, String str, InetSocketAddress inetSocketAddress, int i) throws IOException {
        this.bound = false;
        this.protocol = str;
        this.wrapper = httpServer;
        this.https = str.equalsIgnoreCase("https");
        this.address = inetSocketAddress;
        if (inetSocketAddress != null) {
            this.schan.socket().bind(inetSocketAddress, i);
            this.bound = true;
        }
        this.selector = Selector.open();
        this.schan.configureBlocking(false);
        this.listenerKey = this.schan.register(this.selector, 16);
        this.dispatcher = new Dispatcher();
        this.idleConnections = Collections.synchronizedSet(new HashSet());
        this.allConnections = Collections.synchronizedSet(new HashSet());
        this.time = System.currentTimeMillis();
        this.timer = new Timer("server-timer", true);
        this.timer.schedule(new ServerTimerTask(), CLOCK_TICK, CLOCK_TICK);
        this.events = new LinkedList();
        Logger logger = this.logger;
        logger.config("HttpServer created " + str + " " + inetSocketAddress);
    }

    public void bind(InetSocketAddress inetSocketAddress, int i) throws IOException {
        if (this.bound) {
            throw new BindException("HttpServer already bound");
        }
        if (inetSocketAddress == null) {
            throw new NullPointerException("null address");
        }
        this.schan.socket().bind(inetSocketAddress, i);
        this.bound = true;
    }

    public void start() {
        if (!this.bound || this.started || this.finished) {
            throw new IllegalStateException("server in wrong state");
        }
        if (this.executor == null) {
            this.executor = new DefaultExecutor();
        }
        Thread thread = new Thread(this.dispatcher);
        this.started = true;
        thread.start();
    }

    public void setExecutor(Executor executor) {
        if (this.started) {
            throw new IllegalStateException("server already started");
        }
        this.executor = executor;
    }

    /* loaded from: classes.dex */
    private static class DefaultExecutor implements Executor {
        private DefaultExecutor() {
        }

        @Override // java.util.concurrent.Executor
        public void execute(Runnable runnable) {
            runnable.run();
        }
    }

    public Executor getExecutor() {
        return this.executor;
    }

    public void setHttpsConfigurator(HttpsConfigurator httpsConfigurator) {
        if (httpsConfigurator == null) {
            throw new NullPointerException("null HttpsConfigurator");
        }
        if (this.started) {
            throw new IllegalStateException("server already started");
        }
        this.httpsConfig = httpsConfigurator;
        this.sslContext = httpsConfigurator.getSSLContext();
    }

    public HttpsConfigurator getHttpsConfigurator() {
        return this.httpsConfig;
    }

    public void stop(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("negative delay parameter");
        }
        this.terminating = true;
        try {
            this.schan.close();
        } catch (IOException e) {
        }
        this.selector.wakeup();
        long currentTimeMillis = System.currentTimeMillis() + (i * 1000);
        while (System.currentTimeMillis() < currentTimeMillis) {
            delay();
            if (this.finished) {
                break;
            }
        }
        this.finished = true;
        this.selector.wakeup();
        synchronized (this.allConnections) {
            for (HttpConnection httpConnection : this.allConnections) {
                httpConnection.close();
            }
        }
        this.allConnections.clear();
        this.idleConnections.clear();
        this.timer.cancel();
    }

    public synchronized HttpContextImpl createContext(String str, HttpHandler httpHandler) {
        HttpContextImpl httpContextImpl;
        if (httpHandler == null || str == null) {
            throw new NullPointerException("null handler, or path parameter");
        }
        httpContextImpl = new HttpContextImpl(this.protocol, str, httpHandler, this);
        this.contexts.add(httpContextImpl);
        Logger logger = this.logger;
        logger.config("context created: " + str);
        return httpContextImpl;
    }

    public synchronized HttpContextImpl createContext(String str) {
        HttpContextImpl httpContextImpl;
        if (str == null) {
            throw new NullPointerException("null path parameter");
        }
        httpContextImpl = new HttpContextImpl(this.protocol, str, null, this);
        this.contexts.add(httpContextImpl);
        Logger logger = this.logger;
        logger.config("context created: " + str);
        return httpContextImpl;
    }

    public synchronized void removeContext(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new NullPointerException("null path parameter");
        }
        this.contexts.remove(this.protocol, str);
        Logger logger = this.logger;
        logger.config("context removed: " + str);
    }

    public synchronized void removeContext(HttpContext httpContext) throws IllegalArgumentException {
        if (!(httpContext instanceof HttpContextImpl)) {
            throw new IllegalArgumentException("wrong HttpContext type");
        }
        this.contexts.remove((HttpContextImpl) httpContext);
        Logger logger = this.logger;
        logger.config("context removed: " + httpContext.getPath());
    }

    public InetSocketAddress getAddress() {
        return (InetSocketAddress) this.schan.socket().getLocalSocketAddress();
    }

    Selector getSelector() {
        return this.selector;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addEvent(Event event) {
        synchronized (this.lolock) {
            this.events.add(event);
            this.selector.wakeup();
        }
    }

    int resultSize() {
        int size;
        synchronized (this.lolock) {
            size = this.events.size();
        }
        return size;
    }

    /* loaded from: classes.dex */
    class Dispatcher implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        Dispatcher() {
        }

        private void handleEvent(Event event) {
            ExchangeImpl exchangeImpl = event.exchange;
            HttpConnection connection = exchangeImpl.getConnection();
            try {
                if (event instanceof WriteFinishedEvent) {
                    int endExchange = ServerImpl.this.endExchange();
                    if (ServerImpl.this.terminating && endExchange == 0) {
                        ServerImpl.this.finished = true;
                    }
                    connection.getChannel();
                    LeftOverInputStream originalInputStream = exchangeImpl.getOriginalInputStream();
                    if (!originalInputStream.isEOF()) {
                        exchangeImpl.close = true;
                    }
                    if (!exchangeImpl.close && ServerImpl.this.idleConnections.size() < ServerImpl.MAX_IDLE_CONNECTIONS) {
                        if (originalInputStream.isDataBuffered()) {
                            handle(connection.getChannel(), connection);
                            return;
                        }
                        SelectionKey selectionKey = connection.getSelectionKey();
                        if (selectionKey.isValid()) {
                            selectionKey.interestOps(selectionKey.interestOps() | 1);
                        }
                        connection.time = ServerImpl.this.getTime() + ServerImpl.IDLE_INTERVAL;
                        ServerImpl.this.idleConnections.add(connection);
                        return;
                    }
                    connection.close();
                    ServerImpl.this.allConnections.remove(connection);
                }
            } catch (IOException e) {
                ServerImpl.this.logger.log(Level.FINER, "Dispatcher (1)", (Throwable) e);
                connection.close();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                run1();
            } catch (Exception e) {
                ServerImpl.this.logger.log(Level.FINE, "Dispatcher (7)", (Throwable) e);
            }
        }

        public void run1() {
            SocketChannel accept;
            while (!ServerImpl.this.finished) {
                while (ServerImpl.this.resultSize() > 0) {
                    try {
                        try {
                            synchronized (ServerImpl.this.lolock) {
                                handleEvent((Event) ServerImpl.this.events.remove(0));
                            }
                        } catch (CancelledKeyException e) {
                            ServerImpl.this.logger.log(Level.FINER, "Dispatcher (3)", (Throwable) e);
                        }
                    } catch (IOException e2) {
                        ServerImpl.this.logger.log(Level.FINER, "Dispatcher (4)", (Throwable) e2);
                    }
                }
                ServerImpl.this.selector.select(1000L);
                Iterator<SelectionKey> it = ServerImpl.this.selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey next = it.next();
                    it.remove();
                    if (next.equals(ServerImpl.this.listenerKey)) {
                        if (!ServerImpl.this.terminating && (accept = ServerImpl.this.schan.accept()) != null) {
                            accept.configureBlocking(false);
                            SelectionKey register = accept.register(ServerImpl.this.selector, 1);
                            HttpConnection httpConnection = new HttpConnection();
                            httpConnection.selectionKey = register;
                            httpConnection.setChannel(accept);
                            register.attach(httpConnection);
                            ServerImpl.this.allConnections.add(httpConnection);
                        }
                    } else {
                        try {
                            if (next.isReadable()) {
                                next.interestOps(0);
                                handle((SocketChannel) next.channel(), (HttpConnection) next.attachment());
                            }
                        } catch (IOException e3) {
                            ServerImpl.this.logger.log(Level.FINER, "Dispatcher (2)", (Throwable) e3);
                            ((HttpConnection) next.attachment()).close();
                        }
                    }
                }
            }
        }

        public void handle(SocketChannel socketChannel, HttpConnection httpConnection) throws IOException {
            try {
                ServerImpl.this.executor.execute(new Exchange(socketChannel, ServerImpl.this.protocol, httpConnection));
            } catch (IOException e) {
                ServerImpl.this.logger.log(Level.FINER, "Dispatcher (6)", (Throwable) e);
                httpConnection.close();
            } catch (HttpError e2) {
                ServerImpl.this.logger.log(Level.FINER, "Dispatcher (5)", (Throwable) e2);
                httpConnection.close();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static synchronized void dprint(String str) {
        synchronized (ServerImpl.class) {
            if (debug) {
                System.out.println(str);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static synchronized void dprint(Exception exc) {
        synchronized (ServerImpl.class) {
            if (debug) {
                System.out.println(exc);
                exc.printStackTrace();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Logger getLogger() {
        return this.logger;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class Exchange implements Runnable {
        SocketChannel chan;
        HttpConnection connection;
        HttpContextImpl context;
        HttpContextImpl ctx;
        String protocol;
        InputStream rawin;
        OutputStream rawout;
        boolean rejected = false;
        ExchangeImpl tx;

        Exchange(SocketChannel socketChannel, String str, HttpConnection httpConnection) throws IOException {
            this.chan = socketChannel;
            this.connection = httpConnection;
            this.protocol = str;
        }

        @Override // java.lang.Runnable
        public void run() {
            String str;
            SSLEngine sSLEngine;
            SSLStreams sSLStreams;
            boolean z;
            int parseInt;
            this.context = this.connection.getHttpContext();
            int i = 400;
            try {
                try {
                    if (this.context == null) {
                        if (ServerImpl.this.https) {
                            if (ServerImpl.this.sslContext == null) {
                                ServerImpl.this.logger.warning("SSL connection received. No https contxt created");
                                throw new HttpError("No SSL context established");
                            }
                            SSLStreams sSLStreams2 = new SSLStreams(ServerImpl.this, ServerImpl.this.sslContext, this.chan);
                            this.rawin = sSLStreams2.getInputStream();
                            this.rawout = sSLStreams2.getOutputStream();
                            SSLEngine sSLEngine2 = sSLStreams2.getSSLEngine();
                            sSLStreams = sSLStreams2;
                            z = true;
                            sSLEngine = sSLEngine2;
                        } else {
                            this.rawin = new BufferedInputStream(new Request.ReadStream(ServerImpl.this, this.chan));
                            this.rawout = new Request.WriteStream(ServerImpl.this, this.chan);
                            sSLEngine = null;
                            sSLStreams = null;
                            z = true;
                        }
                    } else {
                        this.rawin = this.connection.getInputStream();
                        this.rawout = this.connection.getRawOutputStream();
                        sSLEngine = null;
                        sSLStreams = null;
                        z = false;
                    }
                    Request request = new Request(this.rawin, this.rawout);
                    String requestLine = request.requestLine();
                    try {
                        try {
                            if (requestLine == null) {
                                this.connection.close();
                                return;
                            }
                            int indexOf = requestLine.indexOf(32);
                            if (indexOf == -1) {
                                reject(400, requestLine, "Bad request line");
                                return;
                            }
                            String substring = requestLine.substring(0, indexOf);
                            int i2 = indexOf + 1;
                            int indexOf2 = requestLine.indexOf(32, i2);
                            if (indexOf2 == -1) {
                                reject(400, requestLine, "Bad request line");
                                return;
                            }
                            URI uri = new URI(requestLine.substring(i2, indexOf2));
                            String substring2 = requestLine.substring(indexOf2 + 1);
                            Headers headers = request.headers();
                            String first = headers.getFirst("Transfer-encoding");
                            if (first == null || !first.equalsIgnoreCase(HttpHeaderValues.CHUNKED)) {
                                String first2 = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
                                parseInt = first2 != null ? Integer.parseInt(first2) : 0;
                            } else {
                                parseInt = -1;
                            }
                            try {
                                this.ctx = ServerImpl.this.contexts.findContext(this.protocol, uri.getPath());
                                if (this.ctx == null) {
                                    reject(404, requestLine, "No context found for request");
                                    return;
                                }
                                this.connection.setContext(this.ctx);
                                if (this.ctx.getHandler() == null) {
                                    reject(500, requestLine, "No handler for context");
                                    return;
                                }
                                this.tx = new ExchangeImpl(substring, uri, request, parseInt, this.connection);
                                String first3 = headers.getFirst(HttpHeaders.CONNECTION);
                                Headers responseHeaders = this.tx.getResponseHeaders();
                                if (first3 != null && first3.equalsIgnoreCase(HttpHeaderValues.CLOSE)) {
                                    this.tx.close = true;
                                }
                                if (substring2.equalsIgnoreCase("http/1.0")) {
                                    this.tx.http10 = true;
                                    if (first3 == null) {
                                        this.tx.close = true;
                                        responseHeaders.set(HttpHeaders.CONNECTION, HttpHeaderValues.CLOSE);
                                    } else if (first3.equalsIgnoreCase(HttpHeaderValues.KEEP_ALIVE)) {
                                        responseHeaders.set(HttpHeaders.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                        responseHeaders.set(HttpHeaders.KEEP_ALIVE, "timeout=" + (((int) ServerConfig.getIdleInterval()) / 1000) + ", max=" + ServerConfig.getMaxIdleConnections());
                                    }
                                }
                                if (z) {
                                    str = requestLine;
                                    try {
                                        this.connection.setParameters(this.rawin, this.rawout, this.chan, sSLEngine, sSLStreams, ServerImpl.this.sslContext, this.protocol, this.ctx, this.rawin);
                                    } catch (NumberFormatException e) {
                                        i = 400;
                                        reject(i, str, "NumberFormatException thrown");
                                        return;
                                    } catch (URISyntaxException e2) {
                                        reject(400, str, "URISyntaxException thrown");
                                        return;
                                    }
                                } else {
                                    str = requestLine;
                                }
                                String first4 = headers.getFirst(HttpHeaders.EXPECT);
                                if (first4 != null && first4.equalsIgnoreCase(HttpHeaderValues.CONTINUE)) {
                                    ServerImpl.this.logReply(100, str, null);
                                    sendReply(100, false, null);
                                }
                                Filter.Chain chain = new Filter.Chain(this.ctx.getFilters(), new LinkHandler(new Filter.Chain(this.ctx.getSystemFilters(), this.ctx.getHandler())));
                                this.tx.getRequestBody();
                                this.tx.getResponseBody();
                                if (ServerImpl.this.https) {
                                    chain.doFilter(new HttpsExchangeImpl(this.tx));
                                } else {
                                    chain.doFilter(new HttpExchangeImpl(this.tx));
                                }
                            } catch (NumberFormatException e3) {
                                str = requestLine;
                            }
                        } catch (URISyntaxException e4) {
                            str = requestLine;
                        }
                    } catch (NumberFormatException e5) {
                        str = requestLine;
                    }
                } catch (NumberFormatException e6) {
                    str = null;
                } catch (URISyntaxException e7) {
                    str = null;
                }
            } catch (IOException e8) {
                ServerImpl.this.logger.log(Level.FINER, "ServerImpl.Exchange (1)", (Throwable) e8);
                this.connection.close();
            } catch (Exception e9) {
                ServerImpl.this.logger.log(Level.FINER, "ServerImpl.Exchange (2)", (Throwable) e9);
                this.connection.close();
            }
        }

        /* loaded from: classes.dex */
        class LinkHandler implements HttpHandler {
            Filter.Chain nextChain;

            LinkHandler(Filter.Chain chain) {
                this.nextChain = chain;
            }

            @Override // com.sun.net.httpserver.HttpHandler
            public void handle(HttpExchange httpExchange) throws IOException {
                this.nextChain.doFilter(httpExchange);
            }
        }

        void reject(int i, String str, String str2) {
            this.rejected = true;
            ServerImpl.this.logReply(i, str, str2);
            sendReply(i, true, "<h1>" + i + Code.msg(i) + "</h1>" + str2);
        }

        void sendReply(int i, boolean z, String str) {
            String str2;
            try {
                String str3 = "HTTP/1.1 " + i + Code.msg(i) + "\r\n";
                if (str != null && str.length() != 0) {
                    str2 = (str3 + "Content-Length: " + str.length() + "\r\n") + "Content-Type: text/html\r\n";
                } else {
                    str2 = str3 + "Content-Length: 0\r\n";
                    str = "";
                }
                if (z) {
                    str2 = str2 + "Connection: close\r\n";
                }
                this.rawout.write((str2 + "\r\n" + str).getBytes("ISO8859_1"));
                this.rawout.flush();
                if (z) {
                    this.connection.close();
                }
            } catch (IOException e) {
                ServerImpl.this.logger.log(Level.FINER, "ServerImpl.sendReply", (Throwable) e);
                this.connection.close();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void logReply(int i, String str, String str2) {
        if (str2 == null) {
            str2 = "";
        }
        this.logger.fine(str + " [" + i + " " + Code.msg(i) + "] (" + str2 + ")");
    }

    long getTicks() {
        return this.ticks;
    }

    @Override // sun.net.httpserver.TimeSource
    public long getTime() {
        return this.time;
    }

    void delay() {
        Thread.yield();
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void startExchange() {
        this.exchangeCount++;
    }

    synchronized int endExchange() {
        this.exchangeCount--;
        return this.exchangeCount;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HttpServer getWrapper() {
        return this.wrapper;
    }

    /* loaded from: classes.dex */
    class ServerTimerTask extends TimerTask {
        ServerTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            LinkedList linkedList = new LinkedList();
            ServerImpl.this.time = System.currentTimeMillis();
            ServerImpl.access$1708(ServerImpl.this);
            synchronized (ServerImpl.this.idleConnections) {
                for (HttpConnection httpConnection : ServerImpl.this.idleConnections) {
                    if (httpConnection.time <= ServerImpl.this.time) {
                        linkedList.add(httpConnection);
                    }
                }
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    HttpConnection httpConnection2 = (HttpConnection) it.next();
                    ServerImpl.this.idleConnections.remove(httpConnection2);
                    ServerImpl.this.allConnections.remove(httpConnection2);
                    httpConnection2.close();
                }
            }
        }
    }
}
