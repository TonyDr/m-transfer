package ru.tony.transfer.repository;

import ru.tony.transfer.model.AccountTransaction;

public interface AccountTransactionRepository {
    AccountTransaction create(AccountTransaction accountTransaction);
}
