package org.eclipse.jetty.client.security;
/* loaded from: classes.dex */
public interface Realm {
    String getCredentials();

    String getId();

    String getPrincipal();
}
