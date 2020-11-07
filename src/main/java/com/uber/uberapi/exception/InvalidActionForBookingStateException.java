package com.uber.uberapi.exception;

public class InvalidActionForBookingStateException extends UberException {
    public InvalidActionForBookingStateException(String message) {
        super(message);
    }
}
