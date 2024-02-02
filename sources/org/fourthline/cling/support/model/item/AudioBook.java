package org.fourthline.cling.support.model.item;

import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class AudioBook extends AudioItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.audioItem.audioBook");

    public AudioBook() {
        setClazz(CLASS);
    }

    public AudioBook(Item other) {
        super(other);
    }

    public AudioBook(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, (Person) null, (Person) null, (String) null, resource);
    }

    public AudioBook(String id, Container parent, String title, String creator, String producer, String contributor, String date, Res... resource) {
        this(id, parent.getId(), title, creator, new Person(producer), new Person(contributor), date, resource);
    }

    public AudioBook(String id, String parentID, String title, String creator, Person producer, Person contributor, String date, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
        if (producer != null) {
            addProperty(new DIDLObject.Property.UPNP.PRODUCER(producer));
        }
        if (contributor != null) {
            addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(contributor));
        }
        if (date != null) {
            setDate(date);
        }
    }

    public StorageMedium getStorageMedium() {
        return (StorageMedium) getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public AudioBook setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public Person getFirstProducer() {
        return (Person) getFirstPropertyValue(DIDLObject.Property.UPNP.PRODUCER.class);
    }

    public Person[] getProducers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.UPNP.PRODUCER.class);
        return (Person[]) list.toArray(new Person[list.size()]);
    }

    public AudioBook setProducers(Person[] persons) {
        removeProperties(DIDLObject.Property.UPNP.PRODUCER.class);
        for (Person p : persons) {
            addProperty(new DIDLObject.Property.UPNP.PRODUCER(p));
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

    public AudioBook setContributors(Person[] contributors) {
        removeProperties(DIDLObject.Property.DC.CONTRIBUTOR.class);
        for (Person p : contributors) {
            addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(p));
        }
        return this;
    }

    public String getDate() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DATE.class);
    }

    public AudioBook setDate(String date) {
        replaceFirstProperty(new DIDLObject.Property.DC.DATE(date));
        return this;
    }
}
