package org.eclipse.jetty.util.component;

import java.io.IOException;
/* loaded from: classes.dex */
public interface Dumpable {
    String dump();

    void dump(Appendable appendable, String str) throws IOException;
}
