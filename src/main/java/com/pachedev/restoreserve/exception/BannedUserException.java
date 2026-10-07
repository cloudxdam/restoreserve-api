package com.pachedev.restoreserve.exception;

public class BannedUserException extends RuntimeException {

    public BannedUserException(String message) {
        super(message);
    }
}
