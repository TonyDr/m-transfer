package ru.tony.transfer.db;

import ru.tony.transfer.exception.AppException;

import java.sql.SQLException;

public class DataAccessException extends AppException {
    public DataAccessException(SQLException e) {
        super("SQL error", e);
    }

    DataAccessException(Exception e) {
        super("SQL error", e);
    }
}
