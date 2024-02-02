package org.fourthline.cling.transport.impl;

import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.InitializationException;
/* loaded from: classes.dex */
public class DatagramIOImpl implements DatagramIO<DatagramIOConfigurationImpl> {
    private static Logger log = Logger.getLogger(DatagramIO.class.getName());
    protected final DatagramIOConfigurationImpl configuration;
    protected DatagramProcessor datagramProcessor;
    protected InetSocketAddress localAddress;
    protected Router router;
    protected MulticastSocket socket;
    private final String TAG = "DatagramIOImpl";
    private int ioExceptionCount = 0;

    public DatagramIOImpl(DatagramIOConfigurationImpl configuration) {
        this.configuration = configuration;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // org.fourthline.cling.transport.spi.DatagramIO
    public DatagramIOConfigurationImpl getConfiguration() {
        return this.configuration;
    }

    @Override // org.fourthline.cling.transport.spi.DatagramIO
    public synchronized void init(InetAddress bindAddress, Router router, DatagramProcessor datagramProcessor) throws InitializationException {
        this.router = router;
        this.datagramProcessor = datagramProcessor;
        try {
            Logger logger = log;
            logger.info("Creating bound socket (for datagram input/output) on: " + bindAddress);
            this.localAddress = new InetSocketAddress(bindAddress, 0);
            this.socket = new MulticastSocket(this.localAddress);
            this.socket.setTimeToLive(this.configuration.getTimeToLive());
            this.socket.setReceiveBufferSize(262144);
        } catch (Exception ex) {
            throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex);
        }
    }

    @Override // org.fourthline.cling.transport.spi.DatagramIO
    public synchronized void stop() {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
        this.ioExceptionCount = 0;
    }

    @Override // java.lang.Runnable
    public void run() {
        byte[] buf = new byte[getConfiguration().getMaxDatagramBytes()];
        DatagramPacket datagram = new DatagramPacket(buf, buf.length);
        ReceivingAsync receivingAsync = null;
        Logger logger = log;
        logger.fine("Entering blocking receiving loop, listening for UDP datagrams on: " + this.socket.getLocalAddress());
        while (!this.socket.isClosed()) {
            try {
                this.socket.receive(datagram);
                Logger logger2 = log;
                logger2.info("UDP datagram received from: " + datagram.getAddress().getHostAddress() + ":" + datagram.getPort() + " on: " + this.localAddress);
                if (this.router.isEnabled()) {
                    IncomingDatagramMessage message = this.datagramProcessor.read(this.localAddress.getAddress(), datagram);
                    if (receivingAsync == null) {
                        receivingAsync = this.router.getProtocolFactory().createReceivingAsync(message);
                    } else {
                        receivingAsync.setInputMessage(message);
                    }
                    this.router.getConfiguration().getAsyncProtocolExecutor().execute(receivingAsync);
                }
            } catch (SocketException e) {
                log.fine("Socket closed");
            } catch (UnsupportedDataException ex) {
                Logger logger3 = log;
                logger3.info("Could not read datagram: " + ex.getMessage());
            } catch (Exception ex2) {
                throw new RuntimeException(ex2);
            }
        }
        try {
            if (!this.socket.isClosed()) {
                log.fine("Closing unicast socket");
                this.socket.close();
            }
        } catch (Exception ex3) {
            throw new RuntimeException(ex3);
        }
    }

    @Override // org.fourthline.cling.transport.spi.DatagramIO
    public synchronized void send(OutgoingDatagramMessage message) {
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Sending message from address: " + this.localAddress);
        }
        DatagramPacket packet = this.datagramProcessor.write(message);
        send(packet);
    }

    @Override // org.fourthline.cling.transport.spi.DatagramIO
    public synchronized void send(DatagramPacket datagram) {
        if (log.isLoggable(Level.FINE)) {
            Logger logger = log;
            logger.fine("Sending message from address: " + this.localAddress);
        }
        try {
            try {
                try {
                    this.socket.send(datagram);
                } catch (Exception ex) {
                    if (ex instanceof IOException) {
                        Log.e("DatagramIOImpl", "fail to sending datagram caused by: " + ex.getCause());
                        if (this.router != null) {
                            int i = this.ioExceptionCount;
                            this.ioExceptionCount = i + 1;
                            if (i > 3) {
                                try {
                                    this.router.disable();
                                } catch (RouterException e) {
                                }
                            }
                        }
                    }
                    Log.e("DatagramIOImpl", "Exception sending datagram to: " + datagram.getAddress() + ": " + ex, ex);
                }
            } catch (RuntimeException ex2) {
                throw ex2;
            }
        } catch (SocketException e2) {
            Log.e("DatagramIOImpl", "Socket closed, aborting datagram send to: " + datagram.getAddress());
        }
    }
}
