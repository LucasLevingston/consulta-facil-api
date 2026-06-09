package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * ABAC policy evaluator.
 *
 * Bean name "policy" — used in @PreAuthorize SpEL:
 *   @PreAuthorize("@policy.canScheduleAppointment(authentication)")
 *
 * Rules are defined once here; controllers never reference role strings directly.
 */
@Component("policy")
public class PolicyService {

    // ── Appointments ─────────────────────────────────────────────────────

    public boolean canScheduleAppointment(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    public boolean canViewPatientAppointments(Authentication auth) {
        return is(auth, UserRole.PATIENT, UserRole.ADMIN);
    }

    public boolean canViewProfessionalAppointments(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canConfirmAppointment(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canRescheduleAppointment(Authentication auth) {
        return is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    /** Ownership enforced at service layer; any authenticated user may attempt. */
    public boolean canCancelAppointment(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public boolean canCompleteAppointment(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canRateAppointment(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    public boolean canGenerateCheckInToken(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    public boolean canCheckIn(Authentication auth) {
        return is(auth, UserRole.RECEPTIONIST, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewQueue(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN, UserRole.RECEPTIONIST);
    }

    public boolean canCallPatient(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canSetModality(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canGenerateMeetLink(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canDeleteAppointment(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    public boolean canViewAnamnesis(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public boolean canSaveAnamnesis(Authentication auth) {
        return is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewClinicalNote(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public boolean canSaveClinicalNote(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Clinics ───────────────────────────────────────────────────────────

    public boolean canManageClinic(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Professionals ─────────────────────────────────────────────────────

    /** PATIENT applies to become professional; ADMIN can also create on behalf. */
    public boolean canCreateProfessionalProfile(Authentication auth) {
        return is(auth, UserRole.PATIENT, UserRole.ADMIN);
    }

    public boolean canListProfessionals(Authentication auth) {
        return is(auth, UserRole.ADMIN, UserRole.PATIENT);
    }

    public boolean canViewOwnProfessionalProfile(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canAdminManageProfessional(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    public boolean canManageProfessionalSchedule(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Patients ──────────────────────────────────────────────────────────

    public boolean canViewPatientProfile(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canManagePatientProfile(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    public boolean canViewPatientMedicalRecords(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    // ── Exam Requests ─────────────────────────────────────────────────────

    public boolean canManageExamRequest(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewExamRequests(Authentication auth) {
        return is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canReviewExamRequestAsPatient(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    public boolean canReviewExamRequestAsProfessional(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Procedure Requests ────────────────────────────────────────────────

    public boolean canManageProcedureRequest(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewProcedureRequests(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public boolean canReviewProcedureRequestAsPatient(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    public boolean canViewProcedureRequest(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    // ── Professional Services ─────────────────────────────────────────────

    public boolean canManageProfessionalService(Authentication auth) {
        return is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Users ─────────────────────────────────────────────────────────────

    public boolean canViewUserProfile(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public boolean canUpdateUserProfile(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public boolean canAdminListUsers(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    public boolean canAdminUpdateUser(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    // ── Payments ──────────────────────────────────────────────────────────

    public boolean canCreatePaymentCheckout(Authentication auth) {
        return is(auth, UserRole.PATIENT);
    }

    // ── Notifications ─────────────────────────────────────────────────────

    public boolean canAccessNotifications(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    // ── Sellers ───────────────────────────────────────────────────────────

    public boolean canManageSellers(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    public boolean canViewSellerDashboard(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    // ── Coupons ───────────────────────────────────────────────────────────

    public boolean canManageCoupons(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    public boolean canValidateCoupon(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    public boolean canAccessAdminPanel(Authentication auth) {
        return is(auth, UserRole.ADMIN);
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private boolean is(Authentication auth, UserRole... roles) {
        if (auth == null || !auth.isAuthenticated()) return false;
        String authority = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");
        for (UserRole role : roles) {
            if (role.getAuthority().equals(authority)) return true;
        }
        return false;
    }
}
