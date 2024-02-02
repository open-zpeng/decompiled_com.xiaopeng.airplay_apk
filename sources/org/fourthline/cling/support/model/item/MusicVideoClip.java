package org.fourthline.cling.support.model.item;

import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class MusicVideoClip extends VideoItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.videoItem.musicVideoClip");

    public MusicVideoClip() {
        setClazz(CLASS);
    }

    public MusicVideoClip(Item other) {
        super(other);
    }

    public MusicVideoClip(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public MusicVideoClip(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
    }

    public PersonWithRole getFirstArtist() {
        return (PersonWithRole) getFirstPropertyValue(DIDLObject.Property.UPNP.ARTIST.class);
    }

    public PersonWithRole[] getArtists() {
        List<PersonWithRole> list = getPropertyValues(DIDLObject.Property.UPNP.ARTIST.class);
        return (PersonWithRole[]) list.toArray(new PersonWithRole[list.size()]);
    }

    public MusicVideoClip setArtists(PersonWithRole[] artists) {
        removeProperties(DIDLObject.Property.UPNP.ARTIST.class);
        for (PersonWithRole artist : artists) {
            addProperty(new DIDLObject.Property.UPNP.ARTIST(artist));
        }
        return this;
    }

    public StorageMedium getStorageMedium() {
        return (StorageMedium) getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public MusicVideoClip setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public String getAlbum() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM.class);
    }

    public MusicVideoClip setAlbum(String album) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.ALBUM(album));
        return this;
    }

    public String getFirstScheduledStartTime() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.SCHEDULED_START_TIME.class);
    }

    public String[] getScheduledStartTimes() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.SCHEDULED_START_TIME.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public MusicVideoClip setScheduledStartTimes(String[] strings) {
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

    public MusicVideoClip setScheduledEndTimes(String[] strings) {
        removeProperties(DIDLObject.Property.UPNP.SCHEDULED_END_TIME.class);
        for (String s : strings) {
            addProperty(new DIDLObject.Property.UPNP.SCHEDULED_END_TIME(s));
        }
        return this;
    }

    public Person getFirstContributor() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.DC.CONTRIBUTOR.class);
    }

    public Person[] getContributors() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.CONTRIBUTOR.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public MusicVideoClip setContributors(Person[] contributors) {
        removeProperties(DIDLObject.Property.DC.CONTRIBUTOR.class);
        for (Person p : contributors) {
            addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(p));
        }
        return this;
    }

    public String getDate() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DATE.class);
    }

    public MusicVideoClip setDate(String date) {
        replaceFirstProperty(new DIDLObject.Property.DC.DATE(date));
        return this;
    }
}
