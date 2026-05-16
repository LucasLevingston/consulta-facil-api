package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    Optional<Address> findByUserId(String userId);
}
