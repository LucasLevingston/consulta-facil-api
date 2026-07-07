package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.application.port.in.UpdateCouponUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCouponService implements UpdateCouponUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponMapper mapper;

    @Override
    @Transactional
    public CouponResponseDTO execute(String id, UpdateCouponDTO dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));

        if (dto.getDescription() != null) coupon.setDescription(dto.getDescription());
        if (dto.getStatus() != null) coupon.setStatus(dto.getStatus());
        if (dto.getExpiresAt() != null) coupon.setExpiresAt(dto.getExpiresAt());
        if (dto.getMaxUses() != null) coupon.setMaxUses(dto.getMaxUses());

        return mapper.toDTO(couponRepository.save(coupon));
    }
}
