package com.consultafacil.api.controller;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.api.dto.coupon.ValidateCouponDTO;
import com.consultafacil.application.port.in.CouponUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponUseCase couponUseCase;

    @PostMapping("/admin/coupons")
    @PreAuthorize("@policy.canManageCoupons(authentication)")
    public ResponseEntity<CouponResponseDTO> createCoupon(
            @Valid @RequestBody CreateCouponDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(couponUseCase.createCoupon(dto, authentication.getName()));
    }

    @GetMapping("/admin/coupons")
    @PreAuthorize("@policy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponResponseDTO>> listCoupons() {
        return ResponseEntity.ok(couponUseCase.listCoupons());
    }

    @PatchMapping("/admin/coupons/{id}")
    @PreAuthorize("@policy.canManageCoupons(authentication)")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable String id,
            @RequestBody UpdateCouponDTO dto) {
        return ResponseEntity.ok(couponUseCase.updateCoupon(id, dto));
    }

    @PostMapping("/coupons/validate")
    @PreAuthorize("@policy.canValidateCoupon(authentication)")
    public ResponseEntity<CouponValidationResponseDTO> validate(
            @Valid @RequestBody ValidateCouponDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(couponUseCase.validate(
                dto.getCode(), authentication.getName(), dto.getPlanId(), dto.getGrossAmount()));
    }
}
