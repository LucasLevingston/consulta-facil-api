package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("appointmentPolicy")
@RequiredArgsConstructor
public class AppointmentPolicy {

    private final RoleAuthorizationChecker checker;

    public boolean canScheduleAppointment(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canViewPatientAppointments(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT, UserRole.ADMIN);
    }

    public boolean canViewProfessionalAppointments(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canConfirmAppointment(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canRescheduleAppointment(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // Ownership enforced at service layer; any authenticated user may attempt.
    public boolean canCancelAppointment(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canCompleteAppointment(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canRateAppointment(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canGenerateCheckInToken(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canCheckIn(Authentication auth) {
        return checker.is(auth, UserRole.RECEPTIONIST, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewQueue(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN, UserRole.RECEPTIONIST);
    }

    public boolean canCallPatient(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canSetModality(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canGenerateMeetLink(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canDeleteAppointment(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canViewAnamnesis(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canSaveAnamnesis(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewClinicalNote(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canSaveClinicalNote(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }
}
