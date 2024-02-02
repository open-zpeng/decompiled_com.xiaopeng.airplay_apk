package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject;
/* loaded from: classes.dex */
public class GenreContainer extends Container {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.genre");

    public GenreContainer() {
        setClazz(CLASS);
    }

    public GenreContainer(Container other) {
        super(other);
    }

    public GenreContainer(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount);
    }

    public GenreContainer(String id, String parentID, String title, String creator, Integer childCount) {
        super(id, parentID, title, creator, CLASS, childCount);
    }
}
