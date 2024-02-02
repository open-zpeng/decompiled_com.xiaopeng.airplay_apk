package com.xpeng.airplay.dlna;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
/* loaded from: classes.dex */
public interface IAudioControl {
    UnsignedIntegerTwoBytes getBrightness();

    UnsignedIntegerFourBytes[] getInstanceIds();

    boolean getMute(String str);

    UnsignedIntegerTwoBytes getVolume(String str);

    boolean hasInstanceId(UnsignedIntegerFourBytes unsignedIntegerFourBytes);

    void setBrightness(UnsignedIntegerTwoBytes unsignedIntegerTwoBytes);

    void setMute(String str, boolean z);

    void setVolume(String str, UnsignedIntegerTwoBytes unsignedIntegerTwoBytes);
}
