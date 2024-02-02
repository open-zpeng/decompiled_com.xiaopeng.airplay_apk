package org.fourthline.cling.support.renderingcontrol.lastchange;

import org.fourthline.cling.support.model.Channel;
/* loaded from: classes.dex */
public class ChannelVolumeDB {
    protected Channel channel;
    protected Integer volumeDB;

    public ChannelVolumeDB(Channel channel, Integer volumeDB) {
        this.channel = channel;
        this.volumeDB = volumeDB;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public Integer getVolumeDB() {
        return this.volumeDB;
    }

    public String toString() {
        return "VolumeDB: " + getVolumeDB() + " (" + getChannel() + ")";
    }
}
