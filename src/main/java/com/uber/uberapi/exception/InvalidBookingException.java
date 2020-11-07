package com.uber.uberapi.exception;

public class InvalidBookingException extends UberException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
