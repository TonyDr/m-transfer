package ru.tony.transfer.repository.impl;

import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.AccountRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepositoryImpl implements AccountRepository {

    private static final String INSERT_SQL = "INSERT INTO ACCOUNT (number, create_date, balance, ACC_NAME)  VALUES (?,?,?,?)";
    private static final String SELECT_SQL = "SELECT ID, NUMBER, CREATE_DATE, BALANCE, ACC_NAME FROM ACCOUNT";
    private static final String SELECT_BY_ID = SELECT_SQL +" WHERE ID = ?";


    @Override
    public Account create(Connection connection, Account account) throws SQLException {

        PreparedStatement stm = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, account.getNumber());
        stm.setTimestamp(2, new Timestamp(account.getCreateDate().getTime()));
        stm.setBigDecimal(3, account.getBalance());
        stm.setString(4, account.getName());
        stm.execute();
        ResultSet rs = stm.getGeneratedKeys();
        if (rs.next()) {
            account.setId(rs.getLong(1));
        }
        return account;
    }

    @Override
    public Account findById(Connection connection, Long id) throws SQLException {
        PreparedStatement stm = connection.prepareStatement(SELECT_BY_ID);
        stm.setLong(1, id);
        stm.execute();
        ResultSet resultSet = stm.getResultSet();
        if (resultSet.next()) {
            return getAccount(resultSet);
        } else {
            return null;
        }
    }

    private Account getAccount(ResultSet resultSet) throws SQLException {
        return Account.builder()
                .id(resultSet.getLong(1))
                .number(resultSet.getString(2))
                .createDate(new Date(resultSet.getTimestamp(3).getTime()))
                .balance(resultSet.getBigDecimal(4))
                .name(resultSet.getString(5)).build();
    }

    @Override
    public List<Account> findAll(Connection conn) throws SQLException {
        Statement stm = conn.createStatement();
        ResultSet rs = stm.executeQuery(SELECT_SQL);
        List<Account> list = new ArrayList<>();
        while (rs.next()) {
            list.add(getAccount(rs));
        }
        return list;
    }
}