package org.seamless.util.dbunit;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
/* loaded from: classes.dex */
public abstract class H2DBUnitOperations extends DBUnitOperations {
    @Override // org.seamless.util.dbunit.DBUnitOperations
    protected void disableReferentialIntegrity(IDatabaseConnection con) {
        try {
            con.getConnection().prepareStatement("set referential_integrity FALSE").execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override // org.seamless.util.dbunit.DBUnitOperations
    protected void enableReferentialIntegrity(IDatabaseConnection con) {
        try {
            con.getConnection().prepareStatement("set referential_integrity TRUE").execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.seamless.util.dbunit.DBUnitOperations
    public void editConfig(DatabaseConfig config) {
        super.editConfig(config);
        config.setProperty("http://www.dbunit.org/properties/datatypeFactory", new DefaultDataTypeFactory() { // from class: org.seamless.util.dbunit.H2DBUnitOperations.1
            public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
                if (sqlType == 16) {
                    return DataType.BOOLEAN;
                }
                return super.createDataType(sqlType, sqlTypeName);
            }
        });
    }
}
