package ru.tony.transfer.repository;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.tony.transfer.db.DBWorkManager;
import ru.tony.transfer.db.DbConnection;
import ru.tony.transfer.db.DbConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.impl.AccountRepositoryImpl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class AccountRepositoryImplTest {

    private static DbConnectionManager cm;
    private AccountRepository sut;
    private static DBWorkManager wm;


    @BeforeClass
    public static void beforeClass() {
        cm = new DbConnectionManager(DbConnection.getDataSource());
        wm = new DBWorkManager(cm);
    }

    @Before
    public void beforeTest() {
        sut = new AccountRepositoryImpl(cm);
    }

    @Test
    public void createAccountShouldSucceed() {
        String number = "Test_number";
        Date createDate = new Date();
        BigDecimal balance = BigDecimal.valueOf(100);
        String testName = "testName";
        Account account = Account.builder()
                .name(testName)
                .number(number)
                .createDate(createDate)
                .balance(balance)
                .build();

        Account result = createAccount(account);

        Long id = result.getId();
        assertNotNull(id);

        result = findAccountById(id);
        assertEquals(id, result.getId());
        assertEquals(number, result.getNumber());
        assertEquals(createDate, result.getCreateDate());
        assertEquals(testName, result.getName());
        assertEquals(balance, result.getBalance());
    }

    @Test
    public void shouldCorrectlyGetAllAccounts() {
        int size = getAll().size();

        createAccount(Account.builder().name("insertNew").number("some_number").createDate(new Date())
                .balance(BigDecimal.TEN)
                .build());

        assertEquals(size + 1, getAll().size());
    }

    @Test
    public void shouldCorrectlyUpdateBalanceForAccount() {
        Account account = createAccount(Account.builder().name("insertNew").number(UUID.randomUUID().toString()).createDate(new Date())
                .balance(BigDecimal.TEN)
                .build());

        assertTrue(wm.doInTransaction(() -> {
            Account toUpdate = sut.findByNumberForUpdate(account.getNumber());
            toUpdate.setBalance(BigDecimal.valueOf(111));
            return sut.updateBalance(toUpdate);

        }));
        Account afterUpdate = findAccountById(account.getId());
        assertEquals(BigDecimal.valueOf(111), afterUpdate.getBalance());
    }

    private List<Account> getAll() {
        return wm.doWork(sut::findAll);
    }

    private Account findAccountById(Long id) {
        return wm.doWork(() -> sut.findById(id));
    }

    private Account createAccount(Account account) {
        return wm.doWork(() -> sut.create(account));
    }


}
