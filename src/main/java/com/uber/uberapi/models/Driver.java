package com.uber.uberapi.models;

import com.uber.uberapi.exception.UnapprovedDriverException;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driver")
public class Driver extends Auditable {

    @OneToOne
    private Account account;

    private Gender gender;

    private String name;

    @OneToOne(mappedBy = "driver")
    private Car car;

    private String licenseDetails;

    @Temporal(value = TemporalType.DATE)
    private Date dob;

    @Enumerated(value = EnumType.STRING)
    private DriverApprovalStatus driverApprovalStatus;

    @OneToMany(mappedBy = "driver")
    private List<Booking> bookings = new ArrayList<>();

    private boolean isAvailable;

    private String activeCity;

    @OneToOne
    private ExactLocation lastKnownLocation;

    @OneToOne
    private ExactLocation home;

    public void setAvailable(boolean available) {
        if (available && !driverApprovalStatus.equals(DriverApprovalStatus.APPROVED)) {
            throw new UnapprovedDriverException("Driver approval pending or denied" + getId());
        }
        isAvailable = available;
    }
}
