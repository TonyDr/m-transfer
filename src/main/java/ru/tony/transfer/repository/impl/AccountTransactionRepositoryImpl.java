package ru.tony.transfer.repository.impl;

import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.model.AccountTransactionHistory;
import ru.tony.transfer.repository.AccountTransactionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountTransactionRepositoryImpl implements AccountTransactionRepository {

    private static final String INSERT_SQL =
            "INSERT INTO ACCOUNT_TRANSACTION (FROM_ACC, TO_ACC, TRANSACTION_TIME, AMOUNT) VALUES (?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT act.ID, act.TRANSACTION_TIME, act.AMOUNT, " +
            " act.from_acc, acc1.number, act.to_acc, acc2.number FROM ACCOUNT_TRANSACTION act " +
            " LEFT JOIN ACCOUNT acc1 ON act.from_acc = acc1.id " +
            " LEFT JOIN ACCOUNT acc2 ON act.to_acc = acc2.id " +
            " WHERE  act.from_acc =? or act.to_acc =? ORDER BY act.TRANSACTION_TIME DESC ";
    private ConnectionManager connectionManager;

    public AccountTransactionRepositoryImpl(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public AccountTransaction create(AccountTransaction accountTransaction) {
        try {
            Connection conn = connectionManager.getActiveConnection();
            PreparedStatement stm = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            stm.setLong(1, accountTransaction.getFrom());
            stm.setLong(2, accountTransaction.getTo());
            stm.setTimestamp(3, new Timestamp(accountTransaction.getTransactionTime().getTime()));
            stm.setBigDecimal(4, accountTransaction.getAmount());
            stm.execute();
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next()) {
                accountTransaction.setId(rs.getLong(1));
            }
            return accountTransaction;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AccountTransactionHistory> findHistoryById(Long id) {
        try {
            Connection connection = connectionManager.getActiveConnection();
            PreparedStatement stm = connection.prepareStatement(SELECT_BY_ID);
            stm.setLong(1, id);
            stm.setLong(2, id);
            stm.execute();
            ResultSet resultSet = stm.getResultSet();
            List<AccountTransactionHistory> historyList = new ArrayList<>();
            while (resultSet.next()) {
                historyList.add(getHistory(resultSet));
            }
            return historyList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
