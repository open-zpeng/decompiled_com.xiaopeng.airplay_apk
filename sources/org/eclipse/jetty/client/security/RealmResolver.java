package org.eclipse.jetty.client.security;

import java.io.IOException;
import org.eclipse.jetty.client.HttpDestination;
/* loaded from: classes.dex */
public interface RealmResolver {
    Realm getRealm(String str, HttpDestination httpDestination, String str2) throws IOException;
}
