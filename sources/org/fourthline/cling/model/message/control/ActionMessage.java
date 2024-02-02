package org.fourthline.cling.model.message.control;
/* loaded from: classes.dex */
public interface ActionMessage {
    String getActionNamespace();

    String getBodyString();

    boolean isBodyNonEmptyString();

    void setBody(String str);
}
