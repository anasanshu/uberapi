package com.uber.uberapi.services.OTP;

import com.uber.uberapi.models.OTP;

public interface OTPService {
    void sendPhoneNumberConfirmationOTP(OTP otp);

    void sendRideStartOTP(OTP rideStartOTP);
}
