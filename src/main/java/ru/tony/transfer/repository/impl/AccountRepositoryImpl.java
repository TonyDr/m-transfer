package ru.tony.transfer.repository.impl;

import ru.tony.transfer.db.DataAccessException;
import ru.tony.transfer.db.DbConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.AccountRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.tony.transfer.db.JdbcUtils.closeResultSet;
import static ru.tony.transfer.db.JdbcUtils.closeStatement;

public class AccountRepositoryImpl implements AccountRepository {

    private static final String INSERT_SQL = "INSERT INTO ACCOUNT (number, create_TIME, balance, ACC_NAME)  VALUES (?,?,?,?)";
    private static final String SELECT_SQL = "SELECT ID, NUMBER, CREATE_TIME, BALANCE, ACC_NAME FROM ACCOUNT";
    private static final String SELECT_BY_ID = SELECT_SQL + " WHERE ID = ?";
    private static final String FOR_UPDATE = SELECT_SQL + " WHERE NUMBER = ? FOR UPDATE ";
    private static final String UPDATE_BALANCE = "UPDATE ACCOUNT SET BALANCE = ? WHERE NUMBER = ?";
    private DbConnectionManager cm;

    public AccountRepositoryImpl(DbConnectionManager cm) {
        this.cm = cm;
    }


    @Override
    public Account create(Account account) {
        ResultSet rs = null;
        PreparedStatement stm = null;
        try {
            stm = cm.getActiveConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, account.getNumber());
            stm.setTimestamp(2, new Timestamp(account.getCreateTime().getTime()));
            stm.setBigDecimal(3, account.getBalance().setScale(4));
            stm.setString(4, account.getName());
            stm.execute();
            rs = stm.getGeneratedKeys();
            if (rs.next()) {
                account.setId(rs.getLong(1));
            }
            return account;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(stm);
        }
    }

    @Override
    public Account findById(Long id) {
        ResultSet rs = null;
        PreparedStatement stm = null;
        try {
            stm = cm.getActiveConnection().prepareStatement(SELECT_BY_ID);
            stm.setLong(1, id);
            stm.execute();
            rs = stm.getResultSet();
            if (rs.next()) {
                return getAccount(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(stm);
        }
    }

    private Account getAccount(ResultSet rs) throws SQLException {
        return Account.builder()
                .id(rs.getLong(1))
                .number(rs.getString(2))
                .createTime(new Date(rs.getTimestamp(3).getTime()))
                .balance(rs.getBigDecimal(4))
                .name(rs.getString(5)).build();
    }

    @Override
    public List<Account> findAll() {
        ResultSet rs = null;
        Statement stm = null;
        try {
            stm = cm.getActiveConnection().createStatement();
            rs = stm.executeQuery(SELECT_SQL);
            List<Account> list = new ArrayList<>();
            while (rs.next()) {
                list.add(getAccount(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(stm);
        }
    }

    @Override
    public Account findByNumberForUpdate(String number) {
        ResultSet rs = null;
        PreparedStatement stm = null;
        try {
            Connection conn = cm.getActiveConnection();
            stm = conn.prepareStatement(FOR_UPDATE);
            stm.setString(1, number);
            stm.execute();
            rs = stm.getResultSet();
            if (rs.next()) {
                return getAccount(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
            closeStatement(stm);
        }
    }

    @Override
    public boolean updateBalance(Account account) {
        PreparedStatement stm = null;
        try {
            Connection conn = cm.getActiveConnection();
            stm = conn.prepareStatement(UPDATE_BALANCE);
            stm.setBigDecimal(1, account.getBalance());
            stm.setString(2, account.getNumber());
            return stm.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeStatement(stm);
        }
    }
}
