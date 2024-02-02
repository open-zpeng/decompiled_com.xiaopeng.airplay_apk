package org.fourthline.cling.support.model.container;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
/* loaded from: classes.dex */
public class MusicAlbum extends Album {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.album.musicAlbum");

    public MusicAlbum() {
        setClazz(CLASS);
    }

    public MusicAlbum(Container other) {
        super(other);
    }

    public MusicAlbum(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount, new ArrayList());
    }

    public MusicAlbum(String id, Container parent, String title, String creator, Integer childCount, List<MusicTrack> musicTracks) {
        this(id, parent.getId(), title, creator, childCount, musicTracks);
    }

    public MusicAlbum(String id, String parentID, String title, String creator, Integer childCount) {
        this(id, parentID, title, creator, childCount, new ArrayList());
    }

    public MusicAlbum(String id, String parentID, String title, String creator, Integer childCount, List<MusicTrack> musicTracks) {
        super(id, parentID, title, creator, childCount);
        setClazz(CLASS);
        addMusicTracks(musicTracks);
    }

    public PersonWithRole getFirstArtist() {
        return (PersonWithRole) getFirstPropertyValue(DIDLObject.Property.UPNP.ARTIST.class);
    }

    public PersonWithRole[] getArtists() {
        List<PersonWithRole> list = getPropertyValues(DIDLObject.Property.UPNP.ARTIST.class);
        return (PersonWithRole[]) list.toArray(new PersonWithRole[list.size()]);
    }

    public MusicAlbum setArtists(PersonWithRole[] artists) {
        removeProperties(DIDLObject.Property.UPNP.ARTIST.class);
        for (PersonWithRole artist : artists) {
            addProperty(new DIDLObject.Property.UPNP.ARTIST(artist));
        }
        return this;
    }

    public String getFirstGenre() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.GENRE.class);
    }

    public String[] getGenres() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.GENRE.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public MusicAlbum setGenres(String[] genres) {
        removeProperties(DIDLObject.Property.UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new DIDLObject.Property.UPNP.GENRE(genre));
        }
        return this;
    }

    public Person getFirstProducer() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.UPNP.PRODUCER.class);
    }

    public Person[] getProducers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.UPNP.PRODUCER.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public MusicAlbum setProducers(Person[] persons) {
        removeProperties(DIDLObject.Property.UPNP.PRODUCER.class);
        for (Person p : persons) {
            addProperty(new DIDLObject.Property.UPNP.PRODUCER(p));
        }
        return this;
    }

    public URI getFirstAlbumArtURI() {
        return (URI) getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
    }

    public URI[] getAlbumArtURIs() {
        List<URI> list = getPropertyValues(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
        return (URI[]) list.toArray(new URI[list.size()]);
    }

    public MusicAlbum setAlbumArtURIs(URI[] uris) {
        removeProperties(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
        for (URI uri : uris) {
            addProperty(new DIDLObject.Property.UPNP.ALBUM_ART_URI(uri));
        }
        return this;
    }

    public String getToc() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.TOC.class);
    }

    public MusicAlbum setToc(String toc) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.TOC(toc));
        return this;
    }

    public MusicTrack[] getMusicTracks() {
        List<MusicTrack> list = new ArrayList<>();
        for (Item item : getItems()) {
            if (item instanceof MusicTrack) {
                list.add((MusicTrack) item);
            }
        }
        return (MusicTrack[]) list.toArray(new MusicTrack[list.size()]);
    }

    public void addMusicTracks(List<MusicTrack> musicTracks) {
        addMusicTracks((MusicTrack[]) musicTracks.toArray(new MusicTrack[musicTracks.size()]));
    }

    public void addMusicTracks(MusicTrack[] musicTracks) {
        if (musicTracks != null) {
            for (MusicTrack musicTrack : musicTracks) {
                musicTrack.setAlbum(getTitle());
                addItem(musicTrack);
            }
        }
    }
}
