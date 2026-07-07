package com.consultafacil.adapter.out.persistence.seller;

import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.port.out.seller.SellerRepositoryPort;
import com.consultafacil.domain.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepositoryPort {

    private final SellerRepository sellerRepository;

    @Override
    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    @Override
    public Optional<Seller> findById(String id) {
        return sellerRepository.findById(id);
    }

    @Override
    public Optional<Seller> findByUserId(String userId) {
        return sellerRepository.findByUserId(userId);
    }

    @Override
    public Optional<Seller> findBySlug(String slug) {
        return sellerRepository.findBySlug(slug);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return sellerRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return sellerRepository.existsByUserId(userId);
    }

    @Override
    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    @Override
    public List<Seller> findAllByStatus(SellerStatus status) {
        return sellerRepository.findAllByStatus(status);
    }
}
