package org.seamless.util.mail;
/* loaded from: classes.dex */
public class Email {
    protected String html;
    protected String plaintext;
    protected String recipient;
    protected String sender;
    protected String subject;

    public Email(String sender, String recipient, String subject, String plaintext) {
        this(sender, recipient, subject, plaintext, null);
    }

    public Email(String sender, String recipient, String subject, String plaintext, String html) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.plaintext = plaintext;
        this.html = html;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPlaintext() {
        return this.plaintext;
    }

    public void setPlaintext(String plaintext) {
        this.plaintext = plaintext;
    }

    public String getHtml() {
        return this.html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
