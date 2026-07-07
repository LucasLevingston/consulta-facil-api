package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.domain.entity.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerMapper {

    private final SellerReferralLinkBuilder referralLinkBuilder;

    public SellerResponseDTO toDTO(Seller seller) {
        return SellerResponseDTO.builder()
                .id(seller.getId())
                .userId(seller.getUser().getId())
                .userName(seller.getUser().getName())
                .userEmail(seller.getUser().getEmail())
                .slug(seller.getSlug())
                .commissionRate(seller.getCommissionRate())
                .status(seller.getStatus())
                .pixKey(seller.getPixKey())
                .notes(seller.getNotes())
                .createdAt(seller.getCreatedAt())
                .referralLink(referralLinkBuilder.build(seller.getSlug()))
                .build();
    }
}
