package com.haonguyen.logService.exception;

public class InvalidFilePathException extends RuntimeException {
    public InvalidFilePathException(String msg) {
        super(msg);
    }

    public InvalidFilePathException() {
    }
}
