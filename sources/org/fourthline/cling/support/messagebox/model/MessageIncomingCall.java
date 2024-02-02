package org.fourthline.cling.support.messagebox.model;

import org.fourthline.cling.support.messagebox.model.Message;
import org.fourthline.cling.support.messagebox.parser.MessageElement;
/* loaded from: classes.dex */
public class MessageIncomingCall extends Message {
    private final DateTime callTime;
    private final NumberName callee;
    private final NumberName caller;

    public MessageIncomingCall(NumberName callee, NumberName caller) {
        this(new DateTime(), callee, caller);
    }

    public MessageIncomingCall(DateTime callTime, NumberName callee, NumberName caller) {
        this(Message.DisplayType.MAXIMUM, callTime, callee, caller);
    }

    public MessageIncomingCall(Message.DisplayType displayType, DateTime callTime, NumberName callee, NumberName caller) {
        super(Message.Category.INCOMING_CALL, displayType);
        this.callTime = callTime;
        this.callee = callee;
        this.caller = caller;
    }

    public DateTime getCallTime() {
        return this.callTime;
    }

    public NumberName getCallee() {
        return this.callee;
    }

    public NumberName getCaller() {
        return this.caller;
    }

    @Override // org.fourthline.cling.support.messagebox.model.ElementAppender
    public void appendMessageElements(MessageElement parent) {
        getCallTime().appendMessageElements(parent.createChild("CallTime"));
        getCallee().appendMessageElements(parent.createChild("Callee"));
        getCaller().appendMessageElements(parent.createChild("Caller"));
    }
}
