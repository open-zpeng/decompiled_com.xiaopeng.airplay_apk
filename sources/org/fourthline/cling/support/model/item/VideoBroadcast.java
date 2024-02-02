package org.fourthline.cling.support.model.item;

import java.net.URI;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class VideoBroadcast extends VideoItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.videoItem.videoBroadcast");

    public VideoBroadcast() {
        setClazz(CLASS);
    }

    public VideoBroadcast(Item other) {
        super(other);
    }

    public VideoBroadcast(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public VideoBroadcast(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
    }

    public URI getIcon() {
        return (URI) getFirstPropertyValue(DIDLObject.Property.UPNP.ICON.class);
    }

    public VideoBroadcast setIcon(URI icon) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.ICON(icon));
        return this;
    }

    public String getRegion() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.REGION.class);
    }

    public VideoBroadcast setRegion(String region) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.REGION(region));
        return this;
    }

    public Integer getChannelNr() {
        return (Integer) getFirstPropertyValue(DIDLObject.Property.UPNP.CHANNEL_NR.class);
    }

    public VideoBroadcast setChannelNr(Integer channelNr) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.CHANNEL_NR(channelNr));
        return this;
    }
}
