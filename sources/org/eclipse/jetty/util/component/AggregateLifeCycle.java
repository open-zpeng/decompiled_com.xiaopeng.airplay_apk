package org.eclipse.jetty.util.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class AggregateLifeCycle extends AbstractLifeCycle implements Destroyable, Dumpable {
    private static final Logger LOG = Log.getLogger(AggregateLifeCycle.class);
    private final List<Bean> _beans = new CopyOnWriteArrayList();
    private boolean _started = false;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Bean {
        final Object _bean;
        volatile boolean _managed = true;

        Bean(Object b) {
            this._bean = b;
        }

        public String toString() {
            return "{" + this._bean + "," + this._managed + "}";
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStart() throws Exception {
        for (Bean b : this._beans) {
            if (b._managed && (b._bean instanceof LifeCycle)) {
                LifeCycle l = (LifeCycle) b._bean;
                if (!l.isRunning()) {
                    l.start();
                }
            }
        }
        this._started = true;
        super.doStart();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.eclipse.jetty.util.component.AbstractLifeCycle
    public void doStop() throws Exception {
        this._started = false;
        super.doStop();
        List<Bean> reverse = new ArrayList<>(this._beans);
        Collections.reverse(reverse);
        for (Bean b : reverse) {
            if (b._managed && (b._bean instanceof LifeCycle)) {
                LifeCycle l = (LifeCycle) b._bean;
                if (l.isRunning()) {
                    l.stop();
                }
            }
        }
    }

    public void destroy() {
        List<Bean> reverse = new ArrayList<>(this._beans);
        Collections.reverse(reverse);
        for (Bean b : reverse) {
            if ((b._bean instanceof Destroyable) && b._managed) {
                Destroyable d = (Destroyable) b._bean;
                d.destroy();
            }
        }
        this._beans.clear();
    }

    public boolean contains(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean == bean) {
                return true;
            }
        }
        return false;
    }

    public boolean isManaged(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean == bean) {
                return b._managed;
            }
        }
        return false;
    }

    public boolean addBean(Object o) {
        return addBean(o, ((o instanceof LifeCycle) && ((LifeCycle) o).isStarted()) ? false : true);
    }

    public boolean addBean(Object o, boolean managed) {
        if (contains(o)) {
            return false;
        }
        Bean b = new Bean(o);
        b._managed = managed;
        this._beans.add(b);
        if (o instanceof LifeCycle) {
            LifeCycle l = (LifeCycle) o;
            if (managed && this._started) {
                try {
                    l.start();
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return true;
    }

    public void manage(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean == bean) {
                b._managed = true;
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    public void unmanage(Object bean) {
        for (Bean b : this._beans) {
            if (b._bean == bean) {
                b._managed = false;
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    public Collection<Object> getBeans() {
        return getBeans(Object.class);
    }

    public <T> List<T> getBeans(Class<T> clazz) {
        ArrayList arrayList = new ArrayList();
        for (Bean b : this._beans) {
            if (clazz.isInstance(b._bean)) {
                arrayList.add(b._bean);
            }
        }
        return arrayList;
    }

    public <T> T getBean(Class<T> clazz) {
        for (Bean b : this._beans) {
            if (clazz.isInstance(b._bean)) {
                return (T) b._bean;
            }
        }
        return null;
    }

    public void removeBeans() {
        this._beans.clear();
    }

    public boolean removeBean(Object o) {
        for (Bean b : this._beans) {
            if (b._bean == o) {
                this._beans.remove(b);
                return true;
            }
        }
        return false;
    }

    public void dumpStdErr() {
        try {
            dump(System.err, "");
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    public String dump() {
        return dump(this);
    }

    public static String dump(Dumpable dumpable) {
        StringBuilder b = new StringBuilder();
        try {
            dumpable.dump(b, "");
        } catch (IOException e) {
            LOG.warn(e);
        }
        return b.toString();
    }

    public void dump(Appendable out) throws IOException {
        dump(out, "");
    }

    protected void dumpThis(Appendable out) throws IOException {
        out.append(String.valueOf(this)).append(" - ").append(getState()).append("\n");
    }

    public static void dumpObject(Appendable out, Object o) throws IOException {
        try {
            if (o instanceof LifeCycle) {
                out.append(String.valueOf(o)).append(" - ").append(AbstractLifeCycle.getState((LifeCycle) o)).append("\n");
            } else {
                out.append(String.valueOf(o)).append("\n");
            }
        } catch (Throwable th) {
            out.append(" => ").append(th.toString()).append('\n');
        }
    }

    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        int size = this._beans.size();
        if (size == 0) {
            return;
        }
        int i = 0;
        for (Bean b : this._beans) {
            i++;
            out.append(indent).append(" +- ");
            if (b._managed) {
                if (b._bean instanceof Dumpable) {
                    Dumpable dumpable = (Dumpable) b._bean;
                    StringBuilder sb = new StringBuilder();
                    sb.append(indent);
                    sb.append(i == size ? "    " : " |  ");
                    dumpable.dump(out, sb.toString());
                } else {
                    dumpObject(out, b._bean);
                }
            } else {
                dumpObject(out, b._bean);
            }
        }
        if (i != size) {
            out.append(indent).append(" |\n");
        }
    }

    public static void dump(Appendable out, String indent, Collection<?>... collections) throws IOException {
        if (collections.length == 0) {
            return;
        }
        int size = 0;
        for (Collection<?> c : collections) {
            size += c.size();
        }
        if (size == 0) {
            return;
        }
        int i = 0;
        for (Collection<?> c2 : collections) {
            for (Object o : c2) {
                i++;
                out.append(indent).append(" +- ");
                if (o instanceof Dumpable) {
                    Dumpable dumpable = (Dumpable) o;
                    StringBuilder sb = new StringBuilder();
                    sb.append(indent);
                    sb.append(i == size ? "    " : " |  ");
                    dumpable.dump(out, sb.toString());
                } else {
                    dumpObject(out, o);
                }
            }
            if (i != size) {
                out.append(indent).append(" |\n");
            }
        }
    }
}
