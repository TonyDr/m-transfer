package ru.tony.transfer.db;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.currentThread;

@Slf4j
public class ConnectionManager {

    private DataSource dataSource;

    private Map<Thread, Connection> threadToConnection;

    public ConnectionManager(DataSource dataSource) {
        threadToConnection = new HashMap<>();
        this.dataSource = dataSource;
    }

    public <T> T doWork(Action<T> action) {
        Connection conn = null;
        T result;
        try {
            conn = dataSource.getConnection();
            result = action.run(conn);
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException clEx) {
                log.error("Error on closing the connection" + clEx, clEx);
            }
            log.error("Work execution has failed: " + e, e);
            throw getExecutionExceptionToThrow(e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
        return result;
    }

    public <T> T doWork2(Action2<T> action) {
        Connection conn = null;
        T result;
        try {
            conn = getConnectionForThread();
            result = action.run();
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException clEx) {
                log.error("Error on closing the connection" + clEx, clEx);
            }
            log.error("Work execution has failed: " + e, e);
            throw getExecutionExceptionToThrow(e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
        return result;
    }

    private Connection getConnectionForThread() throws SQLException {
        Connection connection = dataSource.getConnection();
        threadToConnection.put(currentThread(), connection);
        return connection;
    }

    private static RuntimeException getExecutionExceptionToThrow(Exception ex) {
        return ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
    }

    public <T> T doInTransaction(Action2<T> action) {
        Connection conn = null;
        T result;
        try {
            conn = getConnectionForThread();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            result = action.run();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException clEx) {
                log.error("Error on closing the connection" + clEx, clEx);
            }
            log.error("Work execution has failed: " + e, e);
            throw getExecutionExceptionToThrow(e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
        return result;
    }

    private void releaseConnection(Connection conn) {
        try {
            threadToConnection.remove(conn);
            conn.close();
        } catch (SQLException ex) {
            log.error("Error on closing the connection" + ex, ex);
        }
    }

    public Connection getActiveConnection() {
        Connection conn = threadToConnection.get(currentThread());
        if (conn == null) {
            throw new IllegalStateException("must be in action block");
        }
        return conn;
    }

    public interface Action<R> {
        R run(Connection conn) throws Exception;
    }

    public interface Action2<R> {
        R run() throws Exception;
    }
}
