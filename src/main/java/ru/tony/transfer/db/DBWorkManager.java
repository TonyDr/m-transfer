package ru.tony.transfer.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DBWorkManager {

    private DbConnectionManager cm;

    public DBWorkManager(DbConnectionManager cm) {
        this.cm = cm;
    }

    public <T> T doWork(Action<T> action) {
        Connection conn = null;
        T result;
        try {
            conn = cm.getConnectionForThread();
            result = action.run();
        } catch (Exception e) {
            log.error("Work execution has failed", e);
            rollback(conn);
            throw getExecutionExceptionToThrow(e);
        } finally {
            closeConnection(conn);
        }
        return result;
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            cm.removeConnectionForThread();
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Error on closing the connection", e);
            }
        }
    }

    private static RuntimeException getExecutionExceptionToThrow(Exception ex) {
        return ex instanceof RuntimeException ? (RuntimeException) ex : new DataAccessException(ex);
    }

    public <T> T doInTransaction(Action<T> action) {
        Connection conn = null;
        T result;
        try {
            conn = cm.getConnectionForThread();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            result = action.run();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            log.error("Work execution has failed: " + e, e);
            rollback(conn);
            throw getExecutionExceptionToThrow(e);
        } finally {
            closeConnection(conn);
        }
        return result;
    }

    private void rollback(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException clEx) {
            log.error("Error on closing the connection" + clEx, clEx);
        }
    }


    public interface Action<R> {
        R run() throws Exception;
    }
}
