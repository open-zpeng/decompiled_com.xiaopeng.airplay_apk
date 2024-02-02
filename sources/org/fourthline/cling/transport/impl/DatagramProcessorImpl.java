package org.fourthline.cling.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.http.HttpVersions;
import org.eclipse.jetty.util.StringUtil;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.OutgoingDatagramMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.seamless.http.Headers;
/* loaded from: classes.dex */
public class DatagramProcessorImpl implements DatagramProcessor {
    private static Logger log = Logger.getLogger(DatagramProcessor.class.getName());
    private DatagramPacket datagramPacket;
    private StringBuilder messageData;
    private StringBuilder statusLine;
    private final String TAG = "DatagramProcessorImpl";
    private UpnpRequest upnpRequest = new UpnpRequest(UpnpRequest.Method.UNKNOWN);
    private IncomingDatagramMessage requestMessage = new IncomingDatagramMessage(null, null, 0, null);

    @Override // org.fourthline.cling.transport.spi.DatagramProcessor
    public IncomingDatagramMessage read(InetAddress receivedOnAddress, DatagramPacket datagram) throws UnsupportedDataException {
        try {
            if (log.isLoggable(Level.FINER)) {
                log.finer("===================================== DATAGRAM BEGIN ============================================");
                log.finer(new String(datagram.getData(), StringUtil.__UTF8));
                log.finer("-===================================== DATAGRAM END =============================================");
            }
            ByteArrayInputStream is = new ByteArrayInputStream(datagram.getData());
            String[] startLine = Headers.readLine(is).split(" ");
            if (startLine[0].startsWith("HTTP/1.")) {
                return readResponseMessage(receivedOnAddress, datagram, is, Integer.valueOf(startLine[1]).intValue(), startLine[2], startLine[0]);
            }
            return readRequestMessage(receivedOnAddress, datagram, is, startLine[0], startLine[2]);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Could not parse headers: " + ex, ex, datagram.getData());
        }
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [org.fourthline.cling.model.message.UpnpOperation] */
    @Override // org.fourthline.cling.transport.spi.DatagramProcessor
    public DatagramPacket write(OutgoingDatagramMessage message) throws UnsupportedDataException {
        if (this.statusLine == null) {
            this.statusLine = new StringBuilder();
        } else {
            this.statusLine.setLength(0);
        }
        ?? operation = message.getOperation();
        if (operation instanceof UpnpRequest) {
            UpnpRequest requestOperation = (UpnpRequest) operation;
            StringBuilder sb = this.statusLine;
            sb.append(requestOperation.getHttpMethodName());
            sb.append(" * ");
            StringBuilder sb2 = this.statusLine;
            sb2.append("HTTP/1.");
            sb2.append(operation.getHttpMinorVersion());
            sb2.append("\r\n");
        } else if (operation instanceof UpnpResponse) {
            UpnpResponse responseOperation = (UpnpResponse) operation;
            StringBuilder sb3 = this.statusLine;
            sb3.append("HTTP/1.");
            sb3.append(operation.getHttpMinorVersion());
            sb3.append(" ");
            StringBuilder sb4 = this.statusLine;
            sb4.append(responseOperation.getStatusCode());
            sb4.append(" ");
            sb4.append(responseOperation.getStatusMessage());
            this.statusLine.append("\r\n");
        } else {
            throw new UnsupportedDataException("Message operation is not request or response, don't know how to process: " + message);
        }
        if (this.messageData == null) {
            this.messageData = new StringBuilder();
        } else {
            this.messageData.setLength(0);
        }
        this.messageData.append((CharSequence) this.statusLine);
        StringBuilder sb5 = this.messageData;
        sb5.append(message.getHeaders().toString());
        sb5.append("\r\n");
        if (log.isLoggable(Level.FINER)) {
            Logger logger = log;
            logger.finer("Writing message data for: " + message);
            log.finer("---------------------------------------------------------------------------------");
            log.finer(this.messageData.toString().substring(0, this.messageData.length() + (-2)));
            log.finer("---------------------------------------------------------------------------------");
        }
        try {
            byte[] data = this.messageData.toString().getBytes("US-ASCII");
            if (this.datagramPacket == null) {
                this.datagramPacket = new DatagramPacket(data, data.length, message.getDestinationAddress(), message.getDestinationPort());
            } else {
                this.datagramPacket.setData(data, 0, data.length);
                this.datagramPacket.setAddress(message.getDestinationAddress());
                this.datagramPacket.setPort(message.getDestinationPort());
            }
            return this.datagramPacket;
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedDataException("Can't convert message content to US-ASCII: " + ex.getMessage(), ex, this.messageData);
        }
    }

    protected IncomingDatagramMessage readRequestMessage(InetAddress receivedOnAddress, DatagramPacket datagram, ByteArrayInputStream is, String requestMethod, String httpProtocol) throws Exception {
        UpnpHeaders headers = new UpnpHeaders(is);
        this.upnpRequest.setMethod(UpnpRequest.Method.getByHttpName(requestMethod));
        this.upnpRequest.setHttpMinorVersion(httpProtocol.toUpperCase(Locale.ROOT).equals(HttpVersions.HTTP_1_1) ? 1 : 0);
        this.requestMessage.setOperation(this.upnpRequest);
        this.requestMessage.setSourceAddress(datagram.getAddress());
        this.requestMessage.setSourcePort(datagram.getPort());
        this.requestMessage.setLocalAddress(receivedOnAddress);
        this.requestMessage.setHeaders(headers);
        return this.requestMessage;
    }

    protected IncomingDatagramMessage readResponseMessage(InetAddress receivedOnAddress, DatagramPacket datagram, ByteArrayInputStream is, int statusCode, String statusMessage, String httpProtocol) throws Exception {
        UpnpHeaders headers = new UpnpHeaders(is);
        UpnpResponse upnpResponse = new UpnpResponse(statusCode, statusMessage);
        upnpResponse.setHttpMinorVersion(httpProtocol.toUpperCase(Locale.ROOT).equals(HttpVersions.HTTP_1_1) ? 1 : 0);
        IncomingDatagramMessage responseMessage = new IncomingDatagramMessage(upnpResponse, datagram.getAddress(), datagram.getPort(), receivedOnAddress);
        responseMessage.setHeaders(headers);
        return responseMessage;
    }
}
