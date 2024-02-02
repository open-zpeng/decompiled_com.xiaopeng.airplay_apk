package org.fourthline.cling.support.messagebox.model;

import org.fourthline.cling.support.messagebox.parser.MessageElement;
/* loaded from: classes.dex */
public class NumberName implements ElementAppender {
    private String name;
    private String number;

    public NumberName(String number, String name) {
        this.number = number;
        this.name = name;
    }

    public String getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    @Override // org.fourthline.cling.support.messagebox.model.ElementAppender
    public void appendMessageElements(MessageElement parent) {
        parent.createChild("Number").setContent(getNumber());
        parent.createChild("Name").setContent(getName());
    }
}
