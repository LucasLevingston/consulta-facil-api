package com.consultafacil.adapter.out.persistence.professional.enrichment;

import com.consultafacil.domain.entity.Address;
import com.consultafacil.domain.port.out.professional.enrichment.AddressRepositoryPort;
import com.consultafacil.domain.repository.professional.enrichment.AddressRepository;
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
