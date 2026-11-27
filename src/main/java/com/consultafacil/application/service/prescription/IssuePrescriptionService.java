package com.consultafacil.application.service.prescription;

import com.consultafacil.api.dto.prescription.IssuePrescriptionRequest;
import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import com.consultafacil.application.port.in.prescription.IssuePrescriptionUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.exception.InvalidStateException;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IssuePrescriptionService implements IssuePrescriptionUseCase {

    private final ProfessionalProfileRepositoryPort professionalRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final PrescriptionRepositoryPort prescriptionRepository;
    private final PrescriptionPdfGenerator pdfGenerator;
    private final StoragePort storagePort;
    private final EmailPort emailPort;

    @Override
    @Transactional
    public PrescriptionDTO execute(String professionalUserId, IssuePrescriptionRequest request) {
        ProfessionalProfile professional = professionalRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", professionalUserId));

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", request.appointmentId()));

        if (!appointment.getProfessional().getId().equals(professional.getId())) {
            throw new InvalidStateException("PRESCRIPTION_FORBIDDEN: Profissional não é responsável por esta consulta");
        }

        if (appointment.getScheduledAt().isBefore(LocalDateTime.now().minusHours(48))) {
            throw new InvalidStateException("PRESCRIPTION_WINDOW_EXPIRED: Prescrições só podem ser emitidas dentro de 48h da consulta");
        }

        PatientProfile patient = appointment.getPatient();
        String patientName = patient.getUser() != null ? patient.getUser().getName() : "Paciente";
        String professionalName = professional.getUser() != null ? professional.getUser().getName() : "Profissional";
        String licenseNumber = professional.getLicenseNumber();
        String specialty = professional.getSpecialty() != null ? professional.getSpecialty().name() : "";

        byte[] pdfBytes = pdfGenerator.generate(
                professionalName, licenseNumber, specialty,
                patientName, request.medications(), request.instructions(),
                request.validUntil(), LocalDate.now());

        String pdfUrl = storagePort.upload(pdfBytes, request.appointmentId() + ".pdf",
                "application/pdf", "prescriptions/" + request.appointmentId());

        Prescription prescription = Prescription.builder()
                .appointment(appointment)
                .professional(professional)
                .patient(patient)
                .medications(request.medications())
                .instructions(request.instructions())
                .validUntil(request.validUntil())
                .pdfUrl(pdfUrl)
                .build();

        prescription = prescriptionRepository.save(prescription);

        if (patient.getUser() != null && patient.getUser().getEmail() != null) {
            try {
                emailPort.sendPrescriptionAvailable(patient.getUser().getEmail(), patientName, pdfUrl);
            } catch (Exception e) {
                // email failure must not roll back the prescription
            }
        }

        return toDTO(prescription, professionalName, patientName);
    }

    static PrescriptionDTO toDTO(Prescription p, String professionalName, String patientName) {
        return new PrescriptionDTO(
                p.getId(),
                p.getAppointment() != null ? p.getAppointment().getId() : null,
                professionalName,
                patientName,
                p.getMedications(),
                p.getInstructions(),
                p.getValidUntil(),
                p.getPdfUrl(),
                p.getCreatedAt());
    }
}
