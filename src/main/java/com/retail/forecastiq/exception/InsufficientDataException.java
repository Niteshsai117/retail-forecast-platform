package com.retail.forecastiq.exception;

public class InsufficientDataException extends RuntimeException {

    public InsufficientDataException(String message) {
        super(message);
    }
}
