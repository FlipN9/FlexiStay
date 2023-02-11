package com.yany.flexistay.exception;

public class StayNotExistException extends RuntimeException {
    public StayNotExistException(String message) {
        super(message);
    }
}
