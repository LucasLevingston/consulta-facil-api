package com.consultafacil.application.service.patient;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.MedicalRecord;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BloodType;
import com.consultafacil.domain.port.out.patient.MedicalRecordRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdatePatientMedicalRecordsServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock MedicalRecordRepositoryPort medicalRecordRepository;

    UpdatePatientMedicalRecordsService service;

    PatientProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("user-1").name("Maria").email("maria@email.com").build();
        profile = PatientProfile.builder().id("pat-1").user(user).build();

        service = new UpdatePatientMedicalRecordsService(
                patientProfileRepository, medicalRecordRepository, new MedicalRecordMapper());

        when(medicalRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_profileNotFound_throwsResourceNotFoundException() {
        when(patientProfileRepository.findByUserId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing", Map.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_noExistingRecord_createsNewRecordWithProfileLink() {
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("allergies", "pollen");

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("allergies")).isEqualTo("pollen");
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void execute_existingRecord_updatesAllergies() {
        MedicalRecord existing = MedicalRecord.builder()
                .id("rec-1")
                .patientProfile(profile)
                .allergies("none")
                .build();
        profile.setMedicalRecord(existing);

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("allergies", "latex, pollen");

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("allergies")).isEqualTo("latex, pollen");
    }

    @Test
    void execute_updatesCurrentMedication() {
        MedicalRecord existing = MedicalRecord.builder().id("rec-1").patientProfile(profile).build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentMedication", "Metformin 500mg");

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("currentMedication")).isEqualTo("Metformin 500mg");
    }

    @Test
    void execute_updatesConsents() {
        MedicalRecord existing = MedicalRecord.builder().id("rec-1").patientProfile(profile).build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("privacyConsent", true);
        updates.put("treatmentConsent", true);
        updates.put("disclosureConsent", false);

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("privacyConsent")).isEqualTo(true);
        assertThat(result.get("treatmentConsent")).isEqualTo(true);
        assertThat(result.get("disclosureConsent")).isEqualTo(false);
    }

    @Test
    void execute_updatesBloodType() {
        MedicalRecord existing = MedicalRecord.builder().id("rec-1").patientProfile(profile).build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("bloodType", "O_POSITIVE");

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("bloodType")).isEqualTo("O_POSITIVE");
    }

    @Test
    void execute_nullBloodType_clearsBloodType() {
        MedicalRecord existing = MedicalRecord.builder()
                .id("rec-1").patientProfile(profile).bloodType(BloodType.A_POSITIVE).build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("bloodType", null);

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("bloodType")).isNull();
    }

    @Test
    void execute_updatesHeightAndWeight() {
        MedicalRecord existing = MedicalRecord.builder().id("rec-1").patientProfile(profile).build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("height", "1.75");
        updates.put("weight", "70.5");

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("height")).isEqualTo(new BigDecimal("1.75"));
        assertThat(result.get("weight")).isEqualTo(new BigDecimal("70.5"));
    }

    @Test
    void execute_nullHeightAndWeight_clearsThem() {
        MedicalRecord existing = MedicalRecord.builder()
                .id("rec-1").patientProfile(profile)
                .height(new BigDecimal("1.80"))
                .weight(new BigDecimal("80.0"))
                .build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("height", null);
        updates.put("weight", null);

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("height")).isNull();
        assertThat(result.get("weight")).isNull();
    }

    @Test
    void execute_emptyUpdates_savesRecordUnchanged() {
        MedicalRecord existing = MedicalRecord.builder()
                .id("rec-1").patientProfile(profile).allergies("dust").build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> result = service.execute("user-1", new HashMap<>());

        assertThat(result.get("allergies")).isEqualTo("dust");
        verify(medicalRecordRepository).save(existing);
    }

    @Test
    void execute_updatesFamilyAndPastHistory() {
        MedicalRecord existing = MedicalRecord.builder().id("rec-1").patientProfile(profile).build();
        profile.setMedicalRecord(existing);
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));

        Map<String, Object> updates = new HashMap<>();
        updates.put("familyMedicalHistory", "diabetes");
        updates.put("pastMedicalHistory", "appendicitis 2010");

        Map<String, Object> result = service.execute("user-1", updates);

        assertThat(result.get("familyMedicalHistory")).isEqualTo("diabetes");
        assertThat(result.get("pastMedicalHistory")).isEqualTo("appendicitis 2010");
    }
}
