package javax.enterprise.context.control;

import javax.enterprise.context.ContextNotActiveException;
/* loaded from: classes.dex */
public interface RequestContextController {
    boolean activate();

    void deactivate() throws ContextNotActiveException;
}
