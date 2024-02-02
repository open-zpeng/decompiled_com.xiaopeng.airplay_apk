package org.fourthline.cling.support.messagebox.model;

import org.eclipse.jetty.http.HttpHeaders;
import org.fourthline.cling.support.messagebox.model.Message;
import org.fourthline.cling.support.messagebox.parser.MessageElement;
/* loaded from: classes.dex */
public class MessageScheduleReminder extends Message {
    private final String body;
    private final DateTime endTime;
    private final String location;
    private final NumberName owner;
    private final DateTime startTime;
    private final String subject;

    public MessageScheduleReminder(DateTime startTime, NumberName owner, String subject, DateTime endTime, String location, String body) {
        this(Message.DisplayType.MAXIMUM, startTime, owner, subject, endTime, location, body);
    }

    public MessageScheduleReminder(Message.DisplayType displayType, DateTime startTime, NumberName owner, String subject, DateTime endTime, String location, String body) {
        super(Message.Category.SCHEDULE_REMINDER, displayType);
        this.startTime = startTime;
        this.owner = owner;
        this.subject = subject;
        this.endTime = endTime;
        this.location = location;
        this.body = body;
    }

    public DateTime getStartTime() {
        return this.startTime;
    }

    public NumberName getOwner() {
        return this.owner;
    }

    public String getSubject() {
        return this.subject;
    }

    public DateTime getEndTime() {
        return this.endTime;
    }

    public String getLocation() {
        return this.location;
    }

    public String getBody() {
        return this.body;
    }

    @Override // org.fourthline.cling.support.messagebox.model.ElementAppender
    public void appendMessageElements(MessageElement parent) {
        getStartTime().appendMessageElements(parent.createChild("StartTime"));
        getOwner().appendMessageElements(parent.createChild("Owner"));
        parent.createChild("Subject").setContent(getSubject());
        getEndTime().appendMessageElements(parent.createChild("EndTime"));
        parent.createChild(HttpHeaders.LOCATION).setContent(getLocation());
        parent.createChild("Body").setContent(getBody());
    }
}
