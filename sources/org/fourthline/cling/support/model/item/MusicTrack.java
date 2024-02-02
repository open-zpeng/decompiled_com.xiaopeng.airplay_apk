package org.fourthline.cling.support.model.item;

import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class MusicTrack extends AudioItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.audioItem.musicTrack");

    public MusicTrack() {
        setClazz(CLASS);
    }

    public MusicTrack(Item other) {
        super(other);
    }

    public MusicTrack(String id, Container parent, String title, String creator, String album, String artist, Res... resource) {
        this(id, parent.getId(), title, creator, album, artist, resource);
    }

    public MusicTrack(String id, Container parent, String title, String creator, String album, PersonWithRole artist, Res... resource) {
        this(id, parent.getId(), title, creator, album, artist, resource);
    }

    public MusicTrack(String id, String parentID, String title, String creator, String album, String artist, Res... resource) {
        this(id, parentID, title, creator, album, artist == null ? null : new PersonWithRole(artist), resource);
    }

    public MusicTrack(String id, String parentID, String title, String creator, String album, PersonWithRole artist, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
        if (album != null) {
            setAlbum(album);
        }
        if (artist != null) {
            addProperty(new DIDLObject.Property.UPNP.ARTIST(artist));
        }
    }

    public PersonWithRole getFirstArtist() {
        return (PersonWithRole) getFirstPropertyValue(DIDLObject.Property.UPNP.ARTIST.class);
    }

    public PersonWithRole[] getArtists() {
        List<PersonWithRole> list = getPropertyValues(DIDLObject.Property.UPNP.ARTIST.class);
        return (PersonWithRole[]) list.toArray(new PersonWithRole[list.size()]);
    }

    public MusicTrack setArtists(PersonWithRole[] artists) {
        removeProperties(DIDLObject.Property.UPNP.ARTIST.class);
        for (PersonWithRole artist : artists) {
            addProperty(new DIDLObject.Property.UPNP.ARTIST(artist));
        }
        return this;
    }

    public String getAlbum() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM.class);
    }

    public MusicTrack setAlbum(String album) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.ALBUM(album));
        return this;
    }

    public Integer getOriginalTrackNumber() {
        return (Integer) getFirstPropertyValue(DIDLObject.Property.UPNP.ORIGINAL_TRACK_NUMBER.class);
    }

    public MusicTrack setOriginalTrackNumber(Integer number) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.ORIGINAL_TRACK_NUMBER(number));
        return this;
    }

    public String getFirstPlaylist() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.PLAYLIST.class);
    }

    public String[] getPlaylists() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.PLAYLIST.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public MusicTrack setPlaylists(String[] playlists) {
        removeProperties(DIDLObject.Property.UPNP.PLAYLIST.class);
        for (String s : playlists) {
            addProperty(new DIDLObject.Property.UPNP.PLAYLIST(s));
        }
        return this;
    }

    public StorageMedium getStorageMedium() {
        return (StorageMedium) getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public MusicTrack setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public Person getFirstContributor() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.DC.CONTRIBUTOR.class);
    }

    public Person[] getContributors() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.CONTRIBUTOR.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public MusicTrack setContributors(Person[] contributors) {
        removeProperties(DIDLObject.Property.DC.CONTRIBUTOR.class);
        for (Person p : contributors) {
            addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(p));
        }
        return this;
    }

    public String getDate() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DATE.class);
    }

    public MusicTrack setDate(String date) {
        replaceFirstProperty(new DIDLObject.Property.DC.DATE(date));
        return this;
    }
}
