package org.seamless.swing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
/* loaded from: classes.dex */
public class JPopupMenuButton extends JButton {
    public JPopupMenu popup;

    public JPopupMenuButton(JPopupMenu m) {
        this.popup = m;
        enableEvents(8L);
        enableEvents(16L);
    }

    public JPopupMenuButton(String s, JPopupMenu m) {
        super(s);
        this.popup = m;
        enableEvents(8L);
        enableEvents(16L);
    }

    public JPopupMenuButton(String s, Icon icon, JPopupMenu popup) {
        super(s, icon);
        this.popup = popup;
        enableEvents(8L);
        enableEvents(16L);
        setModel(new DefaultButtonModel() { // from class: org.seamless.swing.JPopupMenuButton.1
            public void setPressed(boolean b) {
            }
        });
    }

    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        int id = e.getID();
        if (id == 501) {
            if (this.popup != null) {
                this.popup.show(this, 0, 0);
            }
        } else if (id == 502 && this.popup != null) {
            this.popup.setVisible(false);
        }
    }

    protected void processKeyEvent(KeyEvent e) {
        int id = e.getID();
        if (id == 401 || id == 400) {
            if (e.getKeyCode() == 10) {
                this.popup.show(this, 0, 10);
            }
            super.processKeyEvent(e);
        }
    }
}
