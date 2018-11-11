package ru.tony.transfer.repository.impl;

import lombok.extern.slf4j.Slf4j;
import ru.tony.transfer.db.DataAccessException;
import ru.tony.transfer.db.DbConnectionManager;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.model.AccountTransactionHistory;
import ru.tony.transfer.repository.AccountTransactionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.tony.transfer.db.JdbcUtils.closeResultSet;
import static ru.tony.transfer.db.JdbcUtils.closeStatement;

@Slf4j
public class AccountTransactionRepositoryImpl implements AccountTransactionRepository {

    private static final String INSERT_SQL =
            "INSERT INTO ACCOUNT_TRANSACTION (FROM_ACC, TO_ACC, TRANSACTION_TIME, AMOUNT) VALUES (?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT act.ID, act.TRANSACTION_TIME, act.AMOUNT, " +
            " act.from_acc, acc1.number, act.to_acc, acc2.number FROM ACCOUNT_TRANSACTION act " +
            " LEFT JOIN ACCOUNT acc1 ON act.from_acc = acc1.id " +
            " LEFT JOIN ACCOUNT acc2 ON act.to_acc = acc2.id " +
            " WHERE  act.from_acc =? or act.to_acc =? ORDER BY act.TRANSACTION_TIME DESC ";
    private DbConnectionManager connectionManager;

    public AccountTransactionRepositoryImpl(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public AccountTransaction create(AccountTransaction accountTransaction) {
        ResultSet rs = null;
        PreparedStatement stm = null;
        try {
            Connection conn = connectionManager.getActiveConnection();
            stm = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            stm.setLong(1, accountTransaction.getFrom());
            stm.setLong(2, accountTransaction.getTo());
            stm.setTimestamp(3, new Timestamp(accountTransaction.getTransactionTime().getTime()));
            stm.setBigDecimal(4, accountTransaction.getAmount());
            stm.execute();
            rs = stm.getGeneratedKeys();
            if (rs.next()) {
                accountTransaction.setId(rs.getLong(1));
            }
            return accountTransaction;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(stm);
        }
    }

    @Override
    public List<AccountTransactionHistory> findHistoryById(Long id) {
        ResultSet rs = null;
        PreparedStatement stm = null;
        try {
            Connection connection = connectionManager.getActiveConnection();
            stm = connection.prepareStatement(SELECT_BY_ID);
            stm.setLong(1, id);
            stm.setLong(2, id);
            stm.execute();
            rs = stm.getResultSet();
            List<AccountTransactionHistory> historyList = new ArrayList<>();
            while (rs.next()) {
                historyList.add(getHistory(rs));
            }
            return historyList;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(stm);
        }
    }



    private AccountTransactionHistory getHistory(ResultSet rs) throws SQLException {
        return AccountTransactionHistory.builder()
                .id(rs.getLong(1))
                .transactionTime(new Date(rs.getTimestamp(2).getTime()))
                .amount(rs.getBigDecimal(3))
                .from(rs.getLong(4))
                .fromNumber(rs.getString(5))
                .to(rs.getLong(6))
                .toNumber(rs.getString(7))
                .build();
    }
}
