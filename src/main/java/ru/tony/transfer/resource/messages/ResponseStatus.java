package ru.tony.transfer.resource.messages;

import ru.tony.transfer.exception.AppException;
import ru.tony.transfer.service.exception.*;

public enum ResponseStatus {

    OK,
    ACCOUNT_NOT_FOUND(AccountNotFoundException.class),
    ACCOUNT_FROM_NOT_FOUND(AccountFromNotFoundException.class),
    ACCOUNT_TO_NOT_FOUND(AccountToNotFoundException.class),
    NOT_ENOUGH_FONDS(InsufficientFundsException.class),
    UNKNOWN_ERROR;

    private Class ex;

    ResponseStatus(Class ex) {
        this.ex = ex;
    }
    ResponseStatus() {

    }

    public static ResponseStatus getByException(AppException e) {
        for(ResponseStatus status: values()){
            if(status.ex != null && status.ex.isAssignableFrom(e.getClass())) {
                return status;
            }
        }
        return UNKNOWN_ERROR;
    }
}
