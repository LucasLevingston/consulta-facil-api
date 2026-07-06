package com.consultafacil.core.security;

import com.consultafacil.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("carePolicy")
@RequiredArgsConstructor
public class CarePolicy {

    private final RoleAuthorizationChecker checker;

    // ── Clinics ──────────────────────────────────────────────────────────
    public boolean canManageClinic(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Professionals ────────────────────────────────────────────────────
    public boolean canCreateProfessionalProfile(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT, UserRole.ADMIN);
    }

    public boolean canListProfessionals(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN, UserRole.PATIENT);
    }

    public boolean canViewOwnProfessionalProfile(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canAdminManageProfessional(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canManageProfessionalSchedule(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    // ── Patients ──────────────────────────────────────────────────────────
    public boolean canViewPatientProfile(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canAdminListPatients(Authentication auth) {
        return checker.is(auth, UserRole.ADMIN);
    }

    public boolean canManagePatientProfile(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canViewPatientMedicalRecords(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    // ── Professional Services & Dependents ──────────────────────────────
    public boolean canManageProfessionalService(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN);
    }

    public boolean canManageDependents(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    // ── Patient Health ────────────────────────────────────────────────────
    public boolean canManageOwnEmergencyContacts(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canManageOwnVaccines(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    public boolean canManageOwnDocuments(Authentication auth) {
        return checker.is(auth, UserRole.PATIENT);
    }

    // ── Professional Enrichment ───────────────────────────────────────────
    public boolean canManageOwnEducation(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL);
    }

    public boolean canManageOwnExperience(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL);
    }

    public boolean canManageOwnCertificate(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL);
    }

    public boolean canUpdateOwnCouncil(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL);
    }

    public boolean canUpdateOwnAddress(Authentication auth) {
        return checker.is(auth, UserRole.PROFESSIONAL);
    }
}
