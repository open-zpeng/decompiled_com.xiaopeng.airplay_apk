package org.seamless.util.dbunit;

import org.dbunit.database.IDatabaseConnection;
/* loaded from: classes.dex */
public abstract class MySQLDBUnitOperations extends DBUnitOperations {
    @Override // org.seamless.util.dbunit.DBUnitOperations
    protected void disableReferentialIntegrity(IDatabaseConnection con) {
        try {
            con.getConnection().prepareStatement("set foreign_key_checks=0").execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override // org.seamless.util.dbunit.DBUnitOperations
    protected void enableReferentialIntegrity(IDatabaseConnection con) {
        try {
            con.getConnection().prepareStatement("set foreign_key_checks=1").execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
