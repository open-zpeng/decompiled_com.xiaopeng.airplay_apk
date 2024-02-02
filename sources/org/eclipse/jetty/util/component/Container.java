package org.eclipse.jetty.util.component;

import java.lang.ref.WeakReference;
import java.util.EventListener;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class Container {
    private static final Logger LOG = Log.getLogger(Container.class);
    private final CopyOnWriteArrayList<Listener> _listeners = new CopyOnWriteArrayList<>();

    /* loaded from: classes.dex */
    public interface Listener extends EventListener {
        void add(Relationship relationship);

        void addBean(Object obj);

        void remove(Relationship relationship);

        void removeBean(Object obj);
    }

    public void addEventListener(Listener listener) {
        this._listeners.add(listener);
    }

    public void removeEventListener(Listener listener) {
        this._listeners.remove(listener);
    }

    public void update(Object parent, Object oldChild, Object child, String relationship) {
        if (oldChild != null && !oldChild.equals(child)) {
            remove(parent, oldChild, relationship);
        }
        if (child != null && !child.equals(oldChild)) {
            add(parent, child, relationship);
        }
    }

    public void update(Object parent, Object oldChild, Object child, String relationship, boolean addRemove) {
        if (oldChild != null && !oldChild.equals(child)) {
            remove(parent, oldChild, relationship);
            if (addRemove) {
                removeBean(oldChild);
            }
        }
        if (child != null && !child.equals(oldChild)) {
            if (addRemove) {
                addBean(child);
            }
            add(parent, child, relationship);
        }
    }

    public void update(Object parent, Object[] oldChildren, Object[] children, String relationship) {
        update(parent, oldChildren, children, relationship, false);
    }

    public void update(Object parent, Object[] oldChildren, Object[] children, String relationship, boolean addRemove) {
        Object[] newChildren = null;
        if (children != null) {
            newChildren = new Object[children.length];
            int i = children.length;
            while (true) {
                int i2 = i - 1;
                if (i <= 0) {
                    break;
                }
                boolean new_child = true;
                if (oldChildren != null) {
                    int j = oldChildren.length;
                    while (true) {
                        int j2 = j - 1;
                        if (j <= 0) {
                            break;
                        }
                        if (children[i2] != null && children[i2].equals(oldChildren[j2])) {
                            oldChildren[j2] = null;
                            new_child = false;
                        }
                        j = j2;
                    }
                }
                if (new_child) {
                    newChildren[i2] = children[i2];
                }
                i = i2;
            }
        }
        if (oldChildren != null) {
            int i3 = oldChildren.length;
            while (true) {
                int i4 = i3 - 1;
                if (i3 <= 0) {
                    break;
                }
                if (oldChildren[i4] != null) {
                    remove(parent, oldChildren[i4], relationship);
                    if (addRemove) {
                        removeBean(oldChildren[i4]);
                    }
                }
                i3 = i4;
            }
        }
        if (newChildren != null) {
            for (int i5 = 0; i5 < newChildren.length; i5++) {
                if (newChildren[i5] != null) {
                    if (addRemove) {
                        addBean(newChildren[i5]);
                    }
                    add(parent, newChildren[i5], relationship);
                }
            }
        }
    }

    public void addBean(Object obj) {
        if (this._listeners != null) {
            for (int i = 0; i < LazyList.size(this._listeners); i++) {
                Listener listener = (Listener) LazyList.get(this._listeners, i);
                listener.addBean(obj);
            }
        }
    }

    public void removeBean(Object obj) {
        if (this._listeners != null) {
            for (int i = 0; i < LazyList.size(this._listeners); i++) {
                ((Listener) LazyList.get(this._listeners, i)).removeBean(obj);
            }
        }
    }

    private void add(Object parent, Object child, String relationship) {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Container " + parent + " + " + child + " as " + relationship, new Object[0]);
        }
        if (this._listeners != null) {
            Relationship event = new Relationship(parent, child, relationship);
            for (int i = 0; i < LazyList.size(this._listeners); i++) {
                ((Listener) LazyList.get(this._listeners, i)).add(event);
            }
        }
    }

    private void remove(Object parent, Object child, String relationship) {
        if (LOG.isDebugEnabled()) {
            Logger logger = LOG;
            logger.debug("Container " + parent + " - " + child + " as " + relationship, new Object[0]);
        }
        if (this._listeners != null) {
            Relationship event = new Relationship(parent, child, relationship);
            for (int i = 0; i < LazyList.size(this._listeners); i++) {
                ((Listener) LazyList.get(this._listeners, i)).remove(event);
            }
        }
    }

    /* loaded from: classes.dex */
    public static class Relationship {
        private final WeakReference<Object> _child;
        private Container _container;
        private final WeakReference<Object> _parent;
        private String _relationship;

        private Relationship(Container container, Object parent, Object child, String relationship) {
            this._container = container;
            this._parent = new WeakReference<>(parent);
            this._child = new WeakReference<>(child);
            this._relationship = relationship;
        }

        public Container getContainer() {
            return this._container;
        }

        public Object getChild() {
            return this._child.get();
        }

        public Object getParent() {
            return this._parent.get();
        }

        public String getRelationship() {
            return this._relationship;
        }

        public String toString() {
            return this._parent + "---" + this._relationship + "-->" + this._child;
        }

        public int hashCode() {
            return this._parent.hashCode() + this._child.hashCode() + this._relationship.hashCode();
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof Relationship)) {
                return false;
            }
            Relationship r = (Relationship) o;
            return r._parent.get() == this._parent.get() && r._child.get() == this._child.get() && r._relationship.equals(this._relationship);
        }
    }
}
