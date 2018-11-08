package ru.tony.transfer.repository;

import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.model.AccountTransactionHistory;

import java.util.List;

public interface AccountTransactionRepository {
    AccountTransaction create(AccountTransaction accountTransaction);

    List<AccountTransactionHistory> findHistoryById(Long id);
}
