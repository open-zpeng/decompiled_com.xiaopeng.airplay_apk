package org.fourthline.cling.support.renderingcontrol.lastchange;

import java.util.Map;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytesDatatype;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.shared.AbstractMap;
/* loaded from: classes.dex */
public class EventedValueChannelVolumeDB extends EventedValue<ChannelVolumeDB> {
    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected /* bridge */ /* synthetic */ ChannelVolumeDB valueOf(Map.Entry[] entryArr) throws InvalidValueException {
        return valueOf2((Map.Entry<String, String>[]) entryArr);
    }

    public EventedValueChannelVolumeDB(ChannelVolumeDB value) {
        super(value);
    }

    public EventedValueChannelVolumeDB(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    /* renamed from: valueOf  reason: avoid collision after fix types in other method */
    protected ChannelVolumeDB valueOf2(Map.Entry<String, String>[] attributes) throws InvalidValueException {
        Channel channel = null;
        Integer volumeDB = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if (attribute.getKey().equals("channel")) {
                channel = Channel.valueOf(attribute.getValue());
            }
            if (attribute.getKey().equals("val")) {
                volumeDB = Integer.valueOf(new UnsignedIntegerTwoBytesDatatype().valueOf(attribute.getValue()).getValue().intValue());
            }
        }
        if (channel == null || volumeDB == null) {
            return null;
        }
        return new ChannelVolumeDB(channel, volumeDB);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public Map.Entry<String, String>[] getAttributes() {
        return new Map.Entry[]{new AbstractMap.SimpleEntry("val", new UnsignedIntegerTwoBytesDatatype().getString(new UnsignedIntegerTwoBytes(getValue().getVolumeDB().intValue()))), new AbstractMap.SimpleEntry("channel", getValue().getChannel().name())};
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public String toString() {
        return getValue().toString();
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected Datatype getDatatype() {
        return null;
    }
}
