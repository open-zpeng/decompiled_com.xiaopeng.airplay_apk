package org.fourthline.cling.support.model.item;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DescMeta;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
/* loaded from: classes.dex */
public class Item extends DIDLObject {
    protected String refID;

    public Item() {
    }

    public Item(Item other) {
        super(other);
        setRefID(other.getRefID());
    }

    public Item(String id, String parentID, String title, String creator, boolean restricted, WriteStatus writeStatus, DIDLObject.Class clazz, List<Res> resources, List<DIDLObject.Property> properties, List<DescMeta> descMetadata) {
        super(id, parentID, title, creator, restricted, writeStatus, clazz, resources, properties, descMetadata);
    }

    public Item(String id, String parentID, String title, String creator, boolean restricted, WriteStatus writeStatus, DIDLObject.Class clazz, List<Res> resources, List<DIDLObject.Property> properties, List<DescMeta> descMetadata, String refID) {
        super(id, parentID, title, creator, restricted, writeStatus, clazz, resources, properties, descMetadata);
        this.refID = refID;
    }

    public Item(String id, Container parent, String title, String creator, DIDLObject.Class clazz) {
        this(id, parent.getId(), title, creator, false, null, clazz, new ArrayList(), new ArrayList(), new ArrayList());
    }

    public Item(String id, Container parent, String title, String creator, DIDLObject.Class clazz, String refID) {
        this(id, parent.getId(), title, creator, false, null, clazz, new ArrayList(), new ArrayList(), new ArrayList(), refID);
    }

    public Item(String id, String parentID, String title, String creator, DIDLObject.Class clazz) {
        this(id, parentID, title, creator, false, null, clazz, new ArrayList(), new ArrayList(), new ArrayList());
    }

    public Item(String id, String parentID, String title, String creator, DIDLObject.Class clazz, String refID) {
        this(id, parentID, title, creator, false, null, clazz, new ArrayList(), new ArrayList(), new ArrayList(), refID);
    }

    public String getRefID() {
        return this.refID;
    }

    public void setRefID(String refID) {
        this.refID = refID;
    }
}
