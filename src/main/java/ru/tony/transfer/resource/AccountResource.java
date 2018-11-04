package ru.tony.transfer.resource;

import ru.tony.transfer.resource.messages.AccountListResponse;
import ru.tony.transfer.resource.messages.AccountRequest;
import ru.tony.transfer.resource.messages.AccountResponse;
import ru.tony.transfer.service.AccountService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static ru.tony.transfer.resource.ResourcesNames.ACCOUNTS;

@Path(ACCOUNTS)
public class AccountResource {

    @Inject
    private AccountService service;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public AccountResponse saveAccount(@Valid AccountRequest request) {
        return service.save(request);
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public AccountResponse getAccount(@NotNull @PathParam("id") Long id) {
        return service.findById(id);
    }

    @GET
    @Produces(APPLICATION_JSON)
    public AccountListResponse getAll() {
        return new AccountListResponse(service.findAll());
    }
}
