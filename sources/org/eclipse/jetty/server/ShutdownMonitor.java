package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.thread.ShutdownThread;
/* loaded from: classes.dex */
public class ShutdownMonitor {
    private boolean DEBUG;
    private boolean exitVm;
    private String key;
    private int port;
    private ServerSocket serverSocket;
    private ShutdownMonitorThread thread;

    /* loaded from: classes.dex */
    static class Holder {
        static ShutdownMonitor instance = new ShutdownMonitor();

        Holder() {
        }
    }

    public static ShutdownMonitor getInstance() {
        return Holder.instance;
    }

    /* loaded from: classes.dex */
    public class ShutdownMonitorThread extends Thread {
        public ShutdownMonitorThread() {
            setDaemon(true);
            setName("ShutdownMonitor");
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            LineNumberReader lin;
            String receivedKey;
            if (ShutdownMonitor.this.serverSocket == null) {
                return;
            }
            while (ShutdownMonitor.this.serverSocket != null) {
                Socket socket = null;
                try {
                    try {
                        socket = ShutdownMonitor.this.serverSocket.accept();
                        lin = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
                        receivedKey = lin.readLine();
                    } catch (Exception e) {
                        ShutdownMonitor.this.debug(e);
                        System.err.println(e.toString());
                    }
                    if (ShutdownMonitor.this.key.equals(receivedKey)) {
                        OutputStream out = socket.getOutputStream();
                        String cmd = lin.readLine();
                        ShutdownMonitor.this.debug("command=%s", cmd);
                        if ("stop".equals(cmd)) {
                            ShutdownMonitor.this.debug("Issuing graceful shutdown..", new Object[0]);
                            ShutdownThread.getInstance().run();
                            ShutdownMonitor.this.debug("Informing client that we are stopped.", new Object[0]);
                            out.write("Stopped\r\n".getBytes(StringUtil.__UTF8));
                            out.flush();
                            ShutdownMonitor.this.debug("Shutting down monitor", new Object[0]);
                            ShutdownMonitor.this.close(socket);
                            socket = null;
                            ShutdownMonitor.this.close(ShutdownMonitor.this.serverSocket);
                            ShutdownMonitor.this.serverSocket = null;
                            if (ShutdownMonitor.this.exitVm) {
                                ShutdownMonitor.this.debug("Killing JVM", new Object[0]);
                                System.exit(0);
                            }
                        } else if ("status".equals(cmd)) {
                            out.write("OK\r\n".getBytes(StringUtil.__UTF8));
                            out.flush();
                        }
                    } else {
                        System.err.println("Ignoring command with incorrect key");
                        ShutdownMonitor.this.close(socket);
                    }
                } finally {
                    ShutdownMonitor.this.close(socket);
                }
            }
        }

        @Override // java.lang.Thread
        public void start() {
            if (isAlive()) {
                System.err.printf("ShutdownMonitorThread already started", new Object[0]);
                return;
            }
            startListenSocket();
            if (ShutdownMonitor.this.serverSocket != null) {
                if (ShutdownMonitor.this.DEBUG) {
                    System.err.println("Starting ShutdownMonitorThread");
                }
                super.start();
            }
        }

        private void startListenSocket() {
            ShutdownMonitor shutdownMonitor;
            String str;
            Object[] objArr;
            if (ShutdownMonitor.this.port < 0) {
                if (ShutdownMonitor.this.DEBUG) {
                    PrintStream printStream = System.err;
                    printStream.println("ShutdownMonitor not in use (port < 0): " + ShutdownMonitor.this.port);
                    return;
                }
                return;
            }
            try {
                try {
                    ShutdownMonitor.this.serverSocket = new ServerSocket(ShutdownMonitor.this.port, 1, InetAddress.getByName("127.0.0.1"));
                    if (ShutdownMonitor.this.port == 0) {
                        ShutdownMonitor.this.port = ShutdownMonitor.this.serverSocket.getLocalPort();
                        System.out.printf("STOP.PORT=%d%n", Integer.valueOf(ShutdownMonitor.this.port));
                    }
                    if (ShutdownMonitor.this.key == null) {
                        ShutdownMonitor.this.key = Long.toString((long) ((9.223372036854776E18d * Math.random()) + hashCode() + System.currentTimeMillis()), 36);
                        System.out.printf("STOP.KEY=%s%n", ShutdownMonitor.this.key);
                    }
                    ShutdownMonitor.this.debug("STOP.PORT=%d", Integer.valueOf(ShutdownMonitor.this.port));
                    ShutdownMonitor.this.debug("STOP.KEY=%s", ShutdownMonitor.this.key);
                    shutdownMonitor = ShutdownMonitor.this;
                    str = "%s";
                    objArr = new Object[]{ShutdownMonitor.this.serverSocket};
                } catch (Exception e) {
                    ShutdownMonitor.this.debug(e);
                    PrintStream printStream2 = System.err;
                    printStream2.println("Error binding monitor port " + ShutdownMonitor.this.port + ": " + e.toString());
                    ShutdownMonitor.this.serverSocket = null;
                    ShutdownMonitor.this.debug("STOP.PORT=%d", Integer.valueOf(ShutdownMonitor.this.port));
                    ShutdownMonitor.this.debug("STOP.KEY=%s", ShutdownMonitor.this.key);
                    shutdownMonitor = ShutdownMonitor.this;
                    str = "%s";
                    objArr = new Object[]{ShutdownMonitor.this.serverSocket};
                }
                shutdownMonitor.debug(str, objArr);
            } catch (Throwable th) {
                ShutdownMonitor.this.debug("STOP.PORT=%d", Integer.valueOf(ShutdownMonitor.this.port));
                ShutdownMonitor.this.debug("STOP.KEY=%s", ShutdownMonitor.this.key);
                ShutdownMonitor.this.debug("%s", ShutdownMonitor.this.serverSocket);
                throw th;
            }
        }
    }

    private ShutdownMonitor() {
        Properties props = System.getProperties();
        this.DEBUG = props.containsKey("DEBUG");
        this.port = Integer.parseInt(props.getProperty("STOP.PORT", "-1"));
        this.key = props.getProperty("STOP.KEY", null);
        this.exitVm = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void close(ServerSocket server) {
        if (server == null) {
            return;
        }
        try {
            server.close();
        } catch (IOException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void close(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void debug(String format, Object... args) {
        if (this.DEBUG) {
            PrintStream printStream = System.err;
            printStream.printf("[ShutdownMonitor] " + format + "%n", args);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void debug(Throwable t) {
        if (this.DEBUG) {
            t.printStackTrace(System.err);
        }
    }

    public String getKey() {
        return this.key;
    }

    public int getPort() {
        return this.port;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public boolean isExitVm() {
        return this.exitVm;
    }

    public void setDebug(boolean flag) {
        this.DEBUG = flag;
    }

    public void setExitVm(boolean exitVm) {
        synchronized (this) {
            if (this.thread != null && this.thread.isAlive()) {
                throw new IllegalStateException("ShutdownMonitorThread already started");
            }
            this.exitVm = exitVm;
        }
    }

    public void setKey(String key) {
        synchronized (this) {
            if (this.thread != null && this.thread.isAlive()) {
                throw new IllegalStateException("ShutdownMonitorThread already started");
            }
            this.key = key;
        }
    }

    public void setPort(int port) {
        synchronized (this) {
            if (this.thread != null && this.thread.isAlive()) {
                throw new IllegalStateException("ShutdownMonitorThread already started");
            }
            this.port = port;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void start() throws Exception {
        synchronized (this) {
            if (this.thread != null && this.thread.isAlive()) {
                System.err.printf("ShutdownMonitorThread already started", new Object[0]);
                return;
            }
            this.thread = new ShutdownMonitorThread();
            ShutdownMonitorThread t = this.thread;
            if (t != null) {
                t.start();
            }
        }
    }

    protected boolean isAlive() {
        boolean result;
        synchronized (this) {
            result = this.thread != null && this.thread.isAlive();
        }
        return result;
    }

    public String toString() {
        return String.format("%s[port=%d]", getClass().getName(), Integer.valueOf(this.port));
    }
}
