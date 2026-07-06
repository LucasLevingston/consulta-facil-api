package com.consultafacil.api.controller;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.api.dto.seller.SellerDashboardDTO;
import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.api.dto.seller.UpdateCommissionStatusDTO;
import com.consultafacil.application.port.in.SellerUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Sellers", description = "Seller affiliate management")
public class SellerController {

    private final SellerUseCase sellerUseCase;

    // ── Admin endpoints ────────────────────────────────────────────────────

    @PostMapping("/admin/sellers")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Criar vendedor")
    public ResponseEntity<SellerResponseDTO> createSeller(@Valid @RequestBody CreateSellerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerUseCase.createSeller(dto));
    }

    @GetMapping("/admin/sellers")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Listar todos os vendedores com métricas")
    public ResponseEntity<List<SellerResponseDTO>> listSellers() {
        return ResponseEntity.ok(sellerUseCase.listSellers());
    }

    @GetMapping("/admin/sellers/{sellerId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Detalhe do vendedor")
    public ResponseEntity<SellerResponseDTO> getSeller(@PathVariable String sellerId) {
        return ResponseEntity.ok(sellerUseCase.getSeller(sellerId));
    }

    @PatchMapping("/admin/sellers/{sellerId}/commission-rate")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Atualizar taxa de comissão do vendedor")
    public ResponseEntity<SellerResponseDTO> updateCommissionRate(
            @PathVariable String sellerId,
            @RequestParam @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal commissionRate) {
        return ResponseEntity.ok(sellerUseCase.updateCommissionRate(sellerId, commissionRate));
    }

    @PatchMapping("/admin/sellers/{sellerId}/deactivate")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Desativar vendedor")
    public ResponseEntity<SellerResponseDTO> deactivateSeller(@PathVariable String sellerId) {
        return ResponseEntity.ok(sellerUseCase.deactivateSeller(sellerId));
    }

    @PatchMapping("/admin/sellers/{sellerId}/activate")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Reativar vendedor")
    public ResponseEntity<SellerResponseDTO> activateSeller(@PathVariable String sellerId) {
        return ResponseEntity.ok(sellerUseCase.activateSeller(sellerId));
    }

    @GetMapping("/admin/sellers/{sellerId}/commissions")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Histórico de comissões do vendedor")
    public ResponseEntity<List<SellerSaleResponseDTO>> getCommissions(@PathVariable String sellerId) {
        return ResponseEntity.ok(sellerUseCase.getCommissions(sellerId));
    }

    @PatchMapping("/admin/sellers/commissions/{saleId}/status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canAccessAdminPanel(authentication)")
    @Operation(summary = "Atualizar status da comissão (PENDING → PAID / REVERSED)")
    public ResponseEntity<SellerSaleResponseDTO> updateCommissionStatus(
            @PathVariable String saleId,
            @Valid @RequestBody UpdateCommissionStatusDTO dto) {
        return ResponseEntity.ok(sellerUseCase.updateCommissionStatus(saleId, dto.getStatus()));
    }

    // ── Seller self-service endpoints ─────────────────────────────────────

    @GetMapping("/sellers/me/dashboard")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canViewSellerDashboard(authentication)")
    @Operation(summary = "Dashboard do próprio vendedor")
    public ResponseEntity<SellerDashboardDTO> getMyDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(sellerUseCase.getMyDashboard(userDetails.getUserId()));
    }

    @GetMapping("/sellers/me/commissions")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canViewSellerDashboard(authentication)")
    @Operation(summary = "Comissões do próprio vendedor")
    public ResponseEntity<List<SellerSaleResponseDTO>> getMyCommissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SellerDashboardDTO dashboard = sellerUseCase.getMyDashboard(userDetails.getUserId());
        return ResponseEntity.ok(dashboard.getRecentSales());
    }
}
