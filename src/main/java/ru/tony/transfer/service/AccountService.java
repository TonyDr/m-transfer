package ru.tony.transfer.service;

import ru.tony.transfer.resource.messages.AccountItem;
import ru.tony.transfer.resource.messages.AccountListResponse;
import ru.tony.transfer.resource.messages.AccountRequest;
import ru.tony.transfer.resource.messages.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse save(AccountRequest request);

    AccountResponse findById(Long id);

    List<AccountItem> findAll();
}
