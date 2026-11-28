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
class CarePolicyTest {

    @Mock
    RoleAuthorizationChecker checker;

    CarePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new CarePolicy(checker);
    }

    private Authentication auth() {
        return new TestingAuthenticationToken(
                "user", "creds", new SimpleGrantedAuthority(UserRole.PROFESSIONAL.getAuthority()));
    }

    // ── Clinics ───────────────────────────────────────────────────────────

    @Test
    void canManageClinic_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageClinic(auth)).isTrue();
    }

    @Test
    void canManageClinic_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canManageClinic(auth)).isFalse();
    }

    // ── Professionals ─────────────────────────────────────────────────────

    @Test
    void canCreateProfessionalProfile_patientOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canCreateProfessionalProfile(auth)).isTrue();
    }

    @Test
    void canCreateProfessionalProfile_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canCreateProfessionalProfile(auth)).isFalse();
    }

    @Test
    void canListProfessionals_adminOrPatient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canListProfessionals(auth)).isTrue();
    }

    @Test
    void canListProfessionals_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canListProfessionals(auth)).isFalse();
    }

    @Test
    void canViewOwnProfessionalProfile_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canViewOwnProfessionalProfile(auth)).isTrue();
    }

    @Test
    void canViewOwnProfessionalProfile_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canViewOwnProfessionalProfile(auth)).isFalse();
    }

    @Test
    void canAdminManageProfessional_admin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canAdminManageProfessional(auth)).isTrue();
    }

    @Test
    void canAdminManageProfessional_nonAdmin_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canAdminManageProfessional(auth)).isFalse();
    }

    @Test
    void canManageProfessionalSchedule_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageProfessionalSchedule(auth)).isTrue();
    }

    @Test
    void canManageProfessionalSchedule_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canManageProfessionalSchedule(auth)).isFalse();
    }

    // ── Patients ──────────────────────────────────────────────────────────

    @Test
    void canViewPatientProfile_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canViewPatientProfile(auth)).isTrue();
    }

    @Test
    void canViewPatientProfile_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canViewPatientProfile(auth)).isFalse();
    }

    @Test
    void canAdminListPatients_admin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canAdminListPatients(auth)).isTrue();
    }

    @Test
    void canAdminListPatients_nonAdmin_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canAdminListPatients(auth)).isFalse();
    }

    @Test
    void canManagePatientProfile_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canManagePatientProfile(auth)).isTrue();
    }

    @Test
    void canManagePatientProfile_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canManagePatientProfile(auth)).isFalse();
    }

    @Test
    void canViewPatientMedicalRecords_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canViewPatientMedicalRecords(auth)).isTrue();
    }

    @Test
    void canViewPatientMedicalRecords_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canViewPatientMedicalRecords(auth)).isFalse();
    }

    // ── Professional Services & Dependents ───────────────────────────────

    @Test
    void canManageProfessionalService_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageProfessionalService(auth)).isTrue();
    }

    @Test
    void canManageProfessionalService_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canManageProfessionalService(auth)).isFalse();
    }

    @Test
    void canManageDependents_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canManageDependents(auth)).isTrue();
    }

    @Test
    void canManageDependents_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canManageDependents(auth)).isFalse();
    }

    // ── Patient Health ────────────────────────────────────────────────────

    @Test
    void canManageOwnEmergencyContacts_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canManageOwnEmergencyContacts(auth)).isTrue();
    }

    @Test
    void canManageOwnVaccines_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canManageOwnVaccines(auth)).isTrue();
    }

    @Test
    void canManageOwnDocuments_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canManageOwnDocuments(auth)).isTrue();
    }

    @Test
    void canManageOwnDocuments_nonPatient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canManageOwnDocuments(auth)).isFalse();
    }

    // ── Insurance ─────────────────────────────────────────────────────────

    @Test
    void canManageOwnInsurance_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canManageOwnInsurance(auth)).isTrue();
    }

    @Test
    void canManageOwnInsurance_nonPatient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canManageOwnInsurance(auth)).isFalse();
    }

    @Test
    void canManageProfessionalInsurance_professional_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(true);
        assertThat(policy.canManageProfessionalInsurance(auth)).isTrue();
    }

    @Test
    void canManageProfessionalInsurance_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(false);
        assertThat(policy.canManageProfessionalInsurance(auth)).isFalse();
    }

    // ── Professional Enrichment ───────────────────────────────────────────

    @Test
    void canManageOwnEducation_professional_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(true);
        assertThat(policy.canManageOwnEducation(auth)).isTrue();
    }

    @Test
    void canManageOwnCertificate_nonProfessional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(false);
        assertThat(policy.canManageOwnCertificate(auth)).isFalse();
    }

    @Test
    void canUpdateOwnAddress_professional_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(true);
        assertThat(policy.canUpdateOwnAddress(auth)).isTrue();
    }

    @Test
    void canUpdateOwnAddress_nonProfessional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL)).thenReturn(false);
        assertThat(policy.canUpdateOwnAddress(auth)).isFalse();
    }
}
