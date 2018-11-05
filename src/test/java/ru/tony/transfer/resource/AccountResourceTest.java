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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static ru.tony.transfer.resource.ResourcesNames.ACCOUNTS;

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
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .invoke().readEntity(AccountResponse.class);

        verify(service, times(1)).save(request);
        assertEquals(ResponseStatus.OK, resp.getStatus());
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
                .createDate(createDate).build()).build();
        when(service.findById(eq(accountId))).thenReturn(val);
        AccountResponse resp = target(ACCOUNTS).path("123").request()
                .buildGet().invoke().readEntity(AccountResponse.class);

        AccountItem item = resp.getAccount();
        assertEquals(accountId, item.getId());
        assertEquals("test", item.getName());
        assertEquals(balance, item.getBalance());
        assertEquals(createDate, item.getCreateDate());
        assertEquals(testNum, item.getNumber());
    }

    @Test
    public void shouldCorrectlyReceiveAccountList() {
        when(service.findAll()).thenReturn(Arrays.asList(new AccountItem(), new AccountItem()));
        AccountListResponse resp = target(ACCOUNTS).request().buildGet().invoke()
                .readEntity(AccountListResponse.class);

        assertEquals(ResponseStatus.OK, resp.getStatus());
        assertEquals(2, resp.getAccounts().size());
    }

    private AccountRequest.AccountRequestBuilder getAccountRequestBuilderFullFilled() {
        return AccountRequest.builder()
                .name("test")
                .balance(BigDecimal.valueOf(100));
    }

    private void assertAccountPostBadRequest(AccountRequest request) {
        Response resp = target(ACCOUNTS)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .invoke();
        assertEquals(BAD_REQUEST.getStatusCode(), resp.getStatus());
    }
}