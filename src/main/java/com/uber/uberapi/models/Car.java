package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "car")
public class Car extends Auditable {

    @ManyToOne
    private Color color;

    private String plateNumber;
    private String brandAndModel;

    @Enumerated(value = EnumType.STRING)
    private CarType carType;


    @OneToOne
    private Driver driver;

}
