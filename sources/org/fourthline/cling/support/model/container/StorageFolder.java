package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject;
/* loaded from: classes.dex */
public class StorageFolder extends Container {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.storageFolder");

    public StorageFolder() {
        setClazz(CLASS);
    }

    public StorageFolder(Container other) {
        super(other);
    }

    public StorageFolder(String id, Container parent, String title, String creator, Integer childCount, Long storageUsed) {
        this(id, parent.getId(), title, creator, childCount, storageUsed);
    }

    public StorageFolder(String id, String parentID, String title, String creator, Integer childCount, Long storageUsed) {
        super(id, parentID, title, creator, CLASS, childCount);
        if (storageUsed != null) {
            setStorageUsed(storageUsed);
        }
    }

    public Long getStorageUsed() {
        return (Long) getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_USED.class);
    }

    public StorageFolder setStorageUsed(Long l) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_USED(l));
        return this;
    }
}
