package ru.tony.transfer.db;

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;

public class DbConnectionTest {

    @Test
    public void shouldOpenConnectionToDb() throws SQLException {
        DataSource ds = DbConnection.getDataSource();
        assertNotNull(ds.getConnection());
    }
}
