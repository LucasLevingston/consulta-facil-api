package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.clinic.ClinicResponseDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;

import java.util.List;

public interface ClinicUseCase {

    ClinicResponseDTO createClinic(String userId, CreateClinicDTO dto);

    List<ClinicResponseDTO> getAllClinics();

    ClinicResponseDTO getClinicById(String clinicId);

    List<ClinicResponseDTO> getMyClinic(String userId);

    ClinicResponseDTO updateClinic(String clinicId, String userId, CreateClinicDTO dto);

    void addMember(String clinicId, String professionalProfileId, String requesterId);

    void removeMember(String clinicId, String professionalProfileId, String requesterId);

    List<ClinicResponseDTO> getClinicsNearby(double lat, double lng, double radiusKm);
}
