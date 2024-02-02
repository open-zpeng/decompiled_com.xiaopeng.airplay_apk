package org.fourthline.cling.support.renderingcontrol.lastchange;

import org.fourthline.cling.support.model.Channel;
/* loaded from: classes.dex */
public class ChannelLoudness {
    protected Channel channel;
    protected Boolean loudness;

    public ChannelLoudness(Channel channel, Boolean loudness) {
        this.channel = channel;
        this.loudness = loudness;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public Boolean getLoudness() {
        return this.loudness;
    }

    public String toString() {
        return "Loudness: " + getLoudness() + " (" + getChannel() + ")";
    }
}
