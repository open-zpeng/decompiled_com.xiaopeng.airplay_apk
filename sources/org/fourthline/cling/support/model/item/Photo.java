package org.fourthline.cling.support.model.item;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class Photo extends ImageItem {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.item.imageItem.photo");

    public Photo() {
        setClazz(CLASS);
    }

    public Photo(Item other) {
        super(other);
    }

    public Photo(String id, Container parent, String title, String creator, String album, Res... resource) {
        this(id, parent.getId(), title, creator, album, resource);
    }

    public Photo(String id, String parentID, String title, String creator, String album, Res... resource) {
        super(id, parentID, title, creator, resource);
        setClazz(CLASS);
        if (album != null) {
            setAlbum(album);
        }
    }

    public String getAlbum() {
        return (String) getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM.class);
    }

    public Photo setAlbum(String album) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.ALBUM(album));
        return this;
    }
}
