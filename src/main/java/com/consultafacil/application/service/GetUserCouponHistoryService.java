package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.application.port.in.GetUserCouponHistoryUseCase;
import com.consultafacil.domain.port.out.CouponUsageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserCouponHistoryService implements GetUserCouponHistoryUseCase {

    private final CouponUsageRepositoryPort couponUsageRepository;
    private final CouponUsageMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CouponUsageResponseDTO> execute(String userId) {
        return couponUsageRepository.findByUserId(userId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
