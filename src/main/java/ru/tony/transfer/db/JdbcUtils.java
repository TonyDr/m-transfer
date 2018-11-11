package ru.tony.transfer.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class JdbcUtils {

    public static void closeStatement(Statement stm) {
        if (stm != null) {
            try {
                stm.close();
            } catch (SQLException e) {
                log.error("Error when closing statement", e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Error when closing resultset", e);
            }
        }
    }
}
