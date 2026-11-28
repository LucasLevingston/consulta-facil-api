package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPolicyTest {

    @Mock
    RoleAuthorizationChecker checker;

    AdminPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AdminPolicy(checker);
    }

    private Authentication auth() {
        return new TestingAuthenticationToken(
                "user", "creds", new SimpleGrantedAuthority(UserRole.ADMIN.getAuthority()));
    }

    // ── Users ─────────────────────────────────────────────────────────────

    @Test
    void canViewUserProfile_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canViewUserProfile(auth)).isTrue();
    }

    @Test
    void canViewUserProfile_notAuthenticated_returnsFalse() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(false);
        assertThat(policy.canViewUserProfile(auth)).isFalse();
    }

    @Test
    void canUpdateUserProfile_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canUpdateUserProfile(auth)).isTrue();
    }

    @Test
    void canAdminListUsers_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canAdminListUsers(auth)).isTrue();
    }

    @Test
    void canAdminListUsers_nonAdmin_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canAdminListUsers(auth)).isFalse();
    }

    @Test
    void canAdminUpdateUser_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canAdminUpdateUser(auth)).isTrue();
    }

    // ── Payments & Notifications ─────────────────────────────────────────

    @Test
    void canCreatePaymentCheckout_delegatesToIsPatientRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canCreatePaymentCheckout(auth)).isTrue();
    }

    @Test
    void canCreatePaymentCheckout_nonPatient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canCreatePaymentCheckout(auth)).isFalse();
    }

    @Test
    void canAccessNotifications_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canAccessNotifications(auth)).isTrue();
    }

    // ── Sellers ───────────────────────────────────────────────────────────

    @Test
    void canManageSellers_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageSellers(auth)).isTrue();
    }

    @Test
    void canViewSellerDashboard_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canViewSellerDashboard(auth)).isTrue();
    }

    // ── Coupons ───────────────────────────────────────────────────────────

    @Test
    void canManageCoupons_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageCoupons(auth)).isTrue();
    }

    @Test
    void canValidateCoupon_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canValidateCoupon(auth)).isTrue();
    }

    // ── Referrals, Wallets & Plans ────────────────────────────────────────

    @Test
    void canManageReferrals_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageReferrals(auth)).isTrue();
    }

    @Test
    void canManageWallets_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageWallets(auth)).isTrue();
    }

    @Test
    void canManagePlans_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManagePlans(auth)).isTrue();
    }

    // ── Subscriptions ─────────────────────────────────────────────────────

    @Test
    void canManageSubscription_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canManageSubscription(auth)).isTrue();
    }

    // ── Analytics & Admin ─────────────────────────────────────────────────

    @Test
    void canViewAnalytics_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canViewAnalytics(auth)).isTrue();
    }

    @Test
    void canAccessAdminPanel_delegatesToIsAdminRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canAccessAdminPanel(auth)).isTrue();
    }

    @Test
    void canCalculateFees_delegatesToProfessionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canCalculateFees(auth)).isTrue();
    }

    @Test
    void canCalculateFees_neitherProfessionalNorAdmin_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canCalculateFees(auth)).isFalse();
    }

    // ── Prescriptions ─────────────────────────────────────────────────────

    @Test
    void canIssuePrescription_delegatesToIsProfessionalRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(true);
        assertThat(policy.canIssuePrescription(auth)).isTrue();
    }

    @Test
    void canIssuePrescription_nonProfessional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(false);
        assertThat(policy.canIssuePrescription(auth)).isFalse();
    }

    @Test
    void canViewPrescriptions_delegatesToIsAuthenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canViewPrescriptions(auth)).isTrue();
    }
}
