package ru.tony.transfer.service.impl;

import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.resource.messages.AccountItem;
import ru.tony.transfer.resource.messages.AccountRequest;
import ru.tony.transfer.resource.messages.AccountResponse;
import ru.tony.transfer.service.AccountService;
import ru.tony.transfer.service.exception.AccountNotFoundException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final ConnectionManager cm;

    public AccountServiceImpl(AccountRepository accountRepository, ConnectionManager connectionManager) {

        this.accountRepository = accountRepository;
        this.cm = connectionManager;
    }

    @Override
    public AccountResponse save(AccountRequest request) {
        Account account = Account.builder()
                .name(request.getName())
                .number(generateNumber())
                .balance(request.getBalance())
                .createDate(new Date()).build();
        Account result = cm.doWork(conn -> accountRepository.create(conn, account));
        return createAccountResponse(result);
    }

    @Override
    public AccountResponse findById(Long id) {
        Account account = cm.doWork(conn -> accountRepository.findById(conn, id));
        if (account == null) {
            throw new AccountNotFoundException();
        }
        return createAccountResponse(account);
    }

    @Override
    public List<AccountItem> findAll() {
        return cm.doWork(accountRepository::findAll)
                .stream()
                .map(this::toAccountItem).collect(toList());
    }

    private AccountItem toAccountItem(Account account) {
        return AccountItem.builder()
                .id(account.getId())
                .number(account.getNumber())
                .createDate(account.getCreateDate())
                .name(account.getName())
                .balance(account.getBalance()).build();
    }

    private AccountResponse createAccountResponse(Account result) {
        return AccountResponse.builder().account(toAccountItem(result)).build();
    }

    private String generateNumber() {
        // TODO:
        return UUID.randomUUID().toString();
    }
}
