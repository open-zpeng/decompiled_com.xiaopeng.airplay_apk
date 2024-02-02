package org.seamless.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JSeparator;
/* loaded from: classes.dex */
public class Form {
    public GridBagConstraints labelConstraints;
    public GridBagConstraints lastConstraints;
    public GridBagConstraints middleConstraints;
    public GridBagConstraints separatorConstraints;

    public Form(int padding) {
        this.lastConstraints = null;
        this.middleConstraints = null;
        this.labelConstraints = null;
        this.separatorConstraints = null;
        this.lastConstraints = new GridBagConstraints();
        this.lastConstraints.fill = 2;
        this.lastConstraints.anchor = 18;
        this.lastConstraints.weightx = 1.0d;
        this.lastConstraints.gridwidth = 0;
        this.lastConstraints.insets = new Insets(padding, padding, padding, padding);
        this.middleConstraints = (GridBagConstraints) this.lastConstraints.clone();
        this.middleConstraints.gridwidth = -1;
        this.labelConstraints = (GridBagConstraints) this.lastConstraints.clone();
        this.labelConstraints.weightx = 0.0d;
        this.labelConstraints.gridwidth = 1;
        this.separatorConstraints = new GridBagConstraints();
        this.separatorConstraints.fill = 2;
        this.separatorConstraints.gridwidth = 0;
    }

    public void addLastField(Component c, Container parent) {
        GridBagLayout gbl = parent.getLayout();
        gbl.setConstraints(c, this.lastConstraints);
        parent.add(c);
    }

    public void addLabel(Component c, Container parent) {
        GridBagLayout gbl = parent.getLayout();
        gbl.setConstraints(c, this.labelConstraints);
        parent.add(c);
    }

    public JLabel addLabel(String s, Container parent) {
        JLabel c = new JLabel(s);
        addLabel((Component) c, parent);
        return c;
    }

    public void addMiddleField(Component c, Container parent) {
        GridBagLayout gbl = parent.getLayout();
        gbl.setConstraints(c, this.middleConstraints);
        parent.add(c);
    }

    public void addLabelAndLastField(String s, Container c, Container parent) {
        addLabel(s, parent);
        addLastField(c, parent);
    }

    public void addLabelAndLastField(String s, String value, Container parent) {
        addLabel(s, parent);
        addLastField(new JLabel(value), parent);
    }

    public void addSeparator(Container parent) {
        JSeparator separator = new JSeparator();
        GridBagLayout gbl = parent.getLayout();
        gbl.setConstraints(separator, this.separatorConstraints);
        parent.add(separator);
    }
}
