package org.seamless.swing;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
/* loaded from: classes.dex */
public class ActionButton extends JButton {
    public ActionButton(String actionCommand) {
        setActionCommand(actionCommand);
    }

    public ActionButton(Icon icon, String actionCommand) {
        super(icon);
        setActionCommand(actionCommand);
    }

    public ActionButton(String s, String actionCommand) {
        super(s);
        setActionCommand(actionCommand);
    }

    public ActionButton(Action action, String actionCommand) {
        super(action);
        setActionCommand(actionCommand);
    }

    public ActionButton(String s, Icon icon, String actionCommand) {
        super(s, icon);
        setActionCommand(actionCommand);
    }

    public ActionButton enableDefaultEvents(final Controller controller) {
        controller.registerAction(this, new DefaultAction() { // from class: org.seamless.swing.ActionButton.1
            public void actionPerformed(ActionEvent actionEvent) {
                Event e = ActionButton.this.createDefaultEvent();
                if (e != null) {
                    controller.fireEvent(e);
                }
                Event e2 = ActionButton.this.createDefaultGlobalEvent();
                if (e2 != null) {
                    controller.fireEventGlobal(e2);
                }
            }
        });
        return this;
    }

    public Event createDefaultEvent() {
        return null;
    }

    public Event createDefaultGlobalEvent() {
        return null;
    }
}
