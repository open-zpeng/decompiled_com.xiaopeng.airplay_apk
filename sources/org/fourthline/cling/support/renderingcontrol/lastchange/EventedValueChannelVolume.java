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
public class EventedValueChannelVolume extends EventedValue<ChannelVolume> {
    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected /* bridge */ /* synthetic */ ChannelVolume valueOf(Map.Entry[] entryArr) throws InvalidValueException {
        return valueOf2((Map.Entry<String, String>[]) entryArr);
    }

    public EventedValueChannelVolume(ChannelVolume value) {
        super(value);
    }

    public EventedValueChannelVolume(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    /* renamed from: valueOf  reason: avoid collision after fix types in other method */
    protected ChannelVolume valueOf2(Map.Entry<String, String>[] attributes) throws InvalidValueException {
        Channel channel = null;
        Integer volume = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if (attribute.getKey().equals("channel")) {
                channel = Channel.valueOf(attribute.getValue());
            }
            if (attribute.getKey().equals("val")) {
                volume = Integer.valueOf(new UnsignedIntegerTwoBytesDatatype().valueOf(attribute.getValue()).getValue().intValue());
            }
        }
        if (channel == null || volume == null) {
            return null;
        }
        return new ChannelVolume(channel, volume);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public Map.Entry<String, String>[] getAttributes() {
        return new Map.Entry[]{new AbstractMap.SimpleEntry("val", new UnsignedIntegerTwoBytesDatatype().getString(new UnsignedIntegerTwoBytes(getValue().getVolume().intValue()))), new AbstractMap.SimpleEntry("channel", getValue().getChannel().name())};
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
