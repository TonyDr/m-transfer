package ru.tony.transfer.db;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.HsqlConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {


    private static DataSource datasource;

    public static DataSource getDataSource() {
        if (datasource == null) {
            datasource = createDataSource();
            initLiquibase(datasource);
        }
        return datasource;
    }

    private static DataSource createDataSource() {
        Properties props = new Properties();
        InputStream fis = null;
        try {
            fis = DbConnection.class.getClassLoader().getResourceAsStream("db.properties");
            props.load(fis);
            JDBCDataSource ds = new JDBCDataSource();
            ds.setURL(props.getProperty("url"));
            ds.setUser(props.getProperty("username"));
            ds.setPassword(props.getProperty("password"));

            return ds;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void initLiquibase(DataSource ds)   {
        Liquibase liquibase = null;
        Connection c = null;
        try {
            c = ds.getConnection();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new HsqlConnection(ds.getConnection()));
            liquibase = new Liquibase("liquibase/main.sql", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        } catch (SQLException | LiquibaseException e ) {
            throw new RuntimeException(e);
        } finally {
            if (c != null) {
                try {
                    c.rollback();
                    c.close();
                } catch (SQLException e) {
                    //nothing to do
                }
            }
        }
    }
}
