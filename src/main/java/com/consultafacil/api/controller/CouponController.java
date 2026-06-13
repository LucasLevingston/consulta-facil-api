package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.application.port.in.CouponValidationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponValidationUseCase couponValidationUseCase;

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
    @PreAuthorize("@policy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponUsageResponseDTO>> adminListAll() {
        return ResponseEntity.ok(couponValidationUseCase.getAllCouponUsages());
    }

    @GetMapping("/admin/billing/coupons/{couponId}/usages")
    @PreAuthorize("@policy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponUsageResponseDTO>> adminListByCoupon(@PathVariable String couponId) {
        return ResponseEntity.ok(couponValidationUseCase.getCouponUsagesByCouponId(couponId));
    }
}
