package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerStatus;

import java.util.List;
import java.util.Optional;

public interface SellerRepositoryPort {
    Seller save(Seller seller);
    Optional<Seller> findById(String id);
    Optional<Seller> findByUserId(String userId);
    Optional<Seller> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByUserId(String userId);
    List<Seller> findAll();
    List<Seller> findAllByStatus(SellerStatus status);
}
