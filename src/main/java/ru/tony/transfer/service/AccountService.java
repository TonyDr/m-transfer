package ru.tony.transfer.service;

import ru.tony.transfer.resource.messages.*;

import java.util.List;

public interface AccountService {

    AccountResponse save(AccountRequest request);

    AccountResponse findById(Long id);

    List<AccountItem> findAll();

    TransferItem transfer(TransferRequest request);

    List<TransferHistoryItem> findHistoryById(Long id);
}
