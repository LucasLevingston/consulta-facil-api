package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Address;

import java.util.Optional;

public interface AddressRepositoryPort {

    Address save(Address address);

    Optional<Address> findByUserId(String userId);
}
