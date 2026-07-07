package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.application.port.in.CreateSellerUseCase;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSellerService implements CreateSellerUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final UserRepositoryPort userRepository;
    private final SellerMapper mapper;

    @Override
    @Transactional
    public SellerResponseDTO execute(CreateSellerDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        if (sellerRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException("Seller", "userId", dto.getUserId());
        }

        Seller seller = Seller.builder()
                .user(user)
                .slug(generateUniqueSlug())
                .commissionRate(dto.getCommissionRate())
                .pixKey(dto.getPixKey())
                .notes(dto.getNotes())
                .build();

        seller = sellerRepository.save(seller);
        log.info("[Seller] Created seller id={} slug={} for userId={}", seller.getId(), seller.getSlug(), dto.getUserId());
        return mapper.toDTO(seller);
    }

    private String generateUniqueSlug() {
        String slug;
        int attempts = 0;
        do {
            slug = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            if (++attempts > 10) {
                throw new IllegalStateException("Failed to generate unique seller slug after 10 attempts");
            }
        } while (sellerRepository.existsBySlug(slug));
        return slug;
    }
}
