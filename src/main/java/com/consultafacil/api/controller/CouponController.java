package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.application.port.in.ApplyCouponUseCase;
import com.consultafacil.application.port.in.CreateCouponUseCase;
import com.consultafacil.application.port.in.GetAllCouponUsagesUseCase;
import com.consultafacil.application.port.in.GetCouponUsagesByCouponIdUseCase;
import com.consultafacil.application.port.in.GetUserCouponHistoryUseCase;
import com.consultafacil.application.port.in.ListCouponsUseCase;
import com.consultafacil.application.port.in.UpdateCouponUseCase;
import com.consultafacil.application.port.in.ValidateCouponUsageUseCase;
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

    private final ValidateCouponUsageUseCase validateCouponUsageUseCase;
    private final ApplyCouponUseCase applyCouponUseCase;
    private final GetUserCouponHistoryUseCase getUserCouponHistoryUseCase;
    private final GetAllCouponUsagesUseCase getAllCouponUsagesUseCase;
    private final GetCouponUsagesByCouponIdUseCase getCouponUsagesByCouponIdUseCase;
    private final ListCouponsUseCase listCouponsUseCase;
    private final CreateCouponUseCase createCouponUseCase;
    private final UpdateCouponUseCase updateCouponUseCase;

    @PostMapping("/billing/coupons/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouponValidationResultDTO> validate(@RequestBody Map<String, Object> body) {
        String code = String.valueOf(body.get("code"));
        String userId = String.valueOf(body.get("userId"));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        return ResponseEntity.ok(validateCouponUsageUseCase.execute(code, userId, amount));
    }

    @PostMapping("/billing/coupons/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouponUsageResponseDTO> apply(@RequestBody Map<String, Object> body) {
        String code = String.valueOf(body.get("code"));
        String userId = String.valueOf(body.get("userId"));
        String paymentId = body.get("paymentId") != null ? String.valueOf(body.get("paymentId")) : null;
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        return ResponseEntity.ok(applyCouponUseCase.execute(code, userId, paymentId, amount));
    }

    @GetMapping("/billing/coupons/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouponUsageResponseDTO>> history(@RequestParam String userId) {
        return ResponseEntity.ok(getUserCouponHistoryUseCase.execute(userId));
    }

    @GetMapping("/admin/billing/coupons")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponUsageResponseDTO>> adminListAll() {
        return ResponseEntity.ok(getAllCouponUsagesUseCase.execute());
    }

    @GetMapping("/admin/billing/coupons/{couponId}/usages")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponUsageResponseDTO>> adminListByCoupon(@PathVariable String couponId) {
        return ResponseEntity.ok(getCouponUsagesByCouponIdUseCase.execute(couponId));
    }

    @GetMapping("/admin/billing/coupons/codes")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<List<CouponResponseDTO>> adminListCoupons() {
        return ResponseEntity.ok(listCouponsUseCase.execute());
    }

    @PostMapping("/admin/billing/coupons/codes")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<CouponResponseDTO> adminCreateCoupon(
            @Valid @RequestBody CreateCouponDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createCouponUseCase.execute(dto, userDetails.getUserId()));
    }

    @PatchMapping("/admin/billing/coupons/codes/{id}")
    @PreAuthorize("@adminPolicy.canManageCoupons(authentication)")
    public ResponseEntity<CouponResponseDTO> adminUpdateCoupon(
            @PathVariable String id,
            @Valid @RequestBody UpdateCouponDTO dto) {
        return ResponseEntity.ok(updateCouponUseCase.execute(id, dto));
    }
}
