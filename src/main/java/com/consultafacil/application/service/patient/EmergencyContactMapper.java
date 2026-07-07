package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.domain.entity.EmergencyContact;
import org.springframework.stereotype.Component;

@Component
public class EmergencyContactMapper {

    public EmergencyContactDTO toDTO(EmergencyContact c) {
        return new EmergencyContactDTO(c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getRelationship());
    }
}
