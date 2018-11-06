package ru.tony.transfer.repository.impl;

import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.repository.AccountTransactionRepository;

import java.sql.*;

public class AccountTransactionRepositoryImpl implements AccountTransactionRepository {

    private static final String INSERT_SQL =
            "INSERT INTO ACCOUNT_TRANSACTION (FROM_ACC, TO_ACC, TRANSACTION_TIME, AMOUNT) VALUES (?,?,?,?)";
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
}
