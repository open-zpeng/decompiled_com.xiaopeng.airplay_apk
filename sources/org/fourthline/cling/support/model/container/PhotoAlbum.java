package org.fourthline.cling.support.model.container;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Photo;
/* loaded from: classes.dex */
public class PhotoAlbum extends Album {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.album.photoAlbum");

    public PhotoAlbum() {
        setClazz(CLASS);
    }

    public PhotoAlbum(Container other) {
        super(other);
    }

    public PhotoAlbum(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount, new ArrayList());
    }

    public PhotoAlbum(String id, Container parent, String title, String creator, Integer childCount, List<Photo> photos) {
        this(id, parent.getId(), title, creator, childCount, photos);
    }

    public PhotoAlbum(String id, String parentID, String title, String creator, Integer childCount) {
        this(id, parentID, title, creator, childCount, new ArrayList());
    }

    public PhotoAlbum(String id, String parentID, String title, String creator, Integer childCount, List<Photo> photos) {
        super(id, parentID, title, creator, childCount);
        setClazz(CLASS);
        addPhotos(photos);
    }

    public Photo[] getPhotos() {
        List<Photo> list = new ArrayList<>();
        for (Item item : getItems()) {
            if (item instanceof Photo) {
                list.add((Photo) item);
            }
        }
        return (Photo[]) list.toArray(new Photo[list.size()]);
    }

    public void addPhotos(List<Photo> photos) {
        addPhotos((Photo[]) photos.toArray(new Photo[photos.size()]));
    }

    public void addPhotos(Photo[] photos) {
        if (photos != null) {
            for (Photo photo : photos) {
                photo.setAlbum(getTitle());
                addItem(photo);
            }
        }
    }
}
