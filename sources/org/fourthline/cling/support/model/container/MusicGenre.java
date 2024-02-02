package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject;
/* loaded from: classes.dex */
public class MusicGenre extends GenreContainer {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.genre.musicGenre");

    public MusicGenre() {
        setClazz(CLASS);
    }

    public MusicGenre(Container other) {
        super(other);
    }

    public MusicGenre(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount);
    }

    public MusicGenre(String id, String parentID, String title, String creator, Integer childCount) {
        super(id, parentID, title, creator, childCount);
        setClazz(CLASS);
    }
}
