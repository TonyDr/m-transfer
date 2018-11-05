package ru.tony.transfer.repository;

import ru.tony.transfer.model.AccountTransaction;

import java.sql.Connection;

public interface AccountTransactionRepository {
    AccountTransaction create(Connection conn, AccountTransaction accountTransaction);
}
