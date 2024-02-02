package org.fourthline.cling.model.meta;
/* loaded from: classes.dex */
public class StateVariableEventDetails {
    private final int eventMaximumRateMilliseconds;
    private final int eventMinimumDelta;
    private final boolean sendEvents;

    public StateVariableEventDetails() {
        this(true, 0, 0);
    }

    public StateVariableEventDetails(boolean sendEvents) {
        this(sendEvents, 0, 0);
    }

    public StateVariableEventDetails(boolean sendEvents, int eventMaximumRateMilliseconds, int eventMinimumDelta) {
        this.sendEvents = sendEvents;
        this.eventMaximumRateMilliseconds = eventMaximumRateMilliseconds;
        this.eventMinimumDelta = eventMinimumDelta;
    }

    public boolean isSendEvents() {
        return this.sendEvents;
    }

    public int getEventMaximumRateMilliseconds() {
        return this.eventMaximumRateMilliseconds;
    }

    public int getEventMinimumDelta() {
        return this.eventMinimumDelta;
    }
}
