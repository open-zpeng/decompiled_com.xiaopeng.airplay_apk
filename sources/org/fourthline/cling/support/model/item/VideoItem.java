package org.fourthline.cling.support.model.item;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class VideoItem extends Item {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.videoItem");

    public VideoItem() {
        setClazz(CLASS);
    }

    public VideoItem(Item other) {
        super(other);
    }

    public VideoItem(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public VideoItem(String id, String parentID, String title, String creator, Res... resource) {
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

    public VideoItem setGenres(String[] genres) {
        removeProperties(DIDLObject.Property.UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new DIDLObject.Property.UPNP.GENRE(genre));
        }
        return this;
    }

    public String getDescription() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DESCRIPTION.class);
    }

    public VideoItem setDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
    }

    public VideoItem setLongDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.LONG_DESCRIPTION(description));
        return this;
    }

    public Person getFirstProducer() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.UPNP.PRODUCER.class);
    }

    public Person[] getProducers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.UPNP.PRODUCER.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public VideoItem setProducers(Person[] persons) {
        removeProperties(DIDLObject.Property.UPNP.PRODUCER.class);
        for (Person p : persons) {
            addProperty(new DIDLObject.Property.UPNP.PRODUCER(p));
        }
        return this;
    }

    public String getRating() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.RATING.class);
    }

    public VideoItem setRating(String rating) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.RATING(rating));
        return this;
    }

    public PersonWithRole getFirstActor() {
        return (PersonWithRole) getFirstPropertyValue(DIDLObject.Property.UPNP.ACTOR.class);
    }

    public PersonWithRole[] getActors() {
        List<PersonWithRole> list = getPropertyValues(DIDLObject.Property.UPNP.ACTOR.class);
        return (PersonWithRole[]) list.toArray(new PersonWithRole[list.size()]);
    }

    public VideoItem setActors(PersonWithRole[] persons) {
        removeProperties(DIDLObject.Property.UPNP.ACTOR.class);
        for (PersonWithRole p : persons) {
            addProperty(new DIDLObject.Property.UPNP.ACTOR(p));
        }
        return this;
    }

    public Person getFirstDirector() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.UPNP.DIRECTOR.class);
    }

    public Person[] getDirectors() {
        List<Person> list = getPropertyValues(DIDLObject.Property.UPNP.DIRECTOR.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public VideoItem setDirectors(Person[] persons) {
        removeProperties(DIDLObject.Property.UPNP.DIRECTOR.class);
        for (Person p : persons) {
            addProperty(new DIDLObject.Property.UPNP.DIRECTOR(p));
        }
        return this;
    }

    public Person getFirstPublisher() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.DC.PUBLISHER.class);
    }

    public Person[] getPublishers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.PUBLISHER.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public VideoItem setPublishers(Person[] publishers) {
        removeProperties(DIDLObject.Property.DC.PUBLISHER.class);
        for (Person publisher : publishers) {
            addProperty(new DIDLObject.Property.DC.PUBLISHER(publisher));
        }
        return this;
    }

    public String getLanguage() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.LANGUAGE.class);
    }

    public VideoItem setLanguage(String language) {
        replaceFirstProperty(new DIDLObject.Property.DC.LANGUAGE(language));
        return this;
    }

    public URI getFirstRelation() {
        return (URI) getFirstPropertyValue(DIDLObject.Property.DC.RELATION.class);
    }

    public URI[] getRelations() {
        List<URI> list = getPropertyValues(DIDLObject.Property.DC.RELATION.class);
        return (URI[]) list.toArray(new URI[list.size()]);
    }

    public VideoItem setRelations(URI[] relations) {
        removeProperties(DIDLObject.Property.DC.RELATION.class);
        for (URI relation : relations) {
            addProperty(new DIDLObject.Property.DC.RELATION(relation));
        }
        return this;
    }
}
