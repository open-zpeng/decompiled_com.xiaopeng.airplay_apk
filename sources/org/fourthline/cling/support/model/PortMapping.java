package org.fourthline.cling.support.model;

import java.util.Map;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
/* loaded from: classes.dex */
public class PortMapping {
    private String description;
    private boolean enabled;
    private UnsignedIntegerTwoBytes externalPort;
    private String internalClient;
    private UnsignedIntegerTwoBytes internalPort;
    private UnsignedIntegerFourBytes leaseDurationSeconds;
    private Protocol protocol;
    private String remoteHost;

    /* loaded from: classes.dex */
    public enum Protocol {
        UDP,
        TCP
    }

    public PortMapping() {
    }

    public PortMapping(Map<String, ActionArgumentValue<Service>> map) {
        this(((Boolean) map.get("NewEnabled").getValue()).booleanValue(), (UnsignedIntegerFourBytes) map.get("NewLeaseDuration").getValue(), (String) map.get("NewRemoteHost").getValue(), (UnsignedIntegerTwoBytes) map.get("NewExternalPort").getValue(), (UnsignedIntegerTwoBytes) map.get("NewInternalPort").getValue(), (String) map.get("NewInternalClient").getValue(), Protocol.valueOf(map.get("NewProtocol").toString()), (String) map.get("NewPortMappingDescription").getValue());
    }

    public PortMapping(int port, String internalClient, Protocol protocol) {
        this(true, new UnsignedIntegerFourBytes(0L), null, new UnsignedIntegerTwoBytes(port), new UnsignedIntegerTwoBytes(port), internalClient, protocol, null);
    }

    public PortMapping(int port, String internalClient, Protocol protocol, String description) {
        this(true, new UnsignedIntegerFourBytes(0L), null, new UnsignedIntegerTwoBytes(port), new UnsignedIntegerTwoBytes(port), internalClient, protocol, description);
    }

    public PortMapping(String remoteHost, UnsignedIntegerTwoBytes externalPort, Protocol protocol) {
        this(true, new UnsignedIntegerFourBytes(0L), remoteHost, externalPort, null, null, protocol, null);
    }

    public PortMapping(boolean enabled, UnsignedIntegerFourBytes leaseDurationSeconds, String remoteHost, UnsignedIntegerTwoBytes externalPort, UnsignedIntegerTwoBytes internalPort, String internalClient, Protocol protocol, String description) {
        this.enabled = enabled;
        this.leaseDurationSeconds = leaseDurationSeconds;
        this.remoteHost = remoteHost;
        this.externalPort = externalPort;
        this.internalPort = internalPort;
        this.internalClient = internalClient;
        this.protocol = protocol;
        this.description = description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UnsignedIntegerFourBytes getLeaseDurationSeconds() {
        return this.leaseDurationSeconds;
    }

    public void setLeaseDurationSeconds(UnsignedIntegerFourBytes leaseDurationSeconds) {
        this.leaseDurationSeconds = leaseDurationSeconds;
    }

    public boolean hasRemoteHost() {
        return this.remoteHost != null && this.remoteHost.length() > 0;
    }

    public String getRemoteHost() {
        return this.remoteHost == null ? "-" : this.remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = (remoteHost == null || remoteHost.equals("-") || remoteHost.length() == 0) ? null : remoteHost;
    }

    public UnsignedIntegerTwoBytes getExternalPort() {
        return this.externalPort;
    }

    public void setExternalPort(UnsignedIntegerTwoBytes externalPort) {
        this.externalPort = externalPort;
    }

    public UnsignedIntegerTwoBytes getInternalPort() {
        return this.internalPort;
    }

    public void setInternalPort(UnsignedIntegerTwoBytes internalPort) {
        this.internalPort = internalPort;
    }

    public String getInternalClient() {
        return this.internalClient;
    }

    public void setInternalClient(String internalClient) {
        this.internalClient = internalClient;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public boolean hasDescription() {
        return this.description != null;
    }

    public String getDescription() {
        return this.description == null ? "-" : this.description;
    }

    public void setDescription(String description) {
        this.description = (description == null || description.equals("-") || description.length() == 0) ? null : description;
    }

    public String toString() {
        return "(" + getClass().getSimpleName() + ") Protocol: " + getProtocol() + ", " + getExternalPort() + " => " + getInternalClient();
    }
}
