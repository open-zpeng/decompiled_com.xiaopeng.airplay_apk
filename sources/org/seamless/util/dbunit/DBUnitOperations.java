package org.seamless.util.dbunit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
/* loaded from: classes.dex */
public abstract class DBUnitOperations extends ArrayList<Op> {
    private static final Logger log = Logger.getLogger(DBUnitOperations.class.getName());

    protected abstract void disableReferentialIntegrity(IDatabaseConnection iDatabaseConnection);

    protected abstract void enableReferentialIntegrity(IDatabaseConnection iDatabaseConnection);

    public abstract DataSource getDataSource();

    /* loaded from: classes.dex */
    public static abstract class Op {
        ReplacementDataSet dataSet;
        DatabaseOperation operation;

        protected abstract InputStream openStream(String str);

        public Op(String dataLocation) {
            this(dataLocation, null, DatabaseOperation.CLEAN_INSERT);
        }

        public Op(String dataLocation, String dtdLocation) {
            this(dataLocation, dtdLocation, DatabaseOperation.CLEAN_INSERT);
        }

        public Op(String dataLocation, String dtdLocation, DatabaseOperation operation) {
            try {
                this.dataSet = dtdLocation != null ? new ReplacementDataSet(new FlatXmlDataSet(openStream(dataLocation), openStream(dtdLocation))) : new ReplacementDataSet(new FlatXmlDataSet(openStream(dataLocation)));
                this.dataSet.addReplacementObject("[NULL]", (Object) null);
                this.operation = operation;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public IDataSet getDataSet() {
            return this.dataSet;
        }

        public DatabaseOperation getOperation() {
            return this.operation;
        }

        public void execute(IDatabaseConnection connection) {
            try {
                this.operation.execute(connection, this.dataSet);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /* loaded from: classes.dex */
    public static class ClasspathOp extends Op {
        public ClasspathOp(String dataLocation) {
            super(dataLocation);
        }

        public ClasspathOp(String dataLocation, String dtdLocation) {
            super(dataLocation, dtdLocation);
        }

        public ClasspathOp(String dataLocation, String dtdLocation, DatabaseOperation operation) {
            super(dataLocation, dtdLocation, operation);
        }

        @Override // org.seamless.util.dbunit.DBUnitOperations.Op
        protected InputStream openStream(String location) {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
        }
    }

    /* loaded from: classes.dex */
    public class FileOp extends Op {
        public FileOp(String dataLocation) {
            super(dataLocation);
        }

        public FileOp(String dataLocation, String dtdLocation) {
            super(dataLocation, dtdLocation);
        }

        public FileOp(String dataLocation, String dtdLocation, DatabaseOperation operation) {
            super(dataLocation, dtdLocation, operation);
        }

        @Override // org.seamless.util.dbunit.DBUnitOperations.Op
        protected InputStream openStream(String location) {
            try {
                return new FileInputStream(location);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void execute() {
        Logger logger = log;
        logger.info("Executing DBUnit operations: " + size());
        IDatabaseConnection con = null;
        try {
            con = getConnection();
            disableReferentialIntegrity(con);
            Iterator<Op> it = iterator();
            while (it.hasNext()) {
                Op op = it.next();
                op.execute(con);
            }
            enableReferentialIntegrity(con);
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                    Logger logger2 = log;
                    Level level = Level.WARNING;
                    logger2.log(level, "Failed to close connection after DBUnit operation: " + ex, (Throwable) ex);
                }
            }
        } catch (Throwable th) {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex2) {
                    Logger logger3 = log;
                    Level level2 = Level.WARNING;
                    logger3.log(level2, "Failed to close connection after DBUnit operation: " + ex2, (Throwable) ex2);
                }
            }
            throw th;
        }
    }

    protected IDatabaseConnection getConnection() {
        try {
            DataSource datasource = getDataSource();
            Connection con = datasource.getConnection();
            DatabaseConnection databaseConnection = new DatabaseConnection(con);
            editConfig(databaseConnection.getConfig());
            return databaseConnection;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void editConfig(DatabaseConfig config) {
    }
}
