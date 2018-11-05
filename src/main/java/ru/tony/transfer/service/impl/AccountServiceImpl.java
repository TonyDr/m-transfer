package ru.tony.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.repository.AccountTransactionRepository;
import ru.tony.transfer.resource.messages.*;
import ru.tony.transfer.service.AccountService;
import ru.tony.transfer.service.exception.AccountFromNotFoundException;
import ru.tony.transfer.service.exception.AccountNotFoundException;
import ru.tony.transfer.service.exception.AccountToNotFoundException;
import ru.tony.transfer.service.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final ConnectionManager cm;
    private final AccountTransactionRepository transactionRepository;

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

    @Override
    public TransferItem transfer(TransferRequest request) {
        return cm.doInTransaction(conn -> {
            Account from = accountRepository.findByNumberForUpdate(conn, request.getFrom());
            if (from == null) {
                throw new AccountFromNotFoundException();
            }
            Account to = accountRepository.findByNumberForUpdate(conn, request.getTo());
            if (to == null) {
                throw new AccountToNotFoundException();
            }
            BigDecimal fromBalance = from.getBalance().subtract(request.getAmount());
            if(!(fromBalance.compareTo(BigDecimal.ZERO) >= 0)) {
                throw new InsufficientFundsException();
            }
            BigDecimal toBalance = to.getBalance().add(request.getAmount());
            from.setBalance(fromBalance);
            to.setBalance(toBalance);
            accountRepository.updateBalance(conn, from);
            accountRepository.updateBalance(conn, to);

            return toTransactionItem(transactionRepository.create(conn,
                    AccountTransaction.builder()
                            .from(from.getId())
                            .to(to.getId())
                            .transactionTime(new Date())
                            .amount(request.getAmount()).build()));
        });
    }

    private TransferItem toTransactionItem(AccountTransaction accountTransaction) {
        return TransferItem.builder()
                .transactionId(accountTransaction.getId())
                .transactionTime(accountTransaction.getTransactionTime())
                .build();
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
        return UUID.randomUUID().toString();
    }
}
