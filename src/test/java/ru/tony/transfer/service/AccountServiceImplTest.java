package ru.tony.transfer.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.resource.messages.AccountItem;
import ru.tony.transfer.resource.messages.AccountRequest;
import ru.tony.transfer.resource.messages.AccountResponse;
import ru.tony.transfer.service.exception.AccountNotFoundException;
import ru.tony.transfer.service.impl.AccountServiceImpl;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountServiceImplTest {

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    private static final Long ACCOUNT_ID = 123L;
    private AccountService sut;

    private AccountRepository accRepo;

    @Before
    public void beforeTest() throws SQLException {
        accRepo = mock(AccountRepository.class);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(Connection.class));
        sut = new AccountServiceImpl(accRepo, new ConnectionManager(dataSource));
    }

    @Test
    public void shouldCorrectlyCreateAccount() throws SQLException {
        when(accRepo.create(any(Connection.class), any(Account.class) )).thenAnswer(AccountServiceImplTest::answerAccount);
        String testName = "Test name";
        BigDecimal balance = BigDecimal.valueOf(100);
        AccountRequest r = AccountRequest.builder().name(testName).balance(balance).build();
        AccountResponse resp = sut.save(r);
        AccountItem item  = resp.getAccount();
        assertEquals(ACCOUNT_ID, item.getId());
        assertNotNull(item.getNumber());
        assertNotNull(item.getCreateDate());
        assertEquals(testName, item.getName());
        assertEquals(balance, item.getBalance());
    }

    @Test
    public void shouldReturnExceptionWhenAccountNotFound() {
        thrown.expect(AccountNotFoundException.class);
        sut.findById(123L);
    }

    @Test
    public void shouldReturnAccountResponseWhenAccountFound() throws SQLException {
        Long id = 123L;
        String number = "Test_number";
        Date createDate = new Date();
        BigDecimal balance = BigDecimal.valueOf(100);
        String testName = "testName";
        Account account = Account.builder()
                .id(id)
                .name(testName)
                .number(number)
                .createDate(createDate)
                .balance(balance)
                .build();

        when(accRepo.findById(any(Connection.class), eq(id))).thenReturn(account);

        AccountResponse resp = sut.findById(id);
        AccountItem item  = resp.getAccount();
        assertEquals(id, item.getId());
        assertEquals(number, item.getNumber());
        assertEquals(createDate, item.getCreateDate());
        assertEquals(testName, item.getName());
        assertEquals(balance, item.getBalance());
    }

    private static Account answerAccount(InvocationOnMock invocation) {
        Account account = invocation.getArgument(1);
        account.setId(ACCOUNT_ID);
        return account;
    }
}
