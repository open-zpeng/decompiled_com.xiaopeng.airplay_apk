package org.fourthline.cling.support.model.item;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class AudioItem extends Item {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.audioItem");

    public AudioItem() {
        setClazz(CLASS);
    }

    public AudioItem(Item other) {
        super(other);
    }

    public AudioItem(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public AudioItem(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, CLASS);
        if (resource != null) {
            getResources().addAll(Arrays.asList(resource));
        }
    }

    public String getFirstGenre() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.GENRE.class);
    }

    public String[] getGenres() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.GENRE.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public AudioItem setGenres(String[] genres) {
        removeProperties(DIDLObject.Property.UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new DIDLObject.Property.UPNP.GENRE(genre));
        }
        return this;
    }

    public String getDescription() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DESCRIPTION.class);
    }

    public AudioItem setDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
    }

    public AudioItem setLongDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.LONG_DESCRIPTION(description));
        return this;
    }

    public Person getFirstPublisher() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.DC.PUBLISHER.class);
    }

    public Person[] getPublishers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.PUBLISHER.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public AudioItem setPublishers(Person[] publishers) {
        removeProperties(DIDLObject.Property.DC.PUBLISHER.class);
        for (Person publisher : publishers) {
            addProperty(new DIDLObject.Property.DC.PUBLISHER(publisher));
        }
        return this;
    }

    public URI getFirstRelation() {
        return (URI) getFirstPropertyValue(DIDLObject.Property.DC.RELATION.class);
    }

    public URI[] getRelations() {
        List<URI> list = getPropertyValues(DIDLObject.Property.DC.RELATION.class);
        return (URI[]) list.toArray(new URI[list.size()]);
    }

    public AudioItem setRelations(URI[] relations) {
        removeProperties(DIDLObject.Property.DC.RELATION.class);
        for (URI relation : relations) {
            addProperty(new DIDLObject.Property.DC.RELATION(relation));
        }
        return this;
    }

    public String getLanguage() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.LANGUAGE.class);
    }

    public AudioItem setLanguage(String language) {
        replaceFirstProperty(new DIDLObject.Property.DC.LANGUAGE(language));
        return this;
    }

    public String getFirstRights() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.RIGHTS.class);
    }

    public String[] getRights() {
        List<String> list = getPropertyValues(DIDLObject.Property.DC.RIGHTS.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public AudioItem setRights(String[] rights) {
        removeProperties(DIDLObject.Property.DC.RIGHTS.class);
        for (String right : rights) {
            addProperty(new DIDLObject.Property.DC.RIGHTS(right));
        }
        return this;
    }
}
