package ru.tony.transfer.db;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.HsqlConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Slf4j
public class DbConnection {

    private static DataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
            initLiquibase(requireNonNull(dataSource));
        }
        return dataSource;
    }

    private static DataSource createDataSource() {
        Properties props = new Properties();
        InputStream fis;
        try {
            fis = DbConnectionManager.class.getClassLoader().getResourceAsStream("db.properties");
            props.load(fis);
            JDBCDataSource ds = new JDBCDataSource();
            ds.setURL(props.getProperty("url"));
            ds.setUser(props.getProperty("username"));
            ds.setPassword(props.getProperty("password"));
            return ds;
        } catch (IOException e) {
            log.error("properties read error", e);
            throw new RuntimeException(e);
        }
    }

    private static void initLiquibase(DataSource ds) {
        Liquibase liquibase = null;
        Connection c = null;
        try {
            c = ds.getConnection();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new HsqlConnection(ds.getConnection()));
            liquibase = new Liquibase("liquibase/main.sql", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        } catch (SQLException | LiquibaseException e) {
            log.error("Init liquibase error", e);
            try {
                if (c != null) {
                    c.rollback();
                }
            } catch (SQLException e1) {
                log.error("rollback error", e);
            }
            throw new RuntimeException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    log.error("Close connection error", e);
                }
            }
        }
    }
}
