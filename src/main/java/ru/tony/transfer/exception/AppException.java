package ru.tony.transfer.exception;

public class AppException extends RuntimeException{
    public AppException(String message, Exception e) {
        super(message, e);
    }

    public AppException() {
        super();
    }
}
