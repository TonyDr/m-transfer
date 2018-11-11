package ru.tony.transfer.resource;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import ru.tony.transfer.resource.messages.*;
import ru.tony.transfer.service.AccountService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static ru.tony.transfer.resource.ResourcesNames.ACCOUNTS;
import static ru.tony.transfer.resource.ResourcesNames.HISTORY;
import static ru.tony.transfer.resource.ResourcesNames.TRANSFER;
import static ru.tony.transfer.resource.messages.ResponseStatus.OK;

public class AccountResourceTest extends JerseyTest {

    private AccountService service;

    @Before
    public void beforeTest(){
        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected Application configure() {
        service = mock(AccountService.class);
        return new ResourceConfig(AccountResource.class).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(service).to(AccountService.class);
            }
        });
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new InMemoryTestContainerFactory();
    }

    @Test
    public void saveAccountShouldFailOnValidationWhenNameIsEmpty() {
        AccountRequest request = getAccountRequestBuilderFullFilled().name(null).build();
        assertAccountPostBadRequest(request);
    }

    @Test
    public void saveAccountShouldFailOnValidationWhenBalanceIsEmpty() {
        AccountRequest request = getAccountRequestBuilderFullFilled().balance(null).build();
        assertAccountPostBadRequest(request);
    }

    @Test
    public void shouldCorrectlyCallSaveServiceMethod() {
        AccountRequest request = getAccountRequestBuilderFullFilled().build();

        when(service.save(request)).thenReturn(new AccountResponse());
        AccountResponse resp = target(ACCOUNTS)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, APPLICATION_JSON_TYPE))
                .invoke().readEntity(AccountResponse.class);

        verify(service, times(1)).save(request);
        assertEquals(OK, resp.getStatus());
    }

    @Test
    public void shouldReturnAccountResponseById() {
        Long accountId = 123L;
        BigDecimal balance = BigDecimal.valueOf(150);
        Date createDate = new Date();
        String testNum = "testNum";
        AccountResponse val = AccountResponse.builder().account(AccountItem.builder()
                .id(accountId)
                .name("test")
                .balance(balance)
                .number(testNum)
                .createTime(createDate).build()).build();
        when(service.findById(eq(accountId))).thenReturn(val);
        AccountResponse resp = target(ACCOUNTS).path("123").request()
                .buildGet().invoke().readEntity(AccountResponse.class);

        AccountItem item = resp.getAccount();
        assertEquals(accountId, item.getId());
        assertEquals("test", item.getName());
        assertEquals(balance, item.getBalance());
        assertEquals(createDate, item.getCreateTime());
        assertEquals(testNum, item.getNumber());
    }

    @Test
    public void shouldCorrectlyReceiveAccountList() {
        when(service.findAll()).thenReturn(Arrays.asList(new AccountItem(), new AccountItem()));
        AccountListResponse resp = target(ACCOUNTS).request().buildGet().invoke()
                .readEntity(AccountListResponse.class);

        assertEquals(OK, resp.getStatus());
        assertEquals(2, resp.getAccounts().size());
    }

    @Test
    public void shouldFailWithBadRequestWhenFromIsEmpty() {
        Response response = callTransfer(fullFilledTransferRequest().from(null).build());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        response = callTransfer(fullFilledTransferRequest().from("").build());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldFailWithBadRequestWhenToIsEmpty() {
        Response response = callTransfer(fullFilledTransferRequest().to(null).build());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        response = callTransfer(fullFilledTransferRequest().to("").build());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldFailWithBadRequestWhenAmountIsIncorrect() {
        Response response =callTransfer(fullFilledTransferRequest().amount(null).build());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        response = callTransfer(fullFilledTransferRequest().amount(BigDecimal.valueOf(-1)).build());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldCorrectlyCallTransferMethod() {
        Long id = 123L;
        TransferRequest request = fullFilledTransferRequest().build();
        when(service.transfer(eq(request))).thenReturn(TransferItem.builder().transactionId(id).build());
        TransferResponse resp = callTransfer(request).readEntity(TransferResponse.class);
        assertEquals(OK, resp.getStatus());
        assertEquals(id, resp.getInfo().getTransactionId());
    }

    @Test
    public void shouldCorrectlyCallHistoryMethod() {
        Long id = 123L;
        when(service.findHistoryById(eq(id)))
                .thenReturn(Arrays.asList(new TransferHistoryItem(), new TransferHistoryItem()));
        TransferHistoryResponse response = target(ACCOUNTS).path("123").path(HISTORY)
                .request(APPLICATION_JSON_TYPE).buildGet()
                .invoke().readEntity(TransferHistoryResponse.class);
        assertEquals(OK, response.getStatus());
        assertEquals(2, response.getItems().size());
    }

    private Response callTransfer(TransferRequest request) {
        Response response;
        response = target(ACCOUNTS).path(TRANSFER).request(APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, APPLICATION_JSON_TYPE)).invoke();
        return response;
    }

    private TransferRequest.TransferRequestBuilder fullFilledTransferRequest() {
        return TransferRequest.builder().from("from").to("to").amount(BigDecimal.TEN);
    }

    private AccountRequest.AccountRequestBuilder getAccountRequestBuilderFullFilled() {
        return AccountRequest.builder()
                .name("test")
                .balance(BigDecimal.valueOf(100));
    }

    private void assertAccountPostBadRequest(AccountRequest request) {
        Response resp = target(ACCOUNTS)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, APPLICATION_JSON_TYPE))
                .invoke();
        assertEquals(BAD_REQUEST.getStatusCode(), resp.getStatus());
    }
}