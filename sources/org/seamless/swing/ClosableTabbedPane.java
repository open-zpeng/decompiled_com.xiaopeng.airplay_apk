package org.seamless.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.text.View;
/* loaded from: classes.dex */
public class ClosableTabbedPane extends JTabbedPane implements MouseListener, MouseMotionListener {
    private EventListenerList listenerList = null;
    private JViewport headerViewport = null;
    private Icon normalCloseIcon = null;
    private Icon hooverCloseIcon = null;
    private Icon pressedCloseIcon = null;

    public ClosableTabbedPane() {
        init(2);
    }

    public ClosableTabbedPane(int horizontalTextPosition) {
        init(horizontalTextPosition);
    }

    private void init(int horizontalTextPosition) {
        this.listenerList = new EventListenerList();
        addMouseListener(this);
        addMouseMotionListener(this);
        if (getUI() instanceof MetalTabbedPaneUI) {
            setUI(new CloseableMetalTabbedPaneUI(horizontalTextPosition));
        } else {
            setUI(new CloseableTabbedPaneUI(horizontalTextPosition));
        }
    }

    public void setCloseIcons(Icon normal, Icon hoover, Icon pressed) {
        this.normalCloseIcon = normal;
        this.hooverCloseIcon = hoover;
        this.pressedCloseIcon = pressed;
    }

    public void addTab(String title, Component component) {
        addTab(title, component, null);
    }

    public void addTab(String title, Component component, Icon extraIcon) {
        JViewport[] components;
        boolean doPaintCloseIcon = true;
        try {
            Object prop = ((JComponent) component).getClientProperty("isClosable");
            if (prop != null) {
                doPaintCloseIcon = ((Boolean) prop).booleanValue();
            }
        } catch (Exception e) {
        }
        super.addTab(title, doPaintCloseIcon ? new CloseTabIcon(extraIcon) : null, component);
        if (this.headerViewport == null) {
            for (JViewport jViewport : getComponents()) {
                if ("TabbedPane.scrollableViewport".equals(jViewport.getName())) {
                    this.headerViewport = jViewport;
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        processMouseEvents(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        for (int i = 0; i < getTabCount(); i++) {
            CloseTabIcon icon = (CloseTabIcon) getIconAt(i);
            if (icon != null) {
                icon.mouseover = false;
            }
        }
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        processMouseEvents(e);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        processMouseEvents(e);
    }

    public void mouseMoved(MouseEvent e) {
        processMouseEvents(e);
    }

    private void processMouseEvents(MouseEvent e) {
        CloseTabIcon icon;
        int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if (tabNumber >= 0 && (icon = (CloseTabIcon) getIconAt(tabNumber)) != null) {
            Rectangle rect = icon.getBounds();
            Point pos = this.headerViewport == null ? new Point() : this.headerViewport.getViewPosition();
            Rectangle drawRect = new Rectangle(rect.x - pos.x, rect.y - pos.y, rect.width, rect.height);
            if (e.getID() != 501) {
                if (e.getID() == 503 || e.getID() == 506 || e.getID() == 500) {
                    pos.x += e.getX();
                    pos.y += e.getY();
                    if (rect.contains(pos)) {
                        if (e.getID() == 500) {
                            int selIndex = getSelectedIndex();
                            if (fireCloseTab(selIndex)) {
                                if (selIndex > 0) {
                                    Rectangle rec = getUI().getTabBounds(this, selIndex - 1);
                                    MouseEvent event = new MouseEvent((Component) e.getSource(), e.getID() + 1, System.currentTimeMillis(), e.getModifiers(), rec.x, rec.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
                                    dispatchEvent(event);
                                }
                                remove(selIndex);
                            } else {
                                icon.mouseover = false;
                                icon.mousepressed = false;
                                repaint(drawRect);
                            }
                        } else {
                            icon.mouseover = true;
                            icon.mousepressed = e.getModifiers() == 16;
                        }
                    } else {
                        icon.mouseover = false;
                    }
                    repaint(drawRect);
                    return;
                }
                return;
            }
            icon.mousepressed = e.getModifiers() == 16;
            repaint(drawRect);
        }
    }

    public void addCloseableTabbedPaneListener(ClosableTabbedPaneListener l) {
        this.listenerList.add(ClosableTabbedPaneListener.class, l);
    }

    public void removeCloseableTabbedPaneListener(ClosableTabbedPaneListener l) {
        this.listenerList.remove(ClosableTabbedPaneListener.class, l);
    }

    public ClosableTabbedPaneListener[] getCloseableTabbedPaneListener() {
        return (ClosableTabbedPaneListener[]) this.listenerList.getListeners(ClosableTabbedPaneListener.class);
    }

    protected boolean fireCloseTab(int tabIndexToClose) {
        Object[] listeners = this.listenerList.getListenerList();
        for (Object i : listeners) {
            if ((i instanceof ClosableTabbedPaneListener) && !((ClosableTabbedPaneListener) i).closeTab(tabIndexToClose)) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class CloseTabIcon implements Icon {
        private Icon fileIcon;
        private int x_pos;
        private int y_pos;
        private boolean mouseover = false;
        private boolean mousepressed = false;
        private int width = 16;
        private int height = 16;

        public CloseTabIcon(Icon fileIcon) {
            this.fileIcon = fileIcon;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            boolean doPaintCloseIcon = true;
            try {
                JTabbedPane tabbedpane = (JTabbedPane) c;
                int tabNumber = tabbedpane.getUI().tabForCoordinate(tabbedpane, x, y);
                JComponent curPanel = tabbedpane.getComponentAt(tabNumber);
                Object prop = curPanel.getClientProperty("isClosable");
                if (prop != null) {
                    doPaintCloseIcon = ((Boolean) prop).booleanValue();
                }
            } catch (Exception e) {
            }
            if (doPaintCloseIcon) {
                this.x_pos = x;
                this.y_pos = y;
                int y_p = y + 1;
                if (ClosableTabbedPane.this.normalCloseIcon == null || this.mouseover) {
                    if (ClosableTabbedPane.this.hooverCloseIcon == null || !this.mouseover || this.mousepressed) {
                        if (ClosableTabbedPane.this.pressedCloseIcon != null && this.mousepressed) {
                            ClosableTabbedPane.this.pressedCloseIcon.paintIcon(c, g, x, y_p);
                            return;
                        }
                        int y_p2 = y_p + 1;
                        Color col = g.getColor();
                        if (this.mousepressed && this.mouseover) {
                            g.setColor(Color.WHITE);
                            g.fillRect(x + 1, y_p2, 12, 13);
                        }
                        g.setColor(Color.black);
                        g.drawLine(x + 1, y_p2, x + 12, y_p2);
                        g.drawLine(x + 1, y_p2 + 13, x + 12, y_p2 + 13);
                        g.drawLine(x, y_p2 + 1, x, y_p2 + 12);
                        g.drawLine(x + 13, y_p2 + 1, x + 13, y_p2 + 12);
                        g.drawLine(x + 3, y_p2 + 3, x + 10, y_p2 + 10);
                        if (this.mouseover) {
                            g.setColor(Color.GRAY);
                        }
                        g.drawLine(x + 3, y_p2 + 4, x + 9, y_p2 + 10);
                        g.drawLine(x + 4, y_p2 + 3, x + 10, y_p2 + 9);
                        g.drawLine(x + 10, y_p2 + 3, x + 3, y_p2 + 10);
                        g.drawLine(x + 10, y_p2 + 4, x + 4, y_p2 + 10);
                        g.drawLine(x + 9, y_p2 + 3, x + 3, y_p2 + 9);
                        g.setColor(col);
                        if (this.fileIcon != null) {
                            this.fileIcon.paintIcon(c, g, this.width + x, y_p2);
                            return;
                        }
                        return;
                    }
                    ClosableTabbedPane.this.hooverCloseIcon.paintIcon(c, g, x, y_p);
                    return;
                }
                ClosableTabbedPane.this.normalCloseIcon.paintIcon(c, g, x, y_p);
            }
        }

        public int getIconWidth() {
            return this.width + (this.fileIcon != null ? this.fileIcon.getIconWidth() : 0);
        }

        public int getIconHeight() {
            return this.height;
        }

        public Rectangle getBounds() {
            return new Rectangle(this.x_pos, this.y_pos, this.width, this.height);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class CloseableTabbedPaneUI extends BasicTabbedPaneUI {
        private int horizontalTextPosition;

        public CloseableTabbedPaneUI() {
            this.horizontalTextPosition = 2;
        }

        public CloseableTabbedPaneUI(int horizontalTextPosition) {
            this.horizontalTextPosition = 2;
            this.horizontalTextPosition = horizontalTextPosition;
        }

        protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            iconRect.y = 0;
            iconRect.x = 0;
            textRect.y = 0;
            textRect.x = 0;
            View v = getTextViewForTab(tabIndex);
            if (v != null) {
                this.tabPane.putClientProperty("html", v);
            }
            SwingUtilities.layoutCompoundLabel(this.tabPane, metrics, title, icon, 0, 0, 0, this.horizontalTextPosition, tabRect, iconRect, textRect, this.textIconGap + 2);
            this.tabPane.putClientProperty("html", (Object) null);
            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
            iconRect.x += xNudge;
            iconRect.y += yNudge;
            textRect.x += xNudge;
            textRect.y += yNudge;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class CloseableMetalTabbedPaneUI extends MetalTabbedPaneUI {
        private int horizontalTextPosition;

        public CloseableMetalTabbedPaneUI() {
            this.horizontalTextPosition = 2;
        }

        public CloseableMetalTabbedPaneUI(int horizontalTextPosition) {
            this.horizontalTextPosition = 2;
            this.horizontalTextPosition = horizontalTextPosition;
        }

        protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            iconRect.y = 0;
            iconRect.x = 0;
            textRect.y = 0;
            textRect.x = 0;
            View v = getTextViewForTab(tabIndex);
            if (v != null) {
                this.tabPane.putClientProperty("html", v);
            }
            SwingUtilities.layoutCompoundLabel(this.tabPane, metrics, title, icon, 0, 0, 0, this.horizontalTextPosition, tabRect, iconRect, textRect, this.textIconGap + 2);
            this.tabPane.putClientProperty("html", (Object) null);
            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
            iconRect.x += xNudge;
            iconRect.y += yNudge;
            textRect.x += xNudge;
            textRect.y += yNudge;
        }
    }
}
