package org.fourthline.cling.registry.event;

import org.fourthline.cling.model.meta.Device;
/* loaded from: classes.dex */
public class DeviceDiscovery<D extends Device> {
    protected D device;

    public DeviceDiscovery(D device) {
        this.device = device;
    }

    public D getDevice() {
        return this.device;
    }
}
