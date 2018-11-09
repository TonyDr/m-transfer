package ru.tony.transfer.repository;

import ru.tony.transfer.model.Account;

import java.util.List;

public interface AccountRepository {

    Account create(Account account);

    Account findById(Long id);

    List<Account> findAll();

    Account findByNumberForUpdate(String from);

    boolean updateBalance(Account account);
}
