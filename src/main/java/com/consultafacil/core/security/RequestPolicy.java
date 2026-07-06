package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("requestPolicy")
@RequiredArgsConstructor
public class RequestPolicy {

    private final RoleAuthorizationChecker checker;

    // ── Exam Requests ─────────────────────────────────────────────────────
    public boolean canManageExamRequest(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewExamRequests(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canReviewExamRequestAsPatient(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canReviewExamRequestAsProfessional(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewOwnExams(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Procedure Requests ────────────────────────────────────────────────
    public boolean canManageProcedureRequest(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canViewProcedureRequests(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canReviewProcedureRequestAsPatient(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canViewProcedureRequest(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    // ── Exam Labs ─────────────────────────────────────────────────────────
    public boolean canManageExamLab(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canViewExamLabs(Authentication auth) {
        return checker.isAuthenticated(auth);
    }

    public boolean canScheduleExamAtLab(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    // ── Walk-in ───────────────────────────────────────────────────────────
    public boolean canRegisterWalkIn(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.RECEPTIONIST, UserRole.ADMIN);
    }
}
