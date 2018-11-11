package ru.tony.transfer.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.currentThread;

public class DbConnectionManager {

    private DataSource dataSource;

    private Map<Thread, Connection> threadToConnection;

    public DbConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        threadToConnection = new HashMap<>();
    }

    Connection getConnectionForThread() throws SQLException {
        Connection connection = dataSource.getConnection();
        threadToConnection.put(currentThread(), connection);
        return connection;
    }

    public Connection getActiveConnection() {
        Connection conn = threadToConnection.get(currentThread());
        if (conn == null) {
            throw new IllegalStateException("must be in action block");
        }
        return conn;
    }

    void removeConnectionForThread() {
        threadToConnection.remove(currentThread());
    }


}
