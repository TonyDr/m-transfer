package ru.tony.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import ru.tony.transfer.db.DBWorkManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.model.AccountTransactionHistory;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.repository.AccountTransactionRepository;
import ru.tony.transfer.resource.messages.*;
import ru.tony.transfer.service.AccountService;
import ru.tony.transfer.service.exception.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static ru.tony.transfer.resource.messages.TransactionType.DEPOSIT;
import static ru.tony.transfer.resource.messages.TransactionType.WITHDRAWAL;

@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final DBWorkManager cm;
    private final AccountTransactionRepository transactionRepository;

    @Override
    public AccountResponse save(AccountRequest request) {
        Account account = Account.builder()
                .name(request.getName())
                .number(generateNumber())
                .balance(request.getBalance())
                .createTime(new Date()).build();
        Account result = cm.doWork(() -> accountRepository.create(account));
        return createAccountResponse(result);
    }

    @Override
    public AccountResponse findById(Long id) {
        Account account = cm.doWork(() -> accountRepository.findById(id));
        checkAccount(account);
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
        return cm.doInTransaction(() -> {
            Account from = getAndCheckFromAccount(request);
            Account to = getAndCheckToAccount(request);
            checkSameAccount(from, to);
            BigDecimal fromBalance = checkFromFundsAndGetRemainingBalance(request, from);
            BigDecimal toBalance = to.getBalance().add(request.getAmount());
            from.setBalance(fromBalance);
            to.setBalance(toBalance);
            accountRepository.updateBalance(from);
            accountRepository.updateBalance(to);

            return toTransactionItem(transactionRepository.create(
                    AccountTransaction.builder()
                            .from(from.getId())
                            .to(to.getId())
                            .transactionTime(new Date())
                            .amount(request.getAmount()).build()));
        });
    }

    private BigDecimal checkFromFundsAndGetRemainingBalance(TransferRequest request, Account from) {
        BigDecimal fromBalance = from.getBalance().subtract(request.getAmount());
        if(!(fromBalance.compareTo(BigDecimal.ZERO) >= 0)) {
            throw new InsufficientFundsException();
        }
        return fromBalance;
    }

    private void checkSameAccount(Account from, Account to) {
        if( from.getId().equals(to.getId())) {
            throw new SameAccountException();
        }
    }

    private Account getAndCheckToAccount(TransferRequest request) {
        Account to = accountRepository.findByNumberForUpdate(request.getTo());
        if (to == null) {
            throw new AccountToNotFoundException();
        }
        return to;
    }

    private Account getAndCheckFromAccount(TransferRequest request) {
        Account from = accountRepository.findByNumberForUpdate(request.getFrom());
        if (from == null) {
            throw new AccountFromNotFoundException();
        }
        return from;
    }

    @Override
    public List<TransferHistoryItem> findHistoryById(Long id) {
        return cm.doWork(() -> {
            Account account = accountRepository.findById(id);
            checkAccount(account);
            return transactionRepository.findHistoryById(id);
        }).stream().map(val -> toHistoryItem(id,val)).collect(toList());
    }

    private TransferHistoryItem toHistoryItem(Long id, AccountTransactionHistory history) {
        TransferHistoryItem.TransferHistoryItemBuilder builder = TransferHistoryItem.builder()
                .transactionId(history.getId())
                .amount(history.getAmount())
                .transactionTime(history.getTransactionTime());
        if(id.equals(history.getFrom())) {
            builder.type(WITHDRAWAL).toNumber(history.getToNumber());
        } else {
            builder.type(DEPOSIT).fromNumber(history.getFromNumber());
        }
        return builder.build();
    }

    private void checkAccount(Account account) {
        if (account == null) {
            throw new AccountNotFoundException();
        }
    }

    private TransferItem toTransactionItem(AccountTransaction transaction) {
        return TransferItem.builder()
                .transactionId(transaction.getId())
                .transactionTime(transaction.getTransactionTime())
                .build();
    }

    private AccountItem toAccountItem(Account account) {
        return AccountItem.builder()
                .id(account.getId())
                .number(account.getNumber())
                .createTime(account.getCreateTime())
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
