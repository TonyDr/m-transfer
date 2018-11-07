package ru.tony.transfer.it;

import org.junit.Test;
import ru.tony.transfer.resource.messages.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ru.tony.transfer.resource.ResourcesNames.ACCOUNTS;
import static ru.tony.transfer.resource.ResourcesNames.TRANSFER;
import static ru.tony.transfer.resource.messages.ResponseStatus.*;

public class AccountTransferIT extends AppBase{

    @Test
    public void shouldCreateAndGetAccount() {
        String name = "test1";
        BigDecimal balance = BigDecimal.valueOf(100);
        AccountResponse result = createAccount(getCreateAccountRequest(name, balance));
        assertAccountResponse(name, balance, result);

        result = getAccountById(result.getAccount().getId().toString()).readEntity(AccountResponse.class);

        assertAccountResponse(name, balance, result);
    }

    @Test
    public void shouldReceiveErrorCodeWhenAccountNotFound() {
        Response response = getAccountById("-123");
        assertEquals(OK.getStatusCode(), response.getStatus());
        BaseResponse bs = response.readEntity(BaseResponse.class);
        assertEquals(ACCOUNT_NOT_FOUND, bs.getStatus());

    }

    @Test
    public void shouldCorrectlyGetAccountList() {
        int size = getListResponse().getAccounts().size();
        createAccount(getCreateAccountRequest("list_test", BigDecimal.TEN));
        assertEquals(size +1, getListResponse().getAccounts().size());
    }

    @Test
    public void shouldCorrectlyTransferBetweenAccounts() {
        AccountItem from = createAccount(getCreateAccountRequest("transfer_test1", BigDecimal.valueOf(100))).getAccount();
        AccountItem to = createAccount(getCreateAccountRequest("transfer_test2", BigDecimal.TEN)).getAccount();
        TransferRequest request = TransferRequest.builder().from(from.getNumber()).to(to.getNumber()).amount(BigDecimal.valueOf(50)).build();
        Response response = target(ACCOUNTS).path(TRANSFER).request(APPLICATION_JSON_TYPE).buildPost(Entity.entity(request, APPLICATION_JSON_TYPE)).invoke();
        assertEquals(OK.getStatusCode(), response.getStatus());
        TransferItem item = response.readEntity(TransferResponse.class).getInfo();
        assertNotNull(item.getTransactionId());
        assertNotNull(item.getTransactionTime());
    }

    @Test
    public void transferShouldFailWhenAccountFromNotFound() {
        TransferRequest request = TransferRequest.builder().from("not_existed_from").to("some_to").amount(BigDecimal.valueOf(50)).build();
        sendAndCheckResponse(ACCOUNT_FROM_NOT_FOUND, request);
    }

    @Test
    public void transferShouldFailWhenAccountToNotFound() {
        AccountItem from = createAccount(getCreateAccountRequest("transfer_to_error", BigDecimal.valueOf(100))).getAccount();
        TransferRequest request = TransferRequest.builder().from(from.getNumber()).to("some_to").amount(BigDecimal.valueOf(50)).build();
        sendAndCheckResponse(ACCOUNT_TO_NOT_FOUND, request);
    }

    @Test
    public void transferShouldFailWhenNotEnoughFonds() {
        AccountItem from = createAccount(getCreateAccountRequest("transfer_fonds1", BigDecimal.valueOf(15))).getAccount();
        AccountItem to = createAccount(getCreateAccountRequest("transfer_fonds2", BigDecimal.TEN)).getAccount();
        TransferRequest request = TransferRequest.builder().from(from.getNumber()).to(to.getNumber()).amount(BigDecimal.valueOf(50)).build();
        sendAndCheckResponse(NOT_ENOUGH_FONDS, request);
    }

    private void sendAndCheckResponse(ResponseStatus expected, TransferRequest request) {
        Response response = target(ACCOUNTS).path(TRANSFER).request(APPLICATION_JSON_TYPE).buildPost(Entity.entity(request, APPLICATION_JSON_TYPE)).invoke();
        assertEquals(OK.getStatusCode(), response.getStatus());
        BaseResponse bs = response.readEntity(BaseResponse.class);
        assertEquals(expected, bs.getStatus());
    }

    private AccountListResponse getListResponse() {
        return target(ACCOUNTS).request().buildGet().invoke().readEntity(AccountListResponse.class);
    }

    private void assertAccountResponse(String name, BigDecimal balance, AccountResponse result) {
        assertEquals(ResponseStatus.OK, result.getStatus());
        AccountItem item = result.getAccount();
        assertEquals(name, item.getName());
        assertNotNull(item.getId());
        assertNotNull(item.getNumber());
        assertEquals(balance, item.getBalance());
    }

    private AccountRequest getCreateAccountRequest(String name, BigDecimal balance) {
        return AccountRequest.builder().balance(balance).name(name).build();
    }

    private Response getAccountById(String id) {
        return target(ACCOUNTS).path(id).request(APPLICATION_JSON_TYPE)
                .buildGet().invoke();
    }

    private AccountResponse createAccount(AccountRequest request) {
        Response response = target(ACCOUNTS).request(APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, APPLICATION_JSON_TYPE))
                .invoke();
        assertEquals(OK.getStatusCode(), response.getStatus());
        return response.readEntity(AccountResponse.class);
    }
}
