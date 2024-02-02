package org.fourthline.cling.model.message;

import java.net.InetAddress;
/* loaded from: classes.dex */
public interface Connection {
    InetAddress getLocalAddress();

    InetAddress getRemoteAddress();

    boolean isOpen();
}
