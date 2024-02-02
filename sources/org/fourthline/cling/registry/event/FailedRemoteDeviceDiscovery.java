package org.fourthline.cling.registry.event;

import org.fourthline.cling.model.meta.RemoteDevice;
/* loaded from: classes.dex */
public class FailedRemoteDeviceDiscovery extends DeviceDiscovery<RemoteDevice> {
    protected Exception exception;

    public FailedRemoteDeviceDiscovery(RemoteDevice device, Exception ex) {
        super(device);
        this.exception = ex;
    }

    public Exception getException() {
        return this.exception;
    }
}
