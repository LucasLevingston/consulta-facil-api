package com.consultafacil.api.controller;

import com.consultafacil.api.dto.analytics.AppointmentAnalyticsDTO;
import com.consultafacil.api.dto.analytics.FinancialAnalyticsDTO;
import com.consultafacil.api.dto.analytics.ReferralAnalyticsDTO;
import com.consultafacil.api.dto.analytics.SubscriptionAnalyticsDTO;
import com.consultafacil.api.dto.analytics.UserAnalyticsDTO;
import com.consultafacil.application.port.in.analytics.AppointmentAnalyticsUseCase;
import com.consultafacil.application.port.in.analytics.FinancialAnalyticsUseCase;
import com.consultafacil.application.port.in.analytics.ReferralAnalyticsUseCase;
import com.consultafacil.application.port.in.analytics.SubscriptionAnalyticsUseCase;
import com.consultafacil.application.port.in.analytics.UserAnalyticsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final FinancialAnalyticsUseCase financialAnalyticsUseCase;
    private final UserAnalyticsUseCase userAnalyticsUseCase;
    private final AppointmentAnalyticsUseCase appointmentAnalyticsUseCase;
    private final ReferralAnalyticsUseCase referralAnalyticsUseCase;
    private final SubscriptionAnalyticsUseCase subscriptionAnalyticsUseCase;

    @GetMapping("/financial")
    @PreAuthorize("@policy.canViewAnalytics(authentication)")
    public ResponseEntity<FinancialAnalyticsDTO> financial() {
        return ResponseEntity.ok(financialAnalyticsUseCase.getFinancialAnalytics());
    }

    @GetMapping("/users")
    @PreAuthorize("@policy.canViewAnalytics(authentication)")
    public ResponseEntity<UserAnalyticsDTO> users() {
        return ResponseEntity.ok(userAnalyticsUseCase.getUserAnalytics());
    }

    @GetMapping("/appointments")
    @PreAuthorize("@policy.canViewAnalytics(authentication)")
    public ResponseEntity<AppointmentAnalyticsDTO> appointments() {
        return ResponseEntity.ok(appointmentAnalyticsUseCase.getAppointmentAnalytics());
    }

    @GetMapping("/referrals")
    @PreAuthorize("@policy.canViewAnalytics(authentication)")
    public ResponseEntity<ReferralAnalyticsDTO> referrals() {
        return ResponseEntity.ok(referralAnalyticsUseCase.getReferralAnalytics());
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("@policy.canViewAnalytics(authentication)")
    public ResponseEntity<SubscriptionAnalyticsDTO> subscriptions() {
        return ResponseEntity.ok(subscriptionAnalyticsUseCase.getSubscriptionAnalytics());
    }
}
