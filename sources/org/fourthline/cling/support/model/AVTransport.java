package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
/* loaded from: classes.dex */
public class AVTransport {
    protected DeviceCapabilities deviceCapabilities;
    protected final UnsignedIntegerFourBytes instanceID;
    protected final LastChange lastChange;
    protected MediaInfo mediaInfo;
    protected PositionInfo positionInfo;
    protected TransportInfo transportInfo;
    protected TransportSettings transportSettings;

    public AVTransport(UnsignedIntegerFourBytes instanceID, LastChange lastChange, StorageMedium possiblePlayMedium) {
        this(instanceID, lastChange, new StorageMedium[]{possiblePlayMedium});
    }

    public AVTransport(UnsignedIntegerFourBytes instanceID, LastChange lastChange, StorageMedium[] possiblePlayMedia) {
        this.instanceID = instanceID;
        this.lastChange = lastChange;
        setDeviceCapabilities(new DeviceCapabilities(possiblePlayMedia));
        setMediaInfo(new MediaInfo());
        setTransportInfo(new TransportInfo());
        setPositionInfo(new PositionInfo());
        setTransportSettings(new TransportSettings());
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return this.instanceID;
    }

    public LastChange getLastChange() {
        return this.lastChange;
    }

    public MediaInfo getMediaInfo() {
        return this.mediaInfo;
    }

    public void setMediaInfo(MediaInfo mediaInfo) {
        this.mediaInfo = mediaInfo;
    }

    public TransportInfo getTransportInfo() {
        return this.transportInfo;
    }

    public void setTransportInfo(TransportInfo transportInfo) {
        this.transportInfo = transportInfo;
    }

    public PositionInfo getPositionInfo() {
        return this.positionInfo;
    }

    public void setPositionInfo(PositionInfo positionInfo) {
        this.positionInfo = positionInfo;
    }

    public DeviceCapabilities getDeviceCapabilities() {
        return this.deviceCapabilities;
    }

    public void setDeviceCapabilities(DeviceCapabilities deviceCapabilities) {
        this.deviceCapabilities = deviceCapabilities;
    }

    public TransportSettings getTransportSettings() {
        return this.transportSettings;
    }

    public void setTransportSettings(TransportSettings transportSettings) {
        this.transportSettings = transportSettings;
    }
}
