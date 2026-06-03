package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.Address;

import java.util.Optional;

public interface AddressRepositoryPort {

    Address save(Address address);

    Optional<Address> findByUserId(String userId);
}
