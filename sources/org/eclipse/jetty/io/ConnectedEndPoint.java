package org.eclipse.jetty.io;
/* loaded from: classes.dex */
public interface ConnectedEndPoint extends EndPoint {
    Connection getConnection();

    void setConnection(Connection connection);
}
