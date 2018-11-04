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
        return null;
    }

    private AccountResponse createAccountResponse(Account result) {
        return AccountResponse.builder().account(
                AccountItem.builder()
                .id(result.getId())
                .number(result.getNumber())
                .createDate(result.getCreateDate())
                .name(result.getName())
                .balance(result.getBalance()).build())
                .build();
    }

    private String generateNumber() {
        // TODO:
        return "";
    }
}
