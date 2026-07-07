package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.application.port.in.ListCouponsUseCase;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListCouponsService implements ListCouponsUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponseDTO> execute() {
        return couponRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
