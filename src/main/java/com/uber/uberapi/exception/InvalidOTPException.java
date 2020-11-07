package com.uber.uberapi.exception;

public class InvalidOTPException extends UberException {
    public InvalidOTPException() {
        super("Invalid OTP entered");
    }
}
