package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.Address;
import com.example.consulta.domain.port.out.AddressRepositoryPort;
import com.example.consulta.domain.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressRepositoryPort {

    private final AddressRepository addressRepository;

    @Override
    public Address save(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public Optional<Address> findByUserId(String userId) {
        return addressRepository.findByUserId(userId);
    }
}
