package org.fourthline.cling.support.renderingcontrol.lastchange;

import java.util.Map;
import org.fourthline.cling.model.types.BooleanDatatype;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.shared.AbstractMap;
/* loaded from: classes.dex */
public class EventedValueChannelMute extends EventedValue<ChannelMute> {
    @Override // org.fourthline.cling.support.lastchange.EventedValue
    protected /* bridge */ /* synthetic */ ChannelMute valueOf(Map.Entry[] entryArr) throws InvalidValueException {
        return valueOf2((Map.Entry<String, String>[]) entryArr);
    }

    public EventedValueChannelMute(ChannelMute value) {
        super(value);
    }

    public EventedValueChannelMute(Map.Entry<String, String>[] attributes) {
        super(attributes);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    /* renamed from: valueOf  reason: avoid collision after fix types in other method */
    protected ChannelMute valueOf2(Map.Entry<String, String>[] attributes) throws InvalidValueException {
        Channel channel = null;
        Boolean mute = null;
        for (Map.Entry<String, String> attribute : attributes) {
            if (attribute.getKey().equals("channel")) {
                channel = Channel.valueOf(attribute.getValue());
            }
            if (attribute.getKey().equals("val")) {
                mute = new BooleanDatatype().valueOf(attribute.getValue());
            }
        }
        if (channel == null || mute == null) {
            return null;
        }
        return new ChannelMute(channel, mute);
    }

    @Override // org.fourthline.cling.support.lastchange.EventedValue
    public Map.Entry<String, String>[] getAttributes() {
        return new Map.Entry[]{new AbstractMap.SimpleEntry("val", new BooleanDatatype().getString(getValue().getMute())), new AbstractMap.SimpleEntry("channel", getValue().getChannel().name())};
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
