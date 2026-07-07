package com.consultafacil.application.service;

import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PatientProfileMapper {

    public Map<String, Object> toResponseMap(User user, PatientProfile patientProfile) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", patientProfile.getId());
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("cpf", user.getCpf());
        response.put("imageUrl", user.getImageUrl());
        response.put("occupation", patientProfile.getOccupation());
        response.put("birthDate", user.getBirthDate());
        response.put("gender", user.getGender());
        response.put("createdAt", user.getCreatedAt());
        response.put("updatedAt", user.getUpdatedAt());
        return response;
    }
}
