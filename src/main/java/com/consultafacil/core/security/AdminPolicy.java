package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("adminPolicy")
@RequiredArgsConstructor
public class AdminPolicy {

    private final RoleAuthorizationChecker checker;

    // ── Users ─────────────────────────────────────────────────────────────
    public boolean canViewUserProfile(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canUpdateUserProfile(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canAdminListUsers(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canAdminUpdateUser(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    // ── Payments & Notifications ─────────────────────────────────────────
    public boolean canCreatePaymentCheckout(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canAccessNotifications(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    // ── Sellers ───────────────────────────────────────────────────────────
    public boolean canManageSellers(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canViewSellerDashboard(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    // ── Coupons ───────────────────────────────────────────────────────────
    public boolean canManageCoupons(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canValidateCoupon(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    // ── Referrals, Wallets & Plans ────────────────────────────────────────
    public boolean canManageReferrals(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canManageWallets(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canManagePlans(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    // ── Analytics & Admin ─────────────────────────────────────────────────
    public boolean canViewAnalytics(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canAccessAdminPanel(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canCalculateFees(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }
}
