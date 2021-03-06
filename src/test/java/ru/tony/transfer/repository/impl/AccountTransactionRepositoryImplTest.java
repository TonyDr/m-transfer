package ru.tony.transfer.repository.impl;

import org.junit.Before;
import org.junit.Test;
import ru.tony.transfer.db.DBWorkManager;
import ru.tony.transfer.db.DbConnection;
import ru.tony.transfer.db.DbConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.model.AccountTransactionHistory;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.repository.AccountTransactionRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountTransactionRepositoryImplTest {

    private AccountTransactionRepository sut;
    private DBWorkManager wm;
    private AccountRepository accRepo;

    @Before
    public void beforeTest() {
        DbConnectionManager cm = new DbConnectionManager(DbConnection.getDataSource());
        wm = new DBWorkManager(cm);
        accRepo = new AccountRepositoryImpl(cm);
        sut = new AccountTransactionRepositoryImpl(cm);
    }

    @Test
    public void createTransactionShouldSucceed() {
        Account from =  createAccount("trTest1", TEN);
        Account to =  createAccount("trTest2", ONE);
        AccountTransaction result = createAccountTransaction(from, to, TEN);
        assertNotNull(result.getId());
    }

    private AccountTransaction createAccountTransaction(Account from, Account to, BigDecimal amount) {
        AccountTransaction build = AccountTransaction.builder()
                .from(from.getId())
                .to(to.getId())
                .transactionTime(new Date())
                .amount(amount)
                .build();
        return wm.doWork(() -> sut.create(build));
    }

    @Test
    public void shouldCorrectlyGetTransactionHistory() {
        Account acc1 = createAccount("hTrTest1", TEN);
        Account acc2 =  createAccount("hTrTest2", ONE);
        Account acc3 =  createAccount("hTrTest3", BigDecimal.valueOf(123));
        BigDecimal amount = BigDecimal.valueOf(111.54).setScale(4);
        createAccountTransaction(acc1, acc2, TEN);
        createAccountTransaction(acc1, acc3, BigDecimal.valueOf(222));
        Long id = createAccountTransaction(acc2, acc1, amount).getId();
        createAccountTransaction(acc2, acc3, BigDecimal.valueOf(333));

        List<AccountTransactionHistory> items = wm.doWork(() ->sut.findHistoryById(acc1.getId()));
        assertEquals(3, items.size());
        AccountTransactionHistory item = items.get(0);
        assertEquals(id, item.getId());
        assertEquals(amount, item.getAmount());
        assertNotNull(item.getTransactionTime());
        assertEquals(acc2.getId(), item.getFrom());
        assertEquals(acc2.getNumber(),item.getFromNumber());
        assertEquals(acc1.getId(), item.getTo());
        assertEquals(acc1.getNumber(), item.getToNumber());
    }

    private Account createAccount(String hTrTest1, BigDecimal ten) {
        return wm.doWork(() -> accRepo.create(Account.builder()
                .name(hTrTest1)
                .number(getNumber())
                .balance(ten)
                .createTime(new Date()).build()));
    }


    private String getNumber() {
        return UUID.randomUUID().toString();
    }
}