package ru.tony.transfer.repository;

import ru.tony.transfer.model.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AccountRepository {

    Account create(Account account);

    Account findById(Connection connection, Long id) throws SQLException;

    List<Account> findAll(Connection conn) throws SQLException;

    Account findByNumberForUpdate(String from);

    boolean updateBalance(Account account);
}
