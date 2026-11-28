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
class RequestPolicyTest {

    @Mock
    RoleAuthorizationChecker checker;

    RequestPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new RequestPolicy(checker);
    }

    private Authentication auth() {
        return new TestingAuthenticationToken(
                "user", "creds", new SimpleGrantedAuthority(UserRole.PROFESSIONAL.getAuthority()));
    }

    // ── Exam Requests ─────────────────────────────────────────────────────

    @Test
    void canManageExamRequest_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageExamRequest(auth)).isTrue();
    }

    @Test
    void canManageExamRequest_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canManageExamRequest(auth)).isFalse();
    }

    @Test
    void canViewExamRequests_anyAllowedRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canViewExamRequests(auth)).isTrue();
    }

    @Test
    void canViewExamRequests_receptionist_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canViewExamRequests(auth)).isFalse();
    }

    @Test
    void canReviewExamRequestAsPatient_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canReviewExamRequestAsPatient(auth)).isTrue();
    }

    @Test
    void canReviewExamRequestAsPatient_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canReviewExamRequestAsPatient(auth)).isFalse();
    }

    @Test
    void canReviewExamRequestAsProfessional_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canReviewExamRequestAsProfessional(auth)).isTrue();
    }

    @Test
    void canReviewExamRequestAsProfessional_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canReviewExamRequestAsProfessional(auth)).isFalse();
    }

    @Test
    void canViewOwnExams_anyAllowedRole_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canViewOwnExams(auth)).isTrue();
    }

    @Test
    void canViewOwnExams_receptionist_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canViewOwnExams(auth)).isFalse();
    }

    // ── Procedure Requests ────────────────────────────────────────────────

    @Test
    void canManageProcedureRequest_professionalOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageProcedureRequest(auth)).isTrue();
    }

    @Test
    void canManageProcedureRequest_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canManageProcedureRequest(auth)).isFalse();
    }

    @Test
    void canViewProcedureRequests_authenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canViewProcedureRequests(auth)).isTrue();
    }

    @Test
    void canViewProcedureRequests_notAuthenticated_returnsFalse() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(false);
        assertThat(policy.canViewProcedureRequests(auth)).isFalse();
    }

    @Test
    void canReviewProcedureRequestAsPatient_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canReviewProcedureRequestAsPatient(auth)).isTrue();
    }

    @Test
    void canReviewProcedureRequestAsPatient_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canReviewProcedureRequestAsPatient(auth)).isFalse();
    }

    @Test
    void canViewProcedureRequest_authenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canViewProcedureRequest(auth)).isTrue();
    }

    @Test
    void canViewProcedureRequest_notAuthenticated_returnsFalse() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(false);
        assertThat(policy.canViewProcedureRequest(auth)).isFalse();
    }

    // ── Exam Labs ─────────────────────────────────────────────────────────

    @Test
    void canManageExamLab_admin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canManageExamLab(auth)).isTrue();
    }

    @Test
    void canManageExamLab_nonAdmin_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canManageExamLab(auth)).isFalse();
    }

    @Test
    void canViewExamLabs_authenticated_returnsTrue() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(true);
        assertThat(policy.canViewExamLabs(auth)).isTrue();
    }

    @Test
    void canViewExamLabs_notAuthenticated_returnsFalse() {
        Authentication auth = auth();
        when(checker.isAuthenticated(auth)).thenReturn(false);
        assertThat(policy.canViewExamLabs(auth)).isFalse();
    }

    @Test
    void canScheduleExamAtLab_patient_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(true);
        assertThat(policy.canScheduleExamAtLab(auth)).isTrue();
    }

    @Test
    void canScheduleExamAtLab_professional_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PATIENT)).thenReturn(false);
        assertThat(policy.canScheduleExamAtLab(auth)).isFalse();
    }

    // ── Walk-in ───────────────────────────────────────────────────────────

    @Test
    void canRegisterWalkIn_professionalOrReceptionistOrAdmin_returnsTrue() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.RECEPTIONIST, UserRole.ADMIN)).thenReturn(true);
        assertThat(policy.canRegisterWalkIn(auth)).isTrue();
    }

    @Test
    void canRegisterWalkIn_patient_returnsFalse() {
        Authentication auth = auth();
        when(checker.is(auth, UserRole.PROFESSIONAL, UserRole.RECEPTIONIST, UserRole.ADMIN)).thenReturn(false);
        assertThat(policy.canRegisterWalkIn(auth)).isFalse();
    }
}
