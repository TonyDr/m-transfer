package ru.tony.transfer.db;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {
    public DataAccessException(SQLException e) {
        super("SQL error", e);
    }

    DataAccessException(Exception e) {
        super("SQL error", e);
    }
}
