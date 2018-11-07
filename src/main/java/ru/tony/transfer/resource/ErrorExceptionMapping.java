package ru.tony.transfer.resource;

import ru.tony.transfer.resource.messages.BaseResponse;
import ru.tony.transfer.resource.messages.ResponseStatus;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static ru.tony.transfer.resource.messages.ResponseStatus.UNKNOWN_ERROR;

@Provider
public class ErrorExceptionMapping implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        return Response.status(Response.Status.OK)
                .entity(new BaseResponse(UNKNOWN_ERROR))
                .build();
    }
}
