package ru.tony.transfer.repository;

import org.junit.Before;
import org.junit.Test;
import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.db.DbConnection;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.impl.AccountRepositoryImpl;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountRepositoryImplTest {

    private AccountRepository sut;
    private ConnectionManager cm;

    @Before
    public void beforeTest() {
        cm = new ConnectionManager(DbConnection.getDataSource());
        sut = new AccountRepositoryImpl();
    }

    @Test
    public void createAccountShouldSucceed(){
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

    private Account findAccountById(Long id) {
        return cm.doWork(conn -> sut.findById(conn, id));
    }

    private Account createAccount(Account account) {
        return cm.doWork(conn -> sut.create(conn, account));
    }


}
