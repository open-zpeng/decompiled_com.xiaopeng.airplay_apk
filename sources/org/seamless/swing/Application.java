package org.seamless.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.eclipse.jetty.http.HttpHeaders;
/* loaded from: classes.dex */
public class Application {
    public static void showError(Component parent, Throwable ex) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Sans-Serif", 0, 10));
        textArea.setEditable(false);
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        textArea.setText(writer.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        JOptionPane.showMessageDialog(parent, scrollPane, "An Error Has Occurred", 0);
    }

    public static void showWarning(Component parent, String... warningLines) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Sans-Serif", 0, 10));
        textArea.setEditable(false);
        for (String s : warningLines) {
            textArea.append(s + "\n");
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        JOptionPane.showMessageDialog(parent, scrollPane, HttpHeaders.WARNING, 0);
    }

    public static void showInfo(Component parent, String... infoLines) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Sans-Serif", 0, 10));
        textArea.setEditable(false);
        for (String s : infoLines) {
            textArea.append(s + "\n");
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        JOptionPane.showMessageDialog(parent, scrollPane, "Info", 1);
    }

    public static void increaseFontSize(JComponent l) {
        l.setFont(new Font(l.getFont().getFontName(), l.getFont().getStyle(), l.getFont().getSize() + 2));
    }

    public static void decreaseFontSize(JComponent l) {
        l.setFont(new Font(l.getFont().getFontName(), l.getFont().getStyle(), l.getFont().getSize() - 2));
    }

    public static Window center(Window w) {
        Dimension us = w.getSize();
        Dimension them = Toolkit.getDefaultToolkit().getScreenSize();
        int newX = (them.width - us.width) / 2;
        int newY = (them.height - us.height) / 2;
        if (newX < 0) {
            newX = 0;
        }
        if (newY < 0) {
            newY = 0;
        }
        w.setLocation(newX, newY);
        return w;
    }

    public static Window center(Window w, Window reference) {
        double refCenterX = reference.getX() + (reference.getSize().getWidth() / 2.0d);
        double refCenterY = reference.getY() + (reference.getSize().getHeight() / 2.0d);
        int newX = (int) (refCenterX - (w.getSize().getWidth() / 2.0d));
        int newY = (int) (refCenterY - (w.getSize().getHeight() / 2.0d));
        w.setLocation(newX, newY);
        return w;
    }

    public static Window maximize(Window w) {
        w.getSize();
        Dimension them = Toolkit.getDefaultToolkit().getScreenSize();
        w.setBounds(0, 0, them.width, them.height);
        return w;
    }

    public static ImageIcon createImageIcon(Class base, String path, String description) {
        URL imgURL = base.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        }
        throw new RuntimeException("Couldn't find image icon on path: " + path);
    }

    public static ImageIcon createImageIcon(Class base, String path) {
        return createImageIcon(base, path, null);
    }

    public static void copyToClipboard(String s) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection data = new StringSelection(s);
        clipboard.setContents(data, data);
    }
}
