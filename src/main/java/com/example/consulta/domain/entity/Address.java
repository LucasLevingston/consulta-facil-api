package com.example.consulta.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String zipCode;

    private String street;

    private String number;

    private String district;

    private String city;

    private String state;

    @Builder.Default
    private String country = "Brazil";

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
