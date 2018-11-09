package ru.tony.transfer.resource;

import ru.tony.transfer.resource.messages.*;
import ru.tony.transfer.service.AccountService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static ru.tony.transfer.resource.ResourcesNames.*;

@Path(ACCOUNTS)
public class AccountResource {

    @Inject
    private AccountService service;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public AccountResponse saveAccount(@Valid @NotNull AccountRequest request) {
        return service.save(request);
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public AccountResponse getAccount(@NotNull @PathParam("id") Long id) {
        return service.findById(id);
    }

    @GET
    @Path("/{id}/" + HISTORY)
    @Produces(APPLICATION_JSON)
    public TransferHistoryResponse getHistory(@NotNull @PathParam("id") Long id) {
        return new TransferHistoryResponse(service.findHistoryById(id));
    }

    @GET
    @Produces(APPLICATION_JSON)
    public AccountListResponse getAll() {
        return new AccountListResponse(service.findAll());
    }

    @POST
    @Path("/" + TRANSFER)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public TransferResponse transfer(@Valid TransferRequest request) {
        return new TransferResponse(service.transfer(request));
    }
}
