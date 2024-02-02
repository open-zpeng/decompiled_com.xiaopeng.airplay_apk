package org.seamless.swing;

import java.util.List;
/* loaded from: classes.dex */
public interface Node<T> {
    List<T> getChildren();

    Long getId();

    T getParent();
}
