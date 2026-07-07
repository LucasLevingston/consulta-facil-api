package com.consultafacil.domain.repository.professional.enrichment;

import com.consultafacil.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    Optional<Address> findByUserId(String userId);
}
