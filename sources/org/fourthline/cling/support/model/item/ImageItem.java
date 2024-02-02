package org.fourthline.cling.support.model.item;

import java.util.Arrays;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class ImageItem extends Item {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.imageItem");

    public ImageItem() {
        setClazz(CLASS);
    }

    public ImageItem(Item other) {
        super(other);
    }

    public ImageItem(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public ImageItem(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, CLASS);
        if (resource != null) {
            getResources().addAll(Arrays.asList(resource));
        }
    }

    public String getDescription() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DESCRIPTION.class);
    }

    public ImageItem setDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
    }

    public ImageItem setLongDescription(String description) {
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

    public ImageItem setPublishers(Person[] publishers) {
        removeProperties(DIDLObject.Property.DC.PUBLISHER.class);
        for (Person publisher : publishers) {
            addProperty(new DIDLObject.Property.DC.PUBLISHER(publisher));
        }
        return this;
    }

    public StorageMedium getStorageMedium() {
        return (StorageMedium) getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public ImageItem setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public String getRating() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.RATING.class);
    }

    public ImageItem setRating(String rating) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.RATING(rating));
        return this;
    }

    public String getDate() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.DATE.class);
    }

    public ImageItem setDate(String date) {
        replaceFirstProperty(new DIDLObject.Property.DC.DATE(date));
        return this;
    }

    public String getFirstRights() {
        return (String) getFirstPropertyValue(DIDLObject.Property.DC.RIGHTS.class);
    }

    public String[] getRights() {
        List<String> list = getPropertyValues(DIDLObject.Property.DC.RIGHTS.class);
        return (String[]) list.toArray(new String[list.size()]);
    }

    public ImageItem setRights(String[] rights) {
        removeProperties(DIDLObject.Property.DC.RIGHTS.class);
        for (String right : rights) {
            addProperty(new DIDLObject.Property.DC.RIGHTS(right));
        }
        return this;
    }
}
