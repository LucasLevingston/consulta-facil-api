package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.application.port.in.CouponUseCase;
import com.consultafacil.application.port.in.CouponValidationUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponValidationUseCase couponValidationUseCase;
    private final CouponUseCase couponUseCase;

    @PostMapping("/billing/coupons/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouponValidationResultDTO> validate(@RequestBody Map<String, Object> body) {
        String code = String.valueOf(body.get("code"));
        String userId = String.valueOf(body.get("userId"));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        return ResponseEntity.ok(couponValidationUseCase.validateCoupon(code, userId, amount));
    }

    @PostMapping("/billing/coupons/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouponUsageResponseDTO> apply(@RequestBody Map<String, Object> body) {
        String code = String.valueOf(body.get("code"));
        String userId = String.valueOf(body.get("userId"));
        String paymentId = body.get("paymentId") != null ? String.valueOf(body.get("paymentId")) : null;
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        return ResponseEntity.ok(couponValidationUseCase.applyCoupon(code, userId, paymentId, amount));
    }

    @GetMapping("/billing/coupons/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouponUsageResponseDTO>> history(@RequestParam String userId) {
        return ResponseEntity.ok(couponValidationUseCase.getUserCouponHistory(userId));
    }

    @GetMapping("/admin/billing/coupons")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponUsageResponseDTO>> adminListAll() {
        return ResponseEntity.ok(couponValidationUseCase.getAllCouponUsages());
    }

    @GetMapping("/admin/billing/coupons/{couponId}/usages")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponUsageResponseDTO>> adminListByCoupon(@PathVariable String couponId) {
        return ResponseEntity.ok(couponValidationUseCase.getCouponUsagesByCouponId(couponId));
    }

    @GetMapping("/admin/billing/coupons/codes")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponResponseDTO>> adminListCoupons() {
        return ResponseEntity.ok(couponUseCase.listCoupons());
    }

    @PostMapping("/admin/billing/coupons/codes")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<CouponResponseDTO> adminCreateCoupon(
            @Valid @RequestBody CreateCouponDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponUseCase.createCoupon(dto, userDetails.getUserId()));
    }

    @PatchMapping("/admin/billing/coupons/codes/{id}")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<CouponResponseDTO> adminUpdateCoupon(
            @PathVariable String id,
            @Valid @RequestBody UpdateCouponDTO dto) {
        return ResponseEntity.ok(couponUseCase.updateCoupon(id, dto));
    }
}
