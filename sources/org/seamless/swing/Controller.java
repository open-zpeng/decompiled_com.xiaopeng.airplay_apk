package org.seamless.swing;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.List;
import javax.swing.AbstractButton;
/* loaded from: classes.dex */
public interface Controller<V extends Container> extends ActionListener, WindowListener {
    void dispose();

    void failedActionExecute();

    void finalActionExecute();

    void fireEvent(Event event);

    void fireEvent(Event event, boolean z);

    void fireEventGlobal(Event event);

    Controller getParentController();

    List<Controller> getSubControllers();

    V getView();

    void postActionExecute();

    void preActionExecute();

    void registerAction(AbstractButton abstractButton, String str, DefaultAction defaultAction);

    void registerAction(AbstractButton abstractButton, DefaultAction defaultAction);

    void registerEventListener(Class cls, EventListener eventListener);
}
