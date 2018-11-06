package ru.tony.transfer.repository.impl;

import org.junit.Before;
import org.junit.Test;
import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.db.DbConnection;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.repository.AccountTransactionRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertNotNull;

public class AccountTransactionRepositoryImplTest {

    private AccountTransactionRepository sut;
    private ConnectionManager cm;
    private AccountRepository accRepo;

    @Before
    public void beforeTest() {
        cm = new ConnectionManager(DbConnection.getDataSource());
        accRepo = new AccountRepositoryImpl(cm);
        sut = new AccountTransactionRepositoryImpl(cm);
    }

    @Test
    public void createTransactionShouldSucceed() {
        Account from =  cm.doWork2(() -> accRepo.create(Account.builder()
                .name("trTest1")
                .number(getNumber())
                .balance(TEN)
                .createDate(new Date()).build()));
        Account to =  cm.doWork2(() -> accRepo.create(Account.builder()
                .name("trTest2")
                .number(getNumber())
                .balance(ONE)
                .createDate(new Date()).build()));
        AccountTransaction build = AccountTransaction.builder()
                .from(from.getId())
                .to(to.getId())
                .transactionTime(new Date())
                .amount(TEN)
                .build();
        AccountTransaction result  = cm.doWork2(() -> sut.create(build));
        assertNotNull(result.getId());
    }

    private String getNumber() {
        return UUID.randomUUID().toString();
    }
}