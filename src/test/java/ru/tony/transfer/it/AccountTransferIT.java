package ru.tony.transfer.it;

import org.junit.Test;
import ru.tony.transfer.resource.messages.AccountItem;
import ru.tony.transfer.resource.messages.AccountRequest;
import ru.tony.transfer.resource.messages.AccountResponse;
import ru.tony.transfer.resource.messages.ResponseStatus;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ru.tony.transfer.resource.ResourcesNames.ACCOUNTS;

public class AccountTransferIT extends AppBase{

    @Test
    public void shouldCreateAndGetAccount() {
        String name = "test1";
        BigDecimal balance = BigDecimal.valueOf(100);
        AccountResponse result = createAccount(getCreateAccountRequest(name, balance));
        assertAccountResponse(name, balance, result);

        result = target(ACCOUNTS).path(result.getAccount().getId().toString()).request(APPLICATION_JSON_TYPE)
                .buildGet().invoke().readEntity(AccountResponse.class);

        assertAccountResponse(name, balance, result);
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

    private AccountResponse createAccount(AccountRequest request) {
        Response response = target(ACCOUNTS).request(APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(request, APPLICATION_JSON_TYPE))
                .invoke();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return response.readEntity(AccountResponse.class);
    }
}
