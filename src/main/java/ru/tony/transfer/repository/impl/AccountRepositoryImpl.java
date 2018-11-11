package ru.tony.transfer.repository.impl;

import ru.tony.transfer.db.DataAccessException;
import ru.tony.transfer.db.DbConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.AccountRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepositoryImpl implements AccountRepository {

    private static final String INSERT_SQL = "INSERT INTO ACCOUNT (number, create_date, balance, ACC_NAME)  VALUES (?,?,?,?)";
    private static final String SELECT_SQL = "SELECT ID, NUMBER, CREATE_DATE, BALANCE, ACC_NAME FROM ACCOUNT";
    private static final String SELECT_BY_ID = SELECT_SQL + " WHERE ID = ?";
    private static final String FOR_UPDATE = SELECT_SQL + " WHERE NUMBER = ? FOR UPDATE ";
    private static final String UPDATE_BALANCE = "UPDATE ACCOUNT SET BALANCE = ? WHERE NUMBER = ?";
    private DbConnectionManager cm;

    public AccountRepositoryImpl(DbConnectionManager cm) {
        this.cm = cm;
    }


    @Override
    public Account create(Account account) {
        try {
            PreparedStatement stm = cm.getActiveConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, account.getNumber());
            stm.setTimestamp(2, new Timestamp(account.getCreateDate().getTime()));
            stm.setBigDecimal(3, account.getBalance().setScale(2));
            stm.setString(4, account.getName());
            stm.execute();
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next()) {
                account.setId(rs.getLong(1));
            }
            return account;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Account findById(Long id)  {
        try {
            PreparedStatement stm = cm.getActiveConnection().prepareStatement(SELECT_BY_ID);
            stm.setLong(1, id);
            stm.execute();
            ResultSet resultSet = stm.getResultSet();
            if (resultSet.next()) {
                return getAccount(resultSet);
            } else {
                return null;
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
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
    public List<Account> findAll() {
        try {
            Statement stm = cm.getActiveConnection().createStatement();
            ResultSet rs = stm.executeQuery(SELECT_SQL);
            List<Account> list = new ArrayList<>();
            while (rs.next()) {
                list.add(getAccount(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Account findByNumberForUpdate(String number) {
        try {
            Connection conn = cm.getActiveConnection();
            PreparedStatement stm = conn.prepareStatement(FOR_UPDATE);
            stm.setString(1, number);
            stm.execute();
            ResultSet resultSet = stm.getResultSet();
            if (resultSet.next()) {
                return getAccount(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean updateBalance(Account account) {
        try {
            Connection conn = cm.getActiveConnection();
            PreparedStatement stm = conn.prepareStatement(UPDATE_BALANCE);
            stm.setBigDecimal(1, account.getBalance());
            stm.setString(2, account.getNumber());
            return stm.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
