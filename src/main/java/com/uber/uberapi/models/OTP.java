package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otp")
public class OTP extends Auditable {
    private String code;
    private String sentToNumber;

    public boolean validateEnteredOTP(OTP otp, Integer expiryMinutes) {
        if (!code.equals(otp.getCode())) {
            return false;
        }

        // if the createdAt + expiryMinutes > currentTime , then it is expired
        return true;
    }
}
