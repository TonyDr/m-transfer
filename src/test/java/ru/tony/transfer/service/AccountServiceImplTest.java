package ru.tony.transfer.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.tony.transfer.db.ConnectionManager;
import ru.tony.transfer.model.Account;
import ru.tony.transfer.model.AccountTransaction;
import ru.tony.transfer.model.AccountTransactionHistory;
import ru.tony.transfer.repository.AccountRepository;
import ru.tony.transfer.repository.AccountTransactionRepository;
import ru.tony.transfer.resource.messages.*;
import ru.tony.transfer.service.exception.AccountFromNotFoundException;
import ru.tony.transfer.service.exception.AccountNotFoundException;
import ru.tony.transfer.service.exception.AccountToNotFoundException;
import ru.tony.transfer.service.exception.InsufficientFundsException;
import ru.tony.transfer.service.impl.AccountServiceImpl;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static ru.tony.transfer.resource.messages.TransactionType.DEPOSIT;
import static ru.tony.transfer.resource.messages.TransactionType.WITHDRAWAL;

public class AccountServiceImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final Long ACCOUNT_ID = 123L;
    private AccountService sut;

    private AccountRepository accRepo;
    private AccountTransactionRepository transactionRepo;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;
    @Captor
    private ArgumentCaptor<AccountTransaction> transactionCaptor;

    @Before
    public void beforeTest() throws SQLException {
        initMocks(this);
        accRepo = mock(AccountRepository.class);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(Connection.class));
        transactionRepo = mock(AccountTransactionRepository.class);
        sut = new AccountServiceImpl(accRepo, new ConnectionManager(dataSource), transactionRepo);
    }

    @Test
    public void shouldCorrectlyCreateAccount() {
        when(accRepo.create(any(Account.class))).thenAnswer(AccountServiceImplTest::answerAccount);
        String testName = "Test name";
        BigDecimal balance = BigDecimal.valueOf(100);
        AccountRequest r = AccountRequest.builder().name(testName).balance(balance).build();
        AccountResponse resp = sut.save(r);
        AccountItem item = resp.getAccount();
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
    public void shouldReturnAccountResponseWhenAccountFound() {
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

        when(accRepo.findById(eq(id))).thenReturn(account);

        AccountResponse resp = sut.findById(id);
        AccountItem item = resp.getAccount();
        assertEquals(id, item.getId());
        assertEquals(number, item.getNumber());
        assertEquals(createDate, item.getCreateDate());
        assertEquals(testName, item.getName());
        assertEquals(balance, item.getBalance());
    }

    @Test
    public void getAllShouldReturnValuesList() throws SQLException {
        when(accRepo.findAll(any(Connection.class))).thenReturn(Arrays.asList(Account.builder().build(), Account.builder().build()));
        List<AccountItem> items = sut.findAll();

        assertEquals(2, items.size());
    }

    @Test
    public void shouldFailWhenAccountFromNotExist() {
        thrown.expect(AccountFromNotFoundException.class);
        sut.transfer(TransferRequest.builder().from("from").build());
    }

    @Test
    public void shouldReturnListOfTransferHistoryItem() {
        long accId = 123L;
        when(accRepo.findById(accId)).thenReturn(Account.builder().id(accId).build());
        when(transactionRepo.findHistoryById(accId)).thenReturn(Arrays.asList(
                AccountTransactionHistory.builder().id(222L).from(accId).fromNumber("fromNumber")
                        .to(444L).toNumber("toNumber").amount(BigDecimal.TEN).transactionTime(new Date()).build(),
                AccountTransactionHistory.builder().id(333L).to(accId).fromNumber("fromNumber")
                        .amount(BigDecimal.valueOf(63)).build()
        ));
        List<TransferHistoryItem> items = sut.findHistoryById(accId);
        assertEquals(2, items.size());
        TransferHistoryItem item = items.get(0);
        assertEquals(222L, item.getTransactionId().longValue());
        assertEquals(WITHDRAWAL, item.getType());
        assertEquals(BigDecimal.TEN, item.getAmount());
        assertEquals("toNumber", item.getToNumber());
        assertNotNull(item.getTransactionTime());

        TransferHistoryItem item2 = items.get(1);
        assertEquals(333L, item2.getTransactionId().longValue());
        assertEquals(DEPOSIT, item2.getType());
        assertEquals(BigDecimal.valueOf(63), item2.getAmount());
        assertEquals("fromNumber", item2.getFromNumber());

    }

    @Test
    public void shouldFailWhenAccountToNotExist() {
        thrown.expect(AccountToNotFoundException.class);
        when(accRepo.findByNumberForUpdate(eq("from"))).thenReturn(new Account());
        sut.transfer(TransferRequest.builder().from("from").build());
    }

    @Test
    public void shouldFailWhenNotEnoughFunds() {
        thrown.expect(InsufficientFundsException.class);
        when(accRepo.findByNumberForUpdate(eq("from"))).thenReturn(Account.builder().balance(BigDecimal.TEN).build());
        when(accRepo.findByNumberForUpdate(eq("to"))).thenReturn(Account.builder().build());
        sut.transfer(TransferRequest.builder().from("from").to("to").amount(BigDecimal.valueOf(11)).build());
    }

    @Test
    public void shouldAddAndSubtractAndCreateTransactionRecord() {
        Long idFrom = 1L;
        Long idTo = 2L;
        Long trId = 333L;
        when(accRepo.findByNumberForUpdate(eq("from"))).thenReturn(Account.builder().id(idFrom).balance(BigDecimal.TEN).build());
        when(accRepo.findByNumberForUpdate(eq("to"))).thenReturn(Account.builder().id(idTo).balance(BigDecimal.ONE).build());
        when(transactionRepo.create(any(AccountTransaction.class))).thenAnswer(getAccountTransactionAnswer(trId));
        TransferItem item = sut.transfer(TransferRequest.builder().from("from").to("to").amount(BigDecimal.valueOf(5)).build());

        assertEquals(trId, item.getTransactionId());
        assertNotNull(item.getTransactionTime());

        verify(transactionRepo).create(transactionCaptor.capture());
        AccountTransaction transaction = transactionCaptor.getValue();
        assertEquals(idFrom, transaction.getFrom());
        assertEquals(idTo, transaction.getTo());
        assertNotNull(transaction.getTransactionTime());
        assertEquals(BigDecimal.valueOf(5), transaction.getAmount());

        verify(accRepo, times(2)).updateBalance(accountCaptor.capture());
        Account accountFrom = accountCaptor.getAllValues().get(0);
        assertEquals(idFrom, accountFrom.getId());
        assertEquals(BigDecimal.valueOf(5), accountFrom.getBalance());
        Account accountTo = accountCaptor.getAllValues().get(1);
        assertEquals(idTo, accountTo.getId());
        assertEquals(BigDecimal.valueOf(6), accountTo.getBalance());
    }

    @Test
    public void shouldFailWhenAccountNotFound() {
        thrown.expect(AccountNotFoundException.class);
        sut.findHistoryById(123L);
    }

    private Answer<AccountTransaction> getAccountTransactionAnswer(Long trId) {
        return invocationOnMock -> {
            AccountTransaction tr = invocationOnMock.getArgument(0);
            tr.setId(trId);
            return tr;
        };
    }

    private static Account answerAccount(InvocationOnMock invocation) {
        Account account = invocation.getArgument(0);
        account.setId(ACCOUNT_ID);
        return account;
    }
}
