package org.fourthline.cling.support.messagebox.model;

import java.util.Random;
import org.fourthline.cling.support.messagebox.parser.MessageDOM;
import org.fourthline.cling.support.messagebox.parser.MessageDOMParser;
import org.fourthline.cling.support.messagebox.parser.MessageElement;
import org.seamless.xml.DOM;
import org.seamless.xml.ParserException;
/* loaded from: classes.dex */
public abstract class Message implements ElementAppender {
    private final Category category;
    private DisplayType displayType;
    private final int id;
    protected final Random randomGenerator;

    /* loaded from: classes.dex */
    public enum Category {
        SMS("SMS"),
        INCOMING_CALL("Incoming Call"),
        SCHEDULE_REMINDER("Schedule Reminder");
        
        public String text;

        Category(String text) {
            this.text = text;
        }
    }

    /* loaded from: classes.dex */
    public enum DisplayType {
        MINIMUM("Minimum"),
        MAXIMUM("Maximum");
        
        public String text;

        DisplayType(String text) {
            this.text = text;
        }
    }

    public Message(Category category, DisplayType displayType) {
        this(0, category, displayType);
    }

    public Message(int id, Category category, DisplayType displayType) {
        this.randomGenerator = new Random();
        this.id = id == 0 ? this.randomGenerator.nextInt(Integer.MAX_VALUE) : id;
        this.category = category;
        this.displayType = displayType;
    }

    public int getId() {
        return this.id;
    }

    public Category getCategory() {
        return this.category;
    }

    public DisplayType getDisplayType() {
        return this.displayType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        if (this.id == message.id) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.id;
    }

    public String toString() {
        try {
            MessageDOMParser mp = new MessageDOMParser();
            MessageDOM dom = (MessageDOM) mp.createDocument();
            MessageElement root = dom.createRoot(mp.createXPath(), "Message");
            root.createChild("Category").setContent(getCategory().text);
            root.createChild("DisplayType").setContent(getDisplayType().text);
            appendMessageElements(root);
            String s = mp.print((DOM) dom, 0, false);
            return s.replaceAll("<Message xmlns=\"urn:samsung-com:messagebox-1-0\">", "").replaceAll("</Message>", "");
        } catch (ParserException ex) {
            throw new RuntimeException(ex);
        }
    }
}
