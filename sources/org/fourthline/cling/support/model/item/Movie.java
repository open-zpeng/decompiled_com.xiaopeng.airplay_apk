package org.fourthline.cling.support.model.item;

import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class Movie extends VideoItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.videoItem.movie");

    public Movie() {
        setClazz(CLASS);
    }

    public Movie(Item other) {
        super(other);
    }

    public Movie(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public Movie(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
    }

    public StorageMedium getStorageMedium() {
        return (StorageMedium) getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public Movie setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public Integer getDVDRegionCode() {
        return (Integer) getFirstPropertyValue(DIDLObject.Property.UPNP.DVD_REGION_CODE.class);
    }

    public Movie setDVDRegionCode(Integer DVDRegionCode) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.DVD_REGION_CODE(DVDRegionCode));
        return this;
    }

    public String getChannelName() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.CHANNEL_NAME.class);
    }

    public Movie setChannelName(String channelName) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.CHANNEL_NAME(channelName));
        return this;
    }

    public String getFirstScheduledStartTime() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.SCHEDULED_START_TIME.class);
    }

    public String[] getScheduledStartTimes() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.SCHEDULED_START_TIME.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public Movie setScheduledStartTimes(String[] strings) {
        removeProperties(DIDLObject.Property.UPNP.SCHEDULED_START_TIME.class);
        for (String s : strings) {
            addProperty(new DIDLObject.Property.UPNP.SCHEDULED_START_TIME(s));
        }
        return this;
    }

    public String getFirstScheduledEndTime() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.SCHEDULED_END_TIME.class);
    }

    public String[] getScheduledEndTimes() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.SCHEDULED_END_TIME.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public Movie setScheduledEndTimes(String[] strings) {
        removeProperties(DIDLObject.Property.UPNP.SCHEDULED_END_TIME.class);
        for (String s : strings) {
            addProperty(new DIDLObject.Property.UPNP.SCHEDULED_END_TIME(s));
        }
        return this;
    }
}
