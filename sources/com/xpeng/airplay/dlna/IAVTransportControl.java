package com.xpeng.airplay.dlna;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.fourthline.cling.support.model.TransportState;
/* loaded from: classes.dex */
public interface IAVTransportControl {
    TransportAction[] getCurrentTransportActions();

    DeviceCapabilities getDeviceCapabilities();

    UnsignedIntegerFourBytes[] getInstanceIds();

    MediaInfo getMediaInfo();

    PositionInfo getPositionInfo();

    TransportInfo getTransportInfo();

    TransportSettings getTransportSettings();

    boolean hasInstanceId(UnsignedIntegerFourBytes unsignedIntegerFourBytes);

    void pause() throws AVTransportException;

    void play(String str) throws AVTransportException;

    void seek(String str, String str2) throws AVTransportException;

    void setAVTransportURI(String str, String str2) throws AVTransportException;

    void setTransportState(TransportState transportState);

    void stop() throws AVTransportException;
}
