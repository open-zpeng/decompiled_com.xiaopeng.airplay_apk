package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.MimeType;
/* loaded from: classes.dex */
public class ProtocolInfo {
    public static final String WILDCARD = "*";
    protected String additionalInfo;
    protected String contentFormat;
    protected String network;
    protected Protocol protocol;

    public ProtocolInfo(String s) throws InvalidValueException {
        this.protocol = Protocol.ALL;
        this.network = "*";
        this.contentFormat = "*";
        this.additionalInfo = "*";
        if (s == null) {
            throw new NullPointerException();
        }
        String s2 = s.trim();
        String[] split = s2.split(":");
        if (split.length != 4) {
            throw new InvalidValueException("Can't parse ProtocolInfo string: " + s2);
        }
        this.protocol = Protocol.value(split[0]);
        this.network = split[1];
        this.contentFormat = split[2];
        this.additionalInfo = split[3];
    }

    public ProtocolInfo(MimeType contentFormatMimeType) {
        this.protocol = Protocol.ALL;
        this.network = "*";
        this.contentFormat = "*";
        this.additionalInfo = "*";
        this.protocol = Protocol.HTTP_GET;
        this.contentFormat = contentFormatMimeType.toString();
    }

    public ProtocolInfo(Protocol protocol, String network, String contentFormat, String additionalInfo) {
        this.protocol = Protocol.ALL;
        this.network = "*";
        this.contentFormat = "*";
        this.additionalInfo = "*";
        this.protocol = protocol;
        this.network = network;
        this.contentFormat = contentFormat;
        this.additionalInfo = additionalInfo;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public String getNetwork() {
        return this.network;
    }

    public String getContentFormat() {
        return this.contentFormat;
    }

    public MimeType getContentFormatMimeType() throws IllegalArgumentException {
        return MimeType.valueOf(this.contentFormat);
    }

    public String getAdditionalInfo() {
        return this.additionalInfo;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolInfo that = (ProtocolInfo) o;
        if (this.additionalInfo.equals(that.additionalInfo) && this.contentFormat.equals(that.contentFormat) && this.network.equals(that.network) && this.protocol == that.protocol) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.protocol.hashCode();
        return (31 * ((31 * ((31 * result) + this.network.hashCode())) + this.contentFormat.hashCode())) + this.additionalInfo.hashCode();
    }

    public String toString() {
        return this.protocol.toString() + ":" + this.network + ":" + this.contentFormat + ":" + this.additionalInfo;
    }
}
