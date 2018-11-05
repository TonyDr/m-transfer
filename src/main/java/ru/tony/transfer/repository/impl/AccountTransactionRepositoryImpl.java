package ru.tony.transfer.repository.impl;

import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.repository.AccountTransactionRepository;

import java.sql.Connection;

public class AccountTransactionRepositoryImpl implements AccountTransactionRepository {
    @Override
    public AccountTransaction create(Connection conn, AccountTransaction accountTransaction) {
        return null;
    }
}
