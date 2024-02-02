package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject;
/* loaded from: classes.dex */
public class MovieGenre extends GenreContainer {
    public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.genre.movieGenre");

    public MovieGenre() {
        setClazz(CLASS);
    }

    public MovieGenre(Container other) {
        super(other);
    }

    public MovieGenre(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount);
    }

    public MovieGenre(String id, String parentID, String title, String creator, Integer childCount) {
        super(id, parentID, title, creator, childCount);
        setClazz(CLASS);
    }
}
