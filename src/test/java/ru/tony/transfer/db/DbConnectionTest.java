package ru.tony.transfer.db;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;

public class DbConnectionTest {

    @Test
    public void shouldOpenConnectionToDb() throws SQLException {
        assertNotNull(new DbConnectionManager(DbConnection.getDataSource()).getConnectionForThread());
    }
}
