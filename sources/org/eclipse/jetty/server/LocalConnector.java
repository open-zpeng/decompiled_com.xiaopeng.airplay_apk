package org.eclipse.jetty.server;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class LocalConnector extends AbstractConnector {
    private static final Logger LOG = Log.getLogger(LocalConnector.class);
    private final BlockingQueue<Request> _requests = new LinkedBlockingQueue();

    public LocalConnector() {
        setMaxIdleTime(30000);
    }

    @Override // org.eclipse.jetty.server.Connector
    public Object getConnection() {
        return this;
    }

    public String getResponses(String requests) throws Exception {
        return getResponses(requests, false);
    }

    public String getResponses(String requests, boolean keepOpen) throws Exception {
        ByteArrayBuffer result = getResponses(new ByteArrayBuffer(requests, StringUtil.__ISO_8859_1), keepOpen);
        if (result == null) {
            return null;
        }
        return result.toString(StringUtil.__ISO_8859_1);
    }

    public ByteArrayBuffer getResponses(ByteArrayBuffer requestsBuffer, boolean keepOpen) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Request request = new Request(requestsBuffer, keepOpen, latch);
        this._requests.add(request);
        latch.await(getMaxIdleTime(), TimeUnit.MILLISECONDS);
        return request.getResponsesBuffer();
    }

    @Override // org.eclipse.jetty.server.AbstractConnector
    protected void accept(int acceptorID) throws IOException, InterruptedException {
        Request request = this._requests.take();
        getThreadPool().dispatch(request);
    }

    @Override // org.eclipse.jetty.server.Connector
    public void open() throws IOException {
    }

    @Override // org.eclipse.jetty.server.Connector
    public void close() throws IOException {
    }

    @Override // org.eclipse.jetty.server.Connector
    public int getLocalPort() {
        return -1;
    }

    public void executeRequest(String rawRequest) throws IOException {
        Request request = new Request(new ByteArrayBuffer(rawRequest, StringUtil.__UTF8), true, null);
        this._requests.add(request);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Request implements Runnable {
        private final boolean _keepOpen;
        private final CountDownLatch _latch;
        private final ByteArrayBuffer _requestsBuffer;
        private volatile ByteArrayBuffer _responsesBuffer;

        private Request(ByteArrayBuffer requestsBuffer, boolean keepOpen, CountDownLatch latch) {
            this._requestsBuffer = requestsBuffer;
            this._keepOpen = keepOpen;
            this._latch = latch;
        }

        /* JADX WARN: Removed duplicated region for block: B:49:0x0046 A[ADDED_TO_REGION, SYNTHETIC] */
        /* JADX WARN: Removed duplicated region for block: B:5:0x0032 A[Catch: all -> 0x0054, Exception -> 0x0056, IOException -> 0x006b, TryCatch #1 {all -> 0x0098, blocks: (B:2:0x0000, B:12:0x0048, B:13:0x004d, B:14:0x0051, B:22:0x0061, B:23:0x0066, B:29:0x0076, B:30:0x007b, B:3:0x0028, B:5:0x0032, B:7:0x0038, B:9:0x0042, B:19:0x0057, B:26:0x006c), top: B:45:0x0000 }] */
        /* JADX WARN: Removed duplicated region for block: B:9:0x0042 A[Catch: all -> 0x0054, Exception -> 0x0056, IOException -> 0x006b, LOOP:1: B:7:0x0038->B:9:0x0042, LOOP_END, TRY_LEAVE, TryCatch #1 {all -> 0x0098, blocks: (B:2:0x0000, B:12:0x0048, B:13:0x004d, B:14:0x0051, B:22:0x0061, B:23:0x0066, B:29:0x0076, B:30:0x007b, B:3:0x0028, B:5:0x0032, B:7:0x0038, B:9:0x0042, B:19:0x0057, B:26:0x006c), top: B:45:0x0000 }] */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void run() {
            /*
                r5 = this;
                org.eclipse.jetty.server.LocalConnector$Request$1 r0 = new org.eclipse.jetty.server.LocalConnector$Request$1     // Catch: java.lang.Throwable -> L98
                org.eclipse.jetty.io.ByteArrayBuffer r1 = r5._requestsBuffer     // Catch: java.lang.Throwable -> L98
                byte[] r1 = r1.asArray()     // Catch: java.lang.Throwable -> L98
                r2 = 1024(0x400, float:1.435E-42)
                r0.<init>(r1, r2)     // Catch: java.lang.Throwable -> L98
                r1 = 1
                r0.setGrowOutput(r1)     // Catch: java.lang.Throwable -> L98
                org.eclipse.jetty.server.BlockingHttpConnection r1 = new org.eclipse.jetty.server.BlockingHttpConnection     // Catch: java.lang.Throwable -> L98
                org.eclipse.jetty.server.LocalConnector r2 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                org.eclipse.jetty.server.LocalConnector r3 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                org.eclipse.jetty.server.Server r3 = r3.getServer()     // Catch: java.lang.Throwable -> L98
                r1.<init>(r2, r0, r3)     // Catch: java.lang.Throwable -> L98
                r0.setConnection(r1)     // Catch: java.lang.Throwable -> L98
                org.eclipse.jetty.server.LocalConnector r2 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                r2.connectionOpened(r1)     // Catch: java.lang.Throwable -> L98
                boolean r2 = r5._keepOpen     // Catch: java.lang.Throwable -> L98
            L28:
                org.eclipse.jetty.io.ByteArrayBuffer r3 = r0.getIn()     // Catch: java.lang.Throwable -> L54 java.lang.Exception -> L56 java.io.IOException -> L6b
                int r3 = r3.length()     // Catch: java.lang.Throwable -> L54 java.lang.Exception -> L56 java.io.IOException -> L6b
                if (r3 <= 0) goto L46
                boolean r3 = r0.isOpen()     // Catch: java.lang.Throwable -> L54 java.lang.Exception -> L56 java.io.IOException -> L6b
                if (r3 == 0) goto L46
            L38:
                org.eclipse.jetty.io.Connection r3 = r0.getConnection()     // Catch: java.lang.Throwable -> L54 java.lang.Exception -> L56 java.io.IOException -> L6b
                org.eclipse.jetty.io.Connection r4 = r3.handle()     // Catch: java.lang.Throwable -> L54 java.lang.Exception -> L56 java.io.IOException -> L6b
                if (r4 == r3) goto L28
                r0.setConnection(r4)     // Catch: java.lang.Throwable -> L54 java.lang.Exception -> L56 java.io.IOException -> L6b
                goto L38
            L46:
                if (r2 != 0) goto L4d
                org.eclipse.jetty.server.LocalConnector r3 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                r3.connectionClosed(r1)     // Catch: java.lang.Throwable -> L98
            L4d:
                org.eclipse.jetty.io.ByteArrayBuffer r3 = r0.getOut()     // Catch: java.lang.Throwable -> L98
            L51:
                r5._responsesBuffer = r3     // Catch: java.lang.Throwable -> L98
                goto L80
            L54:
                r3 = move-exception
                goto L8a
            L56:
                r3 = move-exception
                org.eclipse.jetty.util.log.Logger r4 = org.eclipse.jetty.server.LocalConnector.access$100()     // Catch: java.lang.Throwable -> L54
                r4.warn(r3)     // Catch: java.lang.Throwable -> L54
                r2 = 0
                if (r2 != 0) goto L66
                org.eclipse.jetty.server.LocalConnector r3 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                r3.connectionClosed(r1)     // Catch: java.lang.Throwable -> L98
            L66:
                org.eclipse.jetty.io.ByteArrayBuffer r3 = r0.getOut()     // Catch: java.lang.Throwable -> L98
                goto L51
            L6b:
                r3 = move-exception
                org.eclipse.jetty.util.log.Logger r4 = org.eclipse.jetty.server.LocalConnector.access$100()     // Catch: java.lang.Throwable -> L54
                r4.debug(r3)     // Catch: java.lang.Throwable -> L54
                r2 = 0
                if (r2 != 0) goto L7b
                org.eclipse.jetty.server.LocalConnector r3 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                r3.connectionClosed(r1)     // Catch: java.lang.Throwable -> L98
            L7b:
                org.eclipse.jetty.io.ByteArrayBuffer r3 = r0.getOut()     // Catch: java.lang.Throwable -> L98
                goto L51
            L80:
                java.util.concurrent.CountDownLatch r0 = r5._latch
                if (r0 == 0) goto L89
                java.util.concurrent.CountDownLatch r0 = r5._latch
                r0.countDown()
            L89:
                return
            L8a:
                if (r2 != 0) goto L91
                org.eclipse.jetty.server.LocalConnector r4 = org.eclipse.jetty.server.LocalConnector.this     // Catch: java.lang.Throwable -> L98
                r4.connectionClosed(r1)     // Catch: java.lang.Throwable -> L98
            L91:
                org.eclipse.jetty.io.ByteArrayBuffer r4 = r0.getOut()     // Catch: java.lang.Throwable -> L98
                r5._responsesBuffer = r4     // Catch: java.lang.Throwable -> L98
                throw r3     // Catch: java.lang.Throwable -> L98
            L98:
                r0 = move-exception
                java.util.concurrent.CountDownLatch r1 = r5._latch
                if (r1 == 0) goto La2
                java.util.concurrent.CountDownLatch r1 = r5._latch
                r1.countDown()
            La2:
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.server.LocalConnector.Request.run():void");
        }

        public ByteArrayBuffer getResponsesBuffer() {
            return this._responsesBuffer;
        }
    }
}
