package org.seamless.swing;
/* loaded from: classes.dex */
public interface DefaultEventListener<PAYLOAD> extends EventListener<DefaultEvent<PAYLOAD>> {
    void handleEvent(DefaultEvent<PAYLOAD> defaultEvent);
}
