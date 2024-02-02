package org.fourthline.cling.model.profile;

import org.fourthline.cling.model.meta.DeviceDetails;
/* loaded from: classes.dex */
public interface DeviceDetailsProvider {
    DeviceDetails provide(RemoteClientInfo remoteClientInfo);
}
