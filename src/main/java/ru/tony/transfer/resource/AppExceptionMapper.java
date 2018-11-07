package ru.tony.transfer.resource;

import ru.tony.transfer.resource.messages.BaseResponse;
import ru.tony.transfer.resource.messages.ResponseStatus;
import ru.tony.transfer.service.exception.AppException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public Response toResponse(AppException e) {
        return Response.status(Response.Status.OK).entity(getBaseResponse(e)).build();
    }

    private BaseResponse getBaseResponse(AppException e) {
        return new BaseResponse(ResponseStatus.getByException(e));
    }
}
