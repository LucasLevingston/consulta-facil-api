package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {
    Optional<Seller> findByUserId(String userId);
    Optional<Seller> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByUserId(String userId);
    List<Seller> findAllByStatus(SellerStatus status);
}
