package ru.tony.transfer.db;

import lombok.extern.slf4j.Slf4j;
import ru.tony.transfer.resource.messages.TransferItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class ConnectionManager {
    private DataSource dataSource;

    public ConnectionManager(DataSource dataSource) {
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
                try {
                    conn.close();
                } catch (SQLException ex) {
                    log.error("Error on closing the connection" + ex, ex);
                }
            }
        }
        return result;
    }

    private static RuntimeException getExecutionExceptionToThrow(Exception ex) {
        return ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
    }

    public <T> T doInTransaction(Action<T> action) {
        Connection conn = null;
        T result;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            result = action.run(conn);
            conn.commit();
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
                try {
                    conn.close();
                } catch (SQLException ex) {
                    log.error("Error on closing the connection" + ex, ex);
                }
            }
        }
        return result;
    }

    public interface Action<R> {
        R run(Connection conn) throws Exception;
    }
}
