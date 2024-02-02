package org.eclipse.jetty.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
/* loaded from: classes.dex */
public class MultiException extends Exception {
    private Object nested;

    public MultiException() {
        super("Multiple exceptions");
    }

    public void add(Throwable e) {
        if (e instanceof MultiException) {
            MultiException me = (MultiException) e;
            for (int i = 0; i < LazyList.size(me.nested); i++) {
                this.nested = LazyList.add(this.nested, LazyList.get(me.nested, i));
            }
            return;
        }
        this.nested = LazyList.add(this.nested, e);
    }

    public int size() {
        return LazyList.size(this.nested);
    }

    public List<Throwable> getThrowables() {
        return LazyList.getList(this.nested);
    }

    public Throwable getThrowable(int i) {
        return (Throwable) LazyList.get(this.nested, i);
    }

    public void ifExceptionThrow() throws Exception {
        switch (LazyList.size(this.nested)) {
            case 0:
                return;
            case 1:
                Throwable th = (Throwable) LazyList.get(this.nested, 0);
                if (th instanceof Error) {
                    throw ((Error) th);
                }
                if (th instanceof Exception) {
                    throw ((Exception) th);
                }
                break;
        }
        throw this;
    }

    public void ifExceptionThrowRuntime() throws Error {
        switch (LazyList.size(this.nested)) {
            case 0:
                return;
            case 1:
                Throwable th = (Throwable) LazyList.get(this.nested, 0);
                if (th instanceof Error) {
                    throw ((Error) th);
                }
                if (th instanceof RuntimeException) {
                    throw ((RuntimeException) th);
                }
                throw new RuntimeException(th);
            default:
                throw new RuntimeException(this);
        }
    }

    public void ifExceptionThrowMulti() throws MultiException {
        if (LazyList.size(this.nested) > 0) {
            throw this;
        }
    }

    @Override // java.lang.Throwable
    public String toString() {
        if (LazyList.size(this.nested) > 0) {
            return MultiException.class.getSimpleName() + LazyList.getList(this.nested);
        }
        return MultiException.class.getSimpleName() + "[]";
    }

    @Override // java.lang.Throwable
    public void printStackTrace() {
        super.printStackTrace();
        for (int i = 0; i < LazyList.size(this.nested); i++) {
            ((Throwable) LazyList.get(this.nested, i)).printStackTrace();
        }
    }

    @Override // java.lang.Throwable
    public void printStackTrace(PrintStream out) {
        super.printStackTrace(out);
        for (int i = 0; i < LazyList.size(this.nested); i++) {
            ((Throwable) LazyList.get(this.nested, i)).printStackTrace(out);
        }
    }

    @Override // java.lang.Throwable
    public void printStackTrace(PrintWriter out) {
        super.printStackTrace(out);
        for (int i = 0; i < LazyList.size(this.nested); i++) {
            ((Throwable) LazyList.get(this.nested, i)).printStackTrace(out);
        }
    }
}
